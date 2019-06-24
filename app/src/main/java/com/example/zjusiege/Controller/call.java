package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.function.FuncParamReal;
import cn.hyperchain.sdk.rpc.function.FunctionDecode;
import cn.hyperchain.sdk.rpc.function.FunctionEncode;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class call {

    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    public final String A_ACCOUNT_JSON = "{\"address\":\"E01022F5A99494849D80C03AB7BC59082121606E\",\"publicKey\":\"04E7210EE8A63557E2756B97A1B7A361EAD0F98241835DF3B6499AE7F04465AEC28F3BD11AD4974E0EAD06CD30E238658A8ACE712D617EA999333B9A91D463E590\",\"privateKey\":\"00B29EDA5D013114EBB7D701AD75EB61869F14B9285A54D7F18FA000E9E3C90F67\",\"privateKeyEncrypted\":false}";
    public final String B_ACCOUNT_JSON = "{\"address\":\"C0C32F9EB8710A5D236C716355028E7B8AD766BA\",\"publicKey\":\"043D66019D64327C0474672E8DBFBBFD61F69260B2477C80B0635D3F960289F7E3398FE283CC3FA8D8FADCFFDB490A685434288D0BD2C50790D99BB40D20FBD621\",\"privateKey\":\"00BBF8BD7344D54986D17EEB55D5414B2F94744E8F1340613745735C6ABBA2CBA7\",\"privateKeyEncrypted\":false}";
    public final String CONTRACT_ADDRESS = "0x32697a9d9f89a9e0bf7ac32c7455ff44ace232d6";

    private static void log(String s){
        System.out.println("HyperchainAPITest ====== " + s);
    }

    @RequestMapping("call")
    public String call() throws Exception {

        HyperchainAPI hyperchain = new HyperchainAPI();

        //建立相应账户
        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);
        Account accountA = new Account(A_ACCOUNT_JSON);
        Account accountB = new Account(B_ACCOUNT_JSON);

        //调用issue接口
//        FuncParamReal addr = new FuncParamReal("address", accountA.getAddress());
//        FuncParamReal number = new FuncParamReal("uint", 10000);
//
//        //方法编码
//        String payloadWithParams = FunctionEncode.encodeFunction("issue", addr, number);
//
//        //合约地址
//        String contractAddress = CONTRACT_ADDRESS;
//        //实例化调用合约
//        Transaction transactionWithParams = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
//        transactionWithParams.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        //同步调用合约
//        ReceiptReturn receiptReturnWithParams = hyperchain.invokeContract(transactionWithParams);
//
//        //获取和解析返回值
//        String rawReturnWithParams;
//        if(!receiptReturnWithParams.isSuccess()){
//            log("合约调用失败,code: ");
//        }
//        int code = receiptReturnWithParams.getRawcode();
//        log("合约调用成功,code: " + code);
//
//        rawReturnWithParams = receiptReturnWithParams.getRet();
//        log("调用合约结果（未解码）：" + rawReturnWithParams);
//
//        //获取abi
//        String abi = Utils.readFile("./contract/SimulateBank.abi").trim();
//        String decodedResultWithParams = FunctionDecode.resultDecode("issue", abi, rawReturnWithParams);
//        log("调用合约结果（解码后）" + decodedResultWithParams);

        //调用getAccountBalance接口
        FuncParamReal addr = new FuncParamReal("address", accountB.getAddress());
        String payloadWithParams = FunctionEncode.encodeFunction("getAccountBalance", addr);
        String contractAddress = CONTRACT_ADDRESS;
        Transaction transactionWithParams = new Transaction(accountB.getAddress(), contractAddress, payloadWithParams, false);
        transactionWithParams.signWithSM2(B_ACCOUNT_JSON,"");
        ReceiptReturn receiptReturnWithParams = hyperchain.invokeContract(transactionWithParams);

        String rawReturn = receiptReturnWithParams.getRet();
        String abi = Utils.readFile("./contract/SimulateBank.abi").trim();
        String decodedResult = FunctionDecode.resultDecode("getAccountBalance", abi, rawReturn);
        log("调用getAccountBalance：" + decodedResult);

        //调用newFunc接口(不存在)
//        String payloadWithoutParams = FunctionEncode.encodeFunction("newFunc");
//        Transaction transactionWithoutParams = new Transaction(accountB.getAddress(), contractAddress, payloadWithoutParams, false);
//        transactionWithoutParams.signWithSM2(B_ACCOUNT_JSON, "");
//        ReceiptReturn receiptReturnWithoutParams = hyperchain.invokeContract(transactionWithoutParams);
//
//        String rawReturn1 = receiptReturnWithoutParams.getRet();
//        String decodeRes = FunctionDecode.resultDecode("newFunc", abi, rawReturn1);
//        log("调用newFunc: " + decodeRes);
        return "call";
    }
}
