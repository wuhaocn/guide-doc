//package org.coral.test.loader;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class ServiceClassLoader extends ClassLoader{
//
//    private String classPath;
//
//    public ServiceClassLoader(String classPath) {
//        this.classPath = classPath;
//    }
//
//    /**
//     * 重写父类的findClass 方法。  父类的loadClass会调用此方法
//     */
//    @Override
//    protected Class<?> findClass(String name) throws ClassNotFoundException {
//
//        Class<?> c = null;
//
//        byte[] classData = getClassData(name);
//
//        if (classData!=null) {
//            c = defineClass(name, classData, 0, classData.length);
//        }else {
//            throw new ClassNotFoundException();
//        }
//
//        return c;
//    }
//　　　　　　 // 将class文件通过IO流读取，转化为字节数组
//    private byte[] getClassData(String name) {
//
//        String path = classPath + "/"+ name.replace('.', '/') + ".class";
//
//        InputStream iStream = null;
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try {
//            iStream = new FileInputStream(path);
//
//            byte[] buffer = new byte[1024];
//            int temp = 0;
//            while ((temp = iStream.read(buffer))!=-1) {
//                byteArrayOutputStream.write(buffer, 0, temp);
//            }
//            if (byteArrayOutputStream!=null) {
//                return byteArrayOutputStream.toByteArray();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            try {
//                if (iStream!=null) {
//                    iStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                if (byteArrayOutputStream!=null) {
//                    byteArrayOutputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//}