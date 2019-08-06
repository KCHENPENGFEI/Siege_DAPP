package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiegeDeploy {
    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";

    private static void log(String s) {
        System.out.println("Siege ====== " + s);
    }

    /**
     * @description Contract deployment
     * @param
     * @return
     *          contractAddress: The address of contract
     */
    @RequestMapping("/SiegeDeploy")
    public String siegeDeploy() throws Exception {
        HyperchainAPI hyperchain = new HyperchainAPI();
        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);

        String bin = Utils.readFile("contract/build/Siege.bin");
        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false);
        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");

        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
        String contractAddress = receiptReturn.getContractAddress();
        return contractAddress;
    }
}
