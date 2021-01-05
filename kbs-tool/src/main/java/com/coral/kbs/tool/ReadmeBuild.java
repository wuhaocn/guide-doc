package com.coral.kbs.tool;

import com.coral.kbs.tool.sort.DocIndexUtils;
import com.coral.kbs.tool.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2019/4/3.
 */
public class ReadmeBuild {
    static boolean isGuide = true;
    static boolean containUrl = true;
    static String baseUrl = "https://github.com/wuhaocn/guide-doc/blob/master/";
    static FileOutputStream fileStream = null;

    public static void main(String[] args) throws IOException {
        File file = new File("docs");
        //头部标题
        if (isGuide){
            writeHeader();
        }
        writeContent(file, 0, isGuide, "", true, 3);
        //底部标题
        if (isGuide){
            writeBoot();
        }

        //findAnrWrite(file, 0, false, "", true);
        getFileStream().close();
    }

    /**
     *
     * @param file
     * @param level
     * @param guide
     * @param index
     * @param referIndex 参照索引
     * @param maxLevel
     * @return
     * @throws IOException
     */
    private static boolean writeContent(File file, int level, boolean guide, String index, boolean referIndex, int maxLevel) throws IOException {
        //是否需要跳过
        if (DocIndexUtils.isIgnoreFile(file, level, maxLevel, referIndex)){
            return false;
        }
        String aPath = file.getAbsolutePath();
        if (aPath.contains(" ")){
            File fileT = new File(aPath.replace(" ", ""));
            file.renameTo(fileT);
            file.delete();
            file = fileT;
        }

        String fileName = "" + file.getName();
        appendFileItem(file, level, guide, index, fileName, maxLevel);
        if (file.isDirectory()) {
            List<File> fileList = DocIndexUtils.sortFile(file.listFiles());
            int subIndex = 1;
            //write file
            for (File fileItem : fileList) {
                String subIndexStr = index + subIndex + ".";
                if (writeContent(fileItem, level + 1, guide, subIndexStr, referIndex, maxLevel)){
                    subIndex ++;
                }
            }
        }
        return true;
    }

    private static void appendFileItem(File file, int level, boolean guide, String index, String fileName, int maxLevel) throws IOException {
        if (level  > 0) {
            StringBuilder sb = new StringBuilder();
            //dir
            if (guide){
                for (int i = 0; i < level; i++){
                    sb.append("  ");
                }
                sb.append("*");
            } else {
                for (int i = 0; i < level; i++){
                    sb.append("#");
                }
            }
            String pathNoWin =  file.getPath().replace("\\", "/");
            if (containUrl){
                sb.append(" [");
            } else {
                sb.append(" ");
            }


            sb.append(fileName);
            if (containUrl){
                sb.append("](");
                sb.append(baseUrl);
                sb.append(pathNoWin);
                sb.append(")\n");

            }else {
                sb.append("\n");
            }

            getFileStream().write(sb.toString().getBytes());
            getFileStream().flush();
        }
    }
    /**
     * 头部标题
     * @throws IOException
     */
    private static void writeHeader() throws IOException {
        getFileStream().write(FileUtils.readContent("header.md").getBytes());
    }

    /**
     * 底部标题
     * @throws IOException
     */
    private static void writeBoot() throws IOException {
        getFileStream().write(FileUtils.readContent("boot.md").getBytes());
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
