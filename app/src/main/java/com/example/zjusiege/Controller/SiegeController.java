package com.example.zjusiege.Controller;

import cn.hyperchain.sdk.rpc.account.Account;
import com.example.zjusiege.AsyncTaskService;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.HyperchainService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private List<String> matchQueue = new ArrayList<>();
    private List<String> matchQueueBak = new ArrayList<>();



    //uri 定义


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
    @CrossOrigin
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestBody JSONObject params, HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception{


//        response.setHeader("Access-Control-Allow-Origin","*");
        final int precision = SiegeParams.getPrecision();
        final String deployAccountJson = Config.getDeployAccountJson();

        final String symbol = "SIG";   // 以后保存到参数类中
        final long value = SiegeParams.getRegistrationReward() * precision;
        boolean isRegister = params.getBoolean("register");
        if (isRegister){
            try {
                // 考虑加入request验证，防止玩家恶意注册多个账号
                // 注册新账号
                String newAccountString = hyperchainService.register();
                JSONObject newAccountJson = JSONObject.fromObject(newAccountString);
                // 获取账号地址
                String address = newAccountJson.getString("address");
                // 发放注册奖励
                String issueResult = hyperchainService.issueCoin(address, value, symbol, deployAccountJson);
                if (issueResult.equals("issue success")) {
                    JSONObject jsonObject = new JSONObject()
                            .element("status", "success")
                            .element("account", newAccountJson);
                    return jsonObject.toString();
                }
                else {
                    return "register failed";
                }
            } catch (Exception e) {
                System.out.println("Got a Exception：" + e.getMessage());
                JSONObject jsonObject = new JSONObject()
                        .element("status", "failed")
                        .element("account", "");
                return jsonObject.toString();
            }
        }
        else {
            JSONObject jsonObject = new JSONObject()
                    .element("status", "failed")
                    .element("account", "");
            return jsonObject.toString();
        }
    }

    @ResponseBody
    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody JSONObject params, HttpServletRequest request, HttpSession session) throws Exception{
        String paramsString = params.toString();
        try {
            // 验证玩家是否注册信息，保证gameId = 0
            String loginResult = hyperchainService.login(paramsString);
            int gameId = Integer.valueOf(getValue(loginResult));
            // 数据放入session中
            session.setAttribute("isLogin", true);
            session.setAttribute("playerAddress", params.getString("address"));
            session.setAttribute("gameId", gameId);
            JSONObject jsonObject = new JSONObject()
                    .element("status", "success");
            return jsonObject.toString();
        } catch (Exception e) {
            System.out.println("Got a Exception：" + e.getMessage());
            JSONObject jsonObject = new JSONObject()
                    .element("status", "failed");
            return jsonObject.toString();
        }
    }

    @ResponseBody
    @RequestMapping(value = "/listRemove", method = RequestMethod.GET)
    public String listR() {
        List<Integer> list = Arrays.asList(4,3,2,1);
//        a.remove(3);
//        System.out.println(a);
//        int[] arr = { 1, 2, 3, 4, 5, 4 };
//        arr = remove(arr, 4);
//        arr = remove(arr, 2);
//        show(arr);
        final CopyOnWriteArrayList<Integer> cowList = new CopyOnWriteArrayList<>(list);
        for (Integer item : cowList) {
            if (item.equals(3)) {
                cowList.remove(item);
            }
        }
        System.out.println(cowList);
        list = cowList;
        System.out.println(list);
//        for(int i = list.size() - 1; i >= 0; i--){
//            Integer item = list.get(i);
//            if(item == 3){
//                list.remove(item);
//            }
//        }
//        System.out.println(list);
//        list.removeIf(item -> item == 3);
        System.out.println(list);

        return "";
    }

    @ResponseBody
    @RequestMapping(value = "/startGame", method = RequestMethod.POST)
    public String startGame(@RequestBody JSONObject params, HttpServletRequest request, HttpSession session) throws  Exception {
        String[] a = new String[]{"D817B5187CCDDDC2DB1B5118BDA5103458E2182E",
                "A550ABB4D96C7036944AB27F1FCA4438F93920A3",
                "6754B4E3C346E714195C0DA6B27566F615A0D06C",
                "545B7E7F41C744F8109847BF4621EBAF7EC56B26",
                "37908FB0843370549C584EE54EAE1B9FBB1D663D",
                "94267A422EA798F1573E949B8B17BD821D11E2C1",
                "F4D1DD19224BAF6BD65B5022E9B04844F25BB609",
                "E7817E353D66255400BB4AC8460E464EEDE5956A",
                "B5444F8A2C1BFF6A7DDA3EC7894D37C62B2BDE68",
                "80EC87A061EB915E52ADB1A4E05B3B7EE69A8DA7"};
        String result = hyperchainService.startGame(a);
        return result;
    }


    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public String gett() throws Exception {
        String result1 = hyperchainService.getCity();
        String result2 = hyperchainService.getSo();
        String result3 = hyperchainService.getDe();
        String result4 = hyperchainService.getPrecision();
        String result5 = hyperchainService.getCityNum();
        String result6 = hyperchainService.getEnterFee();
        String result7 = hyperchainService.getCityPrice();
        String result8 = hyperchainService.getSoldierNum();
        String result9 = hyperchainService.getInterval();
        String result10 = hyperchainService.getGameAssetAddr();

        System.out.println("result1" + result1);
        System.out.println("result2" + result2);
        System.out.println("result3" + result3);
        System.out.println("result4" + result4);
        System.out.println("result5" + result5);
        System.out.println("result6" + result6);
        System.out.println("result7" + result7);
        System.out.println("result8" + result8);
        System.out.println("result9" + result9);
        System.out.println("result10" + result10);
        return "11";
    }

    @ResponseBody
    @RequestMapping(value = "/assetTest", method = RequestMethod.POST)
    public String assetTest(@RequestBody JSONObject params) throws Exception {
        String sig = params.getString("signature");
        String result = hyperchainService.assetTest(sig);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/setTestAddr", method = RequestMethod.POST)
    public String setTestAddr(@RequestBody JSONObject params) throws Exception {
        String address = params.getString("address");
        String result = hyperchainService.setTestAddr(address);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getTestAddr", method = RequestMethod.POST)
    public String getTestAddr(@RequestBody JSONObject params) throws Exception {
//        String address = params.getString("address");
        String result = hyperchainService.getTestAddr();
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/callTest", method = RequestMethod.POST)
    public String callTest(@RequestBody JSONObject params) throws Exception {
//        String address = params.getString("address");
        String result = hyperchainService.callTest();
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateRankingTb", method = RequestMethod.POST)
    public void up() throws Exception {
        List<Integer> ranking = new ArrayList<>();
        List<String> playerAddresses = new ArrayList<>();
        List<Long> price = new ArrayList<>();
        List<Long> time = new ArrayList<>();

        ranking.add(1);
        ranking.add(2);
        playerAddresses.add("6754B4E3C346E714195C0DA6B27566F615A0D06C");
        playerAddresses.add("545B7E7F41C744F8109847BF4621EBAF7EC56B26");
        price.add(Long.valueOf(10000));
        price.add(Long.valueOf(200000));
        time.add(new Date().getTime());
        time.add(new Date().getTime());
        String result = hyperchainService.updateRankingTb(10, ranking, playerAddresses, price, time);
        System.out.println(result);
    }

    @ResponseBody
    @RequestMapping(value = "/getGlobalTb", method = RequestMethod.POST)
    public String getG(@RequestBody JSONObject params) throws Exception {
        int gameId = params.getInt("gameId");
        String result = hyperchainService.getGlobalTb(gameId);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getFrozenTb", method = RequestMethod.POST)
    public String getFrozenTb(@RequestBody JSONObject params) throws Exception {
        String address = params.getString("address");
        String result = hyperchainService.getFrozenTb(address);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/allocate", method = RequestMethod.POST)
    public String allocate(@RequestBody JSONObject params) throws Exception {

        List<String> players = new ArrayList<>();
        List<Integer> cityId = new ArrayList<>();
        List<Long> price = new ArrayList<>();
        players.add("545B7E7F41C744F8109847BF4621EBAF7EC56B26");
        players.add("6754B4E3C346E714195C0DA6B27566F615A0D06C");
        cityId.add(1);
        cityId.add(2);
        price.add(Long.valueOf(99200));
        price.add(Long.valueOf(80000));
        String result = hyperchainService.allocateCity(10, players, cityId, price);
        return result;
    }

//    @ResponseBody
//    @RequestMapping(value = "/startGame", method = RequestMethod.POST)
//    public String startGame(@RequestBody JSONObject params, HttpServletRequest request, HttpSession session) throws  Exception {
//
//        // 验证登陆
//        try {
//            boolean isLogin = (boolean) session.getAttribute("isLogin");
//            if (!isLogin) throw new AssertionError();
//        } catch (Exception e) {
//            System.out.println("Got a Exception：" + e.getMessage());
//            return "login expired";
//        }
//
//        // 缴纳入场费50SIG
//        String from = params.getString("playerAddress");
//        String to = deployAccount.getAddress();
//        String sig = "https://siege-token-sig-1";
//        long id;
//        try {
//            id = (long) session.getAttribute(sig);
//        } catch (Exception e) {
//            // 从区块链上查找
//            String getIdResult = hyperchainService.getId(sig, DEPLOY_ACCOUNT_JSON);
//            id = Long.valueOf(getValue(getIdResult));
//            // 将uri和id的映射写入session
//            session.setAttribute(sig, id);
//        }
//        long value = 50;
//        String data = "startGame";
//        String signature = params.getString("signature");
//        try {
//            String transferResult = hyperchainService.safeTransferFrom(from, to, id, value, data, signature);
//            if (transferResult.equals("transfer success")) {
//                // 将玩家加入匹配队列中
//                int matchResult = match(from);
//                if (matchResult == 1) {
//                    return "match success";
//                }
//                else if (matchResult == -1) {
//                    return "already in match queue";
//                }
//                else if (matchResult == 0) {
//                    return "match waiting";
//                }
//                else if (matchResult == -2){
//                    return "match error";
//                }
//                else {
//                    return "unknown error";
//                }
//            }
//            else {
//                return "transfer failed";
//            }
//        } catch (Exception e) {
//            System.out.println("Got a Exception：" + e.getMessage());
//            return "startGame failed";
//        }
//    }

//    @ResponseBody
//    @RequestMapping(value = "/pipei", method = RequestMethod.POST)
//    public String pipei(@RequestBody JSONObject params) throws Exception {
//
//        String playerAddress = params.getString("playerAddress");
//        int len = pipei.size();
//        assert (len < 50);
//        if (len == 49) {
//            pipei.add(playerAddress);
//            System.out.println(pipei.get(0));
//            System.out.println(pipei.get(1));
//            System.out.println(pipei.get(2));
//            pipei.clear();
//            return "匹配成功";
//        }
//        else {
//            pipei.add(playerAddress);
//            return "";
//        }
//    }

    @ResponseBody
    @RequestMapping(value = "/getPlayersTable", method = RequestMethod.POST)
    public String getPlayersTable(@RequestBody JSONObject params) throws Exception {

        String playerAddress = params.getString("playerAddress");
        String signature = params.getString("signature");

        String result = hyperchainService.getPlayersTable1(playerAddress, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getBiddingTb", method = RequestMethod.POST)
    public String getBiddingTb(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        int rankId = params.getInt("rankId");

        String result = hyperchainService.getBiddingTb(gameId, rankId);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getCitiesTable", method = RequestMethod.POST)
    public String getCitiesTable(@RequestBody JSONObject params) throws Exception {

        long gameId = params.getLong("gameId");
        long cityId = params.getLong("cityId");

        String result = hyperchainService.getCitiesTable(gameId, cityId);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getPlayersStatus", method = RequestMethod.POST)
    public String getPlayersStatus(@RequestBody JSONObject params) throws Exception {

        String address = params.getString("address");

        String result = hyperchainService.getPlayersStatus(address);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateCityBonus", method = RequestMethod.POST)
    public String updateCityBonus(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        long leftIntervalNum = params.getLong("leftIntervalNum");

        String result = hyperchainService.updateCityBonus(gameId, leftIntervalNum);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/occupy", method = RequestMethod.POST)
    public String occupy(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        String address = params.getString("address");
        int cityId = params.getInt("cityId");
        long amount = new Double(params.getDouble("amount") * SiegeParams.getPrecision()).longValue();
        String signature = params.getString("signature");


        String result = hyperchainService.occupyCity(gameId, address, cityId, amount, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateGameStage", method = RequestMethod.POST)
    public String updateGameStage(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        int stage = params.getInt("stage");


        String result = hyperchainService.updateGameStage(gameId, stage);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getStage", method = RequestMethod.POST)
    public String getStage(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");

        String result = hyperchainService.getStage(gameId);
        return result;
    }



    /*********************************************  Siege Params Configuration **************************************/

    @ResponseBody
    @RequestMapping(value = "/siegeParamsConfig", method = RequestMethod.POST)
    public String siegeParamsConfig() throws Exception {
        String assetAddress = Config.getAssetAddress();
        int precision = SiegeParams.getPrecision();
        int cityNum = SiegeParams.getCityNum();
        int enterFee = SiegeParams.getEnterFee();
        int cityPrice = SiegeParams.getCityPrice();
        int soldierNum = SiegeParams.getSoldierNum();
        int interval = SiegeParams.getInterval();
        int duration = SiegeParams.getGameDuration();
        List<Integer> soldiersPoint = SiegeParams.getSoldiersPoint();
        List<String> cityName = SiegeParams.getCityName();
        List<Integer> cityDefenseIndex = SiegeParams.getCityDefenseIndex();

        try {
            String result1 = hyperchainService.setAssetAddr(assetAddress);
            String result2 = hyperchainService.setPrecision(precision);
            String result3 = hyperchainService.setCityNum(cityNum);
            String result4 = hyperchainService.setEnterFee(enterFee);
            String result5 = hyperchainService.setCityPrice(cityPrice);
            String result6 = hyperchainService.setSoldierNum(soldierNum);
            String result7 = hyperchainService.setTime(interval, duration);
            String result8 = hyperchainService.setSoldiersPoint(soldiersPoint);
            List<byte[]> cityNameBytes = new ArrayList<>();
            for (String name : cityName) {
                cityNameBytes.add(name.getBytes());
            }
            String result9 = hyperchainService.setCityName(cityNameBytes);
            String result10 = hyperchainService.setCityDefenseIndex(cityDefenseIndex);

            if (result1.equals("success")
                    && result2.equals("success")
                    && result3.equals("success")
                    && result4.equals("success")
                    && result5.equals("success")
                    && result6.equals("success")
                    && result7.equals("success")
                    && result8.equals("success")
                    && result9.equals("success")
                    && result10.equals("success")) {
                return "set params success";
            }
            else {
                return "set params failed";
            }
        } catch (Exception e) {
            System.out.println("Got a Exception：" + e.getMessage());
            return "set params failed";
        }
    }

    /******************************************************  GameItem ***********************************************/

    @ResponseBody
    @RequestMapping(value = "/createAsset", method = RequestMethod.POST)
    public String createAsset(@RequestBody JSONObject params) throws Exception {

        String issuer = params.getString("issuer");
        long value = params.getLong("value") * SiegeParams.getPrecision();
        String symbol = params.getString("symbol");
        int type = params.getInt("type");

        String result = hyperchainService.create(issuer, value, symbol, type);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/issueCoin", method = RequestMethod.POST)
    public String issueCoin(@RequestBody JSONObject params) throws Exception {

        String to = params.getString("to");
        long value = params.getLong("value") * SiegeParams.getPrecision();
        String symbol = params.getString("symbol");
        String signature = params.getString("signature");

        String result = hyperchainService.issueCoin(to, value, symbol, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/transferCoin", method = RequestMethod.POST)
    public String transferCoin(@RequestBody JSONObject params) throws Exception {

        int precision = SiegeParams.getPrecision();
        String from = params.getString("from");
        String to = params.getString("to");
        long value = params.getLong("value") * precision;
        String symbol = "SIG";
        String data = params.getString("data");
        String signature = params.getString("signature");

        String result = hyperchainService.transfer(from, to, value, symbol, data, signature);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/balanceOfAsset", method = RequestMethod.POST)
    public String balanceOfAsset(@RequestBody JSONObject params) throws Exception {

        String owner = params.getString("owner");
        String symbol = params.getString("symbol");

        String result = hyperchainService.balanceOf(owner, symbol);
        return getValue(result);
    }

    @ResponseBody
    @RequestMapping(value = "/supplyOfAsset", method = RequestMethod.POST)
    public String supplyOfAsset(@RequestBody JSONObject params) throws Exception {

        String symbol = params.getString("symbol");
        int ext = params.getInt("ext");

        String result = hyperchainService.supplyOf(symbol, ext);
        return getValue(result);
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
    @RequestMapping(value = "/thread", method = RequestMethod.POST)
    public String thread(@RequestBody JSONObject params) {
        AsyncTaskService asyncTaskService = new AsyncTaskService();
        for (int i = 0; i < 20; i++) {
            asyncTaskService.executeAsyncTask(i);
        }
        return "ss";
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

//    private int match(String playerAddress) throws Exception {
//        int len = matchQueue.size();
//        // 暂时使用5人匹配做测试
//        assert (len < 5);
//        // 检查是否已经在匹配队列中
//        if (matchQueue.contains(playerAddress)) {
//            // 已经在匹配中，返回错误
//            return -1;
//        }
//        else {
//            // 将其加入匹配队列
//            if (len == 4) {
//                matchQueue.add(playerAddress);
//                String[] array = new String[matchQueue.size()];
//                for (int i = 0; i < matchQueue.size(); ++i) {
//                    array[i] = matchQueue.get(i);
//                }
//                String result = hyperchainService.startGame(array, DEPLOY_ACCOUNT_JSON);
//                if (result.equals("startGameSuccess")) {
//                    matchQueue.clear();
//                    // 匹配人数满，匹配成功
//                    return 1;
//                }
//                else {
//                    // 匹配过程出错
//                    return -2;
//                }
//            }
//            else {
//                matchQueue.add(playerAddress);
//                // 加入队列，匹配等待
//                return 0;
//            }
//        }
//    }
}
