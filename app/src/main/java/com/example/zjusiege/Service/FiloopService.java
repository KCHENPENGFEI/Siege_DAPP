package com.example.zjusiege.Service;

import cn.filoop.sdk.client.SDKClient;
import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.account.Algo;
import cn.hyperchain.sdk.common.solidity.Abi;
import cn.hyperchain.sdk.common.utils.ByteUtil;
import cn.hyperchain.sdk.common.utils.FileUtil;
import cn.hyperchain.sdk.common.utils.FuncParams;
import cn.hyperchain.sdk.response.ReceiptResponse;
import cn.hyperchain.sdk.service.AccountService;
import cn.hyperchain.sdk.service.ServiceManager;
import cn.hyperchain.sdk.transaction.Transaction;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Utils.Utils;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FiloopService {
    private static void log(String s) {
        System.out.println("Siege ====== " + s);
    }
    private static SDKClient sdkClientSiegeMain = Config.getSdkClientSiegeMain();
    private static SDKClient sdkClientSiegeAsset = Config.getSdkClientSiegeAsset();
    private static String deployAccountJson = Config.getDeployAccountJson();
    private static Account deployAccount = Config.getDeployAccount();
    private static String assetDeployAccountJson = Config.getAssetDeployAccountJson();
    private static Account assetDeployAccount = Config.getAssetDeployAccount();
    private static String contractAddress = Config.getContractAddress();
    private static String assetAddress = Config.getAssetAddress();

    private Abi SiegeMainAbi;
//    private String siegeBin;
    private Abi SiegeAssetAbi;
//    private String SiegeAssetBin;

    {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("contract/build/Siege_0.4.15/Siege.abi");
            String SiegeMainAbiStr = FileUtil.readFile(inputStream);
            SiegeMainAbi = Abi.fromJson(SiegeMainAbiStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("contract/build/Siege_0.4.15/Siege.abi");
            String SiegeAssetAbiStr = FileUtil.readFile(inputStream);
            SiegeAssetAbi = Abi.fromJson(SiegeAssetAbiStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*************************************************  SiegeMain function invoke ******************************************/
    public String register() {
        // 生成新账户
        AccountService accountService = ServiceManager.getAccountService(sdkClientSiegeMain.getProviderManager());
        Account newAccount = accountService.genAccount(Algo.SMRAW);
        JSONObject newAccountUppercase = new JSONObject()
                .element("address", newAccount.getAddress().toUpperCase())
                .element("publicKey", newAccount.getPublicKey().toUpperCase())
                .element("privateKey", newAccount.getPrivateKey().toUpperCase())
                .element("version", newAccount.getVersion())
                .element("algo", newAccount.getAlgo());
        String address = newAccount.getAddress().toUpperCase();
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(address);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "register(address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用register, code: " + code);
            return newAccountUppercase.toString();
        } catch (Exception e) {
            log("调用register失败");
            return "contract calling error";
        }
    }

    public String login(String accountString) {
        AccountService accountService = ServiceManager.getAccountService(sdkClientSiegeMain.getProviderManager());
        Account account = accountService.fromAccountJson(accountString);
        String address = account.getAddress();
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(address);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "login(address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("login(address)").decodeResult(bytes);
            log("调用login, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用login失败!");
            return "contract calling error";
        }
    }

    public String updateGameStage(int gameId, int stage) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(stage);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "updateGameStage(uint256,uint8)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用updateGameStage, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用updateGameStage失败!");
            return "contract calling error";
        }
    }

    public String setRemain(int gameId, int num) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(num);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setRemain(uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setRemain, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setRemain失败!");
            return "contract calling error";
        }
    }

    public String updateCityBonus(int gameId, Long leftIntervalNum) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(leftIntervalNum);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "updateCityBonus(uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("updateCityBonus(uint256,uint256)").decodeResult(bytes);
            log("调用updateCityBonus, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用updateCityBonus失败!");
            return "contract calling error";
        }
    }

    public String freezePlayer(List<String> playerAddresses, List<Integer> rank, List<Long> time) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(playerAddresses);
        params.addParams(rank);
        params.addParams(time);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "freezePlayer(address[],uint256[],uint256[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用freezePlayer, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用freezePlayer失败!");
            return "contract calling error";
        }
    }

    public String startGame(String[] playersAddresses) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(playersAddresses);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "startGame(address[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("startGame(address[])").decodeResult(bytes);
            log("调用startGame, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用startGame失败!");
            return "contract calling error";
        }
    }

    public String updateRankingTb(int gameId, List<Integer> ranking, List<String> playerAddresses, List<Long> price, List<Long> time) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(ranking);
        params.addParams(playerAddresses);
        params.addParams(price);
        params.addParams(time);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "updateRankingTb(uint256,uint256[],address[],uint256[],uint256[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用updateRankingTb, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用updateRankingTb失败!");
            return "contract calling error";
        }
    }

    public String allocateCity(int gameId, List<String> playerAddresses, List<Integer> cityIds, List<Long> price) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddresses);
        params.addParams(cityIds);
        params.addParams(price);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "allocateCity(uint256,address[],uint256[],uint256[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用allocateCity, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用allocateCity失败!");
            return "contract calling error";
        }
    }

    public String occupyCity(int gameId, String playerAddress, int cityId, long amount, String signAccountJson) {
        AccountService accountService = ServiceManager.getAccountService(sdkClientSiegeMain.getProviderManager());
        Account signAccount = accountService.fromAccountJson(signAccountJson);
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddress);
        params.addParams(cityId);
        params.addParams(amount);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(signAccount.getAddress()).invoke(contractAddress, "occupyCity(uint256,address,uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(signAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用occupyCity, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用occupyCity失败!");
            return "contract calling error";
        }
    }

    public String leaveCity(int gameId, String playerAddress) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddress);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "leaveCity(uint256,address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用leaveCity, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用leaveCity失败!");
            return "contract calling error";
        }
    }

    public String attack(int gameId, String attackerAddress, String defenderAddress) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(attackerAddress);
        params.addParams(defenderAddress);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "attack(uint256,address,address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用attack, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用attack失败!");
            return "contract calling error";
        }
    }

    public String defense(int gameId, String defenderAddress, String attackerAddress, int cityId, int choice) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(defenderAddress);
        params.addParams(attackerAddress);
        params.addParams(cityId);
        params.addParams(choice);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "defense(uint256,address,address,uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用defense, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用defense失败!");
            return "contract calling error";
        }
    }

    public String buySoldiers(int gameId, String playerAddress, long amount, List<Integer> soldiersBought, long point, int quantity) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddress);
        params.addParams(amount);
        params.addParams(soldiersBought);
        params.addParams(point);
        params.addParams(quantity);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "buySoldiers(uint256,address,uint256,uint256[],uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用buySoldiers, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用buySoldiers失败!");
            return "contract calling error";
        }
    }

    public String departure(int gameId, String playerAddress) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddress);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "departure(uint256,address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用departure, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用departure失败!");
            return "contract calling error";
        }
    }

    public String pickAndBattle(int gameId, String attackerAddress, String defenderAddress, int aType, int dType) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(attackerAddress);
        params.addParams(defenderAddress);
        params.addParams(aType);
        params.addParams(dType);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "pickAndBattle(uint256,address,address,uint8,uint8)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("pickAndBattle(uint256,address,address,uint8,uint8)").decodeResult(bytes);
            log("调用pickAndBattle, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用pickAndBattle失败!");
            return "contract calling error";
        }
    }

    public String battleEnd(int gameId, String attackerAddress, String defenderAddress, int cityId) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(attackerAddress);
        params.addParams(defenderAddress);
        params.addParams(cityId);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "battleEnd(uint256,address,address,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("battleEnd(uint256,address,address,uint256)").decodeResult(bytes);
            log("调用battleEnd, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用battleEnd失败!");
            return "contract calling error";
        }
    }

    public String settlement(int gameId, List<String> playerAddresses) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddresses);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "settlement(uint256,address[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用settlement, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用settlement失败!");
            return "contract calling error";
        }
    }

    public String endGame(int gameId, List<String> playerAddresses) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(playerAddresses);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "endGame(uint256,address[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用endGame, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用endGame失败!");
            return "contract calling error";
        }
    }

    public String getPlayersStatus(String playerAddress) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(playerAddress);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getPlayersStatus(address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getPlayersStatus(address)").decodeResult(bytes);
            log("调用getPlayersStatus, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getPlayersStatus失败!");
            return "contract calling error";
        }
    }

    public String getCitiesTb(long gameId, long cityId) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(cityId);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getCitiesTb(uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getCitiesTb(uint256,uint256)").decodeResult(bytes);
            log("调用getCitiesTb, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getCitiesTb失败!");
            return "contract calling error";
        }
    }

    public String getBiddingTb(int gameId, int rankId) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        params.addParams(rankId);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getBiddingTb(uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getBiddingTb(uint256,uint256)").decodeResult(bytes);
            log("调用getBiddingTb, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getBiddingTb失败!");
            return "contract calling error";
        }
    }

    public String getGlobalTb(int gameId) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getGlobalTb(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getGlobalTb(uint256)").decodeResult(bytes);
            log("调用getGlobalTb, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getGlobalTb失败!");
            return "contract calling error";
        }
    }

    public String getFrozenTb(String playerAddress) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(playerAddress);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getFrozenTb(address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getFrozenTb(address)").decodeResult(bytes);
            log("调用getFrozenTb, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getFrozenTb失败!");
            return "contract calling error";
        }
    }

    public String getStage(int gameId) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getStage(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getStage(uint256)").decodeResult(bytes);
            log("调用getStage, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getStage失败!");
            return "contract calling error";
        }
    }

    public String getGameData(String playerAddress, int pointer) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(playerAddress);
        params.addParams(pointer);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getGameData(address,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getGameData(address,uint256)").decodeResult(bytes);
            log("调用getGameData, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getGameData失败!");
            return "contract calling error";
        }
    }

    public String cp(String a) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(a);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "cp(address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用cp, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用cp失败!");
            return "contract calling error";
        }
    }

    public String cc(int gameId) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(gameId);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "cc(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用cc, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用cc失败!");
            return "contract calling error";
        }
    }

    public String getCity() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getCityName()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getCityName()").decodeResult(bytes);
            System.out.println("object: " + objects);
            log("调用getCityName, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getCityName失败!");
            return "contract calling error";
        }
    }

    public String getSo() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getSo()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getSo()").decodeResult(bytes);
            log("调用getSo, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getSo失败!");
            return "contract calling error";
        }
    }

    public String getDe() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getDe()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getDe()").decodeResult(bytes);
            log("调用getDe, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getDe失败!");
            return "contract calling error";
        }
    }

    public String getPrecision() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getPrecision()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getPrecision()").decodeResult(bytes);
            log("调用getPrecision, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getPrecision失败!");
            return "contract calling error";
        }
    }

    public String getCityNum() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getCityNum()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getCityNum()").decodeResult(bytes);
            log("调用getCityNum, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getCityNum失败!");
            return "contract calling error";
        }
    }

    public String getEnterFee() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getEnterFee()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getEnterFee()").decodeResult(bytes);
            log("调用getEnterFee, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getEnterFee失败!");
            return "contract calling error";
        }
    }

    public String getCityPrice() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getCityPrice()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getCityPrice()").decodeResult(bytes);
            log("调用getCityPrice, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getCityPrice失败!");
            return "contract calling error";
        }
    }

    public String getSoldierNum() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getSoldierNum()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getSoldierNum()").decodeResult(bytes);
            log("调用getSoldierNum, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getSoldierNum失败!");
            return "contract calling error";
        }
    }

    public String getInterval() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getInterval()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getInterval()").decodeResult(bytes);
            log("调用getInterval, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getInterval失败!");
            return "contract calling error";
        }
    }

    public String getGameAssetAddr() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getGameAssetAddr()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getGameAssetAddr()").decodeResult(bytes);
            log("调用getGameAssetAddr, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getGameAssetAddr失败!");
            return "contract calling error";
        }
    }

    public String getRoot() {
        // 构造交易参数
        FuncParams params = new FuncParams();
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "getRoot()", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeMainAbi.getFunction("getRoot()").decodeResult(bytes);
            log("调用getRoot, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用getRoot失败!");
            return "contract calling error";
        }
    }

    /*************************************************  Siege Params Configuration *****************************************/
    public String setAssetAddr(String address) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(address);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setAssetAddr(address)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setAssetAddr, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setAssetAddr失败!");
            return "contract calling error";
        }
    }

    public String setPrecision(int precision) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(precision);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setPrecision(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setPrecision, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setPrecision失败!");
            return "contract calling error";
        }
    }

    public String setCityNum(int cityNum) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(cityNum);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setCityNum(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setCityNum, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setCityNum失败!");
            return "contract calling error";
        }
    }

    public String setEnterFee(int enterFee) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(enterFee);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setEnterFee(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setEnterFee, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setEnterFee失败!");
            return "contract calling error";
        }
    }

    public String setCityPrice(int cityPrice) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(cityPrice);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setCityPrice(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setCityPrice, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setCityPrice失败!");
            return "contract calling error";
        }
    }

    public String setSoldierNum(int soldierNum) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(soldierNum);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setSoldierNum(uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setSoldierNum, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setSoldierNum失败!");
            return "contract calling error";
        }
    }

    public String setTime(int interval, int duration) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(interval);
        params.addParams(duration);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setTime(uint256,uint256)", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setTime, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setTime失败!");
            return "contract calling error";
        }
    }

    public String setSoldiersPoint(List<Integer> soldiersPointList) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(soldiersPointList);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setSoldiersPoint(uint256[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setSoldiersPoint, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setSoldiersPoint失败!");
            return "contract calling error";
        }
    }

    public String setCityName(List<byte[]> cityNameList) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(cityNameList);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setCityName(bytes32[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setCityName, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setCityName失败!");
            return "contract calling error";
        }
    }

    public String setCityDefenseIndex(List<Integer> cityDefenseIndexList) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(cityDefenseIndexList);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(deployAccount.getAddress()).invoke(contractAddress, "setCityDefenseIndex(uint256[])", SiegeMainAbi, params).build();
        // 签名
        transaction.sign(deployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeMain.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用setCityDefenseIndex, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用setCityDefenseIndex失败!");
            return "contract calling error";
        }
    }

    /***********************************************************  SiegeAsset ****************************************************/
    public String create(String issuer, long value, String symbol, int type) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(issuer);
        params.addParams(value);
        params.addParams(symbol);
        params.addParams(type);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(assetDeployAccount.getAddress()).invoke(assetAddress, "create(address,uint256,string,uint8)", SiegeAssetAbi, params).build();
        // 签名
        transaction.sign(assetDeployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeAsset.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用create, code: " + code);
            return "success";
        } catch (Exception e) {
            log("调用create失败!");
            return "contract calling error";
        }
    }

    public String issueCoin(String to, long value, String symbol, String issuerAccountString) {
        AccountService accountService = ServiceManager.getAccountService(sdkClientSiegeAsset.getProviderManager());
        Account issueAccount = accountService.fromAccountJson(issuerAccountString);
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(to);
        params.addParams(value);
        params.addParams(symbol);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(issueAccount.getAddress()).invoke(assetAddress, "issueCoin(address,uint256,string)", SiegeAssetAbi, params).build();
        // 签名
        transaction.sign(issueAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeAsset.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用issueCoin, code: " + code);
            return "issue success";
        } catch (Exception e) {
            log("调用issueCoin失败!");
            return "contract calling error";
        }
    }

    public String transfer(String from, String to, long value, String symbol, String data, String signAccountString) {
        AccountService accountService = ServiceManager.getAccountService(sdkClientSiegeAsset.getProviderManager());
        Account signAccount = accountService.fromAccountJson(signAccountString);
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(from);
        params.addParams(to);
        params.addParams(value);
        params.addParams(symbol);
        params.addParams(data);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(signAccount.getAddress()).invoke(assetAddress, "transfer(address,address,uint256,string,bytes)", SiegeAssetAbi, params).build();
        // 签名
        transaction.sign(signAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeAsset.invoke(transaction);
            int code = receiptResponse.getCode();
            log("调用transfer, code: " + code);
            return "transfer success";
        } catch (Exception e) {
            log("调用transfer失败!");
            return "contract calling error";
        }
    }

    public String supplyOf(String symbol, int ext) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(symbol);
        params.addParams(ext);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(assetDeployAccount.getAddress()).invoke(assetAddress, "supplyOf(string,uint8)", SiegeAssetAbi, params).build();
        // 签名
        transaction.sign(assetDeployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeAsset.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeAssetAbi.getFunction("supplyOf(string,uint8)").decodeResult(bytes);
            log("调用supplyOf, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用supplyOf失败!");
            return "contract calling error";
        }
    }

    public String balanceOf(String owner, String symbol) {
        // 构造交易参数
        FuncParams params = new FuncParams();
        params.addParams(owner);
        params.addParams(symbol);
        // 构造交易
        Transaction transaction = new Transaction.EVMBuilder(assetDeployAccount.getAddress()).invoke(assetAddress, "balanceOf(address,string)", SiegeAssetAbi, params).build();
        // 签名
        transaction.sign(assetDeployAccount);
        // 通过sdkClient调用合约
        try {
            ReceiptResponse receiptResponse = sdkClientSiegeAsset.invoke(transaction);
            int code = receiptResponse.getCode();
            byte[] bytes = ByteUtil.fromHex(receiptResponse.getRet());
            List<?> objects = SiegeAssetAbi.getFunction("balanceOf(address,string)").decodeResult(bytes);
            log("调用balanceOf, code: " + code);
            return Utils.decodeResultTransform(objects);
        } catch (Exception e) {
            log("调用balanceOf失败!");
            return "contract calling error";
        }
    }
}
