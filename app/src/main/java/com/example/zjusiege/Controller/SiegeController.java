package com.example.zjusiege.Controller;


import com.example.zjusiege.AsyncTaskService;
import com.example.zjusiege.Config.Config;
import com.example.zjusiege.Service.FiloopService;
import com.example.zjusiege.SiegeParams.SiegeParams;
import com.example.zjusiege.Utils.Utils;
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
//    private HyperchainService hyperchainService = new HyperchainService();
    private FiloopService filoopService = new FiloopService();
    private final String DEPLOY_ACCOUNT_JSON = "{\"address\":\"11BD06F184F3767FC02C7F27E812F51BC6F28B39\",\"publicKey\":\"04D46EDF9AF28D2E911816973805B686539517177EE0598A34A21FA101F511B7AEE9E987509EE033CB1D6C222F44B86C37EC8869F93A551A1CF262D267A0668D56\",\"privateKey\":\"00C744D486012CBE1F32F618967DBCE69B706F9D74FC456A1430EFB94CA43E68AB\",\"privateKeyEncrypted\":false}";

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
//    @CrossOrigin
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestBody JSONObject params, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
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
                String newAccountString = filoopService.register();
                JSONObject newAccountJson = JSONObject.fromObject(newAccountString);
                // 获取账号地址
                String address = newAccountJson.getString("address");
                JSONObject jsonObject = new JSONObject()
                            .element("stage", "register")
                            .element("status", true)
                            .element("account", newAccountJson);
                    return jsonObject.toString();
                // 发放注册奖励
                // TODO
