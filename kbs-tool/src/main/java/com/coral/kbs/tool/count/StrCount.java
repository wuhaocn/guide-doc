package com.coral.kbs.tool.count;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wuhao
 * @createTime 2021-05-18 11:41:00
 */
public class StrCount {
    public static void main(String[] args) {
        String[] refers = StrCountRefer.str.split("\n");
        HashMap<String, String> hashMap = new HashMap();
        System.out.println("StrCountRefer init count:" + refers.length);
        //排重
        for (String item : refers) {
            hashMap.put(item, item);
        }
        System.out.println("StrCountRefer multi remove count:" + hashMap.size());
        //输出
        for (Map.Entry<String, String> stringEntry : hashMap.entrySet()) {
            System.out.println(stringEntry.getKey());
        }

    }
}
