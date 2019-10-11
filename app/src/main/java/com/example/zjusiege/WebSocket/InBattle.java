package com.example.zjusiege.WebSocket;

import cn.hyperchain.sdk.rpc.account.Account;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import com.example.zjusiege.Utils.Utils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/inBattle/{gameId}/{battleId}")
@Component
public class InBattle {
    private HyperchainService hyperchainService = new HyperchainService();
    private Account deployAccount = Config.getDeployAccount();
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    private static final Map<String, Boolean> gameStarted = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Session>> playerSession = BeforeBattle.getPlayerSession();
    private static final Map<String, Map<String, JSONObject>> playerSoldiers = BeforeBattle.getPlayerSoldiersInit();
    private static final Map<String, JSONObject> battleData = BeforeBattle.getBattleData();
    // 使用map来收集各个用户的Session
    // 使用map来存储每一场对战的数据
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
//    private Session session;
//    private static String attackerAddress;
//    private static String defenderAddress;
//    private static int cityId;
    private static int playersPerGame = 2;
//    private static int buySoldiersTimer = 20;
    //    private static int round = 1;
    private static int roundTimer = 20;
//    private static boolean isOver = false;

    private static int debug = 0;

    @OnOpen
    public void connect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception{
        initGameStarted(battleId);
        playerNum += 1;
        System.out.println("InBattle connect success!");
        System.out.println("playerNum: " + playerNum);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception {
        playerNum -= 1;
        System.out.println("InBattle disConnect!");
        System.out.println("playerNum: " + playerNum);
    }

    @OnMessage
    public void onMessage(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, String msg, Session session) throws Exception {
        JSONObject params = JSONObject.fromObject(msg);
        boolean first = params.getBoolean("first");
        String address = params.getString("address");

        if (first) {
            // 检查链上信息和后端信息是否相符
            boolean matched = checkPlayerInfo(gameId, battleId, address, session);
            if (matched) {
                // 相符
                // 检查是否注册
                if (playerSession.get(battleId).containsKey(address)) {
                    // 已注册
                    if (playerSession.get(battleId).size() == playersPerGame && !gameStarted.get(battleId)) {
                        // 开启对战
                        gameStarted.replace(battleId, true);
                        // 开启第一轮游戏倒计时
                        new Thread(()-> {
                            try {
                                // 延迟两秒用于初始化时间
                                TimeUnit.SECONDS.sleep(2);
                                // 当前round
                                int cRound = battleData.get(battleId).getInt("round");
                                String attackerAddress = battleData.get(battleId).getString("attackerAddress");
                                String defenderAddress = battleData.get(battleId).getString("defenderAddress");
                                // 开启一轮
                                roundCountDown(roundTimer, gameId, battleId, cRound, attackerAddress, defenderAddress);
                            } catch (Exception e) {
                                System.out.println("Got an exception: " + e.getMessage());
                            }
                        }).start();
                    }
                    // 初始化对局

                }
                else {
                    // 未注册，非法连接
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "error")
                            .element("message", "battleId not match");
                    sendMsg(session, jsonObject.toString());
                }
            }
            else {
                // 不相符
                //TODO 告知玩家
                System.out.println("battleId not match");
            }
        }
        else {
            String operation = params.getString("operation");
            if (operation.equals("pickSoldier")) {
                pickSoldier(params, gameId, battleId, address);
            }
            else {
                System.out.println("invalid operation");
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("InBattle connect error!");
        error.printStackTrace();
    }


    private static <T> List<T> castList(Object obj, Class<T> clazz)
    {
        List<T> result = new ArrayList<T>();
        if(obj instanceof List<?>)
        {
            for (Object o : (List<?>) obj)
            {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }

    private void initGameStarted(String battleId) {
        if (!gameStarted.containsKey(battleId)) {
            gameStarted.put(battleId, false);
        }
    }

    private void GenerateBattleField(String battleId, String address, String opponent) throws Exception {
        List<Integer> mySoldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
        double myPoint = playerSoldiers.get(battleId).get(address).getDouble("price");
        int myQuantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
        boolean myPick = playerSoldiers.get(battleId).get(address).getBoolean("pick");
        int mySoldier = playerSoldiers.get(battleId).get(address).getInt("soldier");
        double opponentPoint = playerSoldiers.get(battleId).get(opponent).getDouble("price");
        int opponentQuantity = playerSoldiers.get(battleId).get(opponent).getInt("quantity");
        boolean opponentPick = playerSoldiers.get(battleId).get(opponent).getBoolean("pick");
        Session session = playerSession.get(battleId).get(address);

        JSONObject jsonObject = new JSONObject()
                .element("stage", "GenerateBattleField")
                .element("opponent", opponent)
                .element("mySoldiers", mySoldiers)
                .element("myPoint", myPoint)
                .element("myQuantity", myQuantity)
                .element("myPick", myPick)
                .element("mySoldier", mySoldier)
                .element("opponentPoint", opponentPoint)
                .element("opponentQuantity", opponentQuantity)
                .element("opponentPick", opponentPick);
        sendMsg(session, jsonObject.toString());
    }

    private void roundCountDown(int seconds, String gameId, String battleId, int curRound, String attackerAddress, String defenderAddress) throws Exception {
        System.out.println("roundCountDown--- " + curRound + " : count down from " + seconds + " s");

//        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
//        String defenderAddress = battleData.get(battleId).getString("defenderAddress");
        // 此处是为了本轮结束之后不再发送倒计时数据，判断当前round和全局round是否一致
//        int round = battleData.get(battleId).getInt("round");
        playerSoldiers.get(battleId).get(attackerAddress).element("round", curRound);
        playerSoldiers.get(battleId).get(defenderAddress).element("round", curRound);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(battleId);
            @Override
            public void run() {
                int round = 0;
                boolean isOver = true;
                if (battleData.containsKey(battleId)) {
                    round = battleData.get(battleId).getInt("round");
                    isOver = battleData.get(battleId).getBoolean("isOver");
                }
                if (round == curRound && !isOver) {
                    System.out.println("Round " + curRound + " time remains " + --curSec + " s");
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "battle")
                            .element("positive", true)
                            .element("round", curRound)
                            .element("timer", curSec)
                            .element("isOver", false);
                    try {
                        sendAll(map, jsonObject.toString());
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }
            }
        }, 0, 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Round " + curRound + " time is out");

        // 假如玩家未出牌，自动出牌
        if (!playerSoldiers.get(battleId).get(attackerAddress).getBoolean("pick")) {
            // 进攻者未出牌
            List<Integer> type = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
            if (type != null && type.size() != 0) {
                int soldier = type.get(0);
                JSONObject params = new JSONObject()
                        .element("soldier", soldier);
                pickSoldier(params, gameId, battleId, attackerAddress);
            }
        }
        if (!playerSoldiers.get(battleId).get(defenderAddress).getBoolean("pick")) {
            // 防守者未出牌
            List<Integer> type = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);
            if (type != null && type.size() != 0) {
                int soldier = type.get(0);
                JSONObject params = new JSONObject()
                        .element("soldier", soldier);
                pickSoldier(params, gameId, battleId, defenderAddress);
            }
        }
    }

    private boolean checkPlayerInfo(String gameId, String battleId, String address, Session session) throws Exception {

        String[] information = battleId.split("&&");
        String attackerAddress = information[0];
        String defenderAddress = information[1];
        int cityId = Integer.valueOf(information[2]);
        // 对链上数据进行查询
        // 获取用户信息
        String playerInfo = hyperchainService.getPlayersStatus(address);
        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            // 用户存在
            int gameIdOnChain = Utils.returnInt(playerInfo, 0);
            if (gameIdOnChain == Integer.valueOf(gameId)) {
                // 链上gameId和后端gameId相匹配
                // 查询指定gameId的游戏状态
                String globalInfo = hyperchainService.getGlobalTb(gameIdOnChain);
                if (!globalInfo.equals("contract calling error") && !globalInfo.equals("unknown error")) {
                    // 获取游戏阶段
                    String[] gameStage = new String[]{"start", "bidding", "running", "settling", "ending"};
                    int gameStageInt = Utils.returnInt(globalInfo, 1);
                    if (gameStage[gameStageInt].equals("running")) {
                        // gameId和gameStage均正确
                        String opponent = Utils.returnString(playerInfo, 2);
                        opponent = opponent.toUpperCase();
//                        if (address.equals(attackerAddress) && opponent.equals(defenderAddress)) {
//                            return true;
//                        }
//                        else if (address.equals(defenderAddress) && opponent.equals(attackerAddress)) {
//                            return true;
//                        }
//                        else {
//                            return false;
//                        }
                        if ((address.equals(attackerAddress) && opponent.equals(defenderAddress)) || (address.equals(defenderAddress) && opponent.equals(attackerAddress))) {
                            return true;
                        }
                        else {
                            // 告知前端游戏出错
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "error")
                                    .element("message", "battleId not match");
                            sendMsg(session, jsonObject.toString());
                            return false;
                        }
                    }
                    else {
                        // 告知前端游戏出错
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "error")
                                .element("message", "gameStage error");
                        sendMsg(session, jsonObject.toString());
                        return false;
                    }
                }
                else {
                    // 找不到指定gameId的游戏阶段
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "error")
                            .element("message", "game not exist");
                    sendMsg(session, jsonObject.toString());
                    return false;
                }
            }
            else {
                // 链上gameId和后端gameId不匹配
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "error")
                        .element("message", "gameId not match");
                sendMsg(session, jsonObject.toString());
                return false;
            }
        }
        else {
            // 用户不存在，返回错误
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "error")
                    .element("message", "player not exist");
            sendMsg(session, jsonObject.toString());
            return false;
        }
    }

    private void pickSoldier(JSONObject params, String gameId, String battleId, String address) throws Exception {
        // 获取游戏数据
        int soldier = params.getInt("soldier");
        Session session = playerSession.get(battleId).get(address);
        // 可以考虑后端加上士兵是否在仓库中的验证
        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
        String defenderAddress = battleData.get(battleId).getString("defenderAddress");
        Session attackerSession = playerSession.get(battleId).get(attackerAddress);
        Session defenderSession = playerSession.get(battleId).get(defenderAddress);
        // 确定对手
        String opponent = address.equals(attackerAddress) ? defenderAddress: attackerAddress;
//        double point = params.getDouble("point");
        // 更新数据
        playerSoldiers.get(battleId).get(address).element("soldier", soldier);
        playerSoldiers.get(battleId).get(address).element("pick", true);
        // 发送response
        JSONObject response = new JSONObject()
                .element("stage", "response")
                .element("operation", "pickSoldier")
                .element("status", true);
        sendMsg(session, response.toString());
        // 更新战场数据
        new Thread(()-> {
            try {
                // 向双方发送战场信息
                GenerateBattleField(battleId, address, opponent);
                GenerateBattleField(battleId, opponent, address);
            } catch (Exception e) {
                System.out.println("Got an exception: " + e.getMessage());
            }
        }).start();

        // 检查对方是否出牌
        boolean allPick = playerSoldiers.get(battleId).get(opponent).getBoolean("pick");
        if (allPick) {
            // 链上执行判断
            int aType = playerSoldiers.get(battleId).get(attackerAddress).getInt("soldier");
            int dType = playerSoldiers.get(battleId).get(defenderAddress).getInt("soldier");
            String result = hyperchainService.pickAndBattle(Integer.valueOf(gameId), attackerAddress, defenderAddress, aType, dType);
            System.out.println("result: " + result);
            if (!result.equals("contract calling error") && !result.equals("unknown error")) {
                switch (Utils.getValue(result)) {
                    case "attacker wins this round": {
                        // 进攻者获胜
                        win(battleId, attackerAddress);
                        System.out.println("进攻者获胜");
                        break;
                    }
                    case "defender wins this round": {
                        // 防守者获胜
                        win(battleId, defenderAddress);
                        System.out.println("防守者获胜");
                        break;
                    }
                    case "tie": {
                        // 战平
                        tie(battleId);
                        System.out.println("平局");
                        break;
                    }
                    default: {
                        // error
                        System.out.println("error");
                        break;
                    }
                }
            }
            else {
                // 链上执行失败
                errorMsg(attackerSession, "judge error");
                errorMsg(defenderSession, "judge error");
            }
            // 本轮结束，重置标志位，并且检查游戏是否结束
            new Thread(()-> {
//                playerSoldiers.get(battleId).get(attackerAddress).element("pick", false);
//                playerSoldiers.get(battleId).get(defenderAddress).element("pick", false);
//
//                List<Integer> attackerSoldiers = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
//                List<Integer> defenderSoldiers = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);
////                    boolean isOver = false;
//                if (attackerSoldiers.size() == 0 || defenderSoldiers.size() == 0 || battleData.get(battleId).getInt("round") >= 5) {
//                    // 游戏结束，设置isOver为true
////                        isOver = true;
//                    System.out.println("游戏结束");
//                    battleData.get(battleId).element("isOver", true);
//                    String winner;
//                    String loser;
//                    // 链上判定谁获胜
//                    try {
//                        int cityId = battleData.get(battleId).getInt("cityId");
//                        String battleResult = hyperchainService.battleEnd(Integer.valueOf(gameId), attackerAddress, defenderAddress, cityId);
////                            String battleResult = "attacker wins the battle";
//                        if (!battleResult.equals("contract calling error") && !battleResult.equals("unknown error")) {
////                            if (!battleResult.equals("")) {
//                            switch (Utils.getValue(battleResult)) {
//                                case "attacker wins the battle": {
//                                    winner = attackerAddress;
//                                    loser = defenderAddress;
//                                    // 转账给winner
//                                    new Thread(() -> {
//                                        // 查询链上数据
//                                        try {
//                                            battleSettlement(winner, loser, false);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }).start();
//                                    break;
//                                }
//                                case "defender wins the battle": {
//                                    winner = defenderAddress;
//                                    loser = attackerAddress;
//                                    new Thread(() -> {
//                                        // 查询链上数据
//                                        try {
//                                            battleSettlement(winner, loser, false);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }).start();
//                                    break;
//                                }
//                                case "tie": {
//                                    winner = "nobody";
//                                    loser = "nobody";
//                                    new Thread(() -> {
//                                        // 查询链上数据
//                                        try {
//                                            battleSettlement(winner, loser, true);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }).start();
//                                    break;
//                                }
//                                default: {
//                                    winner = "";
//                                    loser = "";
//                                    break;
//                                }
//                            }
//                            JSONObject jsonObject = new JSONObject()
//                                    .element("stage", "battle")
//                                    .element("positive", true)
//                                    .element("round", battleData.get(battleId).getInt("round"))
//                                    .element("timer", 0)
//                                    .element("isOver", true)
//                                    .element("winner", winner)
//                                    .element("loser", loser);
//                            sendMsg(attackerSession, jsonObject.toString());
//                            sendMsg(defenderSession, jsonObject.toString());
//                        }
//                        else {
//                            JSONObject jsonObject = new JSONObject()
//                                    .element("stage", "error")
//                                    .element("message", "battle end error");
//                            sendMsg(attackerSession, jsonObject.toString());
//                            sendMsg(defenderSession, jsonObject.toString());
//                        }
//                        // 清除数据
//                        playerSoldiers.remove(battleId);
//                        playerSession.remove(battleId);
//                        battleData.remove(battleId);
//                    } catch (Exception e) {
//                        System.out.println("Got an exception: " + e.getMessage());
//                    }
//                }
//                else {
//                    // 游戏未结束，开启下一轮
//                    try {
//                        int round = battleData.get(battleId).getInt("round");
//                        round += 1;
//                        battleData.get(battleId).element("round", round);
//                        // 延迟3秒
//                        TimeUnit.SECONDS.sleep(3);
//                        // 游戏开始倒计时
//                        roundCountDown(roundTimer, gameId, battleId, round, attackerAddress, defenderAddress);
//                    } catch (Exception e) {
//                        System.out.println("Got an exception: " + e.getMessage());
//                    }
//                }
                try {
                    roundEnd(gameId, battleId, attackerAddress, defenderAddress, attackerSession, defenderSession);
                } catch (Exception e) {
                    System.out.println("Got an exception: " + e.getMessage());
                }
            }).start();
        }
        else {
            // 告知玩家等待
            System.out.println("waiting opponent");
//            try {
//                JSONObject jsonObject = new JSONObject()
//                        .element("operation", "pickSoldier")
//                        .element("status", true)
//                        .element("situation", "wait");
//                sendMsg(playerSession.get(battleId).get(address), jsonObject.toString());
//            } catch (Exception e) {
//                System.out.println("Got an exception: " + e.getMessage());
//            }
        }

    }

    private void roundEnd(String gameId, String battleId, String attackerAddress, String defenderAddress, Session attackerSession, Session defenderSession) throws Exception {
        playerSoldiers.get(battleId).get(attackerAddress).element("pick", false);
        playerSoldiers.get(battleId).get(defenderAddress).element("pick", false);

        List<Integer> attackerSoldiers = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
        List<Integer> defenderSoldiers = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);
