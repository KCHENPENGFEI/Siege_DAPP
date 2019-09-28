package com.example.zjusiege.WebSocket;

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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/battle/{gameId}/{battleId}")
@Component
public class SiegeBattle {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    // 使用map来收集各个用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, JSONObject>> playerSoldiers = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
//    private Session session;
    private static String attackerAddress;
    private static String defenderAddress;
    private static int cityId;
    private static int playersPerGame = 2;
    private static int buySoldiersTimer = 120;
    private static int round = 1;
    private static int roundTimer = 60;
    private static boolean isOver = false;


    @OnOpen
    public void connect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception{
        // 创建战场以及添加用户
        String[] information = battleId.split("&&");
        attackerAddress = information[0];
        defenderAddress = information[1];
        cityId = Integer.valueOf(information[2]);

        if (!playerSoldiers.containsKey(battleId)) {
            Map<String, JSONObject> map = new ConcurrentHashMap<>();
            map.put(attackerAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false)
                    .element("round", round)
                    .element("soldier", 0)
                    .element("pick", false)
                    .element("result", "none"));
            map.put(defenderAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false)
                    .element("round", round)
                    .element("soldier", 0)
                    .element("pick", false)
                    .element("result", "none"));
            playerSoldiers.put(battleId, map);
        }

