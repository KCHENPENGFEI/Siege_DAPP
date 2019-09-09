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

    private final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    public final String CONTRACT_ADDRESS = "0xb168af3bb6f3ee53fc0b249bd8b62f5ca0b7f6bb";
    public final String GAME_ITEM_CONTRACT_ADDRESS = "0x429f382e15054439b0bc4fc1139e729d4dc5e578";

    private final String ACCOUNT1 = "{\"address\":\"8BCD0D5A2F1E1EE9DBAF9D8C1DCB040EF7F99B6E\",\"publicKey\":\"04572E3C237DD4EB47575F4EE310DF6DE8CBC3608A61550E7F9F8913F662A38CBB5D12A6C3DD40E17AD65983E52C21433446BD8A6F4A4B8CAA1C1B2FBB81D66197\",\"privateKey\":\"00FFBE1052A8033A0F7B2D6FAADC9CE45DB7A92F3A4AB33D15695445F6F8EB1581\",\"privateKeyEncrypted\":false}";
    private final String ACCOUNT2 = "{\"address\":\"B8CF127F63F46EE1121342732893CCA8D692A965\",\"publicKey\":\"0405E3E71C2D5CD8E80A9A31EB9B4989D8FF78058FB5C922A4331831B452DA303154632360C9B93F8FB800DB288EC64AF48E43A681C3E52E34FBDC266FDD935FCB\",\"privateKey\":\"009E706B2AAC9BD7F4DB56CED768177F6422715995DA6AD4D0AFC06B427D4C11DA\",\"privateKeyEncrypted\":false}";
    private final String ACCOUNT3 = "{\"address\":\"090F6CA51B7F97EAA46C544C0A8E1015F096AF8E\",\"publicKey\":\"04C0D4E86F4850754F90CDADADC659EE5051BF982855E9382C376D591C846F7031FEBF9C5BAA5F47FE8E0C4D4012BDA8A753564A56853E0B5DEC03ED2356966CBF\",\"privateKey\":\"397CFC3B1059E329005875D64030D09D69214445215D11D212DD07D4CE09BA59\",\"privateKeyEncrypted\":false}";

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

    //    private HyperchainAPI hyperchain;
