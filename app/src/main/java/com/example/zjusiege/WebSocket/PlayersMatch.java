package com.example.zjusiege.WebSocket;

import cn.hyperchain.sdk.account.Account;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.FiloopService;
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
    // TODO
//    private HyperchainService hyperchainService = new HyperchainService();
    private FiloopService filoopService = new FiloopService();
//    private final String deployAccountJson = Config.getDeployAccountJson();
    private Account deployAccount = Config.getDeployAccount();

    private static int matched = 4;
    // 进入匹配玩家的Session集合
    private static final Map<String, Session> playersSession = new ConcurrentHashMap<>();
    // 匹配成功玩家集合
//    private static final Map<Integer, Map<String, Session>> matchedPlayersSession = new ConcurrentHashMap<>();
//    private static final Map<String, Integer> playersGameId = new ConcurrentHashMap<>();

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
        System.out.println("playersMatch connect success");
//        playersNum += 1;
//        System.out.println("playersMatch: " + playersNum);
    }

    @OnClose
    public void disConnect(Session session) {
        System.out.println("playersMatch disConnect");
//        playersNum -= 1;
        for (String address: playersSession.keySet()) {
            if (playersSession.get(address) == session) {
                System.out.println("debug@cpf: " + address);
            }
        }
    }

    @OnMessage
    public void onMessage(String msg, Session session) throws Exception{
        JSONObject params = JSONObject.fromObject(msg);
//        boolean first = params.getBoolean("first");
//        // 获取用户的地址
//        String address = params.getString("address");
//        System.out.println(address);
//        if (first) {
//            // 对链上数据进行查询
//            // 获取用户信息

//        }
//        else {
//
//        }
        // 玩家进入匹配
        boolean match = params.getBoolean("match");
        String signature = params.getString("signature");
        String address = JSONObject.fromObject(signature).getString("address");
        System.out.println(address);

        boolean matched = checkPlayerStatus(address, session);
        if (matched) {
            if (match){
                // 缴纳入场费
                String sigSymbol = "SIG";  //以后保存到参数类中
                String to = deployAccount.getAddress();
                int value = SiegeParams.getEnterFee() * SiegeParams.getPrecision();
                String data = "enter fee";
                // TODO
                String transferResult = filoopService.transfer(address, to, value, sigSymbol, data, signature);
//                String transferResult = "transfer success";
                if (transferResult.equals("transfer success")) {
//            if(true){
                    // 转账成功，加入匹配
                    while(true) {
                        // 判断是否在匹配队列中
                        if (playersSession.containsKey(address)) {
                            // 已经进入匹配
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "match")
                                    .element("message", "already in matching queue")
                                    .element("gameId", 0);
                            sendMsg(session, jsonObject.toString());
                            break;
                        }
                        else {
                            if (!matchWaiting) {
                                // 加入匹配队列
                                playersSession.put(address, session);
                                playersNum += 1;
//                            System.out.println(playersSession);
//                                enqueue(address, session);
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "match")
                                        .element("message", "enter matching queue")
                                        .element("gameId", 0);
                                sendMsg(session, jsonObject.toString());
                                match();
                                break;
                            }
                            else {
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "match")
                                        .element("message", "match waiting")
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
                            .element("stage", "match")
                            .element("message", "transfer failed")
                            .element("gameId", 0);
                    System.out.println("transfer failed");
                    sendMsg(session, jsonObject.toString());
                }
            }
            else {
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "match")
                        .element("match", "match error")
                        .element("gameId", 0);
                sendMsg(session, jsonObject.toString());
            }
        }
        else {
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "match")
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
                // TODO
                String result = filoopService.startGame(playersAddresses);
                // 匹配完成后获取gameId
                if (!result.equals("contract calling error") && !result.equals("unknown error")) {
                    int gameId = Utils.returnInt(result, 0);
                    if (gameId != 0) {
                        // 更新游戏阶段值bidding
                        // TODO
                        String updateStage = filoopService.updateGameStage(gameId, SiegeParams.gameStage.BIDDING.ordinal());
                        if (updateStage.equals("success")) {
                            // 匹配成功，发送信息
                            for (String address: map.keySet()) {
                                Session session = map.get(address);
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "match")
                                        .element("message", "match success")
                                        .element("gameId", gameId);
                                sendMsg(session, jsonObject.toString());
                            }
                            playersNum -= matched;
                        }
                        else {
                            // 匹配失败
                            for (String address: map.keySet()) {
                                Session session = map.get(address);
                                JSONObject jsonObject = new JSONObject()
                                        .element("stage", "match")
                                        .element("message", "match error")
                                        .element("gameId", gameId);
                                sendMsg(session, jsonObject.toString());
                            }
                        }
                    }
                    else {
                        // 匹配失败
                        for (String address: map.keySet()) {
                            Session session = map.get(address);
                            JSONObject jsonObject = new JSONObject()
                                    .element("stage", "match")
                                    .element("message", "match error")
                                    .element("gameId", gameId);
                            sendMsg(session, jsonObject.toString());
                        }
                    }
                }
                else {
                    for (String address: map.keySet()) {
                        Session session = map.get(address);
                        JSONObject jsonObject = new JSONObject()
                                .element("stage", "match")
                                .element("message", "match error")
                                .element("gameId", 0);
                        sendMsg(session, jsonObject.toString());
                    }
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

//    public static Map getmatchedPlayersSession(int index) {
//        return matchedPlayersSession.get(index);
//    }

    private boolean checkPlayerStatus(String address, Session session) throws Exception {
        // TODO
        String playerInfo = filoopService.getPlayersStatus(address);
        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            // 用户存在
            int gameId = Utils.returnInt(playerInfo, 0);
            if (gameId != 0) {
                // 玩家已经成功匹配，处于游戏中
                // 查询指定gameId的游戏状态
                // TODO
                String globalInfo = filoopService.getGlobalTb(gameId);
                if (!globalInfo.equals("contract calling error") && !globalInfo.equals("unknown error")) {
                    // 获取游戏阶段
                    String[] gameStage = new String[]{"start", "bidding", "running", "settling", "ending"};
                    int gameStageInt = Utils.returnInt(globalInfo, 1);
                    // 发送给前端
                    JSONObject jsonObject = new JSONObject()
                            .element("stage", "match")
                            .element("message", "already match success")
                            .element("gameId", gameId)
                            .element("gameStage", gameStage[gameStageInt]);
                    sendMsg(session, jsonObject.toString());
                    return false;
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
                // 玩家未开始游戏，准备进入匹配
//                JSONObject jsonObject = new JSONObject()
//                        .element("stage", "match")
//                        .element("message", "");
//                sendMsg(session, jsonObject.toString());
                return true;
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
}
