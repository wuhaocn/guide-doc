package com.coral.kbs.tool.transform;

import com.coral.kbs.tool.sort.DocIndexUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TnyRenameToMd {
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
                File fileT = new File(file.getAbsolutePath().replace(".tny", ".tny.md"));
                file.renameTo(fileT);
            }
        }
        return true;
    }
}
