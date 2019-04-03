package com.coral.kbs.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2019/4/3.
 */
public class KbsTool {
    static FileOutputStream fileStream = null;

    public static void main(String[] args) throws IOException {
        File file = new File("./");
        findAnrWrite(file, 0);
        getFileStream().close();
    }

    private static void findAnrWrite(File file, int index) throws IOException {
        if (file.getName().startsWith(".") && index != 0){
            return;
        }
        if (file.isDirectory()) {
            if (index == 0){
                getFileStream().write("## KBS知识库目录结构\n\n".getBytes());
            } else {
                StringBuilder sb = new StringBuilder();
                //dir
                for (int i = 0; i < index + 2; i++){
                    sb.append("#");
                }
                sb.append(" ").append(file.getPath()).append("\n\n");
                getFileStream().write(sb.toString().getBytes());
            }

            //write file
            for (File fileItem : file.listFiles()) {
                findAnrWrite(fileItem, index + 1);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("    ").append(file.getName()).append("\n");
            getFileStream().write(sb.toString().getBytes());
        }
    }

    private static FileOutputStream getFileStream() throws IOException {
        if (fileStream != null) {
            return fileStream;
        }
        File fileWrite = new File("readme.md");
        if (fileWrite.exists()) {
            fileWrite.delete();
        }
        fileWrite.createNewFile();
        fileStream = new FileOutputStream(fileWrite);
        return fileStream;
    }
}
