package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.account.Account;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Siege {
    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    public final String CONTRACT_ADDRESS = "0x98242162bfae11dff6a5132ccf67954981e920ef";

    private static void log(String s) {
        System.out.println("Siege ====== " + s);
    }


    @RequestMapping("start")
    public String start() throws Exception {
        HyperchainAPI hyperchain = new HyperchainAPI();
        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);

//        String abi = Utils.readFile("contract/build/Siege.abi");
//        String paylaod = FunctionEncode.encodeFunction("allStart");
//        Transaction transaction = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, paylaod, false);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        ReceiptReturn receiptReturn = hyperchain.invokeContract(transaction);
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("allStart", abi, rawReturn);
//        log("调用allStart: " + decodeResult);
        return "start";
    }
}
