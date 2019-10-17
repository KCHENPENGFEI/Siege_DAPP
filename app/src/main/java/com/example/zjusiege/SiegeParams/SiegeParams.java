package com.example.zjusiege.SiegeParams;

import java.util.Arrays;
import java.util.List;

public class SiegeParams {

    /******************************************************  游戏参数设置 ***********************************************/
    private static int precision = 10000;
    private static int cityNum = 5;
    private static int playerNum = 5;
    private static int enterFee = 50;
    private static int cityPrice = 6;
    private static int soldierNum = 5;
    private static int interval = 10;
    private static int gameDuration = 1800;

    private static List<Integer> soldiersPoint = Arrays.asList(0, 10, 15, 20, 25, 30);
    private static List<String> soldiersName = Arrays.asList("none", "infantry", "spearman", "shieldman", "archer", "cavalry");
    private static List<String> soldiersDescription = Arrays.asList("", "", "", "", "", "");
    public enum gameStage {
        START,
        BIDDING,
        RUNNING,
        SETTLING,
        ENDING
    }
//    private static List<String> cityName = Arrays.asList(
//            "长安",
//            "燕京",
//            "洛阳",
//            "金陵",
//            "荆州",
//            "汴州",
//            "临安",
//            "徐州",
//            "襄阳",
//            "汉中",
//            "咸阳",
//            "益州",
//            "晋阳"
//    );
    private static List<String> cityName = Arrays.asList(
            "",
            "长安",
            "燕京",
            "洛阳",
            "金陵",
            "荆州"
    );
    private static List<Integer> cityDefenseIndex = Arrays.asList(0, 115, 110, 106, 100, 100);

    public void setPrecision(int pre) {
        precision = pre;
    }

    public void setCityNum(int cn) {
        cityNum = cn;
        playerNum = cn * 2;
    }

//    public void setPlayerNum(int pn) {
//        playerNum = pn;
//    }

    public void setEnterFee(int ef) {
        enterFee = ef;
    }

    public void setCityPrice(int cp) {
        cityPrice = cp;
    }

    public void setSoldierNum(int sn) {
        soldierNum = sn;
    }

    public void setInterval(int inter) {
        interval = inter;
    }

    public void setGameDuration(int gd) {
        gameDuration = gd;
    }

    public void setSoldiersPoint(List<Integer> sp) {
        soldiersPoint = sp;
    }

    public void setCityName(List<String> cn) {
        cityName = cn;
    }

    public void setCityDefenseIndex(List<Integer> cdi) {
        cityDefenseIndex = cdi;
    }


    public static int getEnterFee() {
        return enterFee;
    }

    public static int getPrecision() {
        return precision;
    }

    public static int getCityNum() {
        return cityNum;
    }

    public static int getPlayerNum() {
        return playerNum;
    }

    public static int getCityPrice() {
        return cityPrice;
    }

    public static int getSoldierNum() {
        return soldierNum;
    }

    public static int getInterval() {
        return interval;
    }

    public static int getGameDuration() {
        return gameDuration;
    }

    public static List<Integer> getSoldiersPoint() {
        return soldiersPoint;
    }

    public static List<String> getSoldiersName() {
        return soldiersName;
    }

    public static List<String> getSoldiersDescription() {
        return soldiersDescription;
    }

    public static List<String> getCityName() {
        return cityName;
    }

    public static List<Integer> getCityDefenseIndex() {
        return cityDefenseIndex;
    }

    public static int getGameStage(gameStage stage) {
        return stage.ordinal();
    }


    /******************************************************  游戏细节参数设置 ***********************************************/
    private static long registrationReward = 10000;

    public void setRegistrationReward(long rr) {
        registrationReward = rr;
    }

    public static long getRegistrationReward() {
        return registrationReward;
    }

}
