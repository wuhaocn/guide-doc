package org.coral.test.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 */
public class GeneratorClassAdapter extends ClassVisitor {

    public GeneratorClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM6, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        return cv.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        //添加字段
        cv.visitEnd();
    }


}