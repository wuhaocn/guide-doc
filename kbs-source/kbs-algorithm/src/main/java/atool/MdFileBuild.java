package atool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MdFileBuild {


    public static void main(String[] args) throws IOException {
        String path = "kbs-source/kbs-algorithm/src/main/java";
        String toFile = "kbs-source/kbs-algorithm/book.md";

        FileWriter fileWriter = new FileWriter(toFile);
        MdFileBuild mdFileBuild = new MdFileBuild();
        mdFileBuild.buildFile(path, fileWriter, 1);
        fileWriter.flush();
        fileWriter.close();
    }

    public void buildFile(String path,  FileWriter fileWriter, int index) throws IOException {
        File file = new File(path);
        File fileList[] = file.listFiles();
        int seq = 0;
        for (File fileItem : fileList) {
            if (fileItem.isDirectory()){
                seq++;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= index + 1; i++) {
                    sb.append("#");
                }
                sb.append(" ");
                sb.append(seq).append(".");
                sb.append(fileItem.getName()).append("\n");
                fileWriter.write(sb.toString());
            } else {
                //
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= index + 2; i++) {
                    sb.append("#");
                }
                sb.append(" ");
                sb.append(seq).append(".");
                sb.append(fileItem.getName()).append("\n");
                fileWriter.write("\n");

                fileWriter.write("* 问题\n");
                fileWriter.write("\n");
                //fileWriter.write("```\n");
                fileWriter.write(FileUtil.read(fileItem.getParentFile().getAbsolutePath()+ "/" + "readme.md"));
                //fileWriter.write("```\n");
                fileWriter.write("\n");

                fileWriter.write("* 解答\n");
                fileWriter.write("```\n");
                fileWriter.write(FileUtil.read(fileItem.getParentFile().getAbsolutePath()+ "/" + "Solution.java"));
                fileWriter.write("```\n");


                break;
            }

            if (fileItem.isDirectory() ){
                buildFile(fileItem.getAbsolutePath(), fileWriter, index + 1);
            }

        }
    }
}
