package com.example.zjusiege.WebSocket;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
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
    private static int biddingTimer = 30;
    private static int biddingTimes = 10;
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
                            countDown(biddingTimer, gameId);
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
                                    sendAll(playerSession.get(gameId), jsonObject.toString());
                                } catch (Exception e) {
                                    System.out.println("Got an exception: " + e.getMessage());
                                }
                            }
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        biddingTimes -= 1;
                    }
                }).start();
            }
        }
        else {
            // 不是第一次连接
            if (params.getBoolean("bidding")) {
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
                topNPrice.add(jsonObject);
            }
            else {

            }
        }
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
        timer.schedule(new TimerTask(){
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(gameId);
            public void run() {
                System.out.println("Time remains "+ --curSec +" s");
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "bidding")
                        .element("timer", curSec);
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