//        测试用
//        if (!playerSoldiers.containsKey(battleId)) {
//            Map<String, JSONObject> map = new ConcurrentHashMap<>();
//            map.put(attackerAddress, new JSONObject()
//                    .element("type", Arrays.asList(5,5,5,5,5))
//                    .element("price", 150.)
//                    .element("quantity", 5)
//                    .element("pay", true)
//                    .element("ready", true)
//                    .element("round", true)
//                    .element("soldier", 0)
//                    .element("pick", false)
//                    .element("result", "none"));
//            map.put(defenderAddress, new JSONObject()
//                    .element("type", Arrays.asList(1,1,1))
//                    .element("price", 30.)
//                    .element("quantity", 3)
//                    .element("pay", true)
//                    .element("ready", true)
//                    .element("round", true)
//                    .element("soldier", 0)
//                    .element("pick", false)
//                    .element("result", "none"));
//            playerSoldiers.put(battleId, map);
//        }

        playerNum += 1;
        System.out.println("playerNum: " + playerNum + " attackerAddress: " + attackerAddress + " defenderAddress: " + defenderAddress);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception {
        playerNum -= 1;
        System.out.println("playerNum: " + playerNum);
    }

    @OnMessage
    public void onMessage(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, String msg, Session session) throws Exception {
        JSONObject params = JSONObject.fromObject(msg);
        if (params.getBoolean("first")) {
            // 第一次连接时做一些链上数据查询，确保玩家的游戏状态是正确的，暂时略
            // 首先进行注册
            String address = params.getString("address");
            register(battleId, address, session);
            if (playerSession.get(battleId).size() == playersPerGame) {
                // 开启一个购买士兵的倒计时
                // 主线程延迟2秒进行数据查找以及初始化
                new Thread(()-> {
                    try {
                        initShop(battleId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }).start();
                TimeUnit.SECONDS.sleep(2);

                new Thread(()-> {
                    try {
                        // 游戏开始倒计时
                        buySoldiersCountDown(buySoldiersTimer, battleId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        else {
            String address = params.getString("address");
            String operation = params.getString("operation");
            switch (operation) {
                case "buySoldiers":
                    buySoldiers(params, battleId, address, session);
                    break;
                case "departure":
                    departure(gameId, battleId, address, session);
                    break;
                case "pickSoldier":
                    pickSoldier(params, gameId, battleId, address, session);
                    break;
                default:
                    System.out.println("invalid operation");
                    break;
            }
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
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

    private void register(String battleId, String address, Session session) {
        if (!playerSession.containsKey(battleId)) {
            // 构建战场
            Map<String, Session> map = new ConcurrentHashMap<>();
            map.put(address, session);
            playerSession.put(battleId, map);
        }
        else {
            if (!playerSession.get(battleId).containsKey(address)) {
                Map<String, Session> map = playerSession.get(battleId);
                map.put(address, session);
                playerSession.replace(battleId, map);
            }
            else {
                playerSession.get(battleId).replace(address, session);
            }
        }
    }

    private void sendMsg(Session session, String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }

    private void sendAll(Map<String, Session> map, String msg) throws Exception {
        for (String address: map.keySet()) {
            sendMsg(map.get(address), msg);
        }
    }

    private void buySoldiersCountDown(int seconds, String battleId) throws InterruptedException {
        System.out.println("buySoldiers--- count down from " + seconds + " s ");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(battleId);
            @Override
            public void run() {
                if (!playerSoldiers.get(battleId).get(attackerAddress).getBoolean("ready") || !playerSoldiers.get(battleId).get(defenderAddress).getBoolean("ready")) {
                    System.out.println("Buy soldiers time remains " + --curSec + " s");
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "buySoldiers")
                            .element("positive", true)
                            .element("timer", curSec);
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
        System.out.println("Buy soldiers time is out");
    }

    private void roundCountDown(int seconds, String battleId, int curRound) throws InterruptedException {
        System.out.println("roundCountDown--- " + curRound + " : count down from " + seconds + " s");

        playerSoldiers.get(battleId).get(attackerAddress).element("round", curRound);
        playerSoldiers.get(battleId).get(defenderAddress).element("round", curRound);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(battleId);
            @Override
            public void run() {
                if (round == curRound) {
                    System.out.println("Round " + curRound + " time remains " + --curSec + " s");
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "battle")
                            .element("positive", true)
                            .element("round", curRound)
                            .element("timer", curSec)
                            .element("isOver", isOver);
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
    }

    private void initShop(String battleId) throws Exception {
        Map<String, Session> map = playerSession.get(battleId);
        JSONArray jsonArray = new JSONArray();
        List<String> soldiersName = SiegeParams.getSoldiersName();
        List<Integer> soldiersPoint = SiegeParams.getSoldiersPoint();
        List<String> soldiersDescription = SiegeParams.getSoldiersDescription();
        for (int i = 1; i <= SiegeParams.getSoldierNum(); ++i) {
            JSONObject jsonObject = new JSONObject()
                    .element("type", soldiersName.get(i))
                    .element("description", soldiersDescription.get(i))
                    .element("price", soldiersPoint.get(i));
            jsonArray.add(jsonObject);
        }
        sendAll(map, jsonArray.toString());
    }

    private void buySoldiers(JSONObject params, String battleId, String address, Session session) {
        List<Integer> type = castList(params.get("type"), Integer.class);
        double price = params.getDouble("price");
        int quantity = params.getInt("quantity");
        String symbol = "SIG";
        String signature = params.getString("signature");

        // 首先进行缴费
        try {
//            HyperchainService hyperchainService = new HyperchainService();
//            String transferResult = hyperchainService.transfer(address, Config.getDeployAccount().getAddress(), new Double(price * SiegeParams.getPrecision()).longValue(), symbol, "buy soldiers", signature);
            String transferResult = "transfer success";
            if (transferResult.equals("transfer success")) {
                // 更新playerSoldier
                if (type != null) {
                    List<Integer> oldSoldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
                    assert oldSoldiers != null;
                    oldSoldiers.addAll(type);
                    double oldPrice = playerSoldiers.get(battleId).get(address).getDouble("price");
                    double newPrice = oldPrice + price;
                    int oldQuantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
                    int newQuantity = oldQuantity + quantity;
                    updatePlayerSoldiers(battleId, address, type, newPrice, newQuantity,true, false, 1, 0, false, "none");
                }
                // 告知购买士兵成功
                JSONObject jsonObject = new JSONObject()
                        .element("operation", "transfer")
                        .element("status", true);
                sendMsg(session, jsonObject.toString());
            }
            else {
                // 告知转账失败
                JSONObject jsonObject = new JSONObject()
                        .element("operation", "transfer")
                        .element("status", false);
                sendMsg(session, jsonObject.toString());
            }
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
    }

    private void departure(String gameId, String battleId, String address, Session session) {
        List<Integer> type = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
        long price = new Double(playerSoldiers.get(battleId).get(address).getDouble("price") * SiegeParams.getPrecision()).longValue();
        int quantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
        try {
//            HyperchainService hyperchainService = new HyperchainService();
            // 考虑将buySoldiers放在购买士兵部分
            // 后续改进代码，将两个操作用if else进行嵌套
//            String buyResult = hyperchainService.buySoldiers(Integer.valueOf(gameId), address, price, type, price, quantity);
//            String departureResult = hyperchainService.departure(Integer.valueOf(gameId), address);
            String buyResult = "success";
            String departureResult = "success";
            if (buyResult.equals("success") && (departureResult.equals("success"))) {
                String opponent = address.equals(attackerAddress) ? defenderAddress: attackerAddress;
                // 更新状态
                playerSoldiers.get(battleId).get(address).element("ready", true);
                // 检查对方状态
                boolean allReady = playerSoldiers.get(battleId).get(opponent).getBoolean("ready");
                if (allReady) {
                    // 告知玩家结果以及对面的信息
                    // 考虑玩家自己牌也由服务器发送？
                    JSONObject jsonObject = new JSONObject()
                            .element("operation", "departure")
                            .element("status", true)
                            .element("ready", true)
                            .element("opponent", opponent)
                            .element("myPoint", playerSoldiers.get(battleId).get(address).getDouble("price"))
                            .element("opponentPoint", playerSoldiers.get(battleId).get(opponent).getDouble("price"))
                            .element("myQuantity", playerSoldiers.get(battleId).get(address).getInt("quantity"))
                            .element("opponentQuantity", playerSoldiers.get(battleId).get(opponent).getInt("quantity"));
                    sendMsg(session, jsonObject.toString());
                    // 告知对面自己的信息
                    JSONObject jsonObject1 = new JSONObject()
                            .element("operation", "departure")
                            .element("status", true)
                            .element("ready", true)
                            .element("opponent", address)
                            .element("myPoint", playerSoldiers.get(battleId).get(opponent).getDouble("price"))
                            .element("opponentPoint", playerSoldiers.get(battleId).get(address).getDouble("price"))
                            .element("myQuantity", playerSoldiers.get(battleId).get(opponent).getInt("quantity"))
                            .element("opponentQuantity", playerSoldiers.get(battleId).get(address).getInt("quantity"));
                    sendMsg(playerSession.get(battleId).get(opponent), jsonObject1.toString());

                    // 短暂延迟一下
                    TimeUnit.SECONDS.sleep(1);
                    // 服务器开启战斗时间倒计时
                    new Thread(()-> {
                        try {
                            // 延迟两秒
                            TimeUnit.SECONDS.sleep(2);
                            // 游戏开始倒计时
                            roundCountDown(roundTimer, battleId, round);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                else {
                    JSONObject jsonObject = new JSONObject()
                            .element("operation", "departure")
                            .element("status", true)
                            .element("ready", false);
                    sendMsg(session, jsonObject.toString());
                }
            }
            else {
                // 告知玩家失败
                JSONObject jsonObject = new JSONObject()
                        .element("operation", "departure")
                        .element("status", false);
                sendMsg(session, jsonObject.toString());
                // 进行退款，后续实现

            }
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
    }

    private void pickSoldier(JSONObject params, String gameId, String battleId, String address, Session session) {
        int soldier = params.getInt("soldier");
        // 确定对手
        String opponent = address.equals(attackerAddress) ? defenderAddress: attackerAddress;
//        double point = params.getDouble("point");
        playerSoldiers.get(battleId).get(address).element("soldier", soldier);
        playerSoldiers.get(battleId).get(address).element("pick", true);
        // 首先通知对方己方已出牌
        try {
            JSONObject jsonObject = new JSONObject()
                    .element("operation", "pickSoldier")
                    .element("status", true)
                    .element("situation", "opponentPick");
            sendMsg(playerSession.get(battleId).get(opponent), jsonObject.toString());
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
        // 检查对方是否出牌

        boolean allPick = playerSoldiers.get(battleId).get(opponent).getBoolean("pick");
        if (allPick) {
            // 链上执行判断
            try {
 //                HyperchainService hyperchainService = new HyperchainService();
                int aType = playerSoldiers.get(battleId).get(attackerAddress).getInt("soldier");
                int dType = playerSoldiers.get(battleId).get(defenderAddress).getInt("soldier");
//                String result = hyperchainService.pickAndBattle(Integer.valueOf(gameId), attackerAddress, defenderAddress, aType, dType);
                Session attackerSession = playerSession.get(battleId).get(attackerAddress);
                Session defenderSession = playerSession.get(battleId).get(defenderAddress);
                String result = "attacker wins this round";
                switch (result) {
                    // 可以考虑后端加上士兵是否在仓库中的验证
                    case "attacker wins this round": {
                        // 进攻者获胜
                        win(battleId, attackerAddress);
                        break;
                    }
                    case "defender wins this round": {
                        // 防守者获胜
                        win(battleId, defenderAddress);
                        break;
                    }
                    case "tie":
                        // 战平
                        tie(battleId);
                        break;
                    default:
                        // error
                        System.out.println("error");
                        break;
                }
                // 本轮结束，重置标志位，并且检查游戏是否结束
                new Thread(()-> {
                    playerSoldiers.get(battleId).get(attackerAddress).element("pick", false);
                    playerSoldiers.get(battleId).get(defenderAddress).element("pick", false);

                    List<Integer> attackerSoldiers = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
                    List<Integer> defenderSoldiers = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);
                    if (attackerSoldiers.size() == 0 || defenderSoldiers.size() == 0) {
                        // 游戏结束
                        isOver = true;
                        String winner;
                        String loser;
                        // 链上判定谁获胜
                        try {
                            HyperchainService hyperchainService = new HyperchainService();
                            String battleResult = hyperchainService.battleEnd(Integer.valueOf(gameId), attackerAddress, defenderAddress, cityId);
                            if (!battleResult.equals("contract calling error") && !battleResult.equals("unknown error")) {
                                switch (Utils.getValue(battleResult)) {
                                    case "attacker wins the battle":
                                        winner = attackerAddress;
                                        loser = defenderAddress;
                                        break;
                                    case "defender wins the battle":
                                        winner = defenderAddress;
                                        loser = attackerAddress;
                                        break;
                                    case "tie":
                                        winner = "nobody";
                                        loser = "nobody";
                                        break;
                                    default:
                                        winner = "";
                                        loser = "";
                                        break;
                                }
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "battle")
                                        .element("positive", true)
                                        .element("round", round)
                                        .element("timer", 0)
                                        .element("isOver", isOver)
                                        .element("winner", winner)
                                        .element("loser", loser);
                                sendMsg(attackerSession, jsonObject.toString());
                                sendMsg(defenderSession, jsonObject.toString());
                            }
                            else {
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "battle")
                                        .element("positive", false);
                                sendMsg(attackerSession, jsonObject.toString());
                                sendMsg(defenderSession, jsonObject.toString());
                            }
                        } catch (Exception e) {
                            System.out.println("Got an exception: " + e.getMessage());
                        }
                    }
                    if (!isOver) {
                        // 开启下一轮
                        try {
                            round += 1;
                            // 延迟两秒
                            TimeUnit.SECONDS.sleep(1);
                            // 游戏开始倒计时
                            roundCountDown(roundTimer, battleId, round);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } catch (Exception e) {
                System.out.println("Got an exception: " + e.getMessage());
            }
        }
        else {
            // 告知玩家等待
            try {
                JSONObject jsonObject = new JSONObject()
                        .element("operation", "pickSoldier")
                        .element("status", true)
                        .element("situation", "wait");
                sendMsg(playerSession.get(battleId).get(address), jsonObject.toString());
            } catch (Exception e) {
                System.out.println("Got an exception: " + e.getMessage());
            }
        }

    }

    private void updatePlayerSoldiers(String battleId, String address, List<Integer> type, double point, int quantity, boolean pay, boolean ready, int round, int soldier, boolean pick, String result) {
        playerSoldiers.get(battleId).get(address).element("type", type);
        playerSoldiers.get(battleId).get(address).element("price", point);
        playerSoldiers.get(battleId).get(address).element("quantity", quantity);
        playerSoldiers.get(battleId).get(address).element("pay", pay);
        playerSoldiers.get(battleId).get(address).element("ready", ready);
        playerSoldiers.get(battleId).get(address).element("round", round);
        playerSoldiers.get(battleId).get(address).element("soldier", soldier);
        playerSoldiers.get(battleId).get(address).element("pick", pick);
        playerSoldiers.get(battleId).get(address).element("result", result);
    }

    private void win(String battleId, String address) throws NullPointerException {
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
            final CopyOnWriteArrayList<Integer> cowOpponentSoldier = new CopyOnWriteArrayList<>(opponentSoldiers);
            for (Integer item : cowSoldiers) {
                if (item == soldier) {
                    cowSoldiers.remove(item);
                    break;
                }
            }
            for (Integer item : cowOpponentSoldier) {
                if (item == opponentSoldier) {
                    cowOpponentSoldier.remove(item);
                    break;
                }
            }
            soldiers = cowSoldiers;
            opponentSoldiers = cowOpponentSoldier;
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
        JSONObject jsonObject = new JSONObject()
                .element("operation", "pickSoldier")
                .element("status", true)
                .element("situation", "judge")
                .element("result", "win")
                .element("myPoint", point)
                .element("opponentPoint", opponentPoint - opponentSoldierPoint)
                .element("myQuantity", quantity - 1)
                .element("opponentQuantity", opponentQuantity - 1);

        JSONObject jsonObject1 = new JSONObject()
                .element("operation", "pickSoldier")
                .element("status", true)
                .element("situation", "judge")
                .element("result", "lose")
                .element("myPoint", opponentPoint - opponentSoldierPoint)
                .element("opponentPoint", point)
                .element("myQuantity", opponentQuantity - 1)
                .element("opponentQuantity", quantity - 1);
        try {
            sendMsg(winSession, jsonObject.toString());
            sendMsg(loseSession, jsonObject1.toString());
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
    }

    private void tie(String battleId) {
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
        List<Integer> attackerSoldiers = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
        List<Integer> defenderSoldiers = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);
        if (attackerSoldiers.contains(attackerSoldier) && defenderSoldiers.contains(defenderSoldier)) {
            attackerSoldiers.remove(attackerSoldier);
            defenderSoldiers.remove(defenderSoldier);
        }
        else {
            throw new NullPointerException("士兵不存在");
        }
        playerSoldiers.get(battleId).get(attackerAddress).element("type", attackerSoldiers);
        playerSoldiers.get(battleId).get(defenderAddress).element("type", defenderSoldiers);

        // 本轮结束
//        playerSoldiers.get(battleId).get(attackerAddress).element("round", true);
//        playerSoldiers.get(battleId).get(defenderAddress).element("round", true);

        Session attackerSession = playerSession.get(battleId).get(attackerAddress);
        Session defenderSession = playerSession.get(battleId).get(defenderAddress);
        // 告知双方
        JSONObject jsonObject = new JSONObject()
                .element("operation", "pickSoldier")
                .element("status", true)
                .element("situation", "judge")
                .element("result", "tie")
                .element("myPoint", attackerPoint)
                .element("opponentPoint", defenderPoint)
                .element("myQuantity", attackerQuantity - 1)
                .element("opponentQuantity", defenderQuantity - 1);

        JSONObject jsonObject1 = new JSONObject()
                .element("operation", "pickSoldier")
                .element("status", true)
                .element("situation", "judge")
                .element("result", "tie")
                .element("myPoint", defenderPoint)
                .element("opponentPoint", attackerPoint)
                .element("myQuantity", defenderQuantity - 1)
                .element("opponentQuantity", attackerQuantity - 1);
        try {
            sendMsg(attackerSession, jsonObject.toString());
            sendMsg(defenderSession, jsonObject1.toString());
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
    }

//    private void encryptWithPubKey(String publicKey, String privateKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidKeyException {
//        // ECDSA secp256k1 algorithm constants
//        BigInteger pointGPre = new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
//        BigInteger pointGPost = new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16);
//        BigInteger factorN = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
//        BigInteger fieldP = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);
//
//        Security.addProvider(new BouncyCastleProvider());
//        Cipher cipher = Cipher.getInstance("ECIES", "BC");
//        IESParameterSpec iesParams = new IESParameterSpec(null, null, 64);
//
//        //----------------------------
//        // Encrypt with public key
//        //----------------------------
//        String prePublicKeyStr = publicKey.substring(0, 64);
//        String postPublicKeyStr = publicKey.substring(64);
//
//        EllipticCurve ellipticCurve = new EllipticCurve(new ECFieldFp(fieldP), new BigInteger("0"), new BigInteger("7"));
//        ECPoint pointG = new ECPoint(pointGPre, pointGPost);
//        ECNamedCurveSpec namedCurveSpec = new ECNamedCurveSpec("secp256k1", ellipticCurve, pointG, factorN);
//
//        SecP256K1Curve secP256K1Curve = new SecP256K1Curve();
//        SecP256K1Point secP256K1Point = new SecP256K1Point(secP256K1Curve, new SecP256K1FieldElement(new BigInteger(prePublicKeyStr, 16)), new SecP256K1FieldElement(new BigInteger(postPublicKeyStr, 16)));
//        SecP256K1Point secP256K1PointG = new SecP256K1Point(secP256K1Curve, new SecP256K1FieldElement(pointGPre), new SecP256K1FieldElement(pointGPost));
//        ECDomainParameters domainParameters = new ECDomainParameters(secP256K1Curve, secP256K1PointG, factorN);
//        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(secP256K1Point, domainParameters);
//        BCECPublicKey publicKeySelf = new BCECPublicKey("ECDSA", publicKeyParameters, namedCurveSpec, BouncyCastleProvider.CONFIGURATION);
////        BCECPublicKey publicKeySelf = new BCECPublicKey();
//
//        // begin encrypt
//        cipher.init(Cipher.ENCRYPT_MODE, publicKeySelf, iesParams);
//        String cleartextFile = "contract/source.txt";
//        String ciphertextFile = "contract/cipher.txt";
//        byte[] block = new byte[64];
//        FileInputStream fis = new FileInputStream(cleartextFile);
//        FileOutputStream fos = new FileOutputStream(ciphertextFile);
//        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
//
//        int i;
//        while ((i = fis.read(block)) != -1) {
//            cos.write(block, 0, i);
//        }
//        cos.close();
//
//        //----------------------------
//        // Decrypt with private key
//        //----------------------------
//        BigInteger privateKeyValue = new BigInteger(privateKey, 16);
//
//        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, namedCurveSpec);
//        BCECPrivateKey privateKeySelf = new BCECPrivateKey("ECDSA", privateKeySpec, BouncyCastleProvider.CONFIGURATION);
//        // begin decrypt
//        String cleartextAgainFile = "contract/decrypt.txt";
//        cipher.init(Cipher.DECRYPT_MODE, privateKeySelf, iesParams);
//        fis = new FileInputStream(ciphertextFile);
//        CipherInputStream cis = new CipherInputStream(fis, cipher);
//        fos = new FileOutputStream(cleartextAgainFile);
//        while ((i = cis.read(block)) != -1) {
//            fos.write(block, 0, i);
//        }
//        fos.close();
//    }
}