//                    boolean isOver = false;
        if (attackerSoldiers.size() == 0 || defenderSoldiers.size() == 0 || battleData.get(battleId).getInt("round") >= 5) {
            // 游戏结束，设置isOver为true
//                        isOver = true;
            System.out.println("battle end");
            battleData.get(battleId).element("isOver", true);
            // 链上判定谁获胜
            int cityId = battleData.get(battleId).getInt("cityId");
            String battleResult = hyperchainService.battleEnd(Integer.valueOf(gameId), attackerAddress, defenderAddress, cityId);
//                            String battleResult = "attacker wins the battle";
            if (!battleResult.equals("contract calling error") && !battleResult.equals("unknown error")) {
//                            if (!battleResult.equals("")) {
                switch (Utils.getValue(battleResult)) {
                    case "attacker wins the battle": {
                        // 转账给winner
                        new Thread(() -> {
                            // 查询链上数据
                            try {
                                battleSettlement(attackerAddress, defenderAddress, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        sendBattleResult(battleId, attackerAddress, defenderAddress, attackerSession, defenderSession, false);
                        break;
                    }
                    case "defender wins the battle": {
                        new Thread(() -> {
                            // 查询链上数据
                            try {
                                battleSettlement(defenderAddress, attackerAddress, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        sendBattleResult(battleId, defenderAddress, attackerAddress, defenderSession, attackerSession, false);
                        break;
                    }
                    case "tie": {
                        new Thread(() -> {
                            // 查询链上数据
                            try {
                                battleSettlement(attackerAddress, defenderAddress, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                        sendBattleResult(battleId, defenderAddress, attackerAddress, defenderSession, attackerSession, true);
                        break;
                    }
                    default: {
                        errorMsg(attackerSession, "battle end error");
                        errorMsg(defenderSession, "battle end error");
                        break;
                    }
                }
//                JSONObject jsonObject = new JSONObject()
//                        .element("stage", "battleEnd")
//                        .element("result")
//                        .element("positive", true)
//                        .element("round", battleData.get(battleId).getInt("round"))
//                        .element("timer", 0)
//                        .element("isOver", true)
//                        .element("winner", winner)
//                        .element("loser", loser);
//                sendMsg(attackerSession, jsonObject.toString());
//                sendMsg(defenderSession, jsonObject.toString());
            }
            else {
                errorMsg(attackerSession, "battle end error");
                errorMsg(defenderSession, "battle end error");
            }
            // 清除数据
            playerSoldiers.remove(battleId);
            playerSession.remove(battleId);
            battleData.remove(battleId);
        }
        else {
            // 游戏未结束，开启下一轮
            int round = battleData.get(battleId).getInt("round");
            round += 1;
            battleData.get(battleId).element("round", round);
            // 延迟3秒
            TimeUnit.SECONDS.sleep(3);
            // 游戏开始倒计时
            roundCountDown(roundTimer, gameId, battleId, round, attackerAddress, defenderAddress);
//            try {
//                int round = battleData.get(battleId).getInt("round");
//                round += 1;
//                battleData.get(battleId).element("round", round);
//                // 延迟3秒
//                TimeUnit.SECONDS.sleep(3);
//                // 游戏开始倒计时
//                roundCountDown(roundTimer, gameId, battleId, round, attackerAddress, defenderAddress);
//            } catch (Exception e) {
//                System.out.println("Got an exception: " + e.getMessage());
//            }
        }
    }

    private void win(String battleId, String address) throws NullPointerException, Exception {
        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
        String defenderAddress = battleData.get(battleId).getString("defenderAddress");
        String opponent = address.equals(attackerAddress) ? defenderAddress : attackerAddress;
        // 对战结果
        playerSoldiers.get(battleId).get(address).element("result", "win");
        playerSoldiers.get(battleId).get(opponent).element("result", "lose");
        // 士兵数量减一
        int quantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
        int opponentQuantity = playerSoldiers.get(battleId).get(opponent).getInt("quantity");
        playerSoldiers.get(battleId).get(address).element("quantity", quantity - 1);
        playerSoldiers.get(battleId).get(opponent).element("quantity", opponentQuantity - 1);
        // 对手士兵战力下降
        int opponentSoldier = playerSoldiers.get(battleId).get(opponent).getInt("soldier");
        double opponentSoldierPoint = SiegeParams.getSoldiersPoint().get(opponentSoldier);
        double opponentPoint = playerSoldiers.get(battleId).get(opponent).getDouble("price");
        double point = playerSoldiers.get(battleId).get(address).getDouble("price");
        playerSoldiers.get(battleId).get(opponent).element("price", opponentPoint - opponentSoldierPoint);
        // 士兵仓库减少
        int soldier = playerSoldiers.get(battleId).get(address).getInt("soldier");
        List<Integer> soldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
        List<Integer> opponentSoldiers = castList(playerSoldiers.get(battleId).get(opponent).get("type"), Integer.class);
        if (soldiers.contains(soldier) && opponentSoldiers.contains(opponentSoldier)) {
            final CopyOnWriteArrayList<Integer> cowSoldiers = new CopyOnWriteArrayList<>(soldiers);
            final CopyOnWriteArrayList<Integer> cowOpponentSoldiers = new CopyOnWriteArrayList<>(opponentSoldiers);
            for (Integer item : cowSoldiers) {
                if (item == soldier) {
                    cowSoldiers.remove(item);
                    break;
                }
            }
            for (Integer item : cowOpponentSoldiers) {
                if (item == opponentSoldier) {
                    cowOpponentSoldiers.remove(item);
                    break;
                }
            }
            soldiers = cowSoldiers;
            opponentSoldiers = cowOpponentSoldiers;
        }
        else {
            throw new NullPointerException("士兵不存在");
        }
        playerSoldiers.get(battleId).get(address).element("type", soldiers);
        playerSoldiers.get(battleId).get(opponent).element("type", opponentSoldiers);

        Session winSession = playerSession.get(battleId).get(address);
        Session loseSession = playerSession.get(battleId).get(opponent);

        // 本轮结束
//        playerSoldiers.get(battleId).get(address).element("round", true);
//        playerSoldiers.get(battleId).get(opponent).element("round", true);
        // 告知双方
//        JSONObject jsonObject = new JSONObject()
//                .element("operation", "pickSoldier")
//                .element("status", true)
//                .element("situation", "judge")
//                .element("result", "win")
//                .element("myPoint", point)
//                .element("opponentPoint", opponentPoint - opponentSoldierPoint)
//                .element("myQuantity", quantity - 1)
//                .element("opponentQuantity", opponentQuantity - 1);
//
//        JSONObject jsonObject1 = new JSONObject()
//                .element("operation", "pickSoldier")
//                .element("status", true)
//                .element("situation", "judge")
//                .element("result", "lose")
//                .element("myPoint", opponentPoint - opponentSoldierPoint)
//                .element("opponentPoint", point)
//                .element("myQuantity", opponentQuantity - 1)
//                .element("opponentQuantity", quantity - 1);
        JSONObject jsonObject = new JSONObject()
                .element("stage", "roundEnd")
                .element("result", "win");
        JSONObject jsonObject1 = new JSONObject()
                .element("stage", "roundEnd")
                .element("result", "lose");
        sendMsg(winSession, jsonObject.toString());
        sendMsg(loseSession, jsonObject1.toString());
    }

    private void tie(String battleId) throws Exception {
        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
        String defenderAddress = battleData.get(battleId).getString("defenderAddress");
        // 对战结果
        playerSoldiers.get(battleId).get(attackerAddress).element("result", "tie");
        playerSoldiers.get(battleId).get(defenderAddress).element("result", "tie");

        // 士兵数量减一
        int attackerQuantity = playerSoldiers.get(battleId).get(attackerAddress).getInt("quantity");
        int defenderQuantity = playerSoldiers.get(battleId).get(defenderAddress).getInt("quantity");
        playerSoldiers.get(battleId).get(attackerAddress).element("quantity", attackerQuantity - 1);
        playerSoldiers.get(battleId).get(defenderAddress).element("quantity", defenderQuantity - 1);
        // 双方战力
        double attackerPoint = playerSoldiers.get(battleId).get(attackerAddress).getDouble("price");
        double defenderPoint = playerSoldiers.get(battleId).get(defenderAddress).getDouble("price");
        // 士兵仓库减少
        int attackerSoldier = playerSoldiers.get(battleId).get(attackerAddress).getInt("soldier");
        int defenderSoldier = playerSoldiers.get(battleId).get(defenderAddress).getInt("soldier");
        double attackerSoldierPoint = SiegeParams.getSoldiersPoint().get(attackerSoldier);
        double defenderSoldierPoint = SiegeParams.getSoldiersPoint().get(defenderSoldier);
        attackerPoint -= attackerSoldierPoint;
        defenderPoint -= defenderSoldierPoint;
        playerSoldiers.get(battleId).get(attackerAddress).element("price", attackerPoint);
        playerSoldiers.get(battleId).get(defenderAddress).element("price", defenderPoint);

        List<Integer> attackerSoldiers = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
        List<Integer> defenderSoldiers = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);

        if (attackerSoldiers.contains(attackerSoldier) && defenderSoldiers.contains(defenderSoldier)) {
            final CopyOnWriteArrayList<Integer> cowAttackerSoldiers = new CopyOnWriteArrayList<>(attackerSoldiers);
            final CopyOnWriteArrayList<Integer> cowDefenderSoldiers = new CopyOnWriteArrayList<>(defenderSoldiers);
            for (Integer item : cowAttackerSoldiers) {
                if (item == attackerSoldier) {
                    cowAttackerSoldiers.remove(item);
                    break;
                }
            }
            for (Integer item : cowDefenderSoldiers) {
                if (item == defenderSoldier) {
                    cowDefenderSoldiers.remove(item);
                    break;
                }
            }
            attackerSoldiers = cowAttackerSoldiers;
            defenderSoldiers = cowDefenderSoldiers;
        }
        else {
            throw new NullPointerException("士兵不存在");
        }
//        if (attackerSoldiers.contains(attackerSoldier) && defenderSoldiers.contains(defenderSoldier)) {
//            attackerSoldiers.remove(attackerSoldier);
//            defenderSoldiers.remove(defenderSoldier);
//        }
//        else {
//            throw new NullPointerException("士兵不存在");
//        }
        playerSoldiers.get(battleId).get(attackerAddress).element("type", attackerSoldiers);
        playerSoldiers.get(battleId).get(defenderAddress).element("type", defenderSoldiers);

        // 本轮结束
//        playerSoldiers.get(battleId).get(attackerAddress).element("round", true);
//        playerSoldiers.get(battleId).get(defenderAddress).element("round", true);

        Session attackerSession = playerSession.get(battleId).get(attackerAddress);
        Session defenderSession = playerSession.get(battleId).get(defenderAddress);
        // 告知双方
//        JSONObject jsonObject = new JSONObject()
//                .element("operation", "pickSoldier")
//                .element("status", true)
//                .element("situation", "judge")
//                .element("result", "tie")
//                .element("myPoint", attackerPoint)
//                .element("opponentPoint", defenderPoint)
//                .element("myQuantity", attackerQuantity - 1)
//                .element("opponentQuantity", defenderQuantity - 1);
//
//        JSONObject jsonObject1 = new JSONObject()
//                .element("operation", "pickSoldier")
//                .element("status", true)
//                .element("situation", "judge")
//                .element("result", "tie")
//                .element("myPoint", defenderPoint)
//                .element("opponentPoint", attackerPoint)
//                .element("myQuantity", defenderQuantity - 1)
//                .element("opponentQuantity", attackerQuantity - 1);
        JSONObject jsonObject = new JSONObject()
                .element("stage", "roundEnd")
                .element("result", "tie");
        sendMsg(attackerSession, jsonObject.toString());
        sendMsg(defenderSession, jsonObject.toString());
    }

    private void battleSettlement(String winner, String loser, boolean tie) throws Exception{
        String winnerInfo = hyperchainService.getPlayersStatus(winner);
        String loserInfo = hyperchainService.getPlayersStatus(loser);
        int winnerPointer = getPointer(winnerInfo, 5);
        int loserPointer = getPointer(loserInfo, 5);
        winnerPointer = decreasePointer(winnerPointer);
        loserPointer = decreasePointer(loserPointer);
        String winnerGameData = hyperchainService.getGameData(winner, winnerPointer);
        String loserGameData = hyperchainService.getGameData(loser, loserPointer);
        long winnerPrice = getPrice(winnerGameData, 1);
        long loserPrice = getPrice(loserGameData, 1);
        String symbol = "SIG";
        if (!tie) {
            // 获胜者拿回80%的费用
            String back = hyperchainService.transfer(Config.getDeployAccount().getAddress(), winner, new Double(winnerPrice * 0.8).longValue(), symbol, "back", Config.getDeployAccountJson());
            // 获胜者获得失败者70%的费用
            String award = hyperchainService.transfer(Config.getDeployAccount().getAddress(), winner, new Double(loserPrice * 0.7).longValue(), symbol, "award", Config.getDeployAccountJson());
            assert (back.equals("transfer success") && award.equals("transfer success"));
        }
        else {
            String returnBack1 = hyperchainService.transfer(Config.getDeployAccount().getAddress(), winner, new Double(winnerPrice).longValue(), symbol, "tie", Config.getDeployAccountJson());
            String returnBack2=  hyperchainService.transfer(Config.getDeployAccount().getAddress(), loser, new Double(loserPrice).longValue(), symbol, "tie", Config.getDeployAccountJson());
            assert (returnBack1.equals("transfer success") && returnBack2.equals("transfer success"));
        }
    }

    private void sendBattleResult(String battleId, String winner, String loser, Session winnerSession, Session loserSession, boolean tie) throws Exception {
        if (!tie) {
            // 有胜负
            double winnerCost = playerSoldiers.get(battleId).get(winner).getDouble("allPrice");
            double loserCost = playerSoldiers.get(battleId).get(loser).getDouble("allPrice");
            double winnerProfit = winnerCost * 0.8 + loserCost * 0.7;
            double loserProfit = -loserCost;
            JSONObject winnerJson = new JSONObject()
                    .element("stage", "battleEnd")
                    .element("result", "win")
                    .element("profit", winnerProfit)
                    .element("cost", winnerCost);
            JSONObject loserJson = new JSONObject()
                    .element("stage", "battleEnd")
                    .element("result", "lose")
                    .element("profit", loserProfit)
                    .element("cost", loserCost);
            sendMsg(winnerSession, winnerJson.toString());
            sendMsg(loserSession, loserJson.toString());
        }
        else {
            JSONObject tieJson = new JSONObject()
                    .element("stage", "battleEnd")
                    .element("result", "tie")
                    .element("profit", 0.)
                    .element("cost", 0.);
            sendMsg(winnerSession, tieJson.toString());
            sendMsg(loserSession, tieJson.toString());
        }
    }

    private int getPointer(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            return (Integer.valueOf(valueStr));
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0;
        }
    }

    private long getPrice(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            return (Long.valueOf(valueStr));
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0;
        }
    }

    private int decreasePointer(int pointer) {
        return (pointer - 1 + 5) % 5;
    }

    private void sendMsg(Session session, String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }

    private void sendAll(Map<String, Session> map, String msg) throws Exception {
        for (String address: map.keySet()) {
            sendMsg(map.get(address), msg);
        }
    }

    private void errorMsg(Session session, String message) throws Exception {
        JSONObject jsonObject = new JSONObject()
                .element("stage", "notification")
                .element("situation", "systemError")
                .element("message", message);
        sendMsg(session, jsonObject.toString());
    }
}
