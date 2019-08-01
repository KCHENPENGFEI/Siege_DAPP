package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import cn.hyperchain.sdk.rpc.Transaction.Transaction;
import cn.hyperchain.sdk.rpc.account.Account;
import cn.hyperchain.sdk.rpc.function.FuncParamReal;
import cn.hyperchain.sdk.rpc.function.FunctionDecode;
import cn.hyperchain.sdk.rpc.function.FunctionEncode;
import cn.hyperchain.sdk.rpc.returns.ReceiptReturn;
import cn.hyperchain.sdk.rpc.utils.Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ERC1155 {
    public final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    public final  String CONTRACT_ADDRESS = "0xf05a31a249db904dbc7ed974bfa9cbabc703f2f5";
    public final String ACCOUNT1 = "{\"address\":\"8BCD0D5A2F1E1EE9DBAF9D8C1DCB040EF7F99B6E\",\"publicKey\":\"04572E3C237DD4EB47575F4EE310DF6DE8CBC3608A61550E7F9F8913F662A38CBB5D12A6C3DD40E17AD65983E52C21433446BD8A6F4A4B8CAA1C1B2FBB81D66197\",\"privateKey\":\"00FFBE1052A8033A0F7B2D6FAADC9CE45DB7A92F3A4AB33D15695445F6F8EB1581\",\"privateKeyEncrypted\":false}";
    public final String ACCOUNT2 = "{\"address\":\"B8CF127F63F46EE1121342732893CCA8D692A965\",\"publicKey\":\"0405E3E71C2D5CD8E80A9A31EB9B4989D8FF78058FB5C922A4331831B452DA303154632360C9B93F8FB800DB288EC64AF48E43A681C3E52E34FBDC266FDD935FCB\",\"privateKey\":\"009E706B2AAC9BD7F4DB56CED768177F6422715995DA6AD4D0AFC06B427D4C11DA\",\"privateKeyEncrypted\":false}";
    public final String ACCOUNT3 = "{\"address\":\"090F6CA51B7F97EAA46C544C0A8E1015F096AF8E\",\"publicKey\":\"04C0D4E86F4850754F90CDADADC659EE5051BF982855E9382C376D591C846F7031FEBF9C5BAA5F47FE8E0C4D4012BDA8A753564A56853E0B5DEC03ED2356966CBF\",\"privateKey\":\"397CFC3B1059E329005875D64030D09D69214445215D11D212DD07D4CE09BA59\",\"privateKeyEncrypted\":false}";

    private static void log(String s) {
        System.out.println("HyperchainAPITest ====== " + s);
    }

    @RequestMapping("/ERC1155")
    public String deploy_erc1155() throws Exception{
        HyperchainAPI hyperchain = new HyperchainAPI();
        Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);
        Account account1 = new Account(ACCOUNT1);
        Account account2 = new Account(ACCOUNT2);
        Account account3 = new Account(ACCOUNT3);

        String abi = Utils.readFile("contract/ERC1155Mintable.abi");
        String bin = Utils.readFile("contract/ERC1155Mintable.bin");

        //部署
//        Transaction transaction = new Transaction(deployAccount.getAddress(), bin, false);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        ReceiptReturn receiptReturn = hyperchain.deployContract(transaction);
//        int code = receiptReturn.getRawcode();
//        log("部署结果: " + code);
//        String contractAddress = receiptReturn.getContractAddress();
//        log("合约地址: " + contractAddress);

        //合约升级
//        Transaction transactionUpdate = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, bin, 1, VMType.EVM);
//        transactionUpdate.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//        ReceiptReturn contractUpdateResult = hyperchain.maintainContract(transactionUpdate);
//        log("合约更新： " + contractUpdateResult.getRet());

        //调用
        //create
//        FuncParamReal initialSupply = new FuncParamReal("uint256", 100000);
//        FuncParamReal uri = new FuncParamReal("string", "https://bubing-half1");
//        String payload = FunctionEncode.encodeFunction("create", initialSupply, uri);
//        Transaction transaction = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payload, false);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        ReceiptReturn receiptReturn = hyperchain.invokeContract(transaction);
//        String rawReturn = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("create", abi, rawReturn);
//        log("调用create: " + decodeResult);

        //balanceOf
        FuncParamReal owner = new FuncParamReal("address", account2.getAddress());
        FuncParamReal id = new FuncParamReal("uint256", 2);
        String paylaod1 = FunctionEncode.encodeFunction("balanceOf", owner, id);
        Transaction transaction1 = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, paylaod1, false);
        transaction1.signWithSM2(DEPLOY_ACCOUNT_JSON, "");

        ReceiptReturn receiptReturn1 = hyperchain.invokeContract(transaction1);
        String rawReturn = receiptReturn1.getRet();
        String decodeResult = FunctionDecode.resultDecode("balanceOf", abi, rawReturn);
        log("调用balanceOf" + decodeResult);

        //mint
//        FuncParamReal id = new FuncParamReal("uint256", 2);
//        FuncParamReal to = new FuncParamReal("address[]", new String[]{account1.getAddress(), account2.getAddress(), account3.getAddress()});
//
//        FuncParamReal value = new FuncParamReal("uint256[]", new int[]{10, 100, 1000});
//        String payload = FunctionEncode.encodeFunction("mint", id, to, value);
//        Transaction transaction = new Transaction(deployAccount.getAddress(), CONTRACT_ADDRESS, payload, false);
//        transaction.signWithSM2(DEPLOY_ACCOUNT_JSON, "");
//
//        ReceiptReturn receiptReturn = hyperchain.invokeContract(transaction);
//        String rawResult = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("mint", abi, rawResult);
//        log("调用mint: " + decodeResult);
        //safeTransferFrom
//        FuncParamReal _from = new FuncParamReal("address", account3.getAddress());
//        FuncParamReal _to = new FuncParamReal("address", account2.getAddress());
//        FuncParamReal _id = new FuncParamReal("uint256", 2);
//        FuncParamReal _value = new FuncParamReal("uint256", 100);
//        FuncParamReal _data = new FuncParamReal("bytes", "jkhfa".getBytes());
//        String payload = FunctionEncode.encodeFunction("safeTransferFrom", _from, _to, _id, _value, _data);
//        Transaction transaction = new Transaction(account3.getAddress(), CONTRACT_ADDRESS, payload, false);
//        transaction.signWithSM2(ACCOUNT3, "");
//
//        ReceiptReturn receiptReturn = hyperchain.invokeContract(transaction);
//        String rawResult = receiptReturn.getRet();
//        String decodeResult = FunctionDecode.resultDecode("safeTransferFrom", abi, rawResult);
//        log("调用safeTransferFrom: " + decodeResult);

        return "ERC1155";
    }
}
