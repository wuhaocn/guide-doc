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
            rename("org.coral.test.asm.AppInfo", "org.coral.test.asm.AbAppInfo", "java/lang/Object");
            rename("org.coral.test.asm.AppInfoExt", "org.coral.test.asm.AppInfo", "org.coral.test.asm.AbAppInfo");
            System.out.println("success!");
            Class.forName("org.coral.test.asm.AbAppInfo");
            Class cl = Class.forName("org.coral.test.asm.AppInfo");
            AppInfo appInfo = (AppInfo) cl.newInstance();
            appInfo.bark1();

            Class<?> loadClass = ClassHotLoader.get("/Users/wuhao/data/code/coral-learning/guide-doc/kbs-source/kbs-java/out/production/classes")
                    .loadClass("org.coral.test.asm.AppInfo");
            Object person = loadClass.newInstance();
//            Method sayHelloMethod = loadClass.getMethod("sayHello");
//            sayHelloMethod.invoke(person);

//            Method sayHelloMethod = loadClass.getMethod("sayHello");
//            sayHelloMethod.invoke(person);
            System.out.println(person.toString());
            appInfo.bark1();
            System.gc();
            System.out.println(person.toString());
            appInfo.bark1();
//            String path = "/Users/wuhao/data/code/coral-learning/guide-doc/kbs-source/kbs-java/src/classes1/";
//            String dstFilee = "org.coral.test.asm.AbAppInfo";
//            MyClassLouder myClassLoudere = new MyClassLouder(path, dstFilee, GeneratorClassFactory.class.getClassLoader());
//
//            String dstFile = "org.coral.test.asm.AppInfo";
//            MyClassLouder myClassLouder = new MyClassLouder(path, dstFile, myClassLoudere);
//
//
//            myClassLoudere.findClass("org.coral.test.asm.AbAppInfo");
////            Class class1 = myClassLoudere.findClass("org.coral.test.asm.AbAppInfo");
//            Class tttv = myClassLouder.findClass("org.coral.test.asm.AppInfo");
//
//            tttv.newInstance();

            //AppInfo.bark1();
        } catch (Exception e) {
            e.printStackTrace();
        }
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