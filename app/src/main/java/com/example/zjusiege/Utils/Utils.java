package com.example.zjusiege.Utils;

import com.example.zjusiege.SiegeParams.SiegeParams;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String getValue(String input) {
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

    public static List<Long> getListValue(String input, boolean duplicate) {
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

    public static String returnString(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            return(jsonObject.getString("value"));
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return "";
        }
    }

    public static double returnDouble(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            double value = Double.valueOf(valueStr);
            if (index == 1) {
                return value / 100;
            }
            else {
                return value / SiegeParams.getPrecision();
            }
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0.;
        }
    }

    public static boolean returnBool(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            return valueStr.equals("true");
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return false;
        }
    }

    public static int returnInt(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            return Integer.valueOf(valueStr);
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0;
        }
    }

    public static long returnLong(String input, int index) {
        try {
            JSONArray jsonArray = JSONArray.fromObject(input);
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            String valueStr = jsonObject.getString("value");
            return Long.valueOf(valueStr);
        } catch (Exception e) {
            System.out.println("Got an exception: " + e.getMessage());
            return 0;
        }
    }
}
