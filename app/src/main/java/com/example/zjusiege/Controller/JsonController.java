package com.example.zjusiege.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
public class JsonController {
    @ResponseBody
    @RequestMapping(value = "/json", method = RequestMethod.POST)
    public String getByJson(@RequestBody String a){
//        System.out.println(jsonParams.toString());

//        JSONObject result = new JSONObject();
//        result.put("msg", "ok");
//        result.put("method", "post")
        System.out.println(a);
//        JSONObject aa = JSONObject.fromObject(a);
        JSONObject aa = JSON.parseObject(a);
        System.out.println(aa);
        return "111";
    }
}
