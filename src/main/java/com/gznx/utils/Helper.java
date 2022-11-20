package com.gznx.utils;

import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {

    // 字符串是否JSON格式
    public static boolean isJSON(String str) {
        boolean result = false;
        try {
            JSONObject.parseObject(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public static String dateFormat(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(date);
    }
}