//
//    public HyperchainService() throws Exception{
//        this.hyperchain = new HyperchainAPI();
//    }
    public HyperchainService() {

    }

    public String register() throws Exception{
        HyperchainAPI hyperchain = new HyperchainAPI();

        String accountJson = HyperchainAPI.newAccountRawSM2();
        String address = JSONObject.fromObject(accountJson).getString("address");
        FuncParamReal _address = new FuncParamReal("address", address);
        String payloadWithParam = FunctionEncode.encodeFunction("register", _address);
        Transaction transaction = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParam, false);
        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");

        ReceiptReturn receiptReturn = hyperchain.invokeContract(transaction);
        String rawReturn = receiptReturn.getRet();
        int code = receiptReturn.getRawcode();
        String decodeResult = FunctionDecode.resultDecode("register", siegeAbi, rawReturn);
        log("调用register: " + decodeResult);

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
        Transaction transaction = new Transaction(account.getAddress(), CONTRACT_ADDRESS, payloadWithParam, false);
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

    public String startGame(String[] playersAddresses, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _playersAddresses = new FuncParamReal("address[]", playersAddresses);
        String payloadWithParam = FunctionEncode.encodeFunction("startGame", _playersAddresses);
        Transaction transaction = new Transaction(signAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParam, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("startGame", siegeAbi, rawReturn);
        log("调用startGame: " + decodeResult);

        if (code == 0) {
            return "startGameSuccess";
        }
        else if (code == -32005) {
            return "contract calling error";
        }
        else {
            return "unknown error";
        }
    }

    public String getPlayersTable1(String playersAddress, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _playersAddress = new FuncParamReal("address", playersAddress);
        String payloadWithParam = FunctionEncode.encodeFunction("getPlayersTablePart1", _playersAddress);
        Transaction transaction = new Transaction(signAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParam, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getPlayersTablePart1", siegeAbi, rawReturn);
        log("调用getPlayersTablePart1: " + decodeResult);

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

    public String getCitiesTable(long gameId, long cityId, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _gameId = new FuncParamReal("uint64", gameId);
        FuncParamReal _cityId = new FuncParamReal("uint64", cityId);
        String payloadWithParam = FunctionEncode.encodeFunction("getCitiesTable", _gameId, _cityId);
        Transaction transaction = new Transaction(signAccount.getAddress(), CONTRACT_ADDRESS, payloadWithParam, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getCitiesTable", siegeAbi, rawReturn);
        log("调用getCitiesTable: " + decodeResult);

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
    public String setPrecision(int precision) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _precision = new FuncParamReal("uint256", precision);
        String payloadWithParams = FunctionEncode.encodeFunction("setPrecision", _precision);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("setPrecision", siegeAbi, rawReturn);
        log("调用setPrecision: " + decodeResult);

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
        FuncParamReal _playerNum = new FuncParamReal("uint256", 2 * cityNum);
        String payloadWithParams1 = FunctionEncode.encodeFunction("setCityNum", _cityNum);
        String payloadWithParams2 = FunctionEncode.encodeFunction("setPlayerNum", _playerNum);
        Transaction transaction1 = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams1, false);
        Transaction transaction2 = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams2, false);
        transaction1.signWithSM2(deployAccountJson, "");
        transaction2.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn1 = hyperchainAPI.invokeContract(transaction1);
        ReceiptReturn receiptReturn2 = hyperchainAPI.invokeContract(transaction2);

        int code1 = receiptReturn1.getRawcode();
        int code2 = receiptReturn2.getRawcode();
        String rawReturn1 = receiptReturn1.getRet();
        String rawReturn2 = receiptReturn2.getRet();
        String decodeResult1 = FunctionDecode.resultDecode("setCityNum", siegeAbi, rawReturn1);
        String decodeResult2 = FunctionDecode.resultDecode("setPlayerNum", siegeAbi, rawReturn2);
        log("调用setCityNum: " + decodeResult1);
        log("调用setPlayerNum: " + decodeResult2);

        if (code1 == 0 && code2 == 0) {
            return "success";
        }
        else if (code1 == -32005 || code2 == -32005) {
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
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("setEnterFee", siegeAbi, rawReturn);
        log("调用setEnterFee: " + decodeResult);

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
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("setCityPrice", siegeAbi, rawReturn);
        log("调用setCityPrice: " + decodeResult);

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
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("setSoldierNum", siegeAbi, rawReturn);
        log("调用setSoldierNum: " + decodeResult);

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

    public String setInterval(int interval) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        FuncParamReal _interval = new FuncParamReal("uint256", interval);
        String payloadWithParams = FunctionEncode.encodeFunction("setInterval", _interval);
        Transaction transaction = new Transaction(deployAccount.getAddress(), contractAddress, payloadWithParams, false);
        transaction.signWithSM2(deployAccountJson, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("setInterval", siegeAbi, rawReturn);
        log("调用setInterval: " + decodeResult);

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
    public String create(long initialSupply, String uri, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _initialSupply = new FuncParamReal("uint256", initialSupply);
        FuncParamReal _uri = new FuncParamReal("string", uri);
        String payloadWithParams = FunctionEncode.encodeFunction("create", _initialSupply, _uri);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("create", SiegeAssetAbi, rawReturn);
        log("调用create: " + decodeResult);

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

    public String issue(long id, String to, long value, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _id = new FuncParamReal("uint256", id);
        FuncParamReal _address = new FuncParamReal("address[]", new String[] {to});
        FuncParamReal _value = new FuncParamReal("uint256[]", new long[] {value});
        String payloadWithParams = FunctionEncode.encodeFunction("issue", _id, _address, _value);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("issue", SiegeAssetAbi, rawReturn);
        log("调用issue: " + decodeResult);

        if (code == 0) {
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
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("transfer", SiegeAssetAbi, rawReturn);
        log("transfer: " + decodeResult);

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

    public String balanceOf(String owner, long id, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _owner = new FuncParamReal("address", owner);
        FuncParamReal _id = new FuncParamReal("uint256", id);
        String payloadWithParams = FunctionEncode.encodeFunction("balanceOf", _owner, _id);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

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

    public String balanceOfBatch(List<String> owners, List<Long> ids, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _owners = new FuncParamReal("address[]", owners);
        FuncParamReal _ids = new FuncParamReal("uint256[]", ids);
        String payloadWithParams = FunctionEncode.encodeFunction("balanceOfBatch", _owners, _ids);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("balanceOfBatch", SiegeAssetAbi, rawReturn);
        log("调用balanceOfBatch: " + decodeResult);

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

    public String propertyOf(String owner, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _owner = new FuncParamReal("address", owner);
        String payloadWithParams = FunctionEncode.encodeFunction("propertyOf", _owner);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("propertyOf", SiegeAssetAbi, rawReturn);
        log("调用propertyOf: " + decodeResult);

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

    public String getUri(long id, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _id = new FuncParamReal("uint256", id);
        String payloadWithParams = FunctionEncode.encodeFunction("getUri", _id);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getUri", SiegeAssetAbi, rawReturn);
        log("调用getUri: " + decodeResult);

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

    public String getId(String uri, String signAccountString) throws Exception {
        HyperchainAPI hyperchainAPI = new HyperchainAPI();

        Account signAccount = new Account(signAccountString);

        FuncParamReal _uri = new FuncParamReal("string", uri);
        String payloadWithParams = FunctionEncode.encodeFunction("getId", _uri);
        Transaction transaction = new Transaction(signAccount.getAddress(), GAME_ITEM_CONTRACT_ADDRESS, payloadWithParams, false);
        transaction.signWithSM2(signAccountString, "");

        ReceiptReturn receiptReturn = hyperchainAPI.invokeContract(transaction);
        int code = receiptReturn.getRawcode();
        String rawReturn = receiptReturn.getRet();
        String decodeResult = FunctionDecode.resultDecode("getId", SiegeAssetAbi, rawReturn);
        log("调用getId: " + decodeResult);

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
}
