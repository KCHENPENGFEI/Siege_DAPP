package com.example.zjusiege.SiegeParams;

public class SiegeParams {
    private static int precision = 10000;
    private static int cityNum = 5;
    private static int playerNum = 10;
    private static int enterFee = 100;
    private static int cityPrice = 6;
    private static int soldierNum = 5;
    private static int interval = 10;
    private static int gameDuration = 3600;

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

}
