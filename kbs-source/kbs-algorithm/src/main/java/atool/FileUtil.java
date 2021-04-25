package atool;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * <b>描述: </b>用于序列化时使用的辅助类,很简单的读取和写入文件的代码，序列化稳定运行后此类不应该再出现
 * <p>
 * <b>功能: </b>很简单的读取和写入文件的代码
 * <p>
 * <b>用法: </b>很简单的读取和写入文件的代码
 * <p>
 *
 *
 */
public class FileUtil {


    public static String read(String path) {
        return read(new File(path));
    }

    public static String read(File file) {
        if (!file.exists()){
            return "";
        }
        StringBuilder content = new StringBuilder();
        BufferedReader reader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {

                }

            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }

        }
        return content.toString();
    }

}
