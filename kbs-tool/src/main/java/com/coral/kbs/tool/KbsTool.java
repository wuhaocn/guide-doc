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
        findAnrWrite(file, 0, true);
        findAnrWrite(file, 0, false);
        getFileStream().close();
    }

    private static void findAnrWrite(File file, int level, boolean guide) throws IOException {
        int index = 1;
        if (file.getName().startsWith(".") && level != 0){
            return;
        }
        if (file.getName().endsWith("jpg") || file.getName().endsWith("png") ){
            return;
        }
        if (file.isDirectory()) {
            if (file.getName().endsWith("kbs-tool")){
                return;
            }
            if (level == 0){
                getFileStream().write("* [知识库目录](#)\n".getBytes());
                getFileStream().flush();
            } else {
                StringBuilder sb = new StringBuilder();
                //dir

                if (guide){
                    for (int i = 0; i < level; i++){
                        sb.append("     ");
                    }
                    sb.append("*");
                } else {
                    for (int i = 0; i < level; i++){
                        sb.append("#");
                    }
                }
                String pathNoWin =  file.getPath().replace("\\", "/");
                String paths[] = pathNoWin.split("/");
                sb.append(" [").append(paths[paths.length -1]).append("](");
                sb.append(pathNoWin);
                sb.append(")\n");
                getFileStream().write(sb.toString().getBytes());
                getFileStream().flush();
            }

            //write file
            for (File fileItem : file.listFiles()) {
                findAnrWrite(fileItem, level + 1, guide);
            }
        } else if (!guide){
            StringBuilder sb = new StringBuilder();
            sb.append("    ").append(file.getName()).append("\n");
            getFileStream().write(sb.toString().getBytes());
            getFileStream().flush();
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
