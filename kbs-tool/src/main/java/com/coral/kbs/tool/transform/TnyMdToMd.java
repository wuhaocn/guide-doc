package com.coral.kbs.tool.transform;

import com.coral.kbs.tool.sort.DocIndexUtils;
import com.coral.kbs.tool.transform.html2markdown.TraTool;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TnyMdToMd {
    public static void main(String[] args) throws IOException {
        findAnrWrite(new File("/Users/wuhao/data/code/github/book/guide-doc"));
    }
    private static boolean findAnrWrite(File file) throws IOException {

        if (file.isDirectory()) {
            List<File> fileList = DocIndexUtils.sortFile(file.listFiles());

            for (File fileItem : fileList) {
                findAnrWrite(fileItem);
            }
        } else{
            if (file.getName().endsWith(".tny")){
                String fileName = file.getAbsolutePath();
                String fileTarget =  fileName.replace(".tny", ".md");
                TraTool.tra(fileName, fileTarget);
            }
            if (file.getName().endsWith(".tny.md")){
                String fileName = file.getAbsolutePath();
                String fileTarget =  fileName.replace(".tny.md", ".md");
                TraTool.tra(fileName, fileTarget);
            }
        }
        return true;
    }
}
