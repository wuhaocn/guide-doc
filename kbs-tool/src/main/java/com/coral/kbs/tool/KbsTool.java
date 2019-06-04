package com.coral.kbs.tool;

import com.coral.kbs.tool.sort.DocIndexUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Administrator on 2019/4/3.
 */
public class KbsTool {
    static FileOutputStream fileStream = null;

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/wuhao/data/code/github/framework/incubator-dubbo/dubbo-common/src/main/resources/META-INF/dubbo/internal");
        findAnrWrite(file, 0, false, "", false, 5);
        //findAnrWrite(file, 0, false, "", true);
        getFileStream().close();
    }

    private static boolean findAnrWrite(File file, int level, boolean guide, String index, boolean referIndex, int maxLevel) throws IOException {
        //是否需要跳过
        if (DocIndexUtils.isIgnoreFile(file, level, maxLevel, referIndex)){
            return false;
        }

        String fileName = "" + file.getName();

        if (file.isDirectory()) {
            List<File> fileList = DocIndexUtils.sortFile(file.listFiles());
            if (level == 0 && guide){
                getFileStream().write("* [知识库目录](#)\n".getBytes());
                getFileStream().flush();
            } else {
                StringBuilder sb = new StringBuilder();
                //dir
                if (guide){
                    for (int i = 0; i < level; i++){
                        sb.append("     ");
                    }
                    sb.append(" ");
                } else {
                    for (int i = 0; i < level; i++){
                        sb.append("#");
                    }
                }
                String pathNoWin =  file.getPath().replace("\\", "/");
                if (DocIndexUtils.containUrl){
                    sb.append(" [");
                } else {
                    sb.append(" ");
                }


                sb.append(fileName);
                if (DocIndexUtils.containUrl){
                    sb.append("](");
                    sb.append(pathNoWin);
                    sb.append(")\n");
                }else {
                    sb.append("\n");
                }

                getFileStream().write(sb.toString().getBytes());
                getFileStream().flush();
            }

            int subIndex = 1;
            //write file
            for (File fileItem : fileList) {
                String subIndexStr = index + subIndex + ".";
                if (findAnrWrite(fileItem, level + 1, guide, subIndexStr, referIndex, maxLevel)){
                    subIndex ++;
                }
            }
        } else if (!guide){
            StringBuilder sb = new StringBuilder();
            sb.append("    ").append(fileName).append("\n");
            getFileStream().write(sb.toString().getBytes());
            getFileStream().flush();
        }
        return true;
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
