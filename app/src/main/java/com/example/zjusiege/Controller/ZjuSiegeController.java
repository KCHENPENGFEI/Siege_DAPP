package com.example.zjusiege.Controller;

import com.example.zjusiege.Service.HyperchainService;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class ZjuSiegeController {
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestBody JSONObject params) throws Exception{

        Boolean isRegister = params.getBoolean("register");
        if (isRegister){
            HyperchainService hyperchainService = new HyperchainService();
            return hyperchainService.createAccountJson();
        }
        return "registerFailed";
    }

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody JSONObject params) throws Exception{
        HyperchainService hyperchainService = new HyperchainService();
        String result = hyperchainService.validateLogin(params);
        if (!result.equals("")){
            return result;
        }
        return "LoginFailed";
    }
}
