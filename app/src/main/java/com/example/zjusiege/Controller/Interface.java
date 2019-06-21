package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.account.Account;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Interface {
    private static void log(String s){
        System.out.println("HyperchainAPITest ====== " + s);
    }

    @RequestMapping("InterfaceTest")
    public void InterfaceTest() throws Exception {
        //创建Hyperchain实例
        HyperchainAPI hyperchain = new HyperchainAPI();
        //创建新账户
        //String account = HyperchainAPI.newAccount("wo970301711");
        //ECPriv priv = HyperchainAPI.decryptAccount(account, "wo970301711");

        //创建账户B
        String accounJsonB = HyperchainAPI.newAccountRawSM2();
        log("明文账户: " + accounJsonB);
        String encryptedAccountJsonB = HyperchainAPI.encryptAccountSM2(accounJsonB, "123");
        Account accountB = new Account(accounJsonB);

        String plainAccountJsonB = HyperchainAPI.decryptAccountSM2(accounJsonB, "123");
        Account accountB1 = new Account(plainAccountJsonB);

        //创建账户C
        String accountC = HyperchainAPI.newAccount( "123");
        //创建账户D
        String accountD = HyperchainAPI.newAccountRaw();
    }
}
