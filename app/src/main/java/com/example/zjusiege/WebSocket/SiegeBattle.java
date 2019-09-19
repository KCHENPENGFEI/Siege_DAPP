package com.example.zjusiege.WebSocket;

import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.IESParameterSpec;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.bouncycastle.math.ec.custom.sec.SecP256K1FieldElement;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Point;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/WebSocket/battle/{gameId}/{battleId}")
@Component
public class SiegeBattle {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int playerNum = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的SiegeWebSocket对象。
    private static CopyOnWriteArraySet<SiegeBattle> webSocketSet = new CopyOnWriteArraySet<SiegeBattle>();
    // 使用map来收集各个用户的Session
    private static final Map<String, Map<String, Session>> playerSession = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, JSONObject>> playerSoldiers = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private static String attackerAddress;
    private static String defenderAddress;
    private static int playersPerGame = 2;
    private static int buySoldiersTimer = 60;


    @OnOpen
    public void connect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception{
        // 创建战场以及添加用户
        String[] addresses = battleId.split("&&");
        attackerAddress = addresses[0];
        defenderAddress = addresses[1];
//        playerSoldiers.get(battleId).get(attackerAddress)
//                .element("type", new ArrayList<Integer>())
//                .element("price", new ArrayList<Double>())
//                .element("ready", false);
//        playerSoldiers.get(battleId).get(defenderAddress)
//                .element("type", new ArrayList<Integer>())
//                .element("price", new ArrayList<Double>())
//                .element("ready", false);
        if (!playerSoldiers.containsKey(battleId)) {
            Map<String, JSONObject> map = new ConcurrentHashMap<>();
            map.put(attackerAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false));
            map.put(defenderAddress, new JSONObject()
                    .element("type", new ArrayList<Integer>())
                    .element("price", 0.)
                    .element("quantity", 0)
                    .element("pay", false)
                    .element("ready", false));
            playerSoldiers.put(battleId, map);
        }

