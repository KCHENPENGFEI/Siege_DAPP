package com.example.zjusiege.WebSocket;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/bidding/{gameId}")
@Component
public class Bidding {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    private static int playersPerGame = 10;
    private int biddingTimer = 30;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    //private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    // 使用map来收集各个gameId的用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
//    private Session session;
    public Bidding() {
//        AsyncTaskService asyncTaskService = new AsyncTaskService();
//        for (int i = 0; i < 20; i++) {
//            asyncTaskService.executeAsyncTask(i);
//        }
//        new Thread(()->doReplace("test")).start();
//        new Thread(()->doReplace("test")).start();
//        new Thread(()->doReplace("test")).start();
//        new Thread(()->doReplace("test")).start();
    }

//    public void doReplace(String replaceLog){
//        System.out.println("线程" + Thread.currentThread().getName() + " 执行异步任务：" + replaceLog);
//    }
//
//    public Runnable doReplace(String msg){
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("新开线程中处理:"+msg);
//            }
//        };
//
//        return runnable;
//    }

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
                // 开启一个线程
                new Thread(()-> {
                    try {
                        countDown(biddingTimer, gameId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        else {
            //
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
}
