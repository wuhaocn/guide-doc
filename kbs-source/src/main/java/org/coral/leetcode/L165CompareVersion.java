package org.coral.leetcode;

public class L165CompareVersion {
    public static void main(String[] args) {
        System.out.println(com("01", "1"));
        System.out.println(com("1.1", "1.1"));
        System.out.println(com("1.3.1", "1.1"));
    }

    public static int com(String version1, String version2){
        return new L165CompareVersion().compareVersion(version1, version2);
    }

    public int compareVersion(String version1, String version2) {
        String[] nums1 = version1.split("\\.");
        String[] nums2 = version2.split("\\.");
        int i = 0, j = 0;
        while (i < nums1.length || j < nums2.length) {
            String num1 = i < nums1.length ? nums1[i] : "0";
            String num2 = j < nums2.length ? nums2[j] : "0";
            int res = compare(num1, num2);
            if (res == 0) {
                i++;
                j++;
            } else {
                return res;
            }
        }
        return 0;
    }

    private int compare(String num1, String num2) {
        //将高位的 0 去掉
        num1 = removeFrontZero(num1);
        num2 = removeFrontZero(num2);
        //先根据长度进行判断
        if (num1.length() > num2.length()) {
            return 1;
        } else if (num1.length() < num2.length()) {
            return -1;
        } else {
            //长度相等的时候
            for (int i = 0; i < num1.length(); i++) {
                if (num1.charAt(i) - num2.charAt(i) > 0) {
                    return 1;
                } else if (num1.charAt(i) - num2.charAt(i) < 0) {
                    return -1;
                }
            }
            return 0;
        }
    }

    private String removeFrontZero(String num) {
        while (num.startsWith("0")){
            num = num.substring(1);
        }
        return num;
    }
}
