package com.example.zjusiege.Config;

import cn.filoop.sdk.client.SDKClient;
import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.service.AccountService;
import cn.hyperchain.sdk.service.ServiceManager;

public class Config {
    // SDK客户端
    private static SDKClient sdkClientSiegeMain = new SDKClient();
    private static SDKClient sdkClientSiegeAsset = new SDKClient();

    // SiegeMain部署账户
//    private final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    private static String deployAccountJson = "{\"address\":\"0x7e60981445116b9f3938fb8097e217340192de25\",\"algo\":\"0x03\",\"encrypted\":\"05701baf54dce7bc9eb221fe7651b7b3e959aec17fe0812d338f9aa20beb476e\",\"version\":\"2.0\",\"privateKeyEncrypted\":false}";
    private static AccountService accountService = ServiceManager.getAccountService(sdkClientSiegeMain.getProviderManager());
    private static Account deployAccount = accountService.fromAccountJson(deployAccountJson);

    // SiegeAsset部署账户
    private static final String assetDeployAccountJson = "{\"address\":\"0x7e60981445116b9f3938fb8097e217340192de25\",\"algo\":\"0x03\",\"encrypted\":\"05701baf54dce7bc9eb221fe7651b7b3e959aec17fe0812d338f9aa20beb476e\",\"version\":\"2.0\",\"privateKeyEncrypted\":false}";
    private static AccountService accountService1 = ServiceManager.getAccountService(sdkClientSiegeAsset.getProviderManager());
    private static Account assetDeployAccount = accountService1.fromAccountJson(assetDeployAccountJson);
    static {
        System.out.println("==============================SDK初始化==============================\n");
        // SiegeMain配置
        sdkClientSiegeMain.setAppKey("T6QNqmZEUrxXC1az3mZx");
        sdkClientSiegeMain.setAppSecret("MAKuoWV5mtbv9DX7JV37icCW7d5I7w");
        sdkClientSiegeMain.setUuid("aa80c45d-ecbe-11e9-8005-000000000000");
        sdkClientSiegeMain.init();
        // SiegeAsset配置
        sdkClientSiegeAsset.setAppKey("T6QNqmZEUrxXC1az3mZx");
        sdkClientSiegeAsset.setAppSecret("MAKuoWV5mtbv9DX7JV37icCW7d5I7w");
        sdkClientSiegeAsset.setUuid("761e155b-f57a-11e9-8068-000000000000");
        sdkClientSiegeAsset.init();
    }

    // 部署账户
//    private static final String deployAccountJson = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
//    private static  deployAccount = new Account(deployAccountJson);

    // 游戏合约地址
//    private static String contractAddress = "0x096044db6676d6d4eeb375186478da4c6e7b3002";
    // 资产地址
//    private static String AssetAddress = "0x326af0f9b6869264d0a56b671d6a02e9d86c0887";

    // 游戏合约地址
    private static String contractAddress = "0x465fca46ae4fca06c138e4fbf92d41616a1d9ce2";
    // 资产地址
    private static String AssetAddress = "0xb14b5024904c7cca2d674fa1964aefa6fd3f7a64";

    public static SDKClient getSdkClientSiegeMain() {
        return sdkClientSiegeMain;
    }

    public static SDKClient getSdkClientSiegeAsset() {
        return sdkClientSiegeAsset;
    }

    public static String getDeployAccountJson() {
        return deployAccountJson;
    }

    public static Account getDeployAccount() {
        return deployAccount;
    }

    public static String getAssetDeployAccountJson() {
        return assetDeployAccountJson;
    }

    public static Account getAssetDeployAccount() {
        return assetDeployAccount;
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