        playerNum += 1;
        System.out.println("playerNum: " + playerNum + " attackerAddress: " + attackerAddress + " defenderAddress: " + defenderAddress);
    }

    @OnClose
    public void disConnect(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, Session session) throws Exception {
        playerNum -= 1;
        System.out.println("playerNum: " + playerNum);
    }

    @OnMessage
    public void onMessage(@PathParam("gameId") String gameId, @PathParam("battleId") String battleId, String msg, Session session) throws Exception {
        JSONObject params = JSONObject.fromObject(msg);
        if (params.getBoolean("first")) {
            // 第一次连接时做一些链上数据查询，确保玩家的游戏状态是正确的，暂时略
            // 首先进行注册
            String address = params.getString("address");
            register(battleId, address, session);
            if (playerSession.get(battleId).size() == playersPerGame) {
                // 开启一个购买士兵的倒计时
                // 主线程延迟2秒进行数据查找以及初始化
                new Thread(()-> {
                    try {
                        initShop(battleId);
                    } catch (Exception e) {
                        System.out.println("Got an exception: " + e.getMessage());
                    }
                }).start();
                TimeUnit.SECONDS.sleep(2);

                new Thread(()-> {
                    try {
                        // 游戏开始倒计时
                        buySoldiersCountDown(buySoldiersTimer, battleId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        else {
            String address = params.getString("address");
            String operation = params.getString("operation");
            if (operation.equals("buySoldiers")) {
                List<Integer> type = castList(params.get("type"), Integer.class);
                double price = params.getDouble("price");
                int quantity = params.getInt("quantity");
                String symbol = "SIG";
                String signature = params.getString("signature");

                // 首先进行缴费
                try {
                    HyperchainService hyperchainService = new HyperchainService();
                    String transferResult = hyperchainService.transfer(address, Config.getDeployAccount().getAddress(), new Double(price * SiegeParams.getPrecision()).longValue(), symbol, "buy soldiers", signature);
                    if (transferResult.equals("transfer success")) {
                        // 更新playerSoldier
                        List<Integer> oldSoldiers = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
                        oldSoldiers.addAll(type);
                        playerSoldiers.get(battleId).get(address).replace("type", oldSoldiers);

                        double oldPrice = playerSoldiers.get(battleId).get(address).getDouble("price");
                        double newPrice = oldPrice + price;
                        playerSoldiers.get(battleId).get(address).replace("price", newPrice);

                        int oldQuantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
                        int newQuantity = oldQuantity + quantity;
                        playerSoldiers.get(battleId).get(address).replace("quantity", newQuantity);

                        playerSoldiers.get(battleId).get(address).replace("pay", true);
                        // 告知购买士兵成功
                        JSONObject jsonObject = new JSONObject()
                                .element("operation", "transfer")
                                .element("status", true);
                        sendMsg(session, jsonObject.toString());
                    }
                    else {
                        // 告知转账失败
                        JSONObject jsonObject = new JSONObject()
                                .element("operation", "transfer")
                                .element("status", false);
                        sendMsg(session, jsonObject.toString());
                    }
                } catch (Exception e) {
                    System.out.println("Got an exception: " + e.getMessage());
                }
            }
            else if (operation.equals("departure")) {
//                encryptWithPubKey("044107987A7C6271BBADC66A16B3C9EF7B69B445BBF0273D43B51DCF48FCE4E481C46CAEFA701A670D31D0FF5CEAD10F0A1DAC45D5A0F82B67B8F14C6E2E1BF369", "778076014E4B5D1430BB3A4F6FE14A6FC2D1C2FD973EC7077E62D25B02F2C05D");
                List<Integer> type = castList(playerSoldiers.get(battleId).get(address).get("type"), Integer.class);
                long price = new Double(playerSoldiers.get(battleId).get(address).getDouble("price") * SiegeParams.getPrecision()).longValue();
                int quantity = playerSoldiers.get(battleId).get(address).getInt("quantity");
                try {
                    HyperchainService hyperchainService = new HyperchainService();
                    String buyResult = hyperchainService.buySoldiers(Integer.valueOf(gameId), price, type, price, quantity);
                    String departureResult = hyperchainService.departure(Integer.valueOf(gameId), address);
                    if (buyResult.equals("success") && (departureResult.equals("success"))) {
                        // 告知玩家
                        JSONObject jsonObject = new JSONObject()
                                .element("operation", "departure")
                                .element("status", true);
                        sendMsg(session, jsonObject.toString());
                    }
                    else {
                        // 告知玩家失败
                        JSONObject jsonObject = new JSONObject()
                                .element("operation", "departure")
                                .element("status", true);
                        sendMsg(session, jsonObject.toString());
                        // 进行退款
                    }
                } catch (Exception e) {
                    System.out.println("Got an exception: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
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

    private void register(String battleId, String address, Session session) {
        if (!playerSession.containsKey(battleId)) {
            // 构建战场
            Map<String, Session> map = new ConcurrentHashMap<>();
            map.put(address, session);
            playerSession.put(battleId, map);
        }
        else {
            if (!playerSession.get(battleId).containsKey(address)) {
                Map<String, Session> map = playerSession.get(battleId);
                map.put(address, session);
                playerSession.replace(battleId, map);
            }
            else {
                playerSession.get(battleId).replace(address, session);
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

    private void buySoldiersCountDown(int seconds, String battleId) throws InterruptedException {
        System.out.println("buySoldiers--- count down from " + seconds + " s ");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int curSec = seconds;
            Map<String, Session> map = playerSession.get(battleId);
            @Override
            public void run() {
                System.out.println("Buy soldiers time remains " + --curSec + " s");
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "buySoldiers")
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
        System.out.println("Buy soldiers time is out");
    }

//    private void initShop(int seconds, String battleId) throws InterruptedException {
//        System.out.println("initShop--- count down from " + seconds + " s ");
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            int curSec = seconds;
//            Map<String, Session> map = playerSession.get(battleId);
//            @Override
//            public void run() {
//                // 商店数据查询
//                System.out.println("curSec: " + --curSec);
//                JSONArray jsonArray = new JSONArray();
//                List<String> soldiersName = SiegeParams.getSoldiersName();
//                List<Integer> soldiersPoint = SiegeParams.getSoldiersPoint();
//                List<String> soldiersDescription = SiegeParams.getSoldiersDescription();
//                for (int i = 1; i <= SiegeParams.getSoldierNum(); ++i) {
//                    JSONObject jsonObject = new JSONObject()
//                            .element("type", soldiersName.get(i))
//                            .element("description", soldiersDescription.get(i))
//                            .element("price", soldiersPoint.get(i));
//                    jsonArray.add(jsonObject);
//                }
//                try {
//                    sendAll(map, jsonArray.toString());
//                } catch (Exception e) {
//                    System.out.println("Got an exception: " + e.getMessage());
//                }
//            }
//        }, 2000, 1000);
//        TimeUnit.SECONDS.sleep(seconds);
//        timer.cancel();
//        System.out.println("Init shop time is out");
//    }

    private void initShop(String battleId) throws Exception {
        Map<String, Session> map = playerSession.get(battleId);
        JSONArray jsonArray = new JSONArray();
        List<String> soldiersName = SiegeParams.getSoldiersName();
        List<Integer> soldiersPoint = SiegeParams.getSoldiersPoint();
        List<String> soldiersDescription = SiegeParams.getSoldiersDescription();
        for (int i = 1; i <= SiegeParams.getSoldierNum(); ++i) {
            JSONObject jsonObject = new JSONObject()
                    .element("type", soldiersName.get(i))
                    .element("description", soldiersDescription.get(i))
                    .element("price", soldiersPoint.get(i));
            jsonArray.add(jsonObject);
        }
        sendAll(map, jsonArray.toString());
    }

    private void encryptWithPubKey(String publicKey, String privateKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException, IOException {
        // ECDSA secp256k1 algorithm constants
        BigInteger pointGPre = new BigInteger("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798", 16);
        BigInteger pointGPost = new BigInteger("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8", 16);
        BigInteger factorN = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
        BigInteger fieldP = new BigInteger("fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f", 16);

        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        IESParameterSpec iesParams = new IESParameterSpec(null, null, 64);

        //----------------------------
        // Encrypt with public key
        //----------------------------
        String prePublicKeyStr = publicKey.substring(0, 64);
        String postPublicKeyStr = publicKey.substring(64);

        EllipticCurve ellipticCurve = new EllipticCurve(new ECFieldFp(fieldP), new BigInteger("0"), new BigInteger("7"));
        ECPoint pointG = new ECPoint(pointGPre, pointGPost);
        ECNamedCurveSpec namedCurveSpec = new ECNamedCurveSpec("secp256k1", ellipticCurve, pointG, factorN);

        SecP256K1Curve secP256K1Curve = new SecP256K1Curve();
        SecP256K1Point secP256K1Point = new SecP256K1Point(secP256K1Curve, new SecP256K1FieldElement(new BigInteger(prePublicKeyStr, 16)), new SecP256K1FieldElement(new BigInteger(postPublicKeyStr, 16)));
        SecP256K1Point secP256K1PointG = new SecP256K1Point(secP256K1Curve, new SecP256K1FieldElement(pointGPre), new SecP256K1FieldElement(pointGPost));
        ECDomainParameters domainParameters = new ECDomainParameters(secP256K1Curve, secP256K1PointG, factorN);
        ECPublicKeyParameters publicKeyParameters = new ECPublicKeyParameters(secP256K1Point, domainParameters);
        BCECPublicKey publicKeySelf = new BCECPublicKey("ECDSA", publicKeyParameters, namedCurveSpec, BouncyCastleProvider.CONFIGURATION);

        // begin encrypt
        cipher.init(Cipher.ENCRYPT_MODE, publicKeySelf, iesParams);
        String cleartextFile = "contract/source.txt";
        String ciphertextFile = "contract/cipher.txt";
        byte[] block = new byte[64];
        FileInputStream fis = new FileInputStream(cleartextFile);
        FileOutputStream fos = new FileOutputStream(ciphertextFile);
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);

        int i;
        while ((i = fis.read(block)) != -1) {
            cos.write(block, 0, i);
        }
        cos.close();

        //----------------------------
        // Decrypt with private key
        //----------------------------
        BigInteger privateKeyValue = new BigInteger(privateKey, 16);

        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyValue, namedCurveSpec);
        BCECPrivateKey privateKeySelf = new BCECPrivateKey("ECDSA", privateKeySpec, BouncyCastleProvider.CONFIGURATION);
        // begin decrypt
        String cleartextAgainFile = "contract/decrypt.txt";
        cipher.init(Cipher.DECRYPT_MODE, privateKeySelf, iesParams);
        fis = new FileInputStream(ciphertextFile);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        fos = new FileOutputStream(cleartextAgainFile);
        while ((i = cis.read(block)) != -1) {
            fos.write(block, 0, i);
        }
        fos.close();
    }
}
