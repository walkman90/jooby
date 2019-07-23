package io.jooby.internal.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

public class ConstructorWriter {
  private static final Type OBJ = getType(Object.class);
  private static final Type PROVIDER = getType(Provider.class);
  private static final String PROVIDER_VAR = "provider";

  public void build(String ownerClass, String controllerClass, ClassWriter writer) {
    String provider = providerOf(Type.getObjectType(controllerClass));

    writer.visitField(ACC_PRIVATE, PROVIDER_VAR, PROVIDER.getDescriptor(), provider, null)
        .visitEnd();

    // Constructor:
    MethodVisitor constructor = writer
        .visitMethod(ACC_PUBLIC, "<init>",
            getMethodDescriptor(Type.VOID_TYPE, PROVIDER),
            getMethodDescriptor(Type.VOID_TYPE, getType(provider)),
            null);
    constructor.visitParameter(PROVIDER_VAR, 0);
    constructor.visitCode();
    constructor.visitVarInsn(ALOAD, 0);
    constructor.visitMethodInsn(INVOKESPECIAL, OBJ.getInternalName(), "<init>", "()V", false);
    constructor.visitVarInsn(ALOAD, 0);
    constructor.visitVarInsn(ALOAD, 1);
    constructor
        .visitFieldInsn(PUTFIELD, ownerClass.replace(".", "/"), PROVIDER_VAR, PROVIDER.getDescriptor());
    constructor.visitInsn(RETURN);
    constructor.visitMaxs(0, 0);
    constructor.visitEnd();
  }

  private static String providerOf(Type owner) {
    StringBuilder signature = new StringBuilder(PROVIDER.getDescriptor());
    signature.insert(signature.length() - 1, "<" + owner.getDescriptor() + ">");
    return signature.toString();
  }
}
