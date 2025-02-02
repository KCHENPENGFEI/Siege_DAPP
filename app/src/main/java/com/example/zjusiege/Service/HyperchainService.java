package com.example.zjusiege.Service;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.function.FuncParamReal;
import cn.hyperchain.sdk.rpc.function.FunctionDecode;
import cn.hyperchain.sdk.rpc.function.FunctionEncode;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import com.example.zjusiege.Config.Config;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class HyperchainService {
    private static String deployAccountJson = Config.getDeployAccountJson();
    private static Account deployAccount = Config.getDeployAccount();
    private static String contractAddress = Config.getContractAddress();
    private static String assetAddress = Config.getAssetAddress();

    private String siegeAbi;
    private String siegeBin;
    private String SiegeAssetAbi;
    private String SiegeAssetBin;

    {
        try {
            siegeAbi = Utils.readFile("contract/build/Siege.abi");
            siegeBin = Utils.readFile("contract/build/Siege.bin");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    {
        try {
            SiegeAssetAbi = Utils.readFile("contract/build/SiegeAsset.abi");
            SiegeAssetBin = Utils.readFile("contract/build/SiegeAsset.bin");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void log(String s) {
        System.out.println("Siege ====== " + s);
    }

    public HyperchainService() {

    }

    public String register() throws Exception{
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String accountJson = HyperchainAPI.newAccountRawSM2();
        String address = JSONObject.fromObject(accountJson).getString("address");
        FuncParamReal _address = new FuncParamReal("address", address);
        String payloadWithParam = FunctionEncode.encodeFunction("register", _address);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用register, code: " + code);

        if (code == 0) {
            return accountJson;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String login(String accountString) throws Exception{
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account account = new Account(accountString);

        FuncParamReal _address = new FuncParamReal("address", account.getAddress());
        String payloadWithParam = FunctionEncode.encodeFunction("login", _address);
        Transaction transaction = new Transaction(account.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(accountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("login", siegeAbi, rawReturn);
        log("调用login: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String updateGameStage(int gameId, int stage) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _stage = new FuncParamReal("uint8", stage);
        String payloadWithParam = FunctionEncode.encodeFunction("updateGameStage", _gameId, _stage);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用updateGameStage, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setRemain(int gameId, int num) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _num = new FuncParamReal("uint256", num);
        String payloadWithParam = FunctionEncode.encodeFunction("setRemain", _gameId, _num);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setRemain, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String updateCityBonus(int gameId, Long leftIntervalNum) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _leftIntervalNum = new FuncParamReal("uint256", leftIntervalNum);
        String payloadWithParam = FunctionEncode.encodeFunction("updateCityBonus", _gameId, _leftIntervalNum);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("updateCityBonus", siegeAbi, rawReturn);
        log("调用updateCityBonus: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String freezePlayer(List<String> playerAddresses, List<Integer> rank, List<Long> time) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _playerAddresses = new FuncParamReal("address[]", playerAddresses);
        FuncParamReal _rank = new FuncParamReal("uint256[]", rank);
        FuncParamReal _time = new FuncParamReal("uint256[]", time);
        String payloadWithParam = FunctionEncode.encodeFunction("freezePlayer", _playerAddresses, _rank, _time);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用freezePlayer, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String startGame(String[] playersAddresses) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _playersAddresses = new FuncParamReal("address[]", playersAddresses);
        String payloadWithParam = FunctionEncode.encodeFunction("startGame", _playersAddresses);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("startGame", siegeAbi, rawReturn);
        log("调用startGame: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String updateRankingTb(int gameId, List<Integer> ranking, List<String> playerAddresses, List<Long> price, List<Long> time) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _ranking = new FuncParamReal("uint256[]", ranking);
        FuncParamReal _playersAddresses = new FuncParamReal("address[]", playerAddresses);
        FuncParamReal _price = new FuncParamReal("uint256[]", price);
        FuncParamReal _time = new FuncParamReal("uint256[]", time);
        String payloadWithParam = FunctionEncode.encodeFunction("updateRankingTb", _gameId, _ranking, _playersAddresses, _price, _time);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用startGame, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String allocateCity(int gameId, List<String> playerAddresses, List<Integer> cityIds, List<Long> price) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddresses = new FuncParamReal("address[]", playerAddresses);
        FuncParamReal _cityIds = new FuncParamReal("uint256[]", cityIds);
        FuncParamReal _price = new FuncParamReal("uint256[]", price);
        String payloadWithParam = FunctionEncode.encodeFunction("allocateCity", _gameId, _playerAddresses, _cityIds, _price);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用allocateCity, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String occupyCity(int gameId, String playerAddress, int cityId, long amount, String signAccountJson) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountJson);

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        FuncParamReal _cityId = new FuncParamReal("uint256", cityId);
        FuncParamReal _amount = new FuncParamReal("uint256", amount);
        String payloadWithParam = FunctionEncode.encodeFunction("occupyCity", _gameId, _playerAddress, _cityId, _amount);
        Transaction transaction = new Transaction(signAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(signAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用occupyCity, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String leaveCity(int gameId, String playerAddress) throws  Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        String payloadWithParam = FunctionEncode.encodeFunction("leaveCity", _gameId, _playerAddress);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用leaveCity, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String attack(int gameId, String attackerAddress, String defenderAddress) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _attackerAddress = new FuncParamReal("address", attackerAddress);
        FuncParamReal _defenderAddress = new FuncParamReal("address", defenderAddress);
        String payloadWithParam = FunctionEncode.encodeFunction("attack", _gameId, _attackerAddress, _defenderAddress);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用attack, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String defense(int gameId, String defenderAddress, String attackerAddress, int cityId, int choice) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _defenderAddress = new FuncParamReal("address", defenderAddress);
        FuncParamReal _attackerAddress = new FuncParamReal("address", attackerAddress);
        FuncParamReal _cityId = new FuncParamReal("uint256", cityId);
        FuncParamReal _choice = new FuncParamReal("uint256", choice);
        String payloadWithParam = FunctionEncode.encodeFunction("defense", _gameId, _defenderAddress, _attackerAddress, _cityId, _choice);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用defense, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String buySoldiers(int gameId, String playerAddress, long amount, List<Integer> soldiersBought, long point, int quantity) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        FuncParamReal _amount = new FuncParamReal("uint256", amount);
        FuncParamReal _soldiersBought = new FuncParamReal("uint256[]", soldiersBought);
        FuncParamReal _point = new FuncParamReal("uint256", point);
        FuncParamReal _quantity = new FuncParamReal("uint256", quantity);
        String payloadWithParam = FunctionEncode.encodeFunction("buySoldiers", _gameId, _playerAddress, _amount, _soldiersBought, _point, _quantity);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用buySoldiers, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String departure(int gameId, String playerAddress) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        String payloadWithParam = FunctionEncode.encodeFunction("departure", _gameId, _playerAddress);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用departure, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String pickAndBattle(int gameId, String attackerAddress, String defenderAddress, int aType, int dType) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _attackerAddress = new FuncParamReal("address", attackerAddress);
        FuncParamReal _defenderAddress = new FuncParamReal("address", defenderAddress);
        FuncParamReal _aType = new FuncParamReal("uint8", aType);
        FuncParamReal _dType = new FuncParamReal("uint8", dType);
        String payloadWithParam = FunctionEncode.encodeFunction("pickAndBattle", _gameId, _attackerAddress, _defenderAddress, _aType, _dType);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("pickAndBattle", siegeAbi, rawReturn);
        log("调用pickAndBattle: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String battleEnd(int gameId, String attackerAddress, String defenderAddress, int cityId) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _attackerAddress = new FuncParamReal("address", attackerAddress);
        FuncParamReal _defenderAddress = new FuncParamReal("address", defenderAddress);
        FuncParamReal _cityId = new FuncParamReal("uint256", cityId);
        String payloadWithParam = FunctionEncode.encodeFunction("battleEnd", _gameId, _attackerAddress, _defenderAddress, _cityId);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("battleEnd", siegeAbi, rawReturn);
        log("调用battleEnd: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String settlement(int gameId, List<String> playerAddresses) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddresses = new FuncParamReal("address[]", playerAddresses);
        String payloadWithParam = FunctionEncode.encodeFunction("settlement", _gameId, _playerAddresses);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用settlement, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String endGame(int gameId, List<String> playerAddresses) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _playerAddresses = new FuncParamReal("address[]", playerAddresses);
        String payloadWithParam = FunctionEncode.encodeFunction("endGame", _gameId, _playerAddresses);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用endGame, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getPlayersStatus(String playerAddress) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        String payloadWithParam = FunctionEncode.encodeFunction("getPlayersStatus", _playerAddress);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getPlayersStatus", siegeAbi, rawReturn);
        log("调用getPlayersStatus: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getCitiesTable(long gameId, long cityId) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _cityId = new FuncParamReal("uint256", cityId);
        String payloadWithParam = FunctionEncode.encodeFunction("getCitiesTb", _gameId, _cityId);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getCitiesTb", siegeAbi, rawReturn);
        log("调用getCitiesTb: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getBiddingTb(int gameId, int rankId) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        FuncParamReal _rankId = new FuncParamReal("uint256", rankId);
        String payloadWithParam = FunctionEncode.encodeFunction("getBiddingTb", _gameId, _rankId);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getBiddingTb", siegeAbi, rawReturn);
        log("调用getBiddingTb: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getGlobalTb(int gameId) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        String payloadWithParam = FunctionEncode.encodeFunction("getGlobalTb", _gameId);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getGlobalTb", siegeAbi, rawReturn);
        log("调用getGlobalTb: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getFrozenTb(String playerAddress) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        String payloadWithParam = FunctionEncode.encodeFunction("getFrozenTb", _playerAddress);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getFrozenTb", siegeAbi, rawReturn);
        log("调用getFrozenTb: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getStage(int gameId) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        String payloadWithParam = FunctionEncode.encodeFunction("getStage", _gameId);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getStage", siegeAbi, rawReturn);
        log("调用getStage: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getGameData(String playerAddress, int pointer) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _playerAddress = new FuncParamReal("address", playerAddress);
        FuncParamReal _pointer = new FuncParamReal("uint256", pointer);
        String payloadWithParam = FunctionEncode.encodeFunction("getGameData", _playerAddress, _pointer);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getGameData", siegeAbi, rawReturn);
        log("调用getGameData: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String cp(String a) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _a = new FuncParamReal("address", a);
        String payloadWithParam = FunctionEncode.encodeFunction("cp", _a);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用cp, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String cc(int gameId) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _gameId = new FuncParamReal("uint256", gameId);
        String payloadWithParam = FunctionEncode.encodeFunction("cc", _gameId);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用cc, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getCity() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getCityName");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getCityName", siegeAbi, rawReturn);
        log("调用getCityName: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getSo() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getSo");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getSo", siegeAbi, rawReturn);
        log("调用getSo: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getDe() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getDe");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getDe", siegeAbi, rawReturn);
        log("调用getDe: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getPrecision() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getPrecision");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getPrecision", siegeAbi, rawReturn);
        log("调用getPrecision: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getCityNum() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getCityNum");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getCityNum", siegeAbi, rawReturn);
        log("调用getCityNum: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getEnterFee() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getEnterFee");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getEnterFee", siegeAbi, rawReturn);
        log("调用getEnterFee: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getCityPrice() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getCityPrice");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getCityPrice", siegeAbi, rawReturn);
        log("调用getCityPrice: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getSoldierNum() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getSoldierNum");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getSoldierNum", siegeAbi, rawReturn);
        log("调用getSoldierNum: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getInterval() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParam = FunctionEncode.encodeFunction("getInterval");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParam, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getInterval", siegeAbi, rawReturn);
        log("调用getInterval: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getGameAssetAddr() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParams = FunctionEncode.encodeFunction("getGameAssetAddr");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getGameAssetAddr", siegeAbi, rawReturn);
        log("调用getGameAssetAddr: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getRoot() throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        String payloadWithParams = FunctionEncode.encodeFunction("getRoot");
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getRoot", siegeAbi, rawReturn);
        log("调用getRoot: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    /*********************************************  Siege Params Configuration **************************************/
    public String setAssetAddr(String addr) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _addr = new FuncParamReal("address", addr);
        String payloadWithParams = FunctionEncode.encodeFunction("setAssetAddr", _addr);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setAssetAddr, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setPrecision(int precision) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _precision = new FuncParamReal("uint256", precision);
        String payloadWithParams = FunctionEncode.encodeFunction("setPrecision", _precision);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setPrecision, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setCityNum(int cityNum) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _cityNum = new FuncParamReal("uint256", cityNum);
        String payloadWithParams = FunctionEncode.encodeFunction("setCityNum", _cityNum);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);

        int code = receiptReturn.getRawcode();
        log("调用setCityNum, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setEnterFee(int enterFee) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _enterFee = new FuncParamReal("uint256", enterFee);
        String payloadWithParams = FunctionEncode.encodeFunction("setEnterFee", _enterFee);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setEnterFee, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setCityPrice(int cityPrice) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _cityPrice = new FuncParamReal("uint256", cityPrice);
        String payloadWithParams = FunctionEncode.encodeFunction("setCityPrice", _cityPrice);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setCityPrice, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setSoldierNum(int soldierNum) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _soldierNum = new FuncParamReal("uint256", soldierNum);
        String payloadWithParams = FunctionEncode.encodeFunction("setSoldierNum", _soldierNum);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setSoldierNum, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setTime(int interval, int duration) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _interval = new FuncParamReal("uint256", interval);
        FuncParamReal _duration = new FuncParamReal("uint256", duration);
        String payloadWithParams = FunctionEncode.encodeFunction("setTime", _interval, _duration);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setTime, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public  String setSoldiersPoint(List<Integer> soldiersPointList) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _soldiersPointList = new FuncParamReal("uint256[]", soldiersPointList);
        String payloadWithParams = FunctionEncode.encodeFunction("setSoldiersPoint", _soldiersPointList);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setSoldiersPoint, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }

    }

    public String setCityName(List<byte[]> cityNameList) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _cityNameList = new FuncParamReal("bytes32[]", cityNameList);
        String payloadWithParams = FunctionEncode.encodeFunction("setCityName", _cityNameList);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setCityName, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String setCityDefenseIndex(List<Integer> cityDefenseIndexList) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _cityDefenseIndexList = new FuncParamReal("uint256[]", cityDefenseIndexList);
        String payloadWithParams = FunctionEncode.encodeFunction("setCityDefenseIndex", _cityDefenseIndexList);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用setCityDefenseIndex, code: " + code);

        if (code == 0) {
            return "success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    /******************************************************  SiegeAsset ***********************************************/
    public String create(String issuer, long value, String symbol, int type) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _issuer = new FuncParamReal("address", issuer);
        FuncParamReal _value = new FuncParamReal("uint256", value);
        FuncParamReal _symbol = new FuncParamReal("string", symbol);
        FuncParamReal _type = new FuncParamReal("uint8", type);
        String payloadWithParams = FunctionEncode.encodeFunction("create", _issuer, _value, _symbol, _type);
        Transaction transaction = new Transaction(deployAccount.getAddress(), assetAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用create, code: " + code);

        if (code == 0) {
            return "create " + symbol + " success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String issueCoin(String to, long value, String symbol, String issuerAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account issuerAccount = new Account(issuerAccountString);

        FuncParamReal _to = new FuncParamReal("address", to);
        FuncParamReal _value = new FuncParamReal("uint256", value);
        FuncParamReal _symbol = new FuncParamReal("string", symbol);
        String payloadWithParams = FunctionEncode.encodeFunction("issueCoin", _to, _value,_symbol);
        Transaction transaction = new Transaction(issuerAccount.getAddress(), assetAddress, payloadWithParams, false);
        transaction.signWithSM2(issuerAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用issue, code: " + code);

        if (code == 0) {
            System.out.println("code: " + code);
            return "issue success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String transfer(String from, String to, long value, String symbol, String data, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _from = new FuncParamReal("address", from);
        FuncParamReal _to = new FuncParamReal("address", to);
        FuncParamReal _value = new FuncParamReal("uint256", value);
        FuncParamReal _symbol = new FuncParamReal("string", symbol);
        FuncParamReal _data = new FuncParamReal("bytes", data.getBytes());
        String payloadWithParams = FunctionEncode.encodeFunction("transfer", _from, _to,  _value, _symbol, _data);
        Transaction transaction = new Transaction(signAccount.getAddress(), assetAddress, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        log("调用transfer, code: " + code);

        if (code == 0) {
            return "transfer success";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String supplyOf(String symbol, int ext) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _symbol = new FuncParamReal("string", symbol);
        FuncParamReal _ext = new FuncParamReal("uint8", ext);
        String payloadWithParams = FunctionEncode.encodeFunction("supplyOf", _symbol, _ext);
        Transaction transaction = new Transaction(deployAccount.getAddress(), assetAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("supplyOf", SiegeAssetAbi, rawReturn);
        log("调用supplyOf: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String balanceOf(String owner, String symbol) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _owner = new FuncParamReal("address", owner);
        FuncParamReal _symbol = new FuncParamReal("string", symbol);
        String payloadWithParams = FunctionEncode.encodeFunction("balanceOf", _owner, _symbol);
        Transaction transaction = new Transaction(deployAccount.getAddress(), assetAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("balanceOf", SiegeAssetAbi, rawReturn);
        log("调用balanceOf: " + decodeResult);

        if (code == 0) {
            return decodeResult;
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

//    public String balanceOfBatch(List<String> owners, List<Long> ids, String signAccountString) throws Exception {
//        HyperchainAPI hyperchainAPI = new HyperchainAPI();
//
//        Account signAccount = new Account(signAccountString);
//
//        FuncParamReal _owners = new FuncParamReal("address[]", owners);
//        FuncParamReal _ids = new FuncParamReal("uint256[]", ids);
//        String payloadWithParams = FunctionEncode.encodeFunction("balanceOfBatch", _owners, _ids);
//        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
//        transaction.signWithSM2(signAccountString, "");
//
//        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
//        int code = receiptReturn.getRawcode();
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("balanceOfBatch", SiegeAssetAbi, rawReturn);
//        log("调用balanceOfBatch: " + decodeResult);
//
//        if (code == 0) {
//            return decodeResult;
//        }
//        else if (code == -32005) {
//            return "contract calling error";
//        }
//        else {
//            return "unknown error";
//        }
//    }

//    public String getProperty(String owner, String signAccountString) throws Exception {
//        HyperchainAPI hyperchainAPI = new HyperchainAPI();
//
//        Account signAccount = new Account(signAccountString);
//
//        FuncParamReal _owner = new FuncParamReal("address", owner);
//        String payloadWithParams = FunctionEncode.encodeFunction("getProperty", _owner);
//        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
//        transaction.signWithSM2(signAccountString, "");
//
//        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
//        int code = receiptReturn.getRawcode();
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("getProperty", SiegeAssetAbi, rawReturn);
//        log("调用getProperty: " + decodeResult);
//
//        if (code == 0) {
//            return decodeResult;
//        }
//        else if (code == -32005) {
//            return "contract calling error";
//        }
//        else {
//            return "unknown error";
//        }
//    }

//    public String propertyOf(String owner, String signAccountString) throws Exception {
//        HyperchainAPI hyperchainAPI = new HyperchainAPI();
//
//        Account signAccount = new Account(signAccountString);
//
//        FuncParamReal _owner = new FuncParamReal("address", owner);
//        String payloadWithParams = FunctionEncode.encodeFunction("propertyOf", _owner);
//        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
//        transaction.signWithSM2(signAccountString, "");
//
//        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
//        int code = receiptReturn.getRawcode();
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("propertyOf", SiegeAssetAbi, rawReturn);
//        log("调用propertyOf: " + decodeResult);
//
//        if (code == 0) {
//            return decodeResult;
//        }
//        else if (code == -32005) {
//            return "contract calling error";
//        }
//        else {
//            return "unknown error";
//        }
//    }

//    public String getUri(long id, String signAccountString) throws Exception {
//        HyperchainAPI hyperchainAPI = new HyperchainAPI();
//
//        Account signAccount = new Account(signAccountString);
//
//        FuncParamReal _id = new FuncParamReal("uint256", id);
//        String payloadWithParams = FunctionEncode.encodeFunction("getUri", _id);
//        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
//        transaction.signWithSM2(signAccountString, "");
//
//        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
//        int code = receiptReturn.getRawcode();
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("getUri", SiegeAssetAbi, rawReturn);
//        log("调用getUri: " + decodeResult);
//
//        if (code == 0) {
//            return decodeResult;
//        }
//        else if (code == -32005) {
//            return "contract calling error";
//        }
//        else {
//            return "unknown error";
//        }
//    }

//    public String getId(String uri, String signAccountString) throws Exception {
//        HyperchainAPI hyperchainAPI = new HyperchainAPI();
//
//        Account signAccount = new Account(signAccountString);
//
//        FuncParamReal _uri = new FuncParamReal("string", uri);
//        String payloadWithParams = FunctionEncode.encodeFunction("getId", _uri);
//        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
//        transaction.signWithSM2(signAccountString, "");
//
//        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
//        int code = receiptReturn.getRawcode();
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("getId", SiegeAssetAbi, rawReturn);
//        log("调用getId: " + decodeResult);
//
//        if (code == 0) {
//            return decodeResult;
//        }
//        else if (code == -32005) {
//            return "contract calling error";
//        }
//        else {
//            return "unknown error";
//        }
//    }
}
