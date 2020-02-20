package org.coral.juc;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListMapTest {
    private static Map<String, String> map = new ConcurrentSkipListMap<String, String>();
    public static void main(String[] args) {

        map.put("1", "1");
        map.put("2", "2");
        map.put("3", "3");
        System.out.println(map.get("3"));
    }

}
