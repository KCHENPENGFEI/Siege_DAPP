package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.account.Account;
import com.example.zjusiege.Service.HyperchainService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SiegeController {
    private HyperchainService hyperchainService = new HyperchainService();

    private final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";
    private Account deployAccount = new Account(DEPLOY_ACCOUNT_JSON);

    // 测试用
    private final String address1 = "65F9B86F4CC7AD56511D7151374A21F0AE016807";
    private final String address2 = "23A2CF0868CBA222A3807C30131822C1005DE126";
    private final String address3 = "56FA774E503BA4530ADAB2BE41A12483DF583B57";
    private final String address4 = "E524B1DC11951BAEF0A58603AB2D2BB3072282A8";
    private final String address5 = "EB24F9CD77222EAB3E5E5F9B785A208530599FE3";

//    private HttpServletRequest request;
//    private HttpSession session;
//
//    public SiegeController() {
////        assert session != null;
//        session.setAttribute("browser", "chrome");
//        Object sessionBrowser = session.getAttribute("browser");
//        if (sessionBrowser == null) {
//            System.out.println("不存在session，设置browser=" + "chrome");
//            session.setAttribute("browser", "chrome");
//        } else {
//            System.out.println("存在session，browser=" + sessionBrowser.toString());
//        }
//
//    }

//    public SiegeController(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//
//        session = request.getSession();
//        session.setAttribute("data", "孤傲苍狼");
//    }

    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestBody JSONObject params) throws Exception{

        final String sig = "https://siege-token-sig-1";
        final long value = 100;
        boolean isRegister = params.getBoolean("register");
        if (isRegister){
            try {
                // 注册新账号
                String newAccountString = hyperchainService.register();
                JSONObject newAccountJson = JSONObject.fromObject(newAccountString);
                // 获取账号地址
                String address = newAccountJson.getString("address");
                // 获取指定URI的id，此处为SIG金币，玩家注册后，向其分发SIG
                String getIdResult = hyperchainService.getId(sig, DEPLOY_ACCOUNT_JSON);
                long id = Long.valueOf(getValue(getIdResult));
                String issueResult = hyperchainService.issue(id, address, value, DEPLOY_ACCOUNT_JSON);
                if (issueResult.equals("issue success")) {
                    return newAccountString;
                }
                else {
                    return "register failed";
                }
            } catch (Exception e) {
                System.out.println("Got a Exception：" + e.getMessage());
                return "register failed";
            }
        }
        else {
            return "request failed";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody JSONObject params) throws Exception{
        String paramsString = params.toString();
        try {
            // 验证玩家是否注册信息，保证gameId = 0
            String loginResult = hyperchainService.login(paramsString);
            int gameId = Integer.valueOf(getValue(loginResult));

            if (gameId == 0) {
                return "login success";
            }
            else {
                return "login failed";
            }
        } catch (Exception e) {
            System.out.println("Got a Exception：" + e.getMessage());
            return "login failed";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/startGame", method = RequestMethod.POST)
    public String startGame(@RequestBody JSONObject params) throws  Exception {

        // 缴纳入场费50SIG
        String from = params.getString("playerAddress");
        String to = deployAccount.getAddress();

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

        // 耗时1.1~1.2s
        String owner = params.getString("owner");
        String signature = params.getString("signature");

        String result = hyperchainService.propertyOf(owner, signature);
        JSONArray jsonArray = JSONArray.fromObject(result);
        JSONObject a = jsonArray.getJSONObject(0);
        JSONArray b = a.getJSONArray("value");
        List<Long> s = new ArrayList<>();
        List<String> l = new ArrayList<>();
        System.out.println(b);
        for (int i = 0; i < b.size(); i++) {
            JSONObject tmp = b.getJSONObject(i);
            long m = Long.valueOf(tmp.getString("value"));
            if (!s.contains(m)) {
                s.add(m);
                l.add("11BD06F184F3767FC02C7F27E812F51BC6F28B39");
            }
        }
        System.out.println(s);
        System.out.println(l);
        String r = hyperchainService.balanceOfBatch(l, s, signature);
        System.out.println(r);
        return r;
    }

    @ResponseBody
    @RequestMapping(value = "/rrtest", method = RequestMethod.POST)
    public String noname(@RequestBody JSONObject params) {
        HttpServletRequest request;
        HttpSession session = null;
        session.setAttribute("browser", "chrome");
        Object sessionBrowser = session.getAttribute("browser");
        if (sessionBrowser == null) {
            System.out.println("不存在session，设置browser=" + "chrome");
            session.setAttribute("browser", "chrome");
        } else {
            System.out.println("存在session，browser=" + sessionBrowser.toString());
        }
        return "111";
    }

    @ResponseBody
    @RequestMapping(value = "/account", method = RequestMethod.POST)
    public String account(@RequestBody JSONObject params, HttpServletRequest request, HttpSession session) {
        session.setAttribute("account", "chen");
        Object obj = session.getAttribute("account");
        if (obj == null) {
            return "null";
        }
        else {
            return obj.toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/output", method = RequestMethod.POST)
    public String output(@RequestBody JSONObject params, HttpServletRequest request, HttpSession session) {
//        session.setAttribute("account", "chen");
        Object obj = session.getAttribute("account");
        if (obj == null) {
            return "null";
        }
        else {
            return obj.toString();
        }
    }

    /******************************************************  Utils ***********************************************/
    private String getValue(String input) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String value = jsonObject.getString("value");
            return value;
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return "getValue error";
        }
    }

    private List<Long> getListValue(String input, boolean duplicate) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONArray jsonArray1 = jsonObject.getJSONArray("value");
            List<Long> listValue = new ArrayList<>();
            for (int i = 0; i < jsonArray1.size(); i++) {
                JSONObject item = jsonArray1.getJSONObject(i);
                long value = Long.valueOf(item.getString("value"));
                if (duplicate) {
                    listValue.add(value);
                }
                else {
                    if (!listValue.contains(value)) {
                        listValue.add(value);
                    }
                }
            }
            return listValue;
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}