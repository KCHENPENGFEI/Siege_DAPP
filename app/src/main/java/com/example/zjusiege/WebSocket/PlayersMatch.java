package com.example.zjusiege.WebSocket;

import cn.hyperchain.sdk.rpc.account.Account;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/WebSocket/playersMatch")
@Component
public class PlayersMatch {
    private HyperchainService hyperchainService = new HyperchainService();

    private final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    private Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);

    private static int matched = 5;
    // 进入匹配玩家的Session集合
    private static final Map<String, Session> playersSession = new ConcurrentHashMap<>();
    // 匹配成功玩家集合
    private static final Map<Integer, Map<String, Session>> matchedPlayersSession = new ConcurrentHashMap<>();

    // 进入匹配人数
    private static int playersNum = 0;
    private static boolean matchWaiting = false;
    private static int gameId = 0;

    /**
     * 连接建立成功调用的方法
     *
     * @param session   用户session
     */
    @OnOpen
    public void connect(Session session) throws Exception {
//        String address = "0x00dhjgaj";
//        while(true) {
//            if (!matchWaiting) {
//                if (!playersSession.containsKey(address)) {
//                    // 加入匹配队列中
//                    playersSession.put(address, session);
//                    // 匹配人数加1
//                    playersNum += 1;
////                    session.getBasicRemote().sendText(Integer.toString(playersNum));
//                    match();
//                    break;
//                }
//                else {
//                    System.out.println(address + "已经在匹配队列中");
//                }
//            }
//        }
//        playersSession.put("0x0", session);
        System.out.println("connect success");
    }

    @OnClose
    public void disConnect() {
        System.out.println("disConnect");
    }

    @OnMessage
    public void onMessage(String msg, Session session) throws Exception{
        JSONObject params = JSONObject.fromObject(msg);
        String address = params.getString("address");
        String sigSymbol = "SIG";
        // 缴纳入场费
        String to = deployAccount.getAddress();
        int value = SiegeParams.getEnterFee();
        String data = "enter fee";
        String transferResult = hyperchainService.transfer(address, to, value, sigSymbol, data, msg);
        if (transferResult.equals("transfer success")) {
            // 转账成功，加入匹配
            while(true) {
                if (!matchWaiting) {
                    playersSession.put(address, session);
                    playersNum += 1;
                    sendMsg(session, "进入匹配队列");
                    match();
                    break;
                }
                else {
                    sendMsg(session, "匹配等待");
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
//        session.getBasicRemote().sendText(msg);
            System.out.println("msg");
        }
        else {
            System.out.println("转账失败");
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
                playersSession.remove(address);
                map.put(address, session);
                i += 1;
                sendMsg(session, "匹配成功");
                if (i == matched) {
                    matchWaiting = false;
                    break;
                }
            }
            matchedPlayersSession.put(gameId, map);
            playersNum -= matched;
            gameId += 1;
        }
    }

    private void sendMsg(Session session, String msg) throws Exception {
        session.getBasicRemote().sendText(msg);
    }
}
//
//@ServerEndpoint("/WebSocket/Siege")
//@Component
//class server {
//    @OnOpen
//    public void onOpen(Session session) {
//        System.out.println("连接1");
//    }
//}
