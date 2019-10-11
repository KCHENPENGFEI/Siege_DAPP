package com.example.zjusiege.WebSocket;

import cn.hyperchain.sdk.rpc.account.Account;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import com.example.zjusiege.Utils.Utils;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/beforeBattle/{gameId}/{battleId}")
@Component
public class BeforeBattle {
    private HyperchainService hyperchainService = new HyperchainService();
    private Account deployAccount = Config.getDeployAccount();
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    private static final Map<String, Boolean> gameStarted = new ConcurrentHashMap<>();
    // 使用map来收集各个用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, JSONObject>> playerSoldiersInit = new ConcurrentHashMap<>();
    // 使用map来存储每一场对战的数据
    private static final Map<String, JSONObject> battleData = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
//    private Session session;
//    private static String attackerAddress;
//    private static String defenderAddress;
//    private static int cityId;
    private static int playersPerGame = 2;
    private static int buySoldiersTimer = 20;
    //    private static int round = 1;
    private static int roundTimer = 30;
//    private static boolean isOver = false;
    private int debug = 0;

    @OnOpen
    public void connect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception {
        initGameStarted(battleId);
        initBattleData(battleId);
        initPlayerSoldiers(battleId);
        playerNum += 1;
        System.out.println("BeforeBattle connect success!");
        System.out.println("playerNum: " + playerNum);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception {
        playerNum -= 1;
        System.out.println("BeforeBattle disConnect!");
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
                // 相符，对玩家进行注册
                boolean registered = register(battleId, address, session);
                if (registered) {
                    // 如果是第一次注册
                    if (playerSession.get(battleId).size() == playersPerGame && !gameStarted.get(battleId)) {
                        gameStarted.replace(battleId, true);
                        // 开启一个购买士兵的倒计时
                        // 主线程延迟2秒进行数据查找以及初始化
                        new Thread(()-> {
                            try {
                                // 购买士兵开始倒计时
                                TimeUnit.SECONDS.sleep(1);
                                buySoldiersCountDown(buySoldiersTimer, gameId, battleId);
                            } catch (Exception e) {
                                System.out.println("Got an exception: " + e.getMessage());
                            }
                        }).start();
                    }
                }
                else {
                    // 断线重连
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "reConnect")
                            .element("status", true);
                    sendMsg(session, jsonObject.toString());
                }
                // TODO ? 是否加入玩家状态检测，以及初始化商店
            }
            else {
                // 不相符
                //TODO 提示用户
                System.out.println("battleId not match");
            }
        }
        else {
            String operation = params.getString("operation");
//            switch (operation) {
//                case "buySoldiers":
//                    buySoldiers(params, gameId, battleId, address, session);
//                    break;
//                case "departure":
//                    departure(gameId, battleId, address, session);
//                    break;
//                default:
//                    System.out.println("invalid operation");
//                    break;
//            }
            if (operation.equals("departure")) {
                departure(params, gameId, battleId, address, session);
            }
            else {
                System.out.println("invalid operation");
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("BeforeBattle connect error!");
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

    private void initBattleData(String battleId) {
        String[] information = battleId.split("&&");
        String attackerAddress = information[0];
        String defenderAddress = information[1];
        int cityId = Integer.valueOf(information[2]);
        int round = 1;
        boolean isOver = false;
        if (!battleData.containsKey(battleId)) {
            // 初始化数据
            JSONObject jsonObject = new JSONObject()
                    .element("attackerAddress", attackerAddress)
                    .element("defenderAddress", defenderAddress)
                    .element("cityId", cityId)
                    .element("round", round)
                    .element("isOver", isOver);
            battleData.put(battleId, jsonObject);
        }
    }

    private void initPlayerSoldiers(String battleId) {
        String[] information = battleId.split("&&");
        String attackerAddress = information[0];
        String defenderAddress = information[1];
        if (!playerSoldiersInit.containsKey(battleId)) {
            Map<String, JSONObject> map = new ConcurrentHashMap<>();
            map.put(attackerAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
                    .element("allPrice", 0.)
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false)
                    .element("round", 1)
                    .element("soldier", 0)
                    .element("pick", false)
                    .element("result", "none"));
            map.put(defenderAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
                    .element("allPrice", 0.)
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false)
                    .element("round", 1)
                    .element("soldier", 0)
                    .element("pick", false)
                    .element("result", "none"));
            playerSoldiersInit.put(battleId, map);
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

    private boolean register(String battleId, String address, Session session) {
        if (!playerSession.containsKey(battleId)) {
            // 构建战场
            Map<String, Session> map = new ConcurrentHashMap<>();
            map.put(address, session);
            playerSession.put(battleId, map);
            // 第一次注册，返回为真
            return true;
        }
        else {
            // 第一次注册，返回为真
            if (!playerSession.get(battleId).containsKey(address)) {
                Map<String, Session> map = playerSession.get(battleId);
                map.put(address, session);
                playerSession.put(battleId, map);
                return true;
            }
            else {
                // 已经注册过，返回为假
                playerSession.get(battleId).replace(address, session);
                return false;
            }
        }
    }

    private void departure(JSONObject params, String gameId, String battleId, String address, Session session) throws Exception {
        assert (gameStarted.get(battleId));
        if (!gameStarted.get(battleId)) {
            // 对手未进入游戏
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "waitingOpponent")
                    .element("operation", "buySoldiers")
                    .element("status", false);
            sendMsg(session, jsonObject.toString());
        }
        else {
            List<Integer> type = castList(params.get("type"), Integer.class);
            String symbol = "SIG";
            String signature = params.getString("signature");
            // 双方地址
            String attackerAddress = battleData.get(battleId).getString("attackerAddress");
            String defenderAddress = battleData.get(battleId).getString("defenderAddress");
            // 当前round
            int cRound = battleData.get(battleId).getInt("round");
            if (type != null && type.size() > 0) {
                int quantity = type.size();
                double price = 0.;
                for (int item: type) {
                    price += SiegeParams.getSoldiersPoint().get(item);
                }
                if (price <= 100) {
                    // 首先进行缴费
                    String transferResult = hyperchainService.transfer(address, deployAccount.getAddress(), new Double(price * SiegeParams.getPrecision()).longValue(), symbol, "buy soldiers", signature);
                    if (transferResult.equals("transfer success")) {
                        // 链上执行buySoldiers操作
                        String buyResult = hyperchainService.buySoldiers(Integer.valueOf(gameId), address, new Double(price * SiegeParams.getPrecision()).longValue(), type, new Double(price * SiegeParams.getPrecision()).longValue(), quantity);
                        if (buyResult.equals("success")) {
                            // 链上执行departure
                            String departureResult = hyperchainService.departure(Integer.valueOf(gameId), address);
                            if (departureResult.equals("success")) {
                                // 成功
                                updatePlayerSoldiers(battleId, address, type, price, price, quantity, true, false, 1, 0, false, "none");
                                String opponent = address.equals(attackerAddress) ? defenderAddress: attackerAddress;
                                // 更新己方状态
                                playerSoldiersInit.get(battleId).get(address).element("ready", true);
                                // 检查对方状态
                                boolean allReady = playerSoldiersInit.get(battleId).get(opponent).getBoolean("ready");
                                if (allReady) {
                                    // 双方就绪
                                    JSONObject jsonObject = new JSONObject()
                                            .element("stage", "response")
                                            .element("operation", "departure")
                                            .element("status", true)
                                            .element("ready", true);
                                    sendMsg(session, jsonObject.toString());
                                }
                                else {
                                    // 告知等待
                                    JSONObject jsonObject = new JSONObject()
                                            .element("stage", "response")
                                            .element("operation", "departure")
                                            .element("status", true)
                                            .element("ready", false);
                                    sendMsg(session, jsonObject.toString());
                                }
                            }
                            else {
                                // departure失败
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "response")
                                        .element("operation", "departure")
                                        .element("status", false)
                                        .element("message", "departure failed");
                                sendMsg(session, jsonObject.toString());
                            }
                        }
                        else {
                            // buySoldier操作失败
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "response")
                                    .element("operation", "departure")
                                    .element("status", false)
                                    .element("message", "buySoldiers failed");
                            sendMsg(session, jsonObject.toString());
                        }
                    }
                    else {
                        // 告知转账失败
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "response")
                                .element("operation", "departure")
                                .element("status", false)
                                .element("message", "transfer failed");
                        sendMsg(session, jsonObject.toString());
                    }
                }
                else {
                    // 买兵超过限制
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "response")
                            .element("operation", "departure")
                            .element("status", false)
                            .element("message", "soldier point limited");
                    sendMsg(session, jsonObject.toString());
                }
            }
            else {
                // 告知操作失败
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "response")
                        .element("operation", "departure")
                        .element("status", false)
                        .element("message", "soldiers empty");
                sendMsg(session, jsonObject.toString());

            }
        }
    }

    private void buySoldiersCountDown(int seconds, String gameId, String battleId) throws Exception {
        System.out.println("buySoldiers--- count down from " + seconds + " s ");
        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
        String defenderAddress = battleData.get(battleId).getString("defenderAddress");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(battleId);
            @Override
            public void run() {
                if (battleData.containsKey(battleId)) {
                    if (!playerSoldiersInit.get(battleId).get(attackerAddress).getBoolean("ready") || !playerSoldiersInit.get(battleId).get(defenderAddress).getBoolean("ready")) {
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
            }
        }, 0, 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Buy soldiers time is out");
        // 若此双方还有未进行departure，则不构成游戏对局
        if (!playerSoldiersInit.get(battleId).get(attackerAddress).getBoolean("ready") || !playerSoldiersInit.get(battleId).get(defenderAddress).getBoolean("ready")) {
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "battleCancel");
            sendMsg(playerSession.get(battleId).get(attackerAddress), jsonObject.toString());
            sendMsg(playerSession.get(battleId).get(defenderAddress), jsonObject.toString());
        }
//        if (!playerSoldiersInit.get(battleId).get(attackerAddress).getBoolean("ready")) {
////            departure(gameId, battleId, attackerAddress, playerSession.get(battleId).get(attackerAddress));
//        }
//        if (!playerSoldiersInit.get(battleId).get(defenderAddress).getBoolean("ready")) {
////            departure(gameId, battleId, defenderAddress, playerSession.get(battleId).get(defenderAddress));
//        }
    }

    private void updatePlayerSoldiers(String battleId, String address, List<Integer> type, double allPoint, double point, int quantity, boolean pay, boolean ready, int round, int soldier, boolean pick, String result) {
        playerSoldiersInit.get(battleId).get(address).element("type", type);
        playerSoldiersInit.get(battleId).get(address).element("allPrice", allPoint);
        playerSoldiersInit.get(battleId).get(address).element("price", point);
        playerSoldiersInit.get(battleId).get(address).element("quantity", quantity);
        playerSoldiersInit.get(battleId).get(address).element("pay", pay);
        playerSoldiersInit.get(battleId).get(address).element("ready", ready);
        playerSoldiersInit.get(battleId).get(address).element("round", round);
        playerSoldiersInit.get(battleId).get(address).element("soldier", soldier);
        playerSoldiersInit.get(battleId).get(address).element("pick", pick);
        playerSoldiersInit.get(battleId).get(address).element("result", result);
    }

    private void sendMsg(Session session, String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }

    private void sendAll(Map<String, Session> map, String msg) throws Exception {
        for (String address: map.keySet()) {
            sendMsg(map.get(address), msg);
        }
    }

    static Map<String, Map<String, Session>> getPlayerSession() {
        return playerSession;
    }

    static Map<String, Map<String, JSONObject>> getPlayerSoldiersInit() {
        return playerSoldiersInit;
    }

    static Map<String, JSONObject> getBattleData() {
        return battleData;
    }

//    static void setplayerSoldiersInit(Map<String, Map<String, JSONObject>> map) {
//        playerSoldiersInit = map;
//    }

//    static void setBattleData(Map<String, JSONObject> map) {
//        battleData = map;
//    }
}
