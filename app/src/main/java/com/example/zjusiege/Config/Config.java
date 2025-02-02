package com.example.zjusiege.Config;

import cn.hyperchain.sdk.rpc.account.Account;

public class Config {
    // 部署账户
    private static final String deployAccountJson = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    private static Account deployAccount = new Account(deployAccountJson);

    // 游戏合约地址
    private static String contractAddress = "0x096044db6676d6d4eeb375186478da4c6e7b3002";
    // 资产地址
    private static String AssetAddress = "0x326af0f9b6869264d0a56b671d6a02e9d86c0887";

    public static String getDeployAccountJson() {
        return deployAccountJson;
    }

    public static Account getDeployAccount() {
        return deployAccount;
    }

    public static String getContractAddress() {
        return contractAddress;
    }

    public static String getAssetAddress() {
        return AssetAddress;
    }

    public static void setContractAddress(String address) {
        contractAddress = address;
    }

    public static void setAssetAddress(String address) {
        AssetAddress = address;
    }
}
