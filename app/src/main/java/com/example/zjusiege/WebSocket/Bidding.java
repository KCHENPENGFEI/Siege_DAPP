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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/bidding/{gameId}")
@Component
public class Bidding {
    private HyperchainService hyperchainService = new HyperchainService();
    private Account deployAccount = Config.getDeployAccount();

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    private static int playersPerGame = 4;
    private static int N = playersPerGame / 2;
    private static int biddingTimer = 10;
    private static int biddingTimes = 5;
    private static int payingTimer = 20;
    private static int allocateTimer = 3;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    //private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    // 使用map来收集各个gameId的用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> gameStarted = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Boolean>> ifPayed = new ConcurrentHashMap<>();
    // 保存出价最高的n个数据
    private static Map<String, List<JSONObject>> topNPrice = new ConcurrentHashMap<>();

    private int debug = 1;
//    private static List<JSONObject> topNPrice = new ArrayList<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
//    private Session session;
    @OnOpen
    public void connect(@PathParam("gameId") String gameId, Session session) {
        System.out.println("bidding connect success");
        // 增加在线人数
        playerNum += 1;
        System.out.println("playerNum: " + playerNum);
        initGameStarted(gameId);
        initIfPayed(gameId);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, Session session) {
        System.out.println("bidding disConnect");
        // 减少在线人数
        playerNum -= 1;
        System.out.println("playerNum: " + playerNum);
        // 打印掉线玩家地址
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
        // 获取用户的地址
        String address = params.getString("address");
        if (first) {
            // 检查玩家链上数据
            boolean matched = checkPlayerInfo(gameId, address, session);
            if (matched) {
                // 如果匹配，进入游戏
                // 对玩家进行注册
                boolean registered = register(gameId, address, session);
                // 如果是第一次注册
                if (registered) {
                    // bidding开启条件是否满足，进入人数达到一半
                    if (playerSession.get(gameId).size() == playersPerGame / 2 && !gameStarted.get(gameId)) {
                        // bidding倒计时准备开始
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "ready")
                                .element("timer", biddingTimer);
                        sendAll(playerSession.get(gameId), jsonObject.toString());
                        // 延迟2秒
                        TimeUnit.SECONDS.sleep(2);
                        // 开启bidding倒计时
                        new Thread(()-> {
//                            int full = biddingTimes;
//                            while (biddingTimes > 0) {
//                                try {
//                                    gameStarted.replace(gameId, true);
//                                    // 30秒倒计时
//                                    countDown(biddingTimer, gameId, full - biddingTimes + 1);
//                                    // 对竞标价格进行排序
//                                    if (topNPrice.get(gameId).size() > 0) {
//                                        sortedTopNPrice(gameId);
//                                        // 开启一个线程将数据发送至链上
//                                        new Thread(() -> {
//                                            int i = 1;
//                                            List<Integer> ranking = new ArrayList<>();
//                                            List<String> playerAddresses = new ArrayList<>();
//                                            List<Long> price = new ArrayList<>();
//                                            List<Long> time = new ArrayList<>();
//                                            for (JSONObject item: topNPrice.get(gameId)) {
//                                                ranking.add(i);
//                                                playerAddresses.add(item.getString("address"));
//                                                price.add(new Double(item.getDouble("price") * SiegeParams.getPrecision()).longValue());
//                                                time.add(item.getLong("time"));
//                                                i += 1;
//                                            }
//                                            try {
//                                                hyperchainService.updateRankingTb(Integer.valueOf(gameId), ranking, playerAddresses, price, time);
//                                            } catch (Exception e) {
//                                                System.out.println("Got an exception: " + e.getMessage());
//                                            }
//                                        }).start();
//                                    }
//                                    TimeUnit.SECONDS.sleep(3);
//                                } catch (Exception e) {
//                                    System.out.println("Got an exception: " + e.getMessage());
//                                }
//                                biddingTimes -= 1;
//                            }

                            try {
                                biddingStart(gameId);
                            } catch (Exception e) {
                                printException(e);
                            }

                            // 竞标结束，向得标者发送缴纳尾款数据
                            try {
                                payBiddingFeeCountDown(payingTimer, gameId);
                            } catch (Exception e) {
                                printException(e);
                            }

                            // 尾款缴纳结束，分配城池
                            try {
                                TimeUnit.SECONDS.sleep(3);
                                allocateCity(allocateTimer, gameId);
                            } catch (Exception e) {
                                printException(e);
                            }

                            // 清除数据
                            clearGameStarted(gameId);
                            clearIfPayed(gameId);
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
            }
            else {
                // 不匹配，此时不用发送错误数据，在检查时已经发送
                System.out.println("playerStatus not correct");
            }
        }
        else {
            // 之后进行封装
            // 不是第一次连接
            if (params.getBoolean("bidding")) {
                System.out.println("params---- " + params);
                double price = params.getDouble("price");
                // 竞标阶段
                // 查询是否注册
//                assert (playerSession.get(gameId).containsKey(address));
//                SimpleDateFormat sdf = new SimpleDateFormat();  // 格式化时间
//                sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");  // a为am/pm的标记
//                Date date = new Date();
//                JSONObject jsonObject = new JSONObject()
//                        .element("price", price)
//                        .element("address", address)
//                        .element("time", date.getTime());
//                // 由前端保证一轮内只能竞标一次
//                // 若已经竞过标则将前一次数据覆盖
//                boolean unique = true;
//                for (JSONObject item: topNPrice.get(gameId)) {
//                    String currentAddress = item.getString("address");
//                    if (currentAddress.equals(address)) {
//                        int i = topNPrice.get(gameId).indexOf(item);
//                        topNPrice.get(gameId).set(i, jsonObject);
//                        unique = false;
//                        break;
//                    }
//                }
//                if (unique) {
//                    topNPrice.get(gameId).add(jsonObject);
//                }
//                JSONObject response = new JSONObject()
//                        .element("stage", "bid")
//                        .element("status", true);
//                sendMsg(session, response.toString());
                bidding(gameId, address, price, session);
            }
            else {
                // 缴纳尾款阶段
                double price = params.getDouble("price");
                String signature = params.getString("signature");
//                if (debug == 1) {
//                    System.out.println("debug@cpf: address " + address);
//                }
//                // 验证缴纳款是否正确
//                boolean exist = false;
//                boolean match = false;
//                // 判断是否在竞标表中
//                for (JSONObject item: topNPrice.get(gameId)) {
//                    if (address.equals(item.getString("address"))) {
//                        exist = true;
//                        if (item.getDouble("price") == price) {
//                            match = true;
//                        }
//                        break;
//                    }
//                }
//                if (!exist) {
//                    if (debug == 1) {
//                        System.out.println("Players not exist");
//                    }
//                    JSONObject jsonObject = new JSONObject()
//                            .element("stage", "error")
//                            .element("message", "not in bidding table");
//                    sendMsg(session, jsonObject.toString());
//                }
//                else {
//                    if (match) {
//                        // 价格匹配，说明上交尾款
//                        long value = new Double(price * SiegeParams.getPrecision()).longValue();
//                        String to = Config.getDeployAccount().getAddress();
//                        String symbol = "SIG";
//                        String result = hyperchainService.transfer(address, to, value, symbol, "pay bidding fee", signature);
////                            String result = "transfer success";
//                        JSONObject jsonObject = new JSONObject()
//                                .element("stage", "transfer")
//                                .element("status", result.equals("transfer success"));
//                        sendMsg(session, jsonObject.toString());
//                        ifPayed.get(gameId).replace(address, true);
//                    }
//                    else {
//                        // 价格不匹配，缴纳失败
//                        JSONObject jsonObject = new JSONObject()
//                                .element("stage", "transfer")
//                                .element("status", false);
//                        sendMsg(session, jsonObject.toString());
//                    }
//                }
//                if (playerSession.get(gameId).size() == 0) {
//                    // 如果所有人都清除了
//                    playerSession.remove(gameId);
//                    topNPrice.remove(gameId);
//                }
                payBiddingFee(gameId, address, price, signature, session);
            }
        }
    }

    @OnError
    public void connectError(Throwable error) {
        System.out.println("connect error");
        error.printStackTrace();
    }

    public static int getPlayersSessionSize(String gameId) {
        return playerSession.get(gameId).size();
    }

    private boolean checkPlayerInfo(String gameId, String address, Session session) throws Exception {
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
                    String[] gameStageList = new String[]{"start", "bidding", "running", "settling", "ending"};
                    int gameStageInt = Utils.returnInt(globalInfo, 1);
                    String gameStage = gameStageList[gameStageInt];
                    if (gameStage.equals("bidding")) {
                        // gameId和gameStage均正确
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "checkPlayerStatus")
                                .element("status", true)
                                .element("gameStage", gameStage)
                                .element("gameId", gameId);
                        sendMsg(session, jsonObject.toString());
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
            List<JSONObject> list = new ArrayList<>();
            topNPrice.put(gameId, list);
            // 返回第一次注册为真
            return true;
        }
        else {
            if (!playerSession.get(gameId).containsKey(address)) {
                // 第一次连接，进行注册，返回true
                Map<String, Session> map = playerSession.get(gameId);
                map.put(address, session);
                playerSession.put(gameId, map);
                return true;
            }
            else {
                // 已经注册过，返回第一次注册为假
                playerSession.get(gameId).replace(address, session);
                return false;
            }
        }
    }

    private void initGameStarted(String gameId) {
        if (!gameStarted.containsKey(gameId)) {
            gameStarted.put(gameId, false);
        }
    }

    private void initIfPayed(String gameId) {
        if (!ifPayed.containsKey(gameId)) {
            Map<String, Boolean> map = new ConcurrentHashMap<>();
            ifPayed.put(gameId, map);
        }
    }

    private void clearGameStarted(String gameId) {
        gameStarted.remove(gameId);
    }

    private void clearIfPayed(String gameId) {
        ifPayed.remove(gameId);
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

    private void biddingStart(String gameId) throws InterruptedException {
        gameStarted.replace(gameId, true);
        int full = biddingTimes;
        while (biddingTimes > 0) {
            // 30秒倒计时
            countDown(biddingTimer, gameId, full - biddingTimes + 1);
            // 对竞标价格进行排序
            if (topNPrice.get(gameId).size() > 0) {
                sortedTopNPrice(gameId);
                // 开启一个线程将数据发送至链上
                new Thread(() -> {
                    int i = 1;
                    List<Integer> ranking = new ArrayList<>();
                    List<String> playerAddresses = new ArrayList<>();
                    List<Long> price = new ArrayList<>();
                    List<Long> time = new ArrayList<>();
                    for (JSONObject item: topNPrice.get(gameId)) {
                        ranking.add(i);
                        playerAddresses.add(item.getString("address"));
                        price.add(new Double(item.getDouble("price") * SiegeParams.getPrecision()).longValue());
                        time.add(item.getLong("time"));
                        i += 1;
                    }
                    try {
                        hyperchainService.updateRankingTb(Integer.valueOf(gameId), ranking, playerAddresses, price, time);
                    } catch (Exception e) {
                        System.out.println("Class===" + getClassName() + " Line===" + getLineNumber() + " Message===updateRankTb error!");
                    }
                }).start();
            }
            TimeUnit.SECONDS.sleep(3);
            biddingTimes -= 1;
        }
    }

    private void countDown(int seconds, String gameId, int round) throws InterruptedException {
        System.out.println("count down from " + seconds + " s ");

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(gameId);
            public void run() {
                System.out.println("Time remains "+ --curSec +" s");
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "bidding")
                        .element("timer", curSec)
                        .element("round", round);
                sendAll(map, jsonObject.toString());
            }
        },0,1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Time is out");
    }

    private void bidding(String gameId, String address, double price, Session session) throws Exception {
//        System.out.println("params---- " + params);
        // 竞标阶段
        // 查询是否注册
        assert (playerSession.get(gameId).containsKey(address));
        SimpleDateFormat sdf = new SimpleDateFormat();  // 格式化时间
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");  // a为am/pm的标记
        Date date = new Date();
        JSONObject jsonObject = new JSONObject()
                .element("price", price)
                .element("address", address)
                .element("time", date.getTime());
        // 由前端保证一轮内只能竞标一次
        // 若已经竞过标则将前一次数据覆盖
        boolean unique = true;
        for (JSONObject item: topNPrice.get(gameId)) {
            String currentAddress = item.getString("address");
            if (currentAddress.equals(address)) {
                int i = topNPrice.get(gameId).indexOf(item);
                topNPrice.get(gameId).set(i, jsonObject);
                unique = false;
                break;
            }
        }
        if (unique) {
            topNPrice.get(gameId).add(jsonObject);
        }
        JSONObject response = new JSONObject()
                .element("stage", "bid")
                .element("status", true);
        sendMsg(session, response.toString());
    }

    private void payBiddingFee(String gameId, String address, double price, String signature, Session session) throws Exception {
        if (debug == 1) {
            System.out.println("debug@cpf: address " + address);
        }
        // 验证缴纳款是否正确
        boolean exist = false;
        boolean match = false;
        // 判断是否在竞标表中
        for (JSONObject item: topNPrice.get(gameId)) {
            if (address.equals(item.getString("address"))) {
                exist = true;
                if (item.getDouble("price") == price) {
                    match = true;
                }
                break;
            }
        }
        if (!exist) {
            if (debug == 1) {
                System.out.println("Players not exist");
            }
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "transfer")
                    .element("status", false);
            sendMsg(session, jsonObject.toString());
        }
        else {
            if (match) {
                // 价格匹配，说明上交尾款
                long value = new Double(price * SiegeParams.getPrecision()).longValue();
                String to = Config.getDeployAccount().getAddress();
                String symbol = "SIG";
                String result = hyperchainService.transfer(address, to, value, symbol, "pay bidding fee", signature);
//                            String result = "transfer success";
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "transfer")
                        .element("status", result.equals("transfer success"));
                sendMsg(session, jsonObject.toString());
                ifPayed.get(gameId).replace(address, true);
            }
            else {
                // 价格不匹配，缴纳失败
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "transfer")
                        .element("status", false);
                sendMsg(session, jsonObject.toString());
            }
        }
        if (playerSession.get(gameId).size() == 0) {
            // 如果所有人都清除了
            playerSession.remove(gameId);
            topNPrice.remove(gameId);
        }
    }

    private void payBiddingFeeCountDown(int seconds, String gameId) throws InterruptedException {
        System.out.println("payBiddingFee---- count down from " + seconds + " s ");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(gameId);
            @Override
            public void run() {
                System.out.println("payBiddingFee----- Time remains "+ --curSec +" s");
                for (String address: map.keySet()) {
                    boolean send = false;
                    for (JSONObject item: topNPrice.get(gameId)) {
                        if (item.getString("address").equals(address)) {
                            // 初始化ifPayed
                            if (!ifPayed.get(gameId).containsKey(address)) {
                                ifPayed.get(gameId).put(address, false);
                            }
                            if (!ifPayed.get(gameId).get(address)) {
                                // 发送
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "pay")
                                        .element("timer", curSec)
                                        .element("deal", item.getDouble("price"));
//                            System.out.println(item.getDouble("price"));
//                                System.out.println("address: " + address + "json: " + jsonObject.toString());
                                try {
                                    sendMsg(map.get(address), jsonObject.toString());
                                } catch (Exception e) {
                                    System.out.println("Got an exception: " + e.getMessage());
                                }
                            }
                            send = true;
                            break;
                        }
                    }
                    if (!send) {
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "pay")
                                .element("timer", curSec)
                                .element("deal", 0);
                        try {
                            sendMsg(map.get(address), jsonObject.toString());
                        } catch (Exception e) {
                            System.out.println("Got an exception: " + e.getMessage());
                        }
                    }
                }
            }
        },0 ,1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        // 判断是否缴纳尾款
        List<String> addr = new ArrayList<>();
        List<Integer> rank = new ArrayList<>();
        List<Long> time = new ArrayList<>();
        for (String address: ifPayed.get(gameId).keySet()) {
            if (!ifPayed.get(gameId).get(address)) {
                // 禁用用户
                addr.add(address);
                rank.add(1);
                time.add(new Date().getTime());
            }
        }
        try {
            if (addr.size() > 0) {
//                HyperchainService hyperchainService = new HyperchainService();
                String result = hyperchainService.freezePlayer(addr, rank, time);
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "frozen")
                        .element("status", result.equals("success"));
                for (String address: addr) {
                    sendMsg(playerSession.get(gameId).get(address), jsonObject.toString());
                    // 从session中移出
                    playerSession.get(gameId).remove(address);
                    System.out.println("topNPrice---: " + topNPrice.get(gameId));
                    for (JSONObject item: topNPrice.get(gameId)) {
                        if (item.getString("address").equals(address)) {
                            topNPrice.get(gameId).remove(item);
                            break;
                        }
                    }
                    System.out.println("topNPrice---: " + topNPrice.get(gameId));
                }
            }
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
        System.out.println("payBiddingFee----- Time is out");
    }

    private void allocateCity(int seconds, String gameId) throws InterruptedException {
        System.out.println("allocateCity---- count down from " + seconds + " s ");
        Map<String, Session> map = playerSession.get(gameId);
        new Thread(() -> {
            // 分配城池
            List<String> playerAddresses = new ArrayList<>();
            List<Integer> cityId = new ArrayList<>();
            List<Long> price = new ArrayList<>();
            int i = 1;
            for (JSONObject item: topNPrice.get(gameId)) {
                playerAddresses.add(item.getString("address"));
                cityId.add(i);
                price.add(new Double(item.getDouble("price") * SiegeParams.getPrecision()).longValue());
                i += 1;
            }
            System.out.println(playerAddresses);
            System.out.println(cityId);
            System.out.println(price);
            try {
//                HyperchainService hyperchainService = new HyperchainService();
                String result = hyperchainService.allocateCity(Integer.valueOf(gameId), playerAddresses, cityId, price);
//                String result = "success";
                if (result.equals("success")) {
                    // 更新游戏阶段值RUNNING
                    String updateStage = hyperchainService.updateGameStage(Integer.valueOf(gameId), SiegeParams.gameStage.RUNNING.ordinal());
                    assert (updateStage.equals("success"));
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "allocate")
                            .element("status", true);
                    sendAll(map, jsonObject.toString());
                }
                else {
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "allocate")
                            .element("status", false);
                    sendAll(map, jsonObject.toString());
                }
            } catch (Exception e) {
                printException(e);
            }
        }).start();
        TimeUnit.SECONDS.sleep(seconds);
        System.out.println("allocateCity----- Time is out" + new Date());
    }

    private void sortedTopNPrice(String gameId) {
        System.out.println("排序前----: " + topNPrice.get(gameId).toString());
        topNPrice.get(gameId).sort(new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                double p1 = o1.getDouble("price");
                double p2 = o2.getDouble("price");
                long d1 = o1.getLong("time");
                long d2 = o2.getLong("time");
                if (p1 < p2) {
                    return 1;
                } else if (p1 > p2) {
                    return -1;
                } else {
                    return Long.compare(d1, d2);
                }
            }
        });
        System.out.println("排序后----: " + topNPrice.get(gameId).toString());
        if (topNPrice.get(gameId).size() > N) {
            topNPrice.replace(gameId, topNPrice.get(gameId).subList(0, N));
        }
        System.out.println("截取后----: " + topNPrice.get(gameId).toString());
        // 将结果进行广播
        try {
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "publicity")
                    .element("timer", 0)
                    .element("biddingTable", topNPrice.get(gameId).toString());
            sendAll(playerSession.get(gameId), jsonObject.toString());
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
        }
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
