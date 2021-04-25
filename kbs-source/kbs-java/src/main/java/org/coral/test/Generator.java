package org.coral.test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;


public class Generator {

    public static void main(String[] args) {
        try {
            ClassReader cr = new ClassReader("com.rcloud.api.models.app.AppInfo");
            ClassWriter cw = new ClassWriter(0);
            ClassVisitor classAdapter = new AddTimeClassAdapter(0);
            //使给定的访问者访问Java类的ClassReader
            cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "com.rcloud.api.models.app.AppInfo", null, "java/lang/Object", null);
            byte[] data = cw.toByteArray();
            File file = new File("/Users/wuhao/data/code/rongcloud/com-rcloud-data/com-rcloud-db-appinfo/build/classes/java/main/com/rcloud/api/models/app/AppInfo.class");
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(data);
            fout.close();
           // rename("com.rcloud.api.models.app.AppInfo", "com.rcloud.api.models.app.AbAppInfo", null);
          //  rename("com.rcloud.api.models.app.AppInfoAdapter", "com.rcloud.api.models.app.AppInfo", "com.rcloud.api.models.app.AbAppInfo");
            System.out.println("success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rename(String source, String to, String pclass){
        try {
            ClassReader cr = new ClassReader(source);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor classAdapter = new AddTimeClassAdapter(ClassWriter.COMPUTE_MAXS);
            //使给定的访问者访问Java类的ClassReader
            cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, to, null, pclass, null);
            byte[] data = cw.toByteArray();
            String []items11 = to.split("\\.");
            String basePath = "/Users/wuhao/data/code/rongcloud/com-rcloud-data/com-rcloud-db-appinfo/build/classes/java/main/com/rcloud/api/models/app/";
            File file = new File(basePath + items11[items11.length -1]+".class");
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(data);
            fout.close();
            System.out.println("success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}