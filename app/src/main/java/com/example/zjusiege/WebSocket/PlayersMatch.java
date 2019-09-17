package com.example.zjusiege.WebSocket;

import cn.hyperchain.sdk.rpc.account.Account;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import com.example.zjusiege.Utils.Utils;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/WebSocket/playersMatch")
@Component
public class PlayersMatch {
    private HyperchainService hyperchainService = new HyperchainService();
//    private final String deployAccountJson = Config.getDeployAccountJson();
    private Account deployAccount = Config.getDeployAccount();

    private static int matched = 4;
    // 进入匹配玩家的Session集合
    private static final Map<String, Session> playersSession = new ConcurrentHashMap<>();
    // 匹配成功玩家集合
    private static final Map<Integer, Map<String, Session>> matchedPlayersSession = new ConcurrentHashMap<>();

    // 进入匹配人数
    private static int playersNum = 0;
    private static boolean matchWaiting = false;
//    private static int gameId = 0;

    /**
     * 连接建立成功调用的方法
     *
     * @param session   用户session
     */
    @OnOpen
    public void connect(Session session) throws Exception {
        System.out.println("connect success");
    }

    @OnClose
    public void disConnect(Session session) {
        System.out.println("disConnect");
        for (String addr: playersSession.keySet()) {
            playersSession.remove(addr);
        }
    }

    @OnMessage
    public void onMessage(String msg, Session session) throws Exception{
        JSONObject params = JSONObject.fromObject(msg);
        boolean match = params.getBoolean("match");
        String signature = params.getString("signature");
        String address = JSONObject.fromObject(signature).getString("address");
//        String address = params.getString("address");

        if (match){
            // 缴纳入场费
            String sigSymbol = "SIG";  //以后保存到参数类中
            String to = deployAccount.getAddress();
            int value = SiegeParams.getEnterFee() * SiegeParams.getPrecision();
            String data = "enter fee";
            String transferResult = hyperchainService.transfer(address, to, value, sigSymbol, data, signature);

            if (transferResult.equals("transfer success")) {
//            if(true){
                // 转账成功，加入匹配
                while(true) {
                    // 判断是否在匹配队列中
                    if (playersSession.containsKey(address)) {
                        JSONObject jsonObject = new JSONObject()
                                .element("match", "already in matching queue")
                                .element("gameId", 0);
                        sendMsg(session, jsonObject.toString());
                        break;
                    }
                    else {
                        if (!matchWaiting) {
                            playersSession.put(address, session);
                            playersNum += 1;
                            JSONObject jsonObject = new JSONObject()
                                    .element("match", "enter matching queue")
                                    .element("gameId", 0);
                            sendMsg(session, jsonObject.toString());
                            match();
                            break;
                        }
                        else {
                            JSONObject jsonObject = new JSONObject()
                                    .element("match", "match waiting")
                                    .element("gameId", 0);
                            sendMsg(session, jsonObject.toString());
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            else {
                JSONObject jsonObject = new JSONObject()
                        .element("match", "transfer failed")
                        .element("gameId", 0);
                System.out.println("transfer failed");
                sendMsg(session, jsonObject.toString());
            }
        }
        else {
            JSONObject jsonObject = new JSONObject()
                    .element("match", "match error")
                    .element("gameId", 0);
            sendMsg(session, jsonObject.toString());
        }
    }

    @OnError
    public void connectError(Throwable error) {
        System.out.println("connect error");
        error.printStackTrace();
    }

    private void match() throws Exception {
        if (playersNum >= matched) {
            int i = 0;
            matchWaiting = true;
            Map<String, Session> map = new ConcurrentHashMap<>();
            for(String address: playersSession.keySet()) {
                Session session = playersSession.get(address);
                // 从匹配队列中剔除
                playersSession.remove(address);
                map.put(address, session);
                i += 1;
                if (i == matched) {
                    matchWaiting = false;
                    break;
                }
            }
            List<String> list = new ArrayList<>();
            boolean b = list.addAll(map.keySet());
            String[] playersAddresses = list.toArray(new String[0]);
            try {
                String result = hyperchainService.startGame(playersAddresses);
                // 匹配完成后获取gameId
                int gameId = Integer.valueOf(Utils.getValue(result));
                assert (gameId != 0);
                // 将本场匹配的玩家数据保存
                matchedPlayersSession.put(gameId, map);
                playersNum -= matched;
                for (String address: map.keySet()) {
                    Session session = map.get(address);
                    JSONObject jsonObject = new JSONObject()
                            .element("match", "match success")
                            .element("gameId", gameId);
                    sendMsg(session, jsonObject.toString());
                }
            } catch (Exception e) {
                for (String address: map.keySet()) {
                    Session session = map.get(address);
                    JSONObject jsonObject = new JSONObject()
                            .element("match", "match error")
                            .element("gameId", 0);
                    sendMsg(session, jsonObject.toString());
                }
            }
        }
    }

    private void sendMsg(Session session, String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }

    public static Map getmatchedPlayersSession(int index) {
        return matchedPlayersSession.get(index);
    }
}
