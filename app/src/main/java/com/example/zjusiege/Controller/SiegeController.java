package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.HyperchainAPI;
import com.example.zjusiege.Service.HyperchainService;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class SiegeController {
    private HyperchainService hyperchainService = new HyperchainService();

    // 测试用
    private final String address1 = "65F9B86F4CC7AD56511D7151374A21F0AE016807";
    private final String address2 = "23A2CF0868CBA222A3807C30131822C1005DE126";
    private final String address3 = "56FA774E503BA4530ADAB2BE41A12483DF583B57";
    private final String address4 = "E524B1DC11951BAEF0A58603AB2D2BB3072282A8";
    private final String address5 = "EB24F9CD77222EAB3E5E5F9B785A208530599FE3";

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestBody JSONObject params) throws Exception{

        Boolean isRegister = params.getBoolean("register");
        if (isRegister){
//            HyperchainService hyperchainService = new HyperchainService();
            return hyperchainService.register();
        }
        return "request failed";
    }

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody JSONObject params) throws Exception{
//        HyperchainService hyperchainService = new HyperchainService();
        String paramsString = params.toString();
        String result = hyperchainService.login(paramsString);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/startGame", method = RequestMethod.POST)
    public String startGame(@RequestBody JSONObject params) throws  Exception {
        // doSth

        String signature = params.getString("signature");

        String[] playersAddresses = new String[]{address1, address2, address3, address4, address5};
        String result = hyperchainService.startGame(playersAddresses, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getPlayersTable", method = RequestMethod.POST)
    public String getPlayersTable(@RequestBody JSONObject params) throws Exception {

        String playerAddress = params.getString("playerAddress");
        String signature = params.getString("signature");

        String result = hyperchainService.getPlayersTable1(playerAddress, signature);
        return result;
    }

    /******************************************************  GameItem ***********************************************/

    @ResponseBody
    @RequestMapping(value = "/createGameItem", method = RequestMethod.POST)
    public String createGameItem(@RequestBody JSONObject params) throws Exception {

        long initialSupply = params.getLong("initialSupply");
        String uri = params.getString("uri");
        String signature = params.getString("signature");

        String result = hyperchainService.create(initialSupply, uri, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/issueGameItem", method = RequestMethod.POST)
    public String issueGameItem(@RequestBody JSONObject params) throws Exception {

        long id = params.getLong("id");
        String to = params.getString("to");
        long value = params.getLong("value");
        String signature = params.getString("signature");

        String result = hyperchainService.issue(id, to, value, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/transferGameItem", method = RequestMethod.POST)
    public String transferGameItem(@RequestBody JSONObject params) throws Exception {

        String from = params.getString("from");
        String to = params.getString("to");
        long id = params.getLong("id");
        long value = params.getLong("value");
        String data = params.getString("data");
        String signature = params.getString("signature");

        String result = hyperchainService.safeTransferFrom(from, to, id, value, data, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/balanceOfGameItem", method = RequestMethod.POST)
    public String balanceOfGameItem(@RequestBody JSONObject params) throws Exception {

        String owner = params.getString("owner");
        long id = params.getLong("id");
        String signature = params.getString("signature");

        String result = hyperchainService.balanceOf(owner, id, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/propertyOfGameItem", method = RequestMethod.POST)
    public String propertyOfGameItem(@RequestBody JSONObject params) throws Exception {

        String owner = params.getString("owner");
        String signature = params.getString("signature");

        String result = hyperchainService.propertyOf(owner, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getUriGameItem", method = RequestMethod.POST)
    public String getUriGameItem(@RequestBody JSONObject params) throws Exception {

        long id = params.getLong("id");
        String signature = params.getString("signature");

        String result = hyperchainService.getUri(id, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/rtest", method = RequestMethod.POST)
    public String test(@RequestBody JSONObject params) throws Exception {

        String accountJson = "";
        if (params.getBoolean("register")) {
            HyperchainAPI hyperchainAPI = new HyperchainAPI();
            accountJson = HyperchainAPI.newAccountRawSM2();
        }
        return accountJson;
    }
}
