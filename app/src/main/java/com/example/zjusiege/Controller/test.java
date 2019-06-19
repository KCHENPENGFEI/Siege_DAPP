package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.function.FuncParamReal;
import cn.hyperchain.sdk.rpc.function.FunctionDecode;
import cn.hyperchain.sdk.rpc.function.FunctionEncode;
import cn.hyperchain.sdk.rpc.returns.CompileReturn;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;

import java.util.List;

public class test {
    private static void log(String s){
        System.out.println("HyperchainAPITest ====== " + s);
    }

    public void deployTest() throws Exception {
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

        Transaction transaction = new Transaction(accountA.getAddress(), bin, false);

        //签名

        transaction.signWithSM2(accountJsonA, "");

        //部署合约

        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);

        //查询交易

        int code = receiptReturn.getRawcode();

        log("部署结果:" + code);

        String contractAddress = receiptReturn.getContractAddress();

        log("合约地址:" + contractAddress);

        //调用合约(有参数)

        FuncParamReal addr = new FuncParamReal("address", accountA.getAddress());

        FuncParamReal number = new FuncParamReal("uint", "10000");

        //方法编码

        String payloadWithParams = FunctionEncode.encodeFunction("issue", addr, number);

        //实例化调用合约

        Transaction transactionWithParmas = new Transaction(accountA.getAddress(), contractAddress, payloadWithParams, false);

        transactionWithParmas.signWithSM2(accountJsonA, "");

        ReceiptReturn receiptReturnWithParams = hyperchain.invokeContract(transactionWithParmas);

        //获取与解析返回值

        String rawReturnWithParams;

        rawReturnWithParams = receiptReturnWithParams.getRet();

        String decodedResultWithParams = FunctionDecode.resultDecode("issue", abi, rawReturnWithParams);

        log("调用issue：" + decodedResultWithParams);

        //调用getAccountBalance方法查看余额

        String payload2 = FunctionEncode.encodeFunction("getAccountBalance", addr);

        Transaction transaction1 = new Transaction(accountB.getAddress(), contractAddress, payload2, false);

        transaction1.signWithSM2(accountJsonB, "");

        ReceiptReturn receiptReturn2 = hyperchain.invokeContract(transaction1);

        String rawReturn2 = receiptReturn2.getRet();

        String decodedResult = FunctionDecode.resultDecode("getAccountBalance", abi, rawReturn2);

        log("调用getAccountBalance：" + decodedResult);

    }

}
