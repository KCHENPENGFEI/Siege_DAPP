package com.example.zjusiege.Controller;

import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class JsonController {
    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.POST)
    public String getByJson(@RequestBody JSONObject aa){
//        System.out.println(jsonParams.toString());

        JSONObject result = new JSONObject();
        result.put("msg", "ok");
        result.put("method", "post");
        return result.toString();
    }
}
