package com.example.zjusiege.WebSocket;

import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
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
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    private static int runningTimer = 3600;
    private static int interval = 10;
    private static int intervalNum = runningTimer / interval;

    // 一个用来保存玩家session的容器
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();

    @OnOpen
    public void connect(@PathParam("gameId") String gameId, Session session) {
        // 增加在线人数
        playerNum += 1;
        System.out.println("playerNum: " + playerNum);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, Session session) {
        // 减少在线人数
        playerNum -= 1;
        System.out.println("playerNum: " + playerNum);
    }

    @OnMessage
    public void onMessage(@PathParam("gameId") String gameId, String msg, Session session) throws Exception {
        JSONObject params = JSONObject.fromObject(msg);
        if (params.getBoolean("first")) {
            // 第一次连接时做一些链上数据查询，确保玩家的游戏状态是正确的，暂时略
            String address = params.getString("address");
            register(gameId, address, session);
//            assert (playerSession.get(gameId).size() < Bidding.getPlayersSessionSize(gameId));
            if (playerSession.get(gameId).size() == 1) {
                // 一旦有人进入游戏，游戏正式开始
                // 需要两个线程同步的问题
                new Thread(()-> {
                    try {
                        // 简单延时3秒
                        TimeUnit.SECONDS.sleep(3);
                        // 游戏开始倒计时
                        countDown(runningTimer, gameId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

                // 开启一个同步线程用于更新cityMap的数据
                new Thread(()-> {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                        updateCityMap(runningTimer, gameId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            // 查询城池数据，发送给玩家
            try {
                JSONArray jsonArray = new JSONArray();
                HyperchainService hyperchainService = new HyperchainService();
                for (int i = 1; i <= SiegeParams.getCityNum(); ++i) {
                    String result = hyperchainService.getCitiesTable(Integer.valueOf(gameId), i);
                    String cityName = returnString(result, 0);
                    double defenseIndex = returnDouble(result, 1);
                    double realtimePrice = returnDouble(result, 2);
                    boolean ifBeOccupied = returnBool(result, 3);
                    String belongPlayer = returnString(result, 4);
                    double producedBonus = returnDouble(result, 5);
//                    System.out.println(result);
                    JSONObject jsonObject = new JSONObject()
                            .element("cityId", i)
                            .element("cityName", cityName)
                            .element("defenseIndex", defenseIndex)
                            .element("realtimePrice", realtimePrice)
                            .element("ifBeOccupied", ifBeOccupied)
                            .element("belongPlayer", belongPlayer)
                            .element("producedBonus", producedBonus);
                    jsonArray.add(jsonObject);
                }
                sendMsg(session, jsonArray.toString());
            } catch (Exception e) {
                System.out.println("Got an exception: " + e.getMessage());
            }
        }
        else {
            switch (params.getString("operation")) {
                case "occupy": {
                    // 玩家占领空城
                    String address = params.getString("address");
                    int cityId = params.getInt("cityId");
                    long price = new Double(params.getDouble("price") * SiegeParams.getPrecision()).longValue();
                    String symbol = "SIG";
                    String signature = params.getString("signature");
                    try {
                        HyperchainService hyperchainService = new HyperchainService();
                        String transferResult = hyperchainService.transfer(address, Config.getDeployAccount().getAddress(), price, symbol, "player occupies city", signature);
//                        String transferResult = "transfer success";
                        if (transferResult.equals("transfer success")) {
                            String result = hyperchainService.occupyCity(Integer.valueOf(gameId), address, cityId, price, signature);
//                            String result = "success";
                            if (result.equals("success")) {
                                // 告知玩家占领成功
                                JSONObject response = new JSONObject()
                                        .element("operation", "occupy")
                                        .element("status", true);
                                sendMsg(session, response.toString());
                                // 广播城池更新消息
                                JSONObject jsonObject = new JSONObject()
                                        .element("cityId", cityId)
                                        .element("realtimePrice", price)
                                        .element("ifBeOccupied", true)
                                        .element("belongPlayer", address)
                                        .element("producedBonus", 0.);
                                sendAll(playerSession.get(gameId), jsonObject.toString());
                            } else {
                                // 告知玩家占领失败
                                JSONObject response = new JSONObject()
                                        .element("operation", "occupy")
                                        .element("status", false);
                                sendMsg(session, response.toString());
                                // 给用户退款
//                                hyperchainService.transfer(Config.getDeployAccount().getAddress(), address, price, symbol, "Refund", Config.getDeployAccountJson());
                            }
                        }
                        else {
                            // 告知玩家缴费失败
                            JSONObject response = new JSONObject()
                                    .element("operation", "transfer")
                                    .element("status", false);
                            sendMsg(session, response.toString());
                        }
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                    break;
                }
                case "leave": {
                    // 玩家离开城池
                    String address = params.getString("address");
                    int cityId = params.getInt("cityId");
                    long bonus = new Double(params.getDouble("bonus") * SiegeParams.getPrecision()).longValue();
                    String symbol = "SIG";
                    try {
                        HyperchainService hyperchainService = new HyperchainService();
                        String transferResult = hyperchainService.transfer(Config.getDeployAccount().getAddress(), address, bonus, symbol, "settle bonus", Config.getDeployAccountJson());
//                        String transferResult = "transfer success";
                        if (transferResult.equals("transfer success")) {
                            String result = hyperchainService.leaveCity(Integer.valueOf(gameId), address);
//                            String result = "success";
                            JSONObject response = new JSONObject()
                                    .element("operation", "leave")
                                    .element("status", result.equals("success"));
                            sendMsg(session, response.toString());
                            if (result.equals("success")) {
                                // 广播城池更新信息
                                JSONObject jsonObject = new JSONObject()
                                        .element("cityId", cityId)
                                        .element("ifBeOccupied", false)
                                        .element("belongPlayer", "0000000000000000000000000000000000000000")
                                        .element("producedBonus", 0.);
                                sendAll(playerSession.get(gameId), jsonObject.toString());
                            }
                        }
                        else {
                            JSONObject response = new JSONObject()
                                    .element("operation", "leave")
                                    .element("status", false);
                            sendMsg(session, response.toString());
                        }
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                    break;
                }
                case "attack": {
                    // 玩家进攻
                    String address = params.getString("address");
                    String target = params.getString("target");
//                int cityId = params.getInt("cityId");
                    try {
                        HyperchainService hyperchainService = new HyperchainService();
                        String result = hyperchainService.attack(Integer.valueOf(gameId), address, target);
//                        String result = "success";
                        if (result.equals("success")) {
                            // 告知攻击者
                            JSONObject response = new JSONObject()
                                    .element("operation", "attack")
                                    .element("status", true);
                            sendMsg(session, response.toString());
                            // 告知被攻击者
                            Session targetSession = playerSession.get(gameId).get(target);
                            JSONObject jsonObject = new JSONObject()
                                    .element("situation", "beAttackedRequest")
                                    .element("opponent", address);
                            sendMsg(targetSession, jsonObject.toString());
                        }
                        else {
                            // 操作失败
                            // 告知攻击者操作失败
                            JSONObject response = new JSONObject()
                                    .element("operation", "attack")
                                    .element("status", false);
                            sendMsg(session, response.toString());
                        }
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                    break;
                }
                case "defense": {
                    // 玩家防守
                    // 前端实现，若选择超时，则自动发送放弃防御选择
                    String address = params.getString("address");
                    int cityId = params.getInt("cityId");
                    String target = params.getString("target");
                    int choice = params.getInt("choice");
                    long bonus = new Double(params.getDouble("bonus") * SiegeParams.getPrecision()).longValue();
                    String symbol = "SIG";
                    try {
                        HyperchainService hyperchainService = new HyperchainService();
                        if (choice == 0) {
                            // 玩家离城
                            String transferResult = hyperchainService.transfer(Config.getDeployAccount().getAddress(), address, bonus, symbol, "settle bonus", Config.getDeployAccountJson());
//                            String transferResult = "transfer success";
                            if (transferResult.equals("transfer success")) {
                                String result = hyperchainService.defense(Integer.valueOf(gameId), address, target, cityId, choice);
//                                String result = "success";
                                if (result.equals("success")) {
                                    // 告知进攻者获得城池
                                    JSONObject jsonObject = new JSONObject()
                                            .element("situation", "getCity");
                                    Session targetSession = playerSession.get(gameId).get(target);
                                    sendMsg(targetSession, jsonObject.toString());
                                    // 告知玩家操作成功
                                    JSONObject response = new JSONObject()
                                            .element("operation", "defense")
                                            .element("status", true);
                                    sendMsg(session, response.toString());
                                    // 广播城池更新信息
                                    JSONObject jsonObject1 = new JSONObject()
                                            .element("cityId", cityId)
                                            .element("ifBeOccupied", true)
                                            .element("belongPlayer", target)
                                            .element("producedBonus", 0.);
                                    sendAll(playerSession.get(gameId), jsonObject1.toString());
                                }
                                else {
                                    // 操作失败
                                    // 告知双方系统错误
                                    JSONObject jsonObject = new JSONObject()
                                            .element("situation", "systemError");
                                    Session targetSession = playerSession.get(gameId).get(target);
                                    sendMsg(session, jsonObject.toString());
                                    sendMsg(targetSession, jsonObject.toString());
                                }
                            }
                            else {
                                // 操作失败
                                // 告知双方系统错误
                                JSONObject jsonObject = new JSONObject()
                                        .element("situation", "systemError");
                                Session targetSession = playerSession.get(gameId).get(target);
                                sendMsg(session, jsonObject.toString());
                                sendMsg(targetSession, jsonObject.toString());
                            }
                        }
                        else {
                            String result = hyperchainService.defense(Integer.valueOf(gameId), address, target, cityId, choice);
//                            String result = "success";
                            if (result.equals("success")) {
                                // 玩家选择防守
                                // 告知进攻者
                                JSONObject jsonObject = new JSONObject()
                                        .element("situation", "beforeBattle");
                                Session targetSession = playerSession.get(gameId).get(target);
                                sendMsg(targetSession, jsonObject.toString());
                                // 告知防守者操作成功
                                JSONObject response = new JSONObject()
                                        .element("operation", "defense")
                                        .element("status", true);
                                sendMsg(session, response.toString());
                            }
                            else {
                                // 操作失败
                                // 告知双方系统错误
                                JSONObject jsonObject = new JSONObject()
                                        .element("situation", "systemError");
                                Session targetSession = playerSession.get(gameId).get(target);
                                sendMsg(session, jsonObject.toString());
                                sendMsg(targetSession, jsonObject.toString());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                    break;
                }
                case "settle": {
                    List<String> cityOwners = castList(params.get("cityOwners"), String.class);
                    try {
                        HyperchainService hyperchainService = new HyperchainService();
                        String updateStage = hyperchainService.updateGameStage(Integer.valueOf(gameId), SiegeParams.gameStage.SETTLING.ordinal());
                        assert (updateStage.equals("success"));

                        String result = hyperchainService.settlement(Integer.valueOf(gameId), cityOwners);
                        if (result.equals("success")) {
                            // 成功
                            JSONObject jsonObject = new JSONObject()
                                    .element("operation", "settle")
                                    .element("status", true);
                            sendAll(playerSession.get(gameId), jsonObject.toString());
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
                        String updateStage1 = hyperchainService.updateGameStage(Integer.valueOf(gameId), SiegeParams.gameStage.ENDING.ordinal());
                        assert (updateStage1.equals("success"));

                        String result1 = hyperchainService.endGame(Integer.valueOf(gameId), allPlayers);
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

    private void register(String gameId, String address, Session session) {
        if (!playerSession.containsKey(gameId)) {
            // 构建游戏
            Map<String, Session> map = new ConcurrentHashMap<>();
            map.put(address, session);
            playerSession.put(gameId, map);
        }
        else {
            if (!playerSession.get(gameId).containsKey(address)) {
                Map<String, Session> map = playerSession.get(gameId);
                map.put(address, session);
                playerSession.put(gameId, map);
            }
            else {
                playerSession.get(gameId).replace(address, session);
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
                        .element("stage", "running")
                        .element("timer", curSec);
                try {
                    sendAll(map, jsonObject.toString());
                } catch (Exception e) {
                    System.out.println("Got an exception: " + e.getMessage());
                }
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
                    HyperchainService hyperchainService = new HyperchainService();
                    String result = hyperchainService.updateCityBonus(Integer.valueOf(gameId), (long) num);
                    double produceRate = getProduceRate(result);
                    double bonusPool = getBonusPool(result);
                    JSONArray cityBonus = getCityBonus(result);
                    JSONObject jsonObject = new JSONObject()
                            .element("produceRate", produceRate)
                            .element("bonusPool", bonusPool)
                            .element("cityBonus", cityBonus);
                    sendAll(map, jsonObject.toString());
                    System.out.println("num---: " + num);
                } catch (Exception e) {
                    System.out.println("Got an exception: " + e.getMessage());
                }
            }
        }, interval * 1000, interval * 1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Game time is out");
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

    private String returnString(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            return(jsonObject.getString("value"));
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return "";
        }
    }

    private double returnDouble(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            double value = Double.valueOf(valueStr);
            if (index == 1) {
                return value / 100;
            }
            else {
                return value / SiegeParams.getPrecision();
            }
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0.;
        }
    }

    private boolean returnBool(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            return valueStr.equals("true");
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return false;
        }
    }
}
