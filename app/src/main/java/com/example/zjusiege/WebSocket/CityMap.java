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
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/cityMap/{gameId}")
@Component
public class CityMap {
    // TODO
//    private HyperchainService hyperchainService = new HyperchainService();
    private FiloopService filoopService = new FiloopService();
    private Account deployAccount = Config.getDeployAccount();

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    private static int runningTimer = SiegeParams.getGameDuration();
    private static int interval = 10;
    private static int intervalNum = runningTimer / interval;
    private static int attackTimer = 60;

    // 一个用来保存玩家session的容器
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> gameStarted = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Boolean>> bothResponse = new ConcurrentHashMap<>();

    private int debug = 1;

    @OnOpen
    public void connect(@PathParam("gameId") String gameId, Session session) {
        // 增加在线人数
        playerNum += 1;
        System.out.println("CityMap connect success!");
        System.out.println("playerNum: " + playerNum);
        initGameStarted(gameId);
        initBothResponse(gameId);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, Session session) {
        // 减少在线人数
        playerNum -= 1;
        System.out.println("CityMap disConnect!");
        System.out.println("playerNum: " + playerNum);
        for (String address: playerSession.get(gameId).keySet()) {
            if (playerSession.get(gameId).get(address) == session) {
                System.out.println("debug@cpf: " + address + "disConnect");
            }
        }
    }

    @OnMessage
    public void onMessage(@PathParam("gameId") String gameId, String msg, Session session) throws Exception {
        JSONObject params = JSONObject.fromObject(msg);
        boolean first = params.getBoolean("first");
        String address = params.getString("address");

        if (first) {
            // 检查链上信息和后端信息是否相符
            boolean matched = checkPlayerInfo(gameId, address, session);
            if (matched) {
                // 相符
                // 对玩家进行注册
                boolean registered = register(gameId, address, session);
                // 如果是第一次注册
                if (registered) {
                    // 游戏开启条件
                    if (playerSession.get(gameId).size() == 1 && !gameStarted.get(gameId)) {
                        gameStarted.replace(gameId, true);
                        // 一旦有人进入游戏，游戏正式开始
                        // 需要解决两个线程同步的问题
                        new Thread(()-> {
                            try {
                                // 简单延时1秒
                                TimeUnit.SECONDS.sleep(1);
                                // 游戏开始倒计时
                                countDown(runningTimer, gameId);
                            } catch (InterruptedException e) {
                                printException(e);
                            }
                        }).start();

                        // 开启一个同步线程用于更新cityMap的数据
                        new Thread(()-> {
                            try {
                                TimeUnit.SECONDS.sleep(2);
                                updateCityMap(runningTimer, gameId);
                            } catch (InterruptedException e) {
                                printException(e);
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
                // 查询城池数据，发送给玩家
                try {
                    initMap(gameId, session);
                } catch (Exception e) {
                    printException(e);
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "error")
                            .element("message", "initMap error");
                    sendMsg(session, jsonObject.toString());
                }
            }
            else {
                // 不相符，不用发送消息
                System.out.println("gameId not match");
            }
        }
        else {
            // 不是第一次连接
            switch (params.getString("operation")) {
                case "occupy": {
                    // 玩家占领空城
                    int cityId = params.getInt("cityId");
                    long price = new Double(params.getDouble("price") * SiegeParams.getPrecision()).longValue();
                    String symbol = "SIG";
                    String signature = params.getString("signature");
                    occupy(gameId, session, address, cityId, price, symbol, signature);
                    break;
                }
                case "leave": {
                    // 玩家离开城池
//                    int cityId = params.getInt("cityId");
//                    long bonus = new Double(params.getDouble("bonus") * SiegeParams.getPrecision()).longValue();
                    String symbol = "SIG";
                    leave(gameId, session, address, symbol);
                    break;
                }
                case "attack": {
                    // 玩家进攻
                    String target = params.getString("target");
//                int cityId = params.getInt("cityId");
//                    try {
//                        HyperchainService hyperchainService = new HyperchainService();
//                        String result = hyperchainService.attack(Integer.valueOf(gameId), address, target);
////                        String result = "success";
//                        if (result.equals("success")) {
//                            // 告知攻击者
//                            JSONObject response = new JSONObject()
//                                    .element("stage", "response")
//                                    .element("operation", "attack")
//                                    .element("status", true);
//                            sendMsg(session, response.toString());
//                            // 告知被攻击者
//                            Session targetSession = playerSession.get(gameId).get(target);
//                            JSONObject jsonObject = new JSONObject()
//                                    .element("stage", "notification")
//                                    .element("situation", "beAttackedRequest")
//                                    .element("opponent", address);
//                            sendMsg(targetSession, jsonObject.toString());
//                        }
//                        else {
//                            // 操作失败
//                            // 告知攻击者操作失败
//                            JSONObject response = new JSONObject()
//                                    .element("stage", "response")
//                                    .element("operation", "attack")
//                                    .element("status", false);
//                            sendMsg(session, response.toString());
//                        }
//                    } catch (Exception e) {
//                        System.out.println("Got an exception: " + e.getMessage());
//                    }
                    attack(gameId, session, address, target);
                    break;
                }
                case "defense": {
                    // 玩家防守
                    // 前端实现，若选择超时，则自动发送放弃防御选择
//                    int cityId = params.getInt("cityId");
                    String target = params.getString("target");
                    int choice = params.getInt("choice");
//                    long bonus = new Double(params.getDouble("bonus") * SiegeParams.getPrecision()).longValue();
                    String symbol = "SIG";
//                    try {
//                        HyperchainService hyperchainService = new HyperchainService();
//                        if (choice == 0) {
//                            // 玩家离城
//                            String transferResult = hyperchainService.transfer(Config.getDeployAccount().getAddress(), address, bonus, symbol, "settle bonus", Config.getDeployAccountJson());
////                            String transferResult = "transfer success";
//                            if (transferResult.equals("transfer success")) {
//                                String result = hyperchainService.defense(Integer.valueOf(gameId), address, target, cityId, choice);
////                                String result = "success";
//                                if (result.equals("success")) {
//                                    // 告知进攻者获得城池
//                                    JSONObject notification = new JSONObject()
//                                            .element("stage", "notification")
//                                            .element("situation", "getCity");
//                                    Session targetSession = playerSession.get(gameId).get(target);
//                                    sendMsg(targetSession, notification.toString());
//                                    // 告知玩家操作成功
//                                    JSONObject response = new JSONObject()
//                                            .element("stage", "response")
//                                            .element("operation", "defense")
//                                            .element("status", true);
//                                    sendMsg(session, response.toString());
//                                    // 广播城池更新信息
//                                    JSONObject jsonObject = new JSONObject()
//                                            .element("stage", "updateCityMap");
//                                    JSONArray jsonArray = new JSONArray();
//                                    for (int i = 1; i <= SiegeParams.getCityNum(); ++i) {
//                                        String result1 = hyperchainService.getCitiesTable(Integer.valueOf(gameId), i);
//                                        String cityName = returnString(result1, 0);
//                                        double defenseIndex = returnDouble(result1, 1);
//                                        double realtimePrice = returnDouble(result1, 2);
//                                        boolean ifBeOccupied = returnBool(result1, 3);
//                                        String belongPlayer = returnString(result1, 4);
//                                        double producedBonus = returnDouble(result1, 5);
//                                        JSONObject city = new JSONObject()
//                                                .element("cityId", i)
//                                                .element("cityName", cityName)
//                                                .element("defenseIndex", defenseIndex)
//                                                .element("realtimePrice", realtimePrice)
//                                                .element("ifBeOccupied", ifBeOccupied)
//                                                .element("belongPlayer", belongPlayer)
//                                                .element("producedBonus", producedBonus);
//                                        jsonArray.add(city);
//                                    }
//                                    jsonObject.element("cityMap", jsonArray);
//
////                                    JSONObject jsonObject1 = new JSONObject()
////                                            .element("stage", "updateCityMap")
////                                            .element("cityId", cityId)
////                                            .element("ifBeOccupied", true)
////                                            .element("belongPlayer", target)
////                                            .element("producedBonus", 0.);
//                                    sendAll(playerSession.get(gameId), jsonObject.toString());
//                                }
//                                else {
//                                    // 操作失败
//                                    // 告知双方系统错误
//                                    JSONObject jsonObject = new JSONObject()
//                                            .element("stage", "notification")
//                                            .element("situation", "systemError");
//                                    Session targetSession = playerSession.get(gameId).get(target);
//                                    sendMsg(session, jsonObject.toString());
//                                    sendMsg(targetSession, jsonObject.toString());
//                                }
//                            }
//                            else {
//                                // 操作失败
//                                // 告知双方系统错误
//                                JSONObject jsonObject = new JSONObject()
//                                        .element("stage", "notification")
//                                        .element("situation", "systemError");
//                                Session targetSession = playerSession.get(gameId).get(target);
//                                sendMsg(session, jsonObject.toString());
//                                sendMsg(targetSession, jsonObject.toString());
//                            }
//                        }
//                        else {
//                            String result = hyperchainService.defense(Integer.valueOf(gameId), address, target, cityId, choice);
////                            String result = "success";
//                            if (result.equals("success")) {
//                                // 玩家选择防守
//                                // 告知进攻者
//                                JSONObject jsonObject = new JSONObject()
//                                        .element("stage", "notification")
//                                        .element("situation", "beforeBattle");
//                                Session targetSession = playerSession.get(gameId).get(target);
//                                sendMsg(targetSession, jsonObject.toString());
//                                // 告知防守者操作成功
//                                JSONObject response = new JSONObject()
//                                        .element("stage", "response")
//                                        .element("operation", "defense")
//                                        .element("status", true);
//                                sendMsg(session, response.toString());
//                            }
//                            else {
//                                // 操作失败
//                                // 告知双方系统错误
//                                JSONObject jsonObject = new JSONObject()
//                                        .element("stage", "notification")
//                                        .element("situation", "systemError");
//                                Session targetSession = playerSession.get(gameId).get(target);
//                                sendMsg(session, jsonObject.toString());
//                                sendMsg(targetSession, jsonObject.toString());
//                            }
//                        }
//                    } catch (Exception e) {
//                        System.out.println("Got an exception: " + e.getMessage());
//                    }
                    defense(gameId, session, address, target, choice, symbol);
                    break;
                }
                case "settle": {
                    List<String> cityOwners = castList(params.get("cityOwners"), String.class);
                    try {
                        // TODO
                        String updateStage = filoopService.updateGameStage(Integer.valueOf(gameId), SiegeParams.gameStage.SETTLING.ordinal());
                        assert (updateStage.equals("success"));
                        // TODO
                        String result = filoopService.settlement(Integer.valueOf(gameId), cityOwners);
                        if (result.equals("success")) {
                            // 成功
                            JSONObject jsonObject = new JSONObject()
                                    .element("operation", "settle")
                                    .element("status", true);
                            sendAll(playerSession.get(gameId), jsonObject.toString());
                            // TODO 向玩家发放奖金
                        }
                        else {
                            JSONObject jsonObject = new JSONObject()
                                    .element("operation", "settle")
                                    .element("status", false);
                            sendAll(playerSession.get(gameId), jsonObject.toString());
                        }
                        TimeUnit.SECONDS.sleep(3);
                        // 执行endGame操作
                        List<String> allPlayers = new ArrayList<>();
                        allPlayers.addAll(playerSession.get(gameId).keySet());
                        // TODO
                        String updateStage1 = filoopService.updateGameStage(Integer.valueOf(gameId), SiegeParams.gameStage.ENDING.ordinal());
                        assert (updateStage1.equals("success"));
                        // TODO
                        String result1 = filoopService.endGame(Integer.valueOf(gameId), allPlayers);
                        if (result1.equals("success")) {
                            // 成功
                            System.out.println("endGame success");
                        }
                        else {
                            System.out.println("endGame failed");
                        }
                        // 删除本次游戏数据
                        playerSession.remove(gameId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    @OnError
    public void connectError(Throwable error) {
        System.out.println("connect error");
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

    private boolean checkPlayerInfo(String gameId, String address, Session session) throws Exception {
        // 对链上数据进行查询
        // 获取用户信息
        // TODO
        String playerInfo = filoopService.getPlayersStatus(address);
        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            // 用户存在
            int gameIdOnChain = Utils.returnInt(playerInfo, 0);
            if (gameIdOnChain == Integer.valueOf(gameId)) {
                // 链上gameId和后端gameId相匹配
                // 查询指定gameId的游戏状态
                // TODO
                String globalInfo = filoopService.getGlobalTb(gameIdOnChain);
                if (!globalInfo.equals("contract calling error") && !globalInfo.equals("unknown error")) {
                    // 获取游戏阶段
                    String[] gameStageList = new String[]{"start", "bidding", "running", "settling", "ending"};
                    int gameStageInt = Utils.returnInt(globalInfo, 1);
                    String gameStage = gameStageList[gameStageInt];
                    if (gameStage.equals("running")) {
                        // gameId和gameStage均正确
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "checkPlayerStatus")
                                .element("status", true)
                                .element("gameStage", gameStage)
                                .element("gameId", gameId);
                        sendMsg(session, jsonObject.toString());
                        // 查询玩家状态
                        String playerStage = Utils.returnString(playerInfo, 4);
                        if (playerStage.equals("beAttackedRequest")) {
                            // 正在被攻击
                            String opponent = Utils.returnString(playerInfo, 2).toUpperCase();
                            JSONObject notification = new JSONObject()
                                    .element("stage", "notification")
                                    .element("message", "beAttacked")
                                    .element("opponent", opponent);
                            sendMsg(session, notification.toString());
                        }
                        return true;
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

    private boolean register(String gameId, String address, Session session) {
        if (!playerSession.containsKey(gameId)) {
            // 构建游戏
            Map<String, Session> map = new ConcurrentHashMap<>();
            map.put(address, session);
            playerSession.put(gameId, map);
            // 返回第一次注册为真
//            if (debug == 1) {
//                System.out.println("debug@cpf: " + playerSession);
//            }
            return true;
        }
        else {
            if (!playerSession.get(gameId).containsKey(address)) {
                // 第一次连接，进行注册，返回true
                Map<String, Session> map = playerSession.get(gameId);
                map.put(address, session);
                playerSession.put(gameId, map);
//                if (debug == 1) {
//                    System.out.println("debug@cpf: " + playerSession);
//                }
                return true;
            }
            else {
                // 已经注册过，返回第一次注册为假
                playerSession.get(gameId).replace(address, session);
//                if (debug == 1) {
//                    System.out.println("debug@cpf: " + playerSession);
//                }
                return false;
            }
        }
    }

    private void initGameStarted(String gameId) {
        if (!gameStarted.containsKey(gameId)) {
            gameStarted.put(gameId, false);
        }
    }

    private void initBothResponse(String gameId) {
        if (!bothResponse.containsKey(gameId)) {
            Map<String, Boolean> map = new ConcurrentHashMap<>();
            bothResponse.put(gameId, map);
        }
    }

    private void initMap(String gameId, Session session) throws Exception {
        JSONObject jsonObject = new JSONObject()
                .element("stage", "initMap");
        JSONArray jsonArray = new JSONArray();
        for (int i = 1; i <= SiegeParams.getCityNum(); ++i) {
            // TODO
            String result = filoopService.getCitiesTb(Integer.valueOf(gameId), i);
            String cityName = Utils.returnString(result, 0);
            double defenseIndex = Utils.returnDouble(result, 1);
            double realtimePrice = Utils.returnDouble(result, 2);
            boolean ifBeOccupied = Utils.returnBool(result, 3);
            String belongPlayer = Utils.returnString(result, 4);
            double producedBonus = Utils.returnDouble(result, 5);
//                    System.out.println(result);
            JSONObject city = new JSONObject()
                    .element("cityId", i)
                    .element("cityName", cityName)
                    .element("defenseIndex", defenseIndex)
                    .element("realtimePrice", realtimePrice)
                    .element("ifBeOccupied", ifBeOccupied)
                    .element("belongPlayer", belongPlayer)
                    .element("producedBonus", producedBonus);
            jsonArray.add(city);
        }
        jsonObject.element("cityMap", jsonArray);
        sendMsg(session, jsonObject.toString());
    }

    private void updateMap(String gameId) throws Exception {
        JSONObject jsonObject = new JSONObject()
                .element("stage", "updateCityMap");
        JSONArray jsonArray = new JSONArray();
        for (int i = 1; i <= SiegeParams.getCityNum(); ++i) {
            // TODO
            String result1 = filoopService.getCitiesTb(Integer.valueOf(gameId), i);
            String cityName = Utils.returnString(result1, 0);
            double defenseIndex = Utils.returnDouble(result1, 1);
            double realtimePrice = Utils.returnDouble(result1, 2);
            boolean ifBeOccupied = Utils.returnBool(result1, 3);
            String belongPlayer = Utils.returnString(result1, 4);
            double producedBonus = Utils.returnDouble(result1, 5);
            JSONObject city = new JSONObject()
                    .element("cityId", i)
                    .element("cityName", cityName)
                    .element("defenseIndex", defenseIndex)
                    .element("realtimePrice", realtimePrice)
                    .element("ifBeOccupied", ifBeOccupied)
                    .element("belongPlayer", belongPlayer)
                    .element("producedBonus", producedBonus);
            jsonArray.add(city);
        }
        jsonObject.element("cityMap", jsonArray);
        sendAll(playerSession.get(gameId), jsonObject.toString());
    }

    private void occupy(String gameId, Session session, String address, int cityId, long price, String symbol, String signature) throws Exception {
        // 先进行转账
        // TODO
//        String transferResult = filoopService.transfer(address, Config.getDeployAccount().getAddress(), price, symbol, "player occupies city", signature);
        String transferResult = "transfer success";
        if (transferResult.equals("transfer success")) {
            // TODO
            String result = filoopService.occupyCity(Integer.valueOf(gameId), address, cityId, price, signature);
//                            String result = "success";
            if (result.equals("success")) {
                // 告知玩家占领成功
                JSONObject response = new JSONObject()
                        .element("stage", "response")
                        .element("operation", "occupy")
                        .element("status", true);
                sendMsg(session, response.toString());
                // 广播城池更新消息(耗时较大)
                new Thread(()-> {
                    try {
                        updateMap(gameId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }).start();
            } else {
                // 告知玩家占领失败
                JSONObject response = new JSONObject()
                        .element("stage", "response")
                        .element("operation", "occupy")
                        .element("status", false);
                sendMsg(session, response.toString());
                // 给用户退款
                // TODO
//                String refundResult = filoopService.transfer(Config.getDeployAccount().getAddress(), address, price, symbol, "occupy refund", Config.getDeployAccountJson());
                String refundResult = "transfer success";
                if (!refundResult.equals("transfer success")) {
                    // 告知玩家退款失败
                    JSONObject refund = new JSONObject()
                            .element("stage", "response")
                            .element("operation", "occupy")
                            .element("status", false)
                            .element("message", "refund failed");
                    sendMsg(session, refund.toString());
                }
            }
        }
        else {
            // 告知玩家缴费失败
            JSONObject response = new JSONObject()
                    .element("stage", "response")
                    .element("operation", "transfer")
                    .element("status", false);
            sendMsg(session, response.toString());
        }
    }

    private void leave(String gameId, Session session, String address, String symbol) throws Exception {
        // 首先进行转账
        // 查询该城池的bonus
        // TODO
        String playerInfo = filoopService.getPlayersStatus(address);
        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            int cityId = Utils.returnInt(playerInfo, 3);
            assert (cityId <= 5 && cityId >= 1);
            // TODO
            String cityInfo = filoopService.getCitiesTb(Integer.valueOf(gameId), cityId);
            if (!cityInfo.equals("contract calling error") && !cityInfo.equals("unknown error")) {
                long bonus = Utils.returnLong(cityInfo, 5);
                assert (bonus != 0);
                // TODO
//                String transferResult = filoopService.transfer(Config.getDeployAccount().getAddress(), address, bonus, symbol, "settle bonus", Config.getDeployAccountJson());
                String transferResult = "transfer success";
                if (transferResult.equals("transfer success")) {
                    // TODO
                    String result = filoopService.leaveCity(Integer.valueOf(gameId), address);
//                            String result = "success";
                    if (result.equals("success")) {
                        // 离开成功
                        JSONObject response = new JSONObject()
                                .element("stage", "response")
                                .element("operation", "leave")
                                .element("status", true);
                        sendMsg(session, response.toString());
                        // 广播城池更新信息(耗时较大)
                        new Thread(()-> {
                            try {
                                updateMap(gameId);
                            } catch (Exception e) {
                                printException(e);
                            }
                        }).start();
                    }
                    else {
                        // 离开失败
                        JSONObject response = new JSONObject()
                                .element("stage", "response")
                                .element("operation", "leave")
                                .element("status", false);
                        sendMsg(session, response.toString());
                    }
                }
                else {
                    // 转账失败
                    JSONObject response = new JSONObject()
                            .element("stage", "response")
                            .element("operation", "transfer")
                            .element("status", false);
                    sendMsg(session, response.toString());
                }
            }
            else {
                // 离开失败
                JSONObject response = new JSONObject()
                        .element("stage", "response")
                        .element("operation", "leave")
                        .element("status", false);
                sendMsg(session, response.toString());
            }
        }
        else {
            // 离开失败
            JSONObject response = new JSONObject()
                    .element("stage", "response")
                    .element("operation", "leave")
                    .element("status", false);
            sendMsg(session, response.toString());
        }
    }

    private void attack(String gameId, Session session, String address, String target) throws Exception {
        target = target.toUpperCase();
        // TODO
        String result = filoopService.attack(Integer.valueOf(gameId), address, target);
        String concat = address + "&&" + target;
        if (debug == 1) {
            System.out.println("debug@cpf: " + target);
        }
        if (bothResponse.get(gameId).containsKey(concat)) {
            bothResponse.get(gameId).replace(concat, false);
        }
        else {
            bothResponse.get(gameId).put(concat, false);
        }
//                        String result = "success";
        if (result.equals("success")) {
            // 重复发送数据
            // 告知攻击者与被攻击者
            JSONObject response = new JSONObject()
                    .element("stage", "response")
                    .element("operation", "attack")
                    .element("status", true);
            sendMsg(session, response.toString());
            JSONObject notification = new JSONObject()
                    .element("stage", "notification")
                    .element("message", "beAttacked")
                    .element("opponent", address);
            if (debug == 1) {
                System.out.println("debug@cpf: " + playerSession.get(gameId));
            }
            sendMsg(playerSession.get(gameId).get(target), notification.toString());
            new Thread(()-> {
                try {
                    attackCountDown(attackTimer, gameId, concat);
                } catch (InterruptedException e) {
                    printException(e);
                }
            }).start();
//            JSONObject response = new JSONObject()
//                    .element("stage", "response")
//                    .element("operation", "attack")
//                    .element("status", true);
//            sendMsg(session, response.toString());
//            // 告知被攻击者
//            Session targetSession = playerSession.get(gameId).get(target);
//            JSONObject jsonObject = new JSONObject()
//                    .element("stage", "notification")
//                    .element("situation", "beAttackedRequest")
//                    .element("opponent", address);
//            sendMsg(targetSession, jsonObject.toString());
        }
        else {
            // 操作失败
            // 告知攻击者操作失败
            JSONObject response = new JSONObject()
                    .element("stage", "response")
                    .element("operation", "attack")
                    .element("status", false);
            sendMsg(session, response.toString());
        }
    }

    private void defense(String gameId, Session session, String address, String target, int choice, String symbol) throws Exception {
        String concat = target + "&&" + address;
        Session targetSession = playerSession.get(gameId).get(target);
        // TODO
        String playerInfo = filoopService.getPlayersStatus(address);

        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            int cityId = Utils.returnInt(playerInfo, 3);
            assert (cityId <= 5 && cityId >= 1);
            // TODO
            String cityInfo = filoopService.getCitiesTb(Integer.valueOf(gameId), cityId);
            if (!cityInfo.equals("contract calling error") && !cityInfo.equals("unknown error")) {
                if (choice == 0) {
                    // 玩家离城
                    long bonus = Utils.returnLong(cityInfo, 5);
                    assert (bonus != 0);
                    // TODO
//                    String transferResult = hyperchainService.transfer(Config.getDeployAccount().getAddress(), address, bonus, symbol, "settle bonus", Config.getDeployAccountJson());
                    String transferResult = "transfer success";
                    if (transferResult.equals("transfer success")) {
                        // TODO
                        String result = filoopService.defense(Integer.valueOf(gameId), address, target, cityId, choice);
                        if (result.equals("success")) {
                            // 告知进攻者获得城池
                            JSONObject notification = new JSONObject()
                                    .element("stage", "notification")
                                    .element("situation", "getCity");
                            sendMsg(targetSession, notification.toString());
                            // 告知玩家操作成功
                            JSONObject response = new JSONObject()
                                    .element("stage", "response")
                                    .element("operation", "defense")
                                    .element("choice", 0)
                                    .element("status", true);
                            sendMsg(session, response.toString());
                            // 广播城池更新信息(耗时较大)
                            new Thread(()-> {
                                try {
                                    updateMap(gameId);
                                } catch (Exception e) {
                                    printException(e);
                                }
                            }).start();
                            // 更新响应表
                            bothResponse.get(gameId).replace(concat, true);
                        }
                        else {
                            // 操作失败
                            // 告知双方系统错误
                            JSONObject response = new JSONObject()
                                    .element("stage", "response")
                                    .element("operation", "defense")
                                    .element("choice", 0)
                                    .element("status", false);
                            sendMsg(session, response.toString());

                            errorMsg(session, "defense error");
                            errorMsg(targetSession, "defense error");
                        }
                    }
                    else {
                        // 操作失败
                        // 告知双方系统错误
//                        JSONObject jsonObject = new JSONObject()
//                                .element("stage", "notification")
//                                .element("situation", "systemError");
//                        sendMsg(session, jsonObject.toString());
//                        sendMsg(targetSession, jsonObject.toString());
                        errorMsg(session, "leave error");
                        errorMsg(targetSession, "leave error");
                    }
                }
                else {
                    // 玩家选择防守
                    // TODO
                    String result = filoopService.defense(Integer.valueOf(gameId), address, target, cityId, choice);
//                    String result = "success";
                    if (result.equals("success")) {
                        // 玩家选择防守
                        // 告知防守者操作成功
                        JSONObject response = new JSONObject()
                                .element("stage", "response")
                                .element("operation", "defense")
                                .element("choice", 1)
                                .element("status", true);
                        sendMsg(session, response.toString());
                        // 告知进攻者
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "notification")
                                .element("message", "beforeBattle");
                        sendMsg(targetSession, jsonObject.toString());
                        // 告知防御者
                        JSONObject jsonObject1 = new JSONObject()
                                .element("stage", "notification")
                                .element("message", "beforeBattle");
                        sendMsg(session, jsonObject1.toString());
                        // 更新响应表
                        bothResponse.get(gameId).replace(concat, true);
                    } else {
                        // 操作失败
                        // 告知双方系统错误
                        JSONObject response = new JSONObject()
                                .element("stage", "response")
                                .element("operation", "defense")
                                .element("choice", 0)
                                .element("status", false);
                        sendMsg(session, response.toString());

                        errorMsg(session, "defense error");
                        errorMsg(targetSession, "defense error");
                    }
                }
            }
            else {
                // 操作失败
                // 告知双方系统错误
//                JSONObject jsonObject = new JSONObject()
//                        .element("stage", "notification")
//                        .element("situation", "systemError");
//                sendMsg(session, jsonObject.toString());
//                sendMsg(targetSession, jsonObject.toString());
                errorMsg(session, "cityId error");
                errorMsg(targetSession, "cityId error");
            }
        }
        else {
            // 操作失败
            // 告知双方系统错误
//            JSONObject jsonObject = new JSONObject()
//                    .element("stage", "notification")
//                    .element("situation", "systemError");
//            sendMsg(session, jsonObject.toString());
//            sendMsg(targetSession, jsonObject.toString());
            errorMsg(session, "defense error");
            errorMsg(targetSession, "defense error");
        }
    }

    private void settle() {

    }

    private void sendMsg(Session session, String msg) {
        try {
            session.getBasicRemote().sendText(msg);
        } catch (Exception e) {
            printException(e);
        }

    }

    private void sendAll(Map<String, Session> map, String msg) {
        for (String address: map.keySet()) {
            sendMsg(map.get(address), msg);
        }
    }

    private void countDown(int seconds, String gameId) throws InterruptedException {
        System.out.println("count down from " + seconds + " s ");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(gameId);
            @Override
            public void run() {
                System.out.println("Game time remains " + --curSec + " s");
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "countDown")
                        .element("timer", curSec);
                sendAll(map, jsonObject.toString());
            }
        }, 0, 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Game time is out");
    }

    private void updateCityMap(int seconds, String gameId) throws InterruptedException {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int num = intervalNum;
            Map<String, Session> map = playerSession.get(gameId);
            @Override
            public void run() {
                num -= 1;
                try {
                    // TODO
                    String result = filoopService.updateCityBonus(Integer.valueOf(gameId), (long) num);
                    double produceRate = getProduceRate(result);
                    double bonusPool = getBonusPool(result);
                    JSONArray cityBonus = getCityBonus(result);
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "updateBonus")
                            .element("produceRate", produceRate)
                            .element("bonusPool", bonusPool)
                            .element("cityBonus", cityBonus);
                    sendAll(map, jsonObject.toString());
                    System.out.println("num---: " + num);
                } catch (Exception e) {
                    printException(e);
                    for (String address: map.keySet()) {
                        try {
                            errorMsg(playerSession.get(gameId).get(address), "updateCityMap error");
                        } catch (Exception ex) {
                            printException(ex);
                        }
                    }
                }
            }
        }, interval * 1000, interval * 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Game time is out");
    }

    private void attackCountDown(int seconds, String gameId, String concat) throws InterruptedException {
        String attacker = concat.split("&&")[0];
        String defender = concat.split("&&")[1];
        System.out.println("Attack count down from " + seconds + " s");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
//            Map<String, Session> map = playerSession.get(gameId);
            @Override
            public void run() {
                if (!bothResponse.get(gameId).get(concat)) {
                    // 防御者还未进行响应
                    System.out.println("Attack time remains " + --curSec + " s");
                    JSONObject attackerJsonObject = new JSONObject()
                            .element("stage", "attackCountDown")
                            .element("operation", "attack")
                            .element("status", true)
                            .element("timer", curSec);
                    JSONObject defenderJsonObject = new JSONObject()
                            .element("stage", "defenseCountDown")
                            .element("situation", "beAttackedRequest")
//                            .element("opponent", attacker)
                            .element("timer", curSec);
                    try {
                        sendMsg(playerSession.get(gameId).get(attacker), attackerJsonObject.toString());
                        sendMsg(playerSession.get(gameId).get(defender), defenderJsonObject.toString());
                    } catch (Exception e) {
                        printException(e);
                    }
                }
            }
        }, 0, 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Attack time is out");
        // 防御者仍未响应
        // 防御者自动弃城，进攻者获得城池
        if (!bothResponse.get(gameId).get(concat)) {
            try {
                defense(gameId, playerSession.get(gameId).get(defender), defender, attacker, 0, "SIG");
            } catch (Exception e) {
                printException(e);
            }
        }
    }

    private double getProduceRate(String input) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String valueStr = jsonObject.getString("value");
            double value = Double.valueOf(valueStr);
            return value / SiegeParams.getPrecision();
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0.;
        }
    }

    private double getBonusPool(String input) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(1);
            String valueStr = jsonObject.getString("value");
            double value = Double.valueOf(valueStr);
            return value / SiegeParams.getPrecision();
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0.;
        }
    }

    private JSONArray getCityBonus(String input) {
        JSONArray cityBonus = new JSONArray();
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(2);
            JSONArray mayValue = jsonObject.getJSONArray("mayvalue");
            for (int i = 0; i < mayValue.size(); ++i) {
                JSONObject item = (JSONObject) mayValue.get(i);
                JSONObject bonus = new JSONObject()
                        .element("cityId", i + 1)
                        .element("producedBonus", ((double) item.getLong("value")) / SiegeParams.getPrecision());
                cityBonus.add(bonus);
            }
            return cityBonus;
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return new JSONArray();
        }
    }

    private void errorMsg(Session session, String message) throws Exception {
        JSONObject jsonObject = new JSONObject()
                .element("stage", "notification")
                .element("situation", "systemError")
                .element("message", message);
        sendMsg(session, jsonObject.toString());
    }

    private static int getLineNumber() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];
        return e.getLineNumber();
    }

    private static String getClassName() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[2];
        return e.getClassName();
    }

    private void printException(Exception e) {
        System.out.println("Class===" + getClassName() + " Line===" + getLineNumber() + " Message===" + e.getMessage());
    }
}
