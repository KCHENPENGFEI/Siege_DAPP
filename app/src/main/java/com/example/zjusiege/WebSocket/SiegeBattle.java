package com.example.zjusiege.WebSocket;

import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/webSocket/battle/{battleId}")
@Component
public class SiegeBattle {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    // 使用map来收集各个用户的Session
    private static final Map<String, Set<Session>> playerSession = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void connect(@PathParam("battleId") String battleId, Session session) throws Exception{
        // 创建战场以及添加用户
        if (!playerSession.containsKey(battleId)) {
            // 创建战场
            Set<Session> battleFiled = new HashSet<>();
            battleFiled.add(session);
            playerNum += 1;
            playerSession.put(battleId, battleFiled);
        }
        else {
            // 战场已经存在，直接添加用户
            playerSession.get(battleId).add(session);
            playerNum += 1;
        }
        System.out.println("a client has connected");
//        try {
//            sendMessage("success");
//        } catch (IOException e) {
//            System.out.println("IO异常");
//        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void disConnect(@PathParam("battleId") String battleId, Session session) throws Exception {
        playerSession.get(battleId).remove(session);
        playerNum -= 1;
        System.out.println("a client has disconnected");
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param params 客户端发送过来的消息*/
    @OnMessage
    public void receiveMsg(@PathParam("battleId") String battleId, JSONObject params, Session session) {

    }
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }
    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message) throws IOException {
        for (SiegeBattle item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }
    public static synchronized int getOnlineCount() {
        return playerNum;
    }
    public static synchronized void addOnlineCount() {
        SiegeBattle.playerNum++;
    }
    public static synchronized void subOnlineCount() {
        SiegeBattle.playerNum--;
    }
}
