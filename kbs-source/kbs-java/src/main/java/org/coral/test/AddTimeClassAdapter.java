package org.coral.test;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class AddTimeClassAdapter extends ClassVisitor {

    public AddTimeClassAdapter(int n) {
        super(n);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        //添加字段
        super.visitEnd();
    }


}