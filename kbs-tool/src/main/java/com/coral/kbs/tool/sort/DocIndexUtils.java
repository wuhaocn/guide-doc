package com.coral.kbs.tool.sort;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DocIndexUtils {

    public static boolean containUrl = false;

    public static List<String> sort(String[] unSortStringArray) {
        List<String> unSortStringList = Arrays.asList(unSortStringArray);
        return sort(unSortStringList);
    }

    public static List<String> sort(List<String> unSortStringList) {
        unSortStringList.sort(new Comparator<String>() {
            public int compare(String o1, String o2) {
                try {
                    return compareString(o1, o2);
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        return unSortStringList;
    }

    public static List<File> sortFile(File[] unSortFileArray) {
        List<File> unSortFileList = Arrays.asList(unSortFileArray);
        return sortFile(unSortFileList);
    }

    public static List<File> sortFile(List<File> unSortFileList) {
        unSortFileList.sort(new Comparator<File>() {
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile()){
                    return 1;
                }
                if (o1.isFile() && o2.isDirectory()){
                    return -1;
                }
                try {
                    return compareString(o1.getName(), o2.getName());
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        return unSortFileList;
    }

    private static int compareString(String o1, String o2){
        String[] itemArray1 = o1.split("\\.");
        String[] itemArray2 = o2.split("\\.");
        if ((itemArray1.length >= 2) && (itemArray2.length >= 2)) {
            int int1 = Integer.parseInt(itemArray1[0]);
            int int2 = Integer.parseInt(itemArray2[0]);
            return int1 - int2;
        }
        return o1.charAt(0) - o2.charAt(0);
    }


    public static boolean isIndex(String name) {
        try {
            String[] itemArray = name.split("\\.");
            if (itemArray.length >= 2) {
                int itemInt = Integer.parseInt(itemArray[0]);
                return true;
            }
            return false;
        } catch (Exception e){
            return false;
        }
    }

    public static boolean isTmp(String name) {
        try {
            for (String ignoreFile: ignoreFiles) {
                if (name.startsWith(ignoreFile)){
                    return true;
                }
            }

            return false;
        } catch (Exception e){
            return false;
        }
    }
    public static boolean isIgnoreFile(File file, int level) {
        return isIgnoreFile(file, level, 999, true);
    }
    public static boolean isIgnoreFile(File file, int level, int maxLevel, boolean referIndex) {

        if (maxLevel <level){
            return true;
        }

        String name = file.getName();
        if (referIndex){
            if (!isIndex(name) && level != 0){
                return true;
            }
        }
        if (isTmp(name)){
            return true;
        }
        if (name.endsWith("jpg") || name.endsWith("png") ){
            return true;
        }
        if (name.endsWith("kbs-tool")){
            return true;
        }
        return false;
    }

    final static String[] ignoreFiles = new String[]{".", "build", "tmp" , "out", "src", "target", "gradle"};
}
