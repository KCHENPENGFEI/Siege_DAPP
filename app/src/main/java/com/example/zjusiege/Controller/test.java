package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.crypto.ECPriv;
import cn.hyperchain.sdk.rpc.HyperchainAPI;
import com.example.zjusiege.WebSocket.SiegeBattle;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class test {

    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    public final String A_ACCOUNT_JSON = "{\"address\":\"E01022F5A99494849D80C03AB7BC59082121606E\",\"publicKey\":\"04E7210EE8A63557E2756B97A1B7A361EAD0F98241835DF3B6499AE7F04465AEC28F3BD11AD4974E0EAD06CD30E238658A8ACE712D617EA999333B9A91D463E590\",\"privateKey\":\"00B29EDA5D013114EBB7D701AD75EB61869F14B9285A54D7F18FA000E9E3C90F67\",\"privateKeyEncrypted\":false}";
    public final String B_ACCOUNT_JSON = "{\"address\":\"C0C32F9EB8710A5D236C716355028E7B8AD766BA\",\"publicKey\":\"043D66019D64327C0474672E8DBFBBFD61F69260B2477C80B0635D3F960289F7E3398FE283CC3FA8D8FADCFFDB490A685434288D0BD2C50790D99BB40D20FBD621\",\"privateKey\":\"00BBF8BD7344D54986D17EEB55D5414B2F94744E8F1340613745735C6ABBA2CBA7\",\"privateKeyEncrypted\":false}";
    public final String CONTRACT_ADDRESS = "0x32697a9d9f89a9e0bf7ac32c7455ff44ace232d6";

    private static void log(String s){
        System.out.println("HyperchainAPITest ====== " + s);
    }


    @RequestMapping("test111")
    public void accountTest() throws Exception {
        HyperchainAPI hyperchain = new HyperchainAPI();
//        String accountJsonA = HyperchainAPI.newAccountSM2("123");
//        Account accountA = new Account(accountJsonA);
//        log("accountA: " + accountJsonA);
//        String accountJsonA = HyperchainAPI.newAccountRawSM2();
//        log("明文: " + accountJsonA);
//        Account b = new Account(accountJsonA);
//        log(b.getPublicKey());
//        String encryptedAccountJsonA = HyperchainAPI.encryptAccountSM2(accountJsonA, "123");
//        log("加密: " + encryptedAccountJsonA);
//        Account a = new Account(encryptedAccountJsonA);
//        log(a.getPublicKey());
//        String plainAccountJsonA = HyperchainAPI.decryptAccountSM2(encryptedAccountJsonA, "123");
//        log("解密: " + plainAccountJsonA);
        //冻结合约
//        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);
//        Transaction transaction = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, 2, VMType.EVM);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON,"");
//        ReceiptReturn receiptReturn = hyperchain.invokeContract(transaction);

        //获取区块中所有交易信息
    //        SingleValueReturn singleValueReturn =  hyperchain.getTx("1", "latest");
    //        String result = singleValueReturn.getResult();
    //        log("result： " + result);

        //通过区块哈希查询交易
//        SingleValueReturn singleValueReturn1 = hyperchain.getTxByBlkHashAndIdx("c5ae0f38610cc83c80e0788cde0f2c36b0019dedb6511595a9a74c82f83089a1", 9);
//        String result1 = singleValueReturn1.getResult();
//        log("result: " + result1);

        //查询区块交易数量
//        SingleValueReturn singleValueReturn = hyperchain.getBlkTxCountByHash("c5ae0f38610cc83c80e0788cde0f2c36b0019dedb6511595a9a74c82f83089a1");
//        String result = singleValueReturn.getResult();
//        log("result: " + result);

        //查询链上所有交易
//        SingleValueReturn singleValueReturn = hyperchain.getTxCount();
//        String result = singleValueReturn.getResult();
//        log("result: " + result);

        //future模式调用合约
//        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);
//        Account accountA = new Account(A_ACCOUNT_JSON);
//        Account accountB = new Account(B_ACCOUNT_JSON);
//        FuncParamReal addr = new FuncParamReal("address", accountB.getAddress());
//        FuncParamReal num = new FuncParamReal("uint", 10000);
//
//        String payload = FunctionEncode.encodeFunction("issue", addr, num);
//        String contractAddress = CONTRACT_ADDRESS;
//        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payload, false);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        FutureTask<ReceiptReturn> futureTask = hyperchain.invokeContractAsync(transaction);
//        String txHash = futureTask.get().getTxHash();
//        log("txHash: " + txHash);
//
//        if(!futureTask.get().isSuccess()){
//            log("合约调用失败!");
//        }
//        int code = futureTask.get().getRawcode();
//        log("合约调用成功, code: " + code);
//
//        String res = futureTask.get().getRet();
//        String abi = Utils.readFile("./contract/SimulateBank.abi").trim();
//        String decodedResult = FunctionDecode.resultDecode("issue", abi, res);
//        log("调用issue: " + decodedResult);
        //合约升级(失败)
//        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);
//        String newBin = Utils.readFile("./contract/SimulateBank.bin").trim();
//        String oldContractAddress = CONTRACT_ADDRESS;
//        Transaction transaction = new Transaction(deployAccount.getAddress(), oldContractAddress, newBin,1, VMType.EVM);
//
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//        ReceiptReturn receiptReturn = hyperchain.maintainContract(transaction);
//        String contractAddress = receiptReturn.getContractAddress();
//        log("更新后合约地址: " + contractAddress);
//        String result = receiptReturn.getResult();
//        log("result: " + result);
//        int code = receiptReturn.getRawcode();
//        log("code: " + code);
//
//        SingleValueReturn singleValueReturn = hyperchain.getContractStatus(oldContractAddress);
//        String status = singleValueReturn.getResult();
//        log(status);
//
//        SingleValueReturn singleValueReturn1 = hyperchain.getDeployedList(deployAccount.getAddress());
//        String status1 = singleValueReturn1.getResult();
//        log(status1);
        //取得最新区块信息
//        BlockReturn blockReturn = hyperchain.getLatestBlock();
//        log("最新区块: " + blockReturn.getResult());
        //取得指定区块 by number
//        BlockReturn blockReturn = hyperchain.getBlkByNum(new BigInteger("2", 16), true);
//        log("第二块区块: " + blockReturn.getResult());

        //查询指定时间内的TPS
//        TPSReturn tpsReturn = hyperchain.queryTPS(new BigInteger("1"), new BigInteger("1561303473674405000"));
//        log(tpsReturn.getResult());

        // 获取节点信息
//        ArrayList<NodeInfoReturn> nodeInfoReturns = hyperchain.getNodes();
//        for (NodeInfoReturn nodeInfoReturn : nodeInfoReturns){
//            System.out.println(nodeInfoReturn.getIp());
//            System.out.println(nodeInfoReturn.getId());
//            System.out.println(nodeInfoReturn.getPrimary());
//            System.out.println(nodeInfoReturn.getDelay());
//        }

        ECPriv testAccount = HyperchainAPI.newAccount();
        log(testAccount.getPrivateKey());
        String account = HyperchainAPI.newAccountRawSM2();
    }

    @RequestMapping("/duizhan")
    public String duizahn() {
        SiegeBattle siegeWebSocket = new SiegeBattle();

        return "ok";
    }
}
