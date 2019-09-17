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

@ServerEndpoint("/WebSocket/cityMap/{gameId}")
@Component
public class CityMap {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    private static int runningTimer = 3600;

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
            if (playerSession.get(gameId).size() == 1) {
                // 一旦有人进入游戏，游戏正式开始
                new Thread(()-> {

                }).start();
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

    private void countDown1(int seconds, String gameId) throws InterruptedException {
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


    private void countDown(int seconds, String gameId) throws InterruptedException {
        System.out.println("count down from " + seconds + " s ");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(gameId);
            @Override
            public void run() {

            }
        });
    }
}
