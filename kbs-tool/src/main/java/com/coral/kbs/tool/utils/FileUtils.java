package com.coral.kbs.tool.utils;

import com.coral.kbs.tool.transform.html2markdown.FilesUtil;

public class FileUtils {
    public static String readContent(String file){
        return FilesUtil.readAll(file);
    }
}
