package org.coral.test.asm;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author wuhao
 * @createTime 2021-04-25 15:40:00
 */
public class AppAdapterInfoTest {
    public static void main(String[] args) throws IOException {
        //AppInfoAdapter appInfoAdapter = new AppInfoAdapter();
        /**
         * https://juejin.cn/post/6844904112396615688
         * https://www.eclipse.org/aspectj/
         */
//        Unsafe.instance.arrayBaseOffset(AppInfoAdapter.class);
//        int aaa = Unsafe.getUnsafe().arrayBaseOffset(AppInfoAdapter.class);
//        System.out.println(aaa);
//        System.out.println(aaa);
        //AppInfo.fromDBWithNoCache(1);
        //AppInfoAdapter.fromDBWithNoCache(1);
        setXXX();
    }

    public static void setXXX() throws IOException {
        ClassReader classReader = new ClassReader("com.rcloud.api.models.ext.AppInfoAdapter");
        ClassWriter classWriter = new ClassWriter(classReader, 1);
//        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "com.rcloud.api.models.app.AppInfo", null, "java/lang/Object", null);
//        classWriter.visitEnd();
        byte[] code = classWriter.toByteArray();
        File file = new File("/Users/wuhao/data/code/rongcloud/com-rcloud-data/com-rcloud-db-appinfo/build/classes/java/main/com/rcloud/api/models/app/AppInfo.class");
        FileOutputStream output = new FileOutputStream(file);
        output.write(code);
        output.close();
    }
}
