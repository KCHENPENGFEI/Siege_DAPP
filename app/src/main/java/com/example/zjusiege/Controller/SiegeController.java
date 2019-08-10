package com.example.zjusiege.Controller;

import com.example.zjusiege.Service.HyperchainService;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class SiegeController {
    public HyperchainService hyperchainService = new HyperchainService();

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
            return hyperchainService.createAccountJson();
        }
        return "request failed";
    }

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody JSONObject params) throws Exception{
//        HyperchainService hyperchainService = new HyperchainService();
        String result = hyperchainService.login(params);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/startGame", method = RequestMethod.POST)
    public String startGame(@RequestBody JSONObject params) throws  Exception {
        // doSth

        String[] playersAddresses = new String[]{address1, address2, address3, address4, address5};
        String result = hyperchainService.startGame(playersAddresses);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getPlayersTable", method = RequestMethod.POST)
    public String getPlayersTable(@RequestBody JSONObject params) throws Exception {
        String result = hyperchainService.getPlayersTable(params);
        return result;
    }

}
