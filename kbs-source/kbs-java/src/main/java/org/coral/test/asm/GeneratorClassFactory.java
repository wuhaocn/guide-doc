package org.coral.test.asm;

import org.coral.test.loader.ClassHotLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;


public class GeneratorClassFactory {

    public static void main(String[] args) {
        try {
            rename("org.coral.test.asm.AppInfo", "org.coral.test.asm.AbAppInfo", "java/lang/Object");
            rename("org.coral.test.asm.AppInfoExt", "org.coral.test.asm.AppInfo", "java/lang/Object");
            System.out.println("rename success!");
//            Class.forName("org.coral.test.asm.AbAppInfo");
//            Class cl = Class.forName("org.coral.test.asm.AppInfo");
//            AppInfo appInfo = (AppInfo) cl.newInstance();
//            System.out.println("xxx:");appInfo.bark1();
//            System.out.println("xxx:");System.out.println(appInfo.toString());
            String path = "/Users/wuhao/data/code/coral-learning/guide-doc/kbs-source/kbs-java/src/classes1";
            Class<?> loadClassAbAppInfo = ClassHotLoader.get(path).loadClass("org.coral.test.asm.AbAppInfo");
            Class<?> loadClassAppInfo = ClassHotLoader.get(path).loadClass("org.coral.test.asm.AppInfo");
            Object appInfo =  loadClassAppInfo.newInstance();
            print(appInfo);
//            System.gc();
//            print(appInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void print(Object appInfo){
        System.out.println(appInfo.toString());
//        System.out.print("object:");appInfo.bark1();
        System.out.print("static:");AppInfo.bark1();
    }

    public static void rename(String source, String to, String pclass) {
        try {
            ClassReader cr = new ClassReader(source);
            ClassWriter cw = new ClassWriter(0);
            ClassVisitor classAdapter = new GeneratorClassAdapter(cw);
            //使给定的访问者访问Java类的ClassReader
            cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, to.replace(".", "/"), null, pclass, null);
            byte[] data = cw.toByteArray();
            String path = Class.forName(source).getProtectionDomain().getCodeSource().getLocation().getPath() + "/";
            String dstFile = to.replace(".", "/") + ".class";
            File file = new File(path + dstFile);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(data);
            fout.close();
            System.out.println("success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}