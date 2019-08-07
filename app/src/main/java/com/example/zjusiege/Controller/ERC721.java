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
public class ERC721 {
    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    public final  String CONTRACT_ADDRESS = "0x4234415ca4d5dfea74ba6bb00592d5454d7df952";

    private static void log(String s){
        System.out.println("HyperchainAPITest ====== " + s);
    }
    @RequestMapping("/ERC721")
    public String deploy_test() throws Exception {
        HyperchainAPI hyperchain = new HyperchainAPI();
        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);

//        String abi = Utils.readFile("contract/GLDToken.abi").trim();
//        String bin = Utils.readFile("contract/GLDToken.bin").trim();
//
//        FuncParamReal initialSupply = new FuncParamReal("uint256", 10000);
//
//        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false, VMType.EVM, initialSupply);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
//        int code = receiptReturn.getRawcode();
//        log("部署结果: " + code);
//        String contractAddress = receiptReturn.getContractAddress();
//        log("合约地址: " + contractAddress);
//
//        FuncParamReal addr = new FuncParamReal("address", deployAccount.getAddress());
//        String payloadWithParams = FunctionEncode.encodeFunction("balanceOf", addr);
//
//        Transaction transactionWithParams = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
//        transactionWithParams.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//        ReceiptReturn receiptReturnWithParams = hyperchain.invokeContract(transactionWithParams);
//
//        String rawReturn = receiptReturnWithParams.getRet();
//        String decodedResult = FunctionDecode.resultDecode("balanceOf", abi, rawReturn);
//        log("调用balanceOf：" + decodedResult);
        String abi = Utils.readFile("contract/GameItem.abi");
        String bin = Utils.readFile("contract/GameItem.bin");

//        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
//        int code = receiptReturn.getRawcode();
//        log("部署结果: " + code);
//        String contractAddress = receiptReturn.getContractAddress();
//        log("合约地址: " + contractAddress);

        FuncParamReal addr = new FuncParamReal("address", deployAccount.getAddress());
        FuncParamReal Url = new FuncParamReal("string", "https://game.example/item-id-8u5m.json");

        String payloadWithParams = FunctionEncode.encodeFunction("awardItem", addr, Url);
        Transaction transactionWithParams = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParams, false);
        transactionWithParams.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        ReceiptReturn receiptReturnWithParams = hyperchain.invokeContract(transactionWithParams);

        String rawReturn = receiptReturnWithParams.getRet();
        String decodeResult = FunctionDecode.resultDecode("awardItem", abi, rawReturn);
        log("调用awardItem: " + decodeResult);

        FuncParamReal ItemId1 = new FuncParamReal("uint256", 1);
        FuncParamReal ItemId2 = new FuncParamReal("uint256", 2);
        FuncParamReal ItemId3 = new FuncParamReal("uint256", 3);
        FuncParamReal ItemId4 = new FuncParamReal("uint256", 4);

        String payloadWithParams0 = FunctionEncode.encodeFunction("ownerOf", ItemId1);
        Transaction transactionWithParams0 = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParams0, false);
        transactionWithParams0.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        ReceiptReturn receiptReturnWithParams0 = hyperchain.invokeContract(transactionWithParams0);
        String rawReturn0 = receiptReturnWithParams0.getRet();
        String decodeResult0 = FunctionDecode.resultDecode("ownerOf", abi, rawReturn0);
        log("调用ownerOf: " + decodeResult0);

        String payloadWithParams1 = FunctionEncode.encodeFunction("tokenURI", ItemId1);
        String payloadWithParams2 = FunctionEncode.encodeFunction("tokenURI", ItemId2);
        String payloadWithParams3 = FunctionEncode.encodeFunction("tokenURI", ItemId3);
        String payloadWithParams4 = FunctionEncode.encodeFunction("tokenURI", ItemId4);
        Transaction transactionWithParams1 = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParams1, false);
        Transaction transactionWithParams2 = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParams2, false);
        Transaction transactionWithParams3 = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParams3, false);
        Transaction transactionWithParams4 = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParams4, false);
        transactionWithParams1.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        transactionWithParams2.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        transactionWithParams3.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        transactionWithParams4.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
        ReceiptReturn receiptReturnWithParams1 = hyperchain.invokeContract(transactionWithParams1);
        ReceiptReturn receiptReturnWithParams2 = hyperchain.invokeContract(transactionWithParams2);
        ReceiptReturn receiptReturnWithParams3 = hyperchain.invokeContract(transactionWithParams3);
        ReceiptReturn receiptReturnWithParams4 = hyperchain.invokeContract(transactionWithParams4);
        String rawReturn1 = receiptReturnWithParams1.getRet();
        String rawReturn2 = receiptReturnWithParams2.getRet();
        String rawReturn3 = receiptReturnWithParams3.getRet();
        String rawReturn4 = receiptReturnWithParams4.getRet();
        String decodeResult1 = FunctionDecode.resultDecode("tokenURI", abi, rawReturn1);
        String decodeResult2 = FunctionDecode.resultDecode("tokenURI", abi, rawReturn2);
        String decodeResult3 = FunctionDecode.resultDecode("tokenURI", abi, rawReturn3);
        String decodeResult4 = FunctionDecode.resultDecode("tokenURI", abi, rawReturn4);
        log("调用tokenURI: " + decodeResult1);
        log("调用tokenURI: " + decodeResult2);
        log("调用tokenURI: " + decodeResult3);
        log("调用tokenURI: " + decodeResult4);





        return "ERC721";
    }
}
