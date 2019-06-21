package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.base.VMType;
import cn.hyperchain.sdk.rpc.function.FuncParamReal;
import cn.hyperchain.sdk.rpc.returns.CompileReturn;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import net.sf.json.JSONArray;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class deploy {

    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";

    private static void log(String s){
        System.out.println("HyperchainAPITest ====== " + s);
    }

    @RequestMapping("/deploy")
//    public String deploy() {
//        return "ccc";
//    }
    public String deploy() throws Exception {
        //创建一个实例
        HyperchainAPI hyperchain = new HyperchainAPI();

        //创建账户A
//        this.accountJsonA = HyperchainAPI.newAccountRawSM2();
//        log("accountJsonA: " + accountJsonA);
//        this.accountA = new Account(accountJsonA);

        //创建账户B
//        this.accountJsonB = HyperchainAPI.newAccountRawSM2();
//        log("accountJsonB: " + accountJsonB);
//        this.accountB = new Account(accountJsonB);

        //DEPLOY_ACCOUNT_JSON
        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);

        //读取合约
        String contract = Utils.readFile("./contract/SimulateBank.sol");
        //编译合约
        CompileReturn compile = hyperchain.compileContract(contract);
        //获取bin
        List<String> listBin = compile.getBin();
        String bin = listBin.get(0);
        log("bin: " + bin);
        //获取abi
        List<String> listAbi = compile.getAbi();
        //String abi = listAbi.get(0);
        String abi = JSONArray.fromObject(listAbi.get(0)).toString();
        log("abi: " + abi);

        //实例化合约部署交易(有构造参数)
        //FuncParamReal funcparam = new FuncParamReal("bytes32", new BigInteger("8"));
        FuncParamReal bankName = new FuncParamReal("bytes32", "chenBank");
        FuncParamReal bankNum = new FuncParamReal("uint", 10000000);
        FuncParamReal isInvalid = new FuncParamReal("bool", true);
        //封装交易
        //Transaction transaction =  new Transaction(accountA.getAddress(), bin, false);
        Transaction transaction =  new Transaction(deployAccount.getAddress(), bin, false, VMType.EVM, bankName, bankNum, isInvalid);
        //签名
        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        //部署合约
        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
        //查询交易
        int code = receiptReturn.getRawcode();
        log("部署结果: " + code);
        String contractAddress = receiptReturn.getContractAddress();
        log("合约地址: " + contractAddress);

        //验证是否为DEPLOY_ACCOUNT部署
//        SingleValueReturn singleValueReturn = this.hyperchain.getContractStatus(this.contractAddress);
//        String status = singleValueReturn.getResult();
//        log(status);

//        SingleValueReturn singleValueReturn = hyperchain.getDeployedList(deployAccount.getAddress());
//        String status = singleValueReturn.getResult();
//        log(status);
//        JSONArray results = JSONArray.fromObject(singleValueReturn.getResult());
//
//        String one = (String) results.get(0);
//
//        System.out.println(one.length());
        return "deploy";
    }

//    HyperchainAPI getHyperchain() {
//        return this.hyperchain;
//    }
//
//    Account getAccountA() {
//        return this.accountA;
//    }
//
//    Account getAccountB() {
//        return this.accountB;
//    }
//
//    String getContractAddress() {
//        return this.contractAddress;
//    }
//
//    String getAccountJsonA() {
//        return this.accountJsonA;
//    }
//
//    String getAccountJsonB() {
//        return this.accountJsonB;
//    }
//
//    String getAbi() {
//        return this.abi;
//    }

//    private HyperchainAPI hyperchain;
//    private Account accountA;
//    private Account accountB;
//    private String accountJsonA;
//    private String accountJsonB;
//    private String abi;
//    private String contractAddress;
}
