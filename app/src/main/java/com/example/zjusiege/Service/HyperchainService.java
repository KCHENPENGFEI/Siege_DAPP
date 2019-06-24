package com.example.zjusiege.Service;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.account.Account;
import net.sf.json.JSONObject;

public class HyperchainService {

//    private HyperchainAPI hyperchain;
//
//    public HyperchainService() throws Exception{
//        this.hyperchain = new HyperchainAPI();
//    }
    public HyperchainService() {

    }

    public String createAccountJson() throws Exception{
        HyperchainAPI hyperchain = new HyperchainAPI();
        return HyperchainAPI.newAccountRawSM2();
    }

    public String validateLogin(JSONObject accountJson) throws Exception{
        HyperchainAPI hyperchain = new HyperchainAPI();
        String accountString = accountJson.toString();
        //暂时不清楚如何进行账户验证
        Account account = new Account(accountString);
        if(true){
            JSONObject result = new JSONObject();
            result.put("loginSuccess", true);
            return result.toString();
        }
        return "";
    }
}
