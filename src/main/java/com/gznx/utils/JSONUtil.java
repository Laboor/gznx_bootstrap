package com.gznx.utils;

import com.alibaba.fastjson.JSONObject;

public class JSONUtil {
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
}
