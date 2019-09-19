package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import com.example.zjusiege.Config.Config;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiegeDeploy {

    private static void log(String s) {
        System.out.println("Siege ====== " + s);
    }

//    private static String bin;

    /**
     * @description Contract deployment
     * @param
     * @return
     *          contractAddress: The address of contract
     */
    @RequestMapping("/SiegeDeploy")
    public String siegeDeploy() throws Exception {
        HyperchainAPI hyperchain = new HyperchainAPI();
        String deployAccountJson = Config.getDeployAccountJson();
        Account deployAccount = Config.getDeployAccount();

        String bin = Utils.readFile("contract/build/Siege.bin");

        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false);
        transaction.signWithSM2(deployAccountJson, "");


        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
        String contractAddress = receiptReturn.getContractAddress();
        Config.setContractAddress(contractAddress);
        return contractAddress;
    }
}
