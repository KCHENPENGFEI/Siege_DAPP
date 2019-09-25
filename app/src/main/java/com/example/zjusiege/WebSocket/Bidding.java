package com.example.zjusiege.WebSocket;

import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
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

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    private static int playersPerGame = 4;
    private static int N = playersPerGame / 2;
    private static int biddingTimer = 10;
    private static int biddingTimes = 1;
    private static int payingTimer = 20;
    private static int allocateTimer = 3;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    //private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    // 使用map来收集各个gameId的用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    // 保存出价最高的n个数据
    private static List<JSONObject> topNPrice = new ArrayList<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
//    private Session session;
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
        // 第一次连接，注册信息
        if (params.getBoolean("first")) {
            String address = params.getString("address");
            register(gameId, address, session);
            // 玩家都已经注册
            if (playerSession.get(gameId).size() == playersPerGame) {
                // 此处需要解决玩家同一个玩家重复发送"first": true的信息
                // 以及玩家不属于该gameId时的处理
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "ready")
                        .element("timer", biddingTimer);
                sendAll(playerSession.get(gameId), jsonObject.toString());
                // 延迟3s开始倒计时
                TimeUnit.SECONDS.sleep(3);
                new Thread(()-> {
                    // 开启计时器
                    while (biddingTimes > 0) {
                        try {
                            countDown(biddingTimer, gameId, 10 - biddingTimes + 1);
                            System.out.println("Times" + biddingTimes);
                            // 对竞标价格进行排序
                            if (topNPrice.size() > 0) {
                                sortedTopNPrice();
                                // 发送竞标结果
                                try {
                                    JSONObject jsonObject1 = new JSONObject()
                                            .element("stage", "publicity")
                                            .element("timer", 0)
                                            .element("biddingTable", topNPrice.toString());
                                    sendAll(playerSession.get(gameId), jsonObject1.toString());
                                } catch (Exception e) {
                                    System.out.println("Got an exception: " + e.getMessage());
                                }
                                // 开启一个线程将数据发送至链上
                                new Thread(() -> {
                                    int i = 1;
                                    List<Integer> ranking = new ArrayList<>();
                                    List<String> playerAddresses = new ArrayList<>();
                                    List<Long> price = new ArrayList<>();
                                    List<Long> time = new ArrayList<>();
                                    for (JSONObject item: topNPrice) {
                                        ranking.add(i);
                                        playerAddresses.add(item.getString("address"));
                                        price.add(new Double(item.getDouble("price") * SiegeParams.getPrecision()).longValue());
                                        time.add(item.getLong("time"));
                                        i += 1;
                                    }
//                                    System.out.println(ranking);
//                                    System.out.println(playerAddresses);
//                                    System.out.println(price);
//                                    System.out.println(time);
                                    try {
                                        HyperchainService hyperchainService = new HyperchainService();
                                        hyperchainService.updateRankingTb(Integer.valueOf(gameId), ranking, playerAddresses, price, time);
                                    } catch (Exception e) {
                                        System.out.println("Got an exception: " + e.getMessage());
                                    }
                                }).start();
                            }
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        biddingTimes -= 1;
                    }
                    // 竞标结束，向得标者发送缴纳尾款数据
                    try {
                        payBiddingFee(payingTimer, gameId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                    // 尾款缴纳结束，分配城池
                    try {
//                        TimeUnit.SECONDS.sleep(3);
                        allocateCity(allocateTimer, gameId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }).start();
            }
        }
        else {
            // 不是第一次连接
            if (params.getBoolean("bidding")) {
                System.out.println("test----" + params);
                // 竞标阶段
                String address = params.getString("address");
                // 查询是否注册
                assert (playerSession.get(gameId).containsKey(address));
                double price = params.getDouble("price");
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
                for (JSONObject item: topNPrice) {
                    String currentAddress = item.getString("address");
                    if (currentAddress.equals(address)) {
                        int i = topNPrice.indexOf(item);
                        topNPrice.set(i, jsonObject);
                        unique = false;
                        break;
                    }
                }
                if (unique) {
                    topNPrice.add(jsonObject);
                }
            }
            else {
                // 缴纳尾款阶段
                String address = params.getString("address");
                double price = params.getDouble("price");
                String signature = params.getString("signature");
                System.out.println("address");
                // 验证缴纳款是否正确
                boolean exist = false;
                boolean match = false;
                for (JSONObject item: topNPrice) {
                    if (address.equals(item.getString("address"))) {
                        exist = true;
                        if (item.getDouble("price") == price) {
                            match = true;
                        }
                        break;
                    }
                }
                if (!exist) {
                    System.out.println("Players not exist");
                }
                else {
                    if (match) {
                        // 价格匹配，说明上交尾款
                        try {
                            HyperchainService hyperchainService = new HyperchainService();
                            long value = new Double(price * SiegeParams.getPrecision()).longValue();
                            String to = Config.getDeployAccount().getAddress();
                            String symbol = "SIG";
                            String result = hyperchainService.transfer(address, to, value, symbol, "pay bidding fee", signature);
//                            String result = "transfer success";
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "pay")
                                    .element("transfer", result.equals("transfer success"));
                            sendMsg(session, jsonObject.toString());
                        } catch (Exception e) {
                            System.out.println("Got an exception: " + e.getMessage());
                        }
                    }
                    else {
                        // 价格不匹配，说明未上交尾款，进行冻结用户
                        List<String> addr = new ArrayList<>();
                        List<Integer> rank = new ArrayList<>();
                        List<Long> time = new ArrayList<>();
                        addr.add(address);
                        rank.add(1);
                        time.add(new Date().getTime());
                        HyperchainService hyperchainService = new HyperchainService();
                        String result = hyperchainService.freezePlayer(addr, rank, time);
//                        String result = "success";
                        if (result.equals("success")) {
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "pay")
                                    .element("frozen", true);
                            try {
                                sendMsg(session, jsonObject.toString());
                            }
                             catch (Exception e) {
                                 System.out.println("Got an exception: " + e.getMessage());
                             }
                        }
                        // 将玩家移出map
                        // 并且从topNPrice中移出
                        playerSession.get(gameId).remove(address);
                        // System.out.println(playerSession.get(gameId).size());
                        System.out.println("topNPrice---: " + topNPrice);
                        for (JSONObject item: topNPrice) {
                            if (item.getString("address").equals(address)) {
                                topNPrice.remove(item);
                                break;
                            }
                        }
                        System.out.println("topNPrice---: " + topNPrice);
                    }
                }
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
                try {
                    sendAll(map, jsonObject.toString());
                } catch (Exception e) {
                    System.out.println("Got an exception: " + e.getMessage());
                }

            }
        },0,1000);
        TimeUnit.SECONDS.sleep(seconds);
        timer.cancel();
        System.out.println("Time is out");
    }

    private void payBiddingFee(int seconds,String gameId) throws InterruptedException {
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
                    for (JSONObject item: topNPrice) {
                        if (item.getString("address").equals(address)) {
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "pay")
                                    .element("timer", curSec)
                                    .element("deal", item.getDouble("price"));
//                            System.out.println(item.getDouble("price"));
                            try {
                                sendMsg(map.get(address), jsonObject.toString());
                            } catch (Exception e) {
                                System.out.println("Got an exception: " + e.getMessage());
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
            for (JSONObject item: topNPrice) {
                playerAddresses.add(item.getString("address"));
                cityId.add(i);
                price.add(new Double(item.getDouble("price") * SiegeParams.getPrecision()).longValue());
                i += 1;
            }
            System.out.println(playerAddresses);
            System.out.println(cityId);
            System.out.println(price);
            try {
                HyperchainService hyperchainService = new HyperchainService();
                String result = hyperchainService.allocateCity(Integer.valueOf(gameId), playerAddresses, cityId, price);
//                String result = "success";
                if (result.equals("success")) {
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "pay")
                            .element("allocate", true);
                    sendAll(map, jsonObject.toString());
                }
                else {
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "pay")
                            .element("allocate", false);
                    sendAll(map, jsonObject.toString());
                }
            } catch (Exception e) {
                System.out.println("Got an exception: " + e.getMessage());
            }
        }).start();
        TimeUnit.SECONDS.sleep(seconds);
        System.out.println("allocateCity----- Time is out" + new Date());
    }

    private void sortedTopNPrice() {
        System.out.println("排序前----: " + topNPrice.toString());
        topNPrice.sort(new Comparator<JSONObject>() {
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
        System.out.println("排序后----: " + topNPrice.toString());
        if (topNPrice.size() > N) {
            topNPrice = topNPrice.subList(0, N);
        }
        System.out.println("截取后----: " + topNPrice.toString());
    }
}
