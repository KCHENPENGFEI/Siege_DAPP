package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.returns.CompileReturn;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class deploy {

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
        String accountJsonA = HyperchainAPI.newAccountRawSM2();
        Account accountA = new Account(accountJsonA);

        //创建账户B
        String accountJsonB = HyperchainAPI.newAccountRawSM2();
        Account accountB = new Account(accountJsonB);

        //读取合约
        String contract = Utils.readFile("contract/SimulateBank.sol");
        //编译合约
        CompileReturn compile = hyperchain.compileContract(contract);
        //获取bin
        List<String> listBin = compile.getBin();
        String bin = listBin.get(0);
        //获取abi
        List<String> listAbi = compile.getAbi();
        String abi = listAbi.get(0);
        //封装交易
        Transaction transaction =  new Transaction(accountA.getAddress(), bin, false);
        //签名
        transaction.signWithSM2(accountJsonA, "");
        //部署合约
        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
        //查询交易
        int code = receiptReturn.getRawcode();
        log("部署结果: " + code);
        String contractAddress = receiptReturn.getContractAddress();
        log("合约地址: " + contractAddress);
        return "ccc";
    }
}