//                String issueResult = filoopService.issueCoin(address, value, symbol, deployAccountJson);
//                if (issueResult.equals("issue success")) {
//                    JSONObject jsonObject = new JSONObject()
//                            .element("stage", "register")
//                            .element("status", true)
//                            .element("account", newAccountJson);
//                    return jsonObject.toString();
//                }
//                else {
//                    return "register failed";
//                }
            } catch (Exception e) {
                System.out.println("Got a Exception：" + e.getMessage());
                JSONObject jsonObject = new JSONObject()
                        .element("stage", "register")
                        .element("status", false)
                        .element("account", "");
                return jsonObject.toString();
            }
        }
        else {
            JSONObject jsonObject = new JSONObject()
                    .element("stage", "register")
                    .element("status", false)
                    .element("account", "");
            return jsonObject.toString();
        }
    }

    @ResponseBody
    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestBody JSONObject params, HttpServletRequest request, HttpSession session) {
        // 检查用户信息
        String paramsString = params.toString();
        String address = params.getString("address");
        // 玩家登陆
        JSONObject loginJson = new JSONObject();
        //TODO
//        String loginResult = hyperchainService.login(paramsString);
        String loginResult = filoopService.login(paramsString);
        if (!loginResult.equals("contract calling error") && !loginResult.equals("unknown error")) {
            loginJson.element("stage", "login")
                     .element("status", true);
        }
        else {
            loginJson.element("stage", "login")
                     .element("stage", false);
        }
        return loginJson.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/jsonTest", method = RequestMethod.POST)
    public String jsonTest() {
//        System.out.println(params);
//        String test = "{\"ni\": \"wo\"}";
//        JSONObject jsonObject = JSONObject.fromObject(test);
        String jsonMese = "{\"语文\":\"88\",\"数学\":\"78\",\"计算机\":\"99\"}";
        JSONObject myJson = JSONObject.fromObject(jsonMese);
//        JSONObject m =
//        System.out.println(jsonObject);
        JSONObject yy = new JSONObject()
                .element("a", "a");
        System.out.println("yy" + yy);
        return "aaa";
    }

    @ResponseBody
    @CrossOrigin
    @RequestMapping(value = "/checkPlayerStatus", method = RequestMethod.POST)
    public String checkPlayer(@RequestBody JSONObject params) throws Exception {
        String address = params.getString("address");
        JSONObject jsonObject = checkPlayerStatus(address);
        return jsonObject.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public String clear(@RequestBody JSONObject params) throws Exception {
        String add1 = params.getString("address1");
        String add2 = params.getString("address2");
        String add3 = params.getString("address3");
        String add4 = params.getString("address4");
        // TODO
//        hyperchainService.cp(add1);
//        hyperchainService.cp(add2);
//        hyperchainService.cp(add3);
//        hyperchainService.cp(add4);
        filoopService.cp(add1);
        filoopService.cp(add2);
        filoopService.cp(add3);
        filoopService.cp(add4);
        return "success";
    }

    @ResponseBody
    @RequestMapping(value = "/setRemain", method = RequestMethod.POST)
    public String setRemain(@RequestBody JSONObject params) throws Exception {
        int gameId = params.getInt("gameId");
        int num = params.getInt("num");
        // TODO
//        return hyperchainService.setRemain(gameId, num);
        return filoopService.setRemain(gameId, num);
    }

    @ResponseBody
    @RequestMapping(value = "/ba", method = RequestMethod.POST)
    public String battleEnd(@RequestBody JSONObject params) throws Exception {
        int gameId = params.getInt("gameId");
        String attacker = params.getString("attacker");
        String defender = params.getString("defender");
        int cityId = params.getInt("cityId");
        // TODO
//        return hyperchainService.battleEnd(gameId, attacker, defender, cityId);
        return filoopService.battleEnd(gameId, attacker, defender, cityId);
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
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public String gett() throws Exception {
        // TODO
//        String result1 = hyperchainService.getCity();
//        String result2 = hyperchainService.getSo();
//        String result3 = hyperchainService.getDe();
//        String result4 = hyperchainService.getPrecision();
//        String result5 = hyperchainService.getCityNum();
//        String result6 = hyperchainService.getEnterFee();
//        String result7 = hyperchainService.getCityPrice();
//        String result8 = hyperchainService.getSoldierNum();
//        String result9 = hyperchainService.getInterval();
//        String result10 = hyperchainService.getGameAssetAddr();
        String result1 = filoopService.getCity();
        String result2 = filoopService.getSo();
        String result3 = filoopService.getDe();
        String result4 = filoopService.getPrecision();
        String result5 = filoopService.getCityNum();
        String result6 = filoopService.getEnterFee();
        String result7 = filoopService.getCityPrice();
        String result8 = filoopService.getSoldierNum();
        String result9 = filoopService.getInterval();

        System.out.println("result1" + result1);
        System.out.println("result2" + result2);
        System.out.println("result3" + result3);
        System.out.println("result4" + result4);
        System.out.println("result5" + result5);
        System.out.println("result6" + result6);
        System.out.println("result7" + result7);
        System.out.println("result8" + result8);
        System.out.println("result9" + result9);
//        System.out.println("result10" + result10);
        return "11";
    }

    @ResponseBody
    @RequestMapping(value = "/clearCity", method = RequestMethod.POST)
    public String cityClear(@RequestBody JSONObject params) throws Exception {
        int gameId = params.getInt("gameId");
        // TODO
//        return hyperchainService.cc(gameId);
        return filoopService.cc(gameId);
    }

    @ResponseBody
    @RequestMapping(value = "/getGameData", method = RequestMethod.POST)
    public String getGameData(@RequestBody JSONObject params) throws Exception {
        String address = params.getString("address");
        int pointer = params.getInt("pointer");
        // TODO
//        return hyperchainService.getGameData(address, pointer);
        return filoopService.getGameData(address, pointer);
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
        // TODO
//        String result = hyperchainService.updateRankingTb(10, ranking, playerAddresses, price, time);
        String result = filoopService.updateRankingTb(10, ranking, playerAddresses, price, time);
        System.out.println(result);
    }

    @ResponseBody
    @RequestMapping(value = "/getGlobalTb", method = RequestMethod.POST)
    public String getG(@RequestBody JSONObject params) throws Exception {
        int gameId = params.getInt("gameId");
        // TODO
//        String result = hyperchainService.getGlobalTb(gameId);
//        return result;
        return filoopService.getGlobalTb(gameId);
    }

    @ResponseBody
    @RequestMapping(value = "/getFrozenTb", method = RequestMethod.POST)
    public String getFrozenTb(@RequestBody JSONObject params) throws Exception {
        String address = params.getString("address");
        // TODO
//        String result = hyperchainService.getFrozenTb(address);
//        return result;
        return filoopService.getFrozenTb(address);
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
        // TODO
//        String result = hyperchainService.allocateCity(10, players, cityId, price);
//        return result;
        return filoopService.allocateCity(10, players, cityId, price);
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
    @RequestMapping(value = "/getBiddingTb", method = RequestMethod.POST)
    public String getBiddingTb(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        int rankId = params.getInt("rankId");
        // TODO
//        String result = hyperchainService.getBiddingTb(gameId, rankId);
//        return result;
        return filoopService.getBiddingTb(gameId, rankId);
    }

    @ResponseBody
    @RequestMapping(value = "/getCitiesTable", method = RequestMethod.POST)
    public String getCitiesTable(@RequestBody JSONObject params) throws Exception {

        long gameId = params.getLong("gameId");
        long cityId = params.getLong("cityId");
        // TODO
        return filoopService.getCitiesTb(gameId, cityId);
//        String result = hyperchainService.getCitiesTable(gameId, cityId);
//        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getPlayersStatus", method = RequestMethod.POST)
    public String getPlayersStatus(@RequestBody JSONObject params) throws Exception {

        String address = params.getString("address");
        // TODO
        return filoopService.getPlayersStatus(address);
//        String result = hyperchainService.getPlayersStatus(address);
//        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateCityBonus", method = RequestMethod.POST)
    public String updateCityBonus(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        long leftIntervalNum = params.getLong("leftIntervalNum");
        // TODO
//        String result = hyperchainService.updateCityBonus(gameId, leftIntervalNum);
//        return result;
        return filoopService.updateCityBonus(gameId, leftIntervalNum);
    }

    @ResponseBody
    @RequestMapping(value = "/occupy", method = RequestMethod.POST)
    public String occupy(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        String address = params.getString("address");
        int cityId = params.getInt("cityId");
        long amount = new Double(params.getDouble("amount") * SiegeParams.getPrecision()).longValue();
        String signature = params.getString("signature");
        // TODO
//        String result = hyperchainService.occupyCity(gameId, address, cityId, amount, signature);
//        return result;
        return filoopService.occupyCity(gameId, address, cityId, amount, signature);
    }

    @ResponseBody
    @RequestMapping(value = "/updateGameStage", method = RequestMethod.POST)
    public String updateGameStage(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        int stage = params.getInt("stage");
        // TODO
//        String result = hyperchainService.updateGameStage(gameId, stage);
//        return result;
        return filoopService.updateGameStage(gameId, stage);
    }

    @ResponseBody
    @RequestMapping(value = "/getStage", method = RequestMethod.POST)
    public String getStage(@RequestBody JSONObject params) throws Exception {

        int gameId = params.getInt("gameId");
        // TODO
//        String result = hyperchainService.getStage(gameId);
//        return result;
        return filoopService.getStage(gameId);
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
//            String result1 = hyperchainService.setAssetAddr(assetAddress);
            // TODO
//            String result2 = hyperchainService.setPrecision(precision);
//            String result3 = hyperchainService.setCityNum(cityNum);
//            String result4 = hyperchainService.setEnterFee(enterFee);
//            String result5 = hyperchainService.setCityPrice(cityPrice);
//            String result6 = hyperchainService.setSoldierNum(soldierNum);
//            String result7 = hyperchainService.setTime(interval, duration);
//            String result8 = hyperchainService.setSoldiersPoint(soldiersPoint);
            String result2 = filoopService.setPrecision(precision);
            String result3 = filoopService.setCityNum(cityNum);
            String result4 = filoopService.setEnterFee(enterFee);
            String result5 = filoopService.setCityPrice(cityPrice);
            String result6 = filoopService.setSoldierNum(soldierNum);
            String result7 = filoopService.setTime(interval, duration);
            String result8 = filoopService.setSoldiersPoint(soldiersPoint);
            List<byte[]> cityNameBytes = new ArrayList<>();
            for (String name : cityName) {
                cityNameBytes.add(name.getBytes());
            }
            String result9 = filoopService.setCityName(cityNameBytes);
            String result10 = filoopService.setCityDefenseIndex(cityDefenseIndex);

//            if (result7.equals("success")) {
            if (result2.equals("success")
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
        // TODO
//        String result = hyperchainService.create(issuer, value, symbol, type);
//        return result;
        return filoopService.create(issuer, value, symbol, type);
    }

    @ResponseBody
    @RequestMapping(value = "/issueCoin", method = RequestMethod.POST)
    public String issueCoin(@RequestBody JSONObject params) throws Exception {

        String to = params.getString("to");
        long value = params.getLong("value") * SiegeParams.getPrecision();
        String symbol = params.getString("symbol");
        String signature = params.getString("signature");
        // TODO
//        String result = hyperchainService.issueCoin(to, value, symbol, signature);
//        return result;
        return filoopService.issueCoin(to, value, symbol, signature);
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
        // TODO
//        String result = hyperchainService.transfer(from, to, value, symbol, data, signature);
//        return result;
        return filoopService.transfer(from, to, value, symbol, data, signature);
    }

    @ResponseBody
    @RequestMapping(value = "/balanceOfAsset", method = RequestMethod.POST)
    public String balanceOfAsset(@RequestBody JSONObject params) throws Exception {

        String owner = params.getString("owner");
        String symbol = params.getString("symbol");
        // TODO
//        String result = hyperchainService.balanceOf(owner, symbol);
        String result = filoopService.balanceOf(owner, symbol);
        return getValue(result);
    }

    @ResponseBody
    @RequestMapping(value = "/supplyOfAsset", method = RequestMethod.POST)
    public String supplyOfAsset(@RequestBody JSONObject params) throws Exception {

        String symbol = params.getString("symbol");
        int ext = params.getInt("ext");
        // TODO
//        String result = hyperchainService.supplyOf(symbol, ext);
        String result = filoopService.supplyOf(symbol, ext);
        return getValue(result);
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

    private JSONObject checkPlayerStatus(String address) throws Exception {
        // 对链上数据进行查询
        // 获取用户信息
        JSONObject response = new JSONObject();
        // TODO
        String playerInfo = filoopService.getPlayersStatus(address);
        if (!playerInfo.equals("contract calling error") && !playerInfo.equals("unknown error")) {
            // 用户存在
            int gameId = Utils.returnInt(playerInfo, 0);
            String identity = Utils.returnString(playerInfo, 1);
            String opponent = Utils.returnString(playerInfo, 2).toUpperCase();
            int cityId = Utils.returnInt(playerInfo, 3);
            String playerStage = Utils.returnString(playerInfo, 4);
            if (gameId != 0) {
                // 玩家已经成功匹配，处于游戏中
                // 查询指定gameId的游戏状态
                // TODO
                String globalInfo = filoopService.getGlobalTb(gameId);
                if (!globalInfo.equals("contract calling error") && !globalInfo.equals("unknown error")) {
                    // 获取游戏阶段
                    String[] gameStage = new String[]{"start", "bidding", "running", "settling", "ending"};
                    int gameStageInt = Utils.returnInt(globalInfo, 1);
                    // 对response进行赋值
                    response.element("stage", "checkPlayerStatus")
                            .element("status", true)
                            .element("gameId", gameId)
                            .element("identity", identity)
                            .element("gameStage", gameStage[gameStageInt]);
                    if (gameStageInt == 2) {
                        // 游戏处于running阶段， 需要判断玩家状态
                        if (playerStage.equals("beforeBattle") || playerStage.equals("inBattle")) {
                            // 处于准备对战或者对战界面
                            if (cityId == 0) {
                                // 玩家为进攻者，需要查询对手的城池id
                                // TODO
                                String opponentInfo = filoopService.getPlayersStatus(opponent);
                                cityId = Utils.returnInt(opponentInfo, 3);
                            }
                            response.element("playerStage", playerStage)
                                    .element("opponent", opponent)
                                    .element("cityId", cityId);
                        }
                        else {
                            // 处于map界面
                            response.element("playerStage", "cityMap");
                        }
                    }
                    return response;
                }
                else {
                    response.element("stage", "error")
                            .element("message", "game not exist");
                }
            }
            else {
                // 玩家未开始游戏，准备进入匹配
                response.element("stage", "checkPlayerStatus")
                        .element("status", true)
                        .element("gameStage", "startGame")
                        .element("gameId", 0);
            }
        }
        else {
            // 用户不存在，返回错误
            response.element("stage", "error")
                    .element("message", "player not exist");
        }
        return response;
    }
}
