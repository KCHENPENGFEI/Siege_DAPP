package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.base.VMType;
import cn.hyperchain.sdk.rpc.returns.CompileReturn;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import com.example.zjusiege.Config.Config;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class testContractDeploy {
    private static void log(String s) {
        System.out.println("Siege ====== " + s);
    }

    private static String bin;

    @RequestMapping("/testContractDeploy")
    public String testContractDeploy() throws Exception {
        HyperchainAPI hyperchain = new HyperchainAPI();
        String deployAccountJson = Config.getDeployAccountJson();
        Account deployAccount = Config.getDeployAccount();

//        String bin = Utils.readFile("contract/build/Siege.bin");
        String contract = Utils.readFile("contract/testContract.sol");
        CompileReturn compile = hyperchain.compileContract(contract);
        List<String> listBin = compile.getBin();

        List<String> listAbi = compile.getAbi();

        bin = listBin.get(0);
//        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false);
//        transaction.signWithSM2(deployAccountJson, "");
        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false, VMType.EVM);

        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
        String contractAddress = receiptReturn.getContractAddress();
        Config.setContractAddress(contractAddress);
        return contractAddress;
    }

    public static String getBin() {
        return bin;
    }
}
