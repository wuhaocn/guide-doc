package org.coral.test.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;


public class GeneratorClassFactory {

    public static void main(String[] args) {
        try {
//            ClassReader cr = new ClassReader("org.coral.test.asm.AppInfoExt");
//            ClassWriter cw = new ClassWriter(0);
//            ClassVisitor classAdapter = new GeneratorClassAdapter(cw);
//            //使给定的访问者访问Java类的ClassReader
//            cr.accept(classAdapter, ClassReader.SKIP_DEBUG);
//            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "org/coral/test/asm/AppInfo", null, "java/lang/Object", null);
//            byte[] data = cw.toByteArray();
//            File file = new File("/Users/wuhao/data/code/coral-learning/guide-doc/kbs-source/kbs-java/out/production/classes/org/coral/test/asm/AppInfo.class");
//            FileOutputStream fout = new FileOutputStream(file);
//            fout.write(data);
//            fout.close();
            rename("org.coral.test.asm.AppInfo", "org.coral.test.asm.AbAppInfo", "java.lang.Object");
            rename("org.coral.test.asm.AppInfoExt", "org.coral.test.asm.AppInfo", "org.coral.test.asm.AbAppInfo");
            System.out.println("success!");
            String path = Class.forName("org.coral.test.asm.AppInfo").getProtectionDomain().getCodeSource().getLocation().getPath() + "/";
            String dstFile = "org.coral.test.asm.AppInfo";
            MyClassLouder myClassLouder = new MyClassLouder(path, dstFile, GeneratorClassFactory.class.getClassLoader());

            String pathe = Class.forName("org.coral.test.asm.AppInfo").getProtectionDomain().getCodeSource().getLocation().getPath() + "/";
            String dstFilee = "org.coral.test.asm.AbAppInfo";
            MyClassLouder myClassLoudere = new MyClassLouder(pathe, dstFilee, GeneratorClassFactory.class.getClassLoader());
            myClassLoudere.findClass("org.coral.test.asm.AbAppInfo");
            Class class1 = myClassLoudere.findClass("org.coral.test.asm.AbAppInfo");
            Class class2 = myClassLouder.findClass("org.coral.test.asm.AppInfo");

            AppInfo.bark1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rename(String source, String to, String pclass){
        try {
            ClassReader cr = new ClassReader(source);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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