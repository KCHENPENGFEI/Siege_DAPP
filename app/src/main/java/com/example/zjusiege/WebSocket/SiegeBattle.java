package com.example.zjusiege.WebSocket;

import cn.hyperchain.sdk.account.Account;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.FiloopService;
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
    private FiloopService filoopService = new FiloopService();
//    private HyperchainService hyperchainService = new HyperchainService();
    private Account deployAccount = Config.getDeployAccount();
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    private static final Map<String, Boolean> gameStarted = new ConcurrentHashMap<>();
    // 使用map来收集各个用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, JSONObject>> playerSoldiers = new ConcurrentHashMap<>();
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


    @OnOpen
    public void connect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception{
        // 创建战场以及添加用户
//        String[] information = battleId.split("&&");
//        attackerAddress = information[0];
//        defenderAddress = information[1];
//        cityId = Integer.valueOf(information[2]);
        initBattleData(battleId);
        initPlayerSoldiers(battleId);
        initGameStarted(battleId);
//        System.out.println("battleData: " + battleData);
//        System.out.println("playerSoldiers: " + playerSoldiers);

//        if (!playerSoldiers.containsKey(battleId)) {
//            Map<String, JSONObject> map = new ConcurrentHashMap<>();
//            map.put(attackerAddress, new JSONObject()
//                    .element("type", new ArrayList<Integer>())
//                    .element("price", 0.)
//                    .element("quantity", 0)
//                    .element("pay", false)
//                    .element("ready", false)
//                    .element("round", round)
//                    .element("soldier", 0)
//                    .element("pick", false)
//                    .element("result", "none"));
//            map.put(defenderAddress, new JSONObject()
//                    .element("type", new ArrayList<Integer>())
//                    .element("price", 0.)
//                    .element("quantity", 0)
//                    .element("pay", false)
//                    .element("ready", false)
//                    .element("round", round)
//                    .element("soldier", 0)
//                    .element("pick", false)
//                    .element("result", "none"));
//            playerSoldiers.put(battleId, map);
//        }

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
        System.out.println("SiegeBattle connect success!");
        System.out.println("playerNum: " + playerNum);
//        System.out.println("playerNum: " + playerNum + " attackerAddress: " + battleData.get(battleId).getString("attackerAddress") + " defenderAddress: " + battleData.get(battleId).getString("defenderAddress"));
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception {
        playerNum -= 1;
        System.out.println("SiegeBattle disConnect!");
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
                // 对玩家进行注册
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
                    // 掉线重连状态
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "reConnect")
                            .element("status", true);
                    sendMsg(session, jsonObject.toString());
                }
                // 检查玩家的游戏状态，进行初始化界面
                String opponent = address.equals(battleData.get(battleId).getString("attackerAddress")) ? battleData.get(battleId).getString("defenderAddress"): battleData.get(battleId).getString("attackerAddress");
                String battleStage = checkBattleStage(battleId, address, opponent);
                switch (battleStage) {
                    case "beforeBattle":
                        // 初始化士兵商店
                        try {
                            initShop(battleId, address);
                        } catch (Exception e) {
                            System.out.println("Got an exception: " + e.getMessage());
                        }
                        break;
                    case "departureWaiting": {
                        // 等待
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "departureWaiting");
                        sendMsg(session, jsonObject.toString());
                        break;
                    }
                    case "inBattle":
                        // 初始化战场
                        try {
                            initBattleField(battleId, address, opponent, session);
                        } catch (Exception e) {
                            System.out.println("Got an exception: " + e.getMessage());
                        }
                        break;
                    default: {
                        // 告知前端游戏出错，返回地图界面
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "error")
                                .element("message", "battleStage not match");
                        sendMsg(session, jsonObject.toString());
                        break;
                    }
                }
            }
            else {
                // 不相符
                System.out.println("battleId not match");
            }
        }
        else {
            String operation = params.getString("operation");
            switch (operation) {
                case "buySoldiers":
                    buySoldiers(params, battleId, address, session);
                    break;
                case "departure":
                    departure(gameId, battleId, address, session);
                    break;
                case "pickSoldier":
                    pickSoldier(params, gameId, battleId, address);
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

    private boolean checkPlayerInfo(String gameId, String battleId, String address, Session session) throws Exception {

        String[] information = battleId.split("&&");
        String attackerAddress = information[0];
        String defenderAddress = information[1];
        int cityId = Integer.valueOf(information[2]);
        // 对链上数据进行查询
        // 获取用户信息
        String playerInfo = filoopService.getPlayersStatus(address);
        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            // 用户存在
            int gameIdOnChain = Utils.returnInt(playerInfo, 0);
            if (gameIdOnChain == Integer.valueOf(gameId)) {
                // 链上gameId和后端gameId相匹配
                // 查询指定gameId的游戏状态
                String globalInfo = filoopService.getGlobalTb(gameIdOnChain);
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

    private String checkBattleStage(String battleId, String address, String opponent) {

        if (!playerSoldiers.get(battleId).get(address).getBoolean("ready")) {
            return "beforeBattle";
        }
        else {
            if (!playerSoldiers.get(battleId).get(opponent).getBoolean("ready")) {
                return "departureWaiting";
            }
            else {
                return "inBattle";
            }
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
        if (!playerSoldiers.containsKey(battleId)) {
            Map<String, JSONObject> map = new ConcurrentHashMap<>();
            map.put(attackerAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
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
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false)
                    .element("round", 1)
                    .element("soldier", 0)
                    .element("pick", false)
                    .element("result", "none"));
            playerSoldiers.put(battleId, map);
        }
    }

    private void initGameStarted(String battleId) {
        if (!gameStarted.containsKey(battleId)) {
            gameStarted.put(battleId, false);
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
            }
        }, 0, 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Buy soldiers time is out");
        // 若此时双方仍未进行departure，则自动departure
        if (!playerSoldiers.get(battleId).get(attackerAddress).getBoolean("ready")) {
            departure(gameId, battleId, attackerAddress, playerSession.get(battleId).get(attackerAddress));
        }
        if (!playerSoldiers.get(battleId).get(defenderAddress).getBoolean("ready")) {
            departure(gameId, battleId, defenderAddress, playerSession.get(battleId).get(defenderAddress));
        }
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

    private void initShop(String battleId, String address) throws Exception {
        Map<String, Session> map = playerSession.get(battleId);
        JSONArray jsonArray = new JSONArray();
        List<String> soldiersName = SiegeParams.getSoldiersName();
        List<Integer> soldiersPoint = SiegeParams.getSoldiersPoint();
        List<String> soldiersDescription = SiegeParams.getSoldiersDescription();
        List<Integer> mySoldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
        double myPoint = playerSoldiers.get(battleId).get(address).getDouble("price");
        int myQuantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
        JSONObject jsonObject = new JSONObject()
                .element("stage", "initShop")
                .element("mySoldiers", mySoldiers)
                .element("myPoint", myPoint)
                .element("myQuantity", myQuantity);
        for (int i = 1; i <= SiegeParams.getSoldierNum(); ++i) {
            JSONObject shop = new JSONObject()
                    .element("type", soldiersName.get(i))
                    .element("description", soldiersDescription.get(i))
                    .element("price", soldiersPoint.get(i));
            jsonArray.add(shop);
        }
        jsonObject.element("shop", jsonArray);
        sendAll(map, jsonObject.toString());
    }

    private void initBattleField(String battleId, String address, String opponent, Session session) throws Exception {
        List<Integer> mySoldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
        double myPoint = playerSoldiers.get(battleId).get(address).getDouble("price");
        int myQuantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
        double opponentPoint = playerSoldiers.get(battleId).get(opponent).getDouble("price");
        int opponentQuantity = playerSoldiers.get(battleId).get(opponent).getInt("quantity");
        JSONObject jsonObject = new JSONObject()
                .element("stage", "initBattleField")
                .element("opponent", opponent)
                .element("mySoldiers", mySoldiers)
                .element("myPoint", myPoint)
                .element("myQuantity", myQuantity)
                .element("opponentPoint", opponentPoint)
                .element("opponentQuantity", opponentQuantity);
        sendMsg(session, jsonObject.toString());
    }

    private void updateBattleField() throws Exception {

    }

    private void buySoldiers(JSONObject params, String battleId, String address, Session session) throws Exception {
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
            double price = params.getDouble("price");
            int quantity = params.getInt("quantity");
            System.out.println(type);
            System.out.println(price);
            System.out.println(quantity);
            String symbol = "SIG";
            String signature = params.getString("signature");

            //TODO 验证买兵、删除price,quantity

            // 首先进行缴费
            try {
                String transferResult = filoopService.transfer(address, deployAccount.getAddress(), new Double(price * SiegeParams.getPrecision()).longValue(), symbol, "buy soldiers", signature);
//            String transferResult = "transfer success";
                if (transferResult.equals("transfer success")) {
                    // 更新playerSoldier
                    if (type != null) {
                        double oldPrice = playerSoldiers.get(battleId).get(address).getDouble("price");
                        double newPrice = oldPrice + price;
                        if (newPrice <= 100) {
                            List<Integer> oldSoldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
                            assert oldSoldiers != null;
                            oldSoldiers.addAll(type);
                            int oldQuantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
                            int newQuantity = oldQuantity + quantity;
                            updatePlayerSoldiers(battleId, address, oldSoldiers, newPrice, newQuantity,true, false, 1, 0, false, "none");
                            // 告知购买士兵成功
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "response")
                                    .element("operation", "buySoldiers")
                                    .element("status", true);
                            sendMsg(session, jsonObject.toString());
                        }
                        else {
                            // 告知士兵已满额
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "response")
                                    .element("operation", "buySoldiers")
                                    .element("status", false);
                            sendMsg(session, jsonObject.toString());
                        }
                    }
                    else {
                        // 告知操作失败
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "response")
                                .element("operation", "buySoldiers")
                                .element("status", false);
                        sendMsg(session, jsonObject.toString());
                    }
                }
                else {
                    // 告知转账失败
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "response")
                            .element("operation", "buySoldiers")
                            .element("status", false);
                    sendMsg(session, jsonObject.toString());
                }
            } catch (Exception e) {
                System.out.println("Got an exception: " + e.getMessage());
            }
        }
    }

    private void departure(String gameId, String battleId, String address, Session session) throws Exception {
        List<Integer> type = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
        long price = new Double(playerSoldiers.get(battleId).get(address).getDouble("price") * SiegeParams.getPrecision()).longValue();
        int quantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
        String defenderAddress = battleData.get(battleId).getString("defenderAddress");
        // 当前round
        int cRound = battleData.get(battleId).getInt("round");
        if (type.size() == 0) {
            // 购买的士兵为空，宣布战斗未打响
            //todo 添加提示
            System.out.println("not a battle");
        }
        else {
            // 考虑将buySoldiers放在购买士兵部分
            // 后续改进代码，将两个操作用if else进行嵌套
            String buyResult = filoopService.buySoldiers(Integer.valueOf(gameId), address, price, type, price, quantity);
            String departureResult = filoopService.departure(Integer.valueOf(gameId), address);
//            String buyResult = "success";
//            String departureResult = "success";
            if (buyResult.equals("success") && (departureResult.equals("success"))) {
//            if (buyResult.equals("success")) {
                String opponent = address.equals(attackerAddress) ? defenderAddress: attackerAddress;
                // 更新状态
                playerSoldiers.get(battleId).get(address).element("ready", true);
                // 检查对方状态
                boolean allReady = playerSoldiers.get(battleId).get(opponent).getBoolean("ready");
                if (allReady) {
                    // 告知玩家结果以及对面的信息
                    // 考虑玩家自己牌也由服务器发送？
                    //todo stage添加,反馈信息修改
                    // 告知操作成功
                    JSONObject response = new JSONObject()
                            .element("stage", "response")
                            .element("operation", "departure")
                            .element("status", true)
                            .element("ready", true);
                    sendMsg(session, response.toString());

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
                            roundCountDown(roundTimer, gameId, battleId, cRound, attackerAddress, defenderAddress);
                        } catch (Exception e) {
                            System.out.println("Got an exception: " + e.getMessage());
                        }
                    }).start();
                }
                else {
                    JSONObject response = new JSONObject()
                            .element("operation", "departure")
                            .element("status", true)
                            .element("ready", false);
                    sendMsg(session, response.toString());
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
        }
    }

    private void pickSoldier(JSONObject params, String gameId, String battleId, String address) throws Exception {
        int soldier = params.getInt("soldier");
        // 可以考虑后端加上士兵是否在仓库中的验证
        String attackerAddress = battleData.get(battleId).getString("attackerAddress");
        String defenderAddress = battleData.get(battleId).getString("defenderAddress");
        Session attackerSession = playerSession.get(battleId).get(attackerAddress);
        Session defenderSession = playerSession.get(battleId).get(defenderAddress);
        // 确定对手
        String opponent = address.equals(attackerAddress) ? defenderAddress: attackerAddress;
//        double point = params.getDouble("point");
        playerSoldiers.get(battleId).get(address).element("soldier", soldier);
        playerSoldiers.get(battleId).get(address).element("pick", true);
        // 首先通知对方己方已出牌
        JSONObject pick = new JSONObject()
                .element("stage", "notification")
                .element("operation", "pickSoldier")
                .element("status", true)
                .element("situation", "opponentPick");
        sendMsg(playerSession.get(battleId).get(opponent), pick.toString());

        // 检查对方是否出牌
        boolean allPick = playerSoldiers.get(battleId).get(opponent).getBoolean("pick");
        if (allPick) {
            // 链上执行判断
            int aType = playerSoldiers.get(battleId).get(attackerAddress).getInt("soldier");
            int dType = playerSoldiers.get(battleId).get(defenderAddress).getInt("soldier");
            String result = filoopService.pickAndBattle(Integer.valueOf(gameId), attackerAddress, defenderAddress, aType, dType);
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
            // 本轮结束，重置标志位，并且检查游戏是否结束
            new Thread(()-> {
                playerSoldiers.get(battleId).get(attackerAddress).element("pick", false);
                playerSoldiers.get(battleId).get(defenderAddress).element("pick", false);

                List<Integer> attackerSoldiers = castList(playerSoldiers.get(battleId).get(attackerAddress).get("type"), Integer.class);
                List<Integer> defenderSoldiers = castList(playerSoldiers.get(battleId).get(defenderAddress).get("type"), Integer.class);
//                    boolean isOver = false;
                if (attackerSoldiers.size() == 0 || defenderSoldiers.size() == 0 || battleData.get(battleId).getInt("round") >= 5) {
                    // 游戏结束，设置isOver为true
//                        isOver = true;
                    System.out.println("游戏结束");
                    battleData.get(battleId).element("isOver", true);
                    String winner;
                    String loser;
                    // 链上判定谁获胜
                    try {
                        int cityId = battleData.get(battleId).getInt("cityId");
                        String battleResult = filoopService.battleEnd(Integer.valueOf(gameId), attackerAddress, defenderAddress, cityId);
//                            String battleResult = "attacker wins the battle";
                        if (!battleResult.equals("contract calling error") && !battleResult.equals("unknown error")) {
//                            if (!battleResult.equals("")) {
                            switch (Utils.getValue(battleResult)) {
                                case "attacker wins the battle": {
                                    winner = attackerAddress;
                                    loser = defenderAddress;
                                    // 转账给winner
                                    new Thread(() -> {
                                        // 查询链上数据
                                        try {
                                            battleSettlement(winner, loser, false);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                    break;
                                }
                                case "defender wins the battle": {
                                    winner = defenderAddress;
                                    loser = attackerAddress;
                                    new Thread(() -> {
                                        // 查询链上数据
                                        try {
                                            battleSettlement(winner, loser, false);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                    break;
                                }
                                case "tie": {
                                    winner = "nobody";
                                    loser = "nobody";
                                    new Thread(() -> {
                                        // 查询链上数据
                                        try {
                                            battleSettlement(winner, loser, true);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                    break;
                                }
                                default: {
                                    winner = "";
                                    loser = "";
                                    break;
                                }
                            }
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "battle")
                                    .element("positive", true)
                                    .element("round", battleData.get(battleId).getInt("round"))
                                    .element("timer", 0)
                                    .element("isOver", true)
                                    .element("winner", winner)
                                    .element("loser", loser);
                            sendMsg(attackerSession, jsonObject.toString());
                            sendMsg(defenderSession, jsonObject.toString());
                        }
                        else {
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "error")
                                    .element("message", "battle end error");
                            sendMsg(attackerSession, jsonObject.toString());
                            sendMsg(defenderSession, jsonObject.toString());
                        }
                        // 清除数据
                        playerSoldiers.remove(battleId);
                        playerSession.remove(battleId);
                        battleData.remove(battleId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }
                else {
                    // 游戏未结束，开启下一轮
                    try {
                        int round = battleData.get(battleId).getInt("round");
                        round += 1;
                        battleData.get(battleId).element("round", round);
                        // 延迟3秒
                        TimeUnit.SECONDS.sleep(3);
                        // 游戏开始倒计时
                        roundCountDown(roundTimer, gameId, battleId, round, attackerAddress, defenderAddress);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }
            }).start();
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

    private void battleSettlement(String winner, String loser, boolean tie) throws Exception{
        String winnerInfo = filoopService.getPlayersStatus(winner);
        String loserInfo = filoopService.getPlayersStatus(loser);
        int winnerPointer = getPointer(winnerInfo, 5);
        int loserPointer = getPointer(loserInfo, 5);
        winnerPointer = decreasePointer(winnerPointer);
        loserPointer = decreasePointer(loserPointer);
        String winnerGameData = filoopService.getGameData(winner, winnerPointer);
        String loserGameData = filoopService.getGameData(loser, loserPointer);
        long winnerPrice = getPrice(winnerGameData, 1);
        long loserPrice = getPrice(loserGameData, 1);
        String symbol = "SIG";
        if (!tie) {
            // 获胜者拿回80%的费用
            String back = filoopService.transfer(Config.getDeployAccount().getAddress(), winner, new Double(winnerPrice * 0.8).longValue(), symbol, "back", Config.getDeployAccountJson());
            // 获胜者获得失败者70%的费用
            String award = filoopService.transfer(Config.getDeployAccount().getAddress(), winner, new Double(loserPrice * 0.7).longValue(), symbol, "award", Config.getDeployAccountJson());
            assert (back.equals("transfer success") && award.equals("transfer success"));
        }
        else {
            String returnBack1 = filoopService.transfer(Config.getDeployAccount().getAddress(), winner, new Double(winnerPrice).longValue(), symbol, "tie", Config.getDeployAccountJson());
            String returnBack2=  filoopService.transfer(Config.getDeployAccount().getAddress(), loser, new Double(loserPrice).longValue(), symbol, "tie", Config.getDeployAccountJson());
            assert (returnBack1.equals("transfer success") && returnBack2.equals("transfer success"));
        }
    }

    private String judge(int type1, int type2) {
        switch (type1) {
            case 1:
                if (type2 == 3) {
                    return "attacker wins this round";
                } else if (type2 == 4 || type2 == 5) {
                    return "defender wins this round";
                } else {
                    return "tie";
                }
            case 2:
                if (type2 == 5) {
                    return "attacker wins this round";
                } else if (type2 == 3 || type2 == 4) {
                    return "defender wins this round";
                } else {
                    return "tie";
                }
            case 3:
                if (type2 == 2 || type2 == 4) {
                    return "attacker wins this round";
                } else if (type2 == 1 || type2 == 5) {
                    return "defender wins this round";
                } else {
                    return "tie";
                }
            case 4:
                if (type2 == 1 || type2 == 2) {
                    return "attacker wins this round";
                } else if (type2 == 3) {
                    return "defender wins this round";
                } else {
                    return "tie";
                }
            case 5:
                if (type2 == 1 || type2 == 3) {
                    return "attacker wins this round";
                } else if (type2 == 2) {
                    return "defender wins this round";
                } else {
                    return "tie";
                }
            default:
                return "tie";
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
