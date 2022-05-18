package com.share.arthas.demo.asm;

import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import org.apache.commons.io.FileUtils;

import java.io.File;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;


public class AsmDemo {

    /**
     * 文件是一个class文件
     * @param path
     * @return
     * @throws Exception
     */
    public static byte[] getBytes(String path) throws Exception {
        File file = new File(path);
        byte[] bytes = FileUtils.readFileToByteArray(file);
        return bytes;
    }

    /**
     * 读取class文件中所有的field和method
     *
     * @param path class文件的路径
     */
    public static void printFieldAndMethod(String path) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                System.out.println("field name: " + name);
                System.out.println("field access: " + access);
                System.out.println("field desc: " + desc);
                System.out.println("field signature: " + signature);
                System.out.println("field value: " + value);
                return super.visitField(access, name, desc, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                System.out.println("method name:" + name);
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                FieldVisitor fieldVisitor = cw.visitField(Opcodes.ACC_PUBLIC, "xyz", "Ljava/lang/String", null, "init_value");
                if (fieldVisitor != null) {
                    fieldVisitor.visitEnd();
                }
            }
        };

        // SKIP_CODE: 跳过方法中的Code属性
        // SKIP_DEBUG: 跳过类文件中的调试信息
        // SKIP_FRAMES: 跳过StackMapTable属性
        cr.accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
    }

    /**
     * 新增一个Filed
     */
    public static void addField(String path, int access, String name, String desc, String signature, Object value) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                FieldVisitor fieldVisitor = cw.visitField(access, name, desc, signature, value);
                if (fieldVisitor != null) {
                    fieldVisitor.visitEnd();
                }
            }
        };
        cr.accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//newMyDemo.class"), bytesModified);
    }

    /**
     * 删除一个Filed
     */
    public static void removeField(String path, String fieldName) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                if (name.equals(fieldName)) {
                    // 返回null即表示需要移除a
                    return null;
                }
                return super.visitField(access, name, desc, signature, value);
            }
        };
        cr.accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//removeFieldDemo.class"), bytesModified);
    }

    /**
     * 新增一个Filed
     */
    public static void addMethod(String path, int access, String name, String desc, String signature, String[] exceptions) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            @Override
            public void visitEnd() {
                super.visitEnd();
                MethodVisitor methodVisitor = cw.visitMethod(access, name, desc, signature, exceptions);
                if (methodVisitor != null) {
                    methodVisitor.visitEnd();
                }
            }
        };
        cr.accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//newMyDemo2.class"), bytesModified);
    }

    /**
     * 删除一个method
     */
    public static void removeMethod(String path, String methodName) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals(methodName)) {
                    return null;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
        cr.accept(classVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//removeMethodDemo.class"), bytesModified);
    }

    /**
     * 修改方法内容
     *
     * @param path       class文件路径
     * @param methodName 需要修改的方法名称
     */
    public static void updateMethod(String path, String methodName) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        // 自动计算操作数栈和局部变量表大小
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            //删除原方法
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (name.equals(methodName)) {
                    return null;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            //新增一个和之前删除方法同名的方法，达到更新方法的目的
            @Override
            public void visitEnd() {
                MethodVisitor methodVisitor = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, "(I)I", null, new String[]{"Exception"});
                methodVisitor.visitCode();
                methodVisitor.visitVarInsn(Opcodes.ILOAD, 1);
                methodVisitor.visitIntInsn(Opcodes.BIPUSH, 100);
                methodVisitor.visitInsn(Opcodes.IADD);
                methodVisitor.visitInsn(Opcodes.IRETURN);
                // 触发计算操作数栈和局部变量表大小
                methodVisitor.visitMaxs(0, 0);
                methodVisitor.visitEnd();
            }
        };
        cr.accept(classVisitor, 0);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//updateMethodDemo.class"), bytesModified);
    }

    /**
     * 通过AdviceAdapter在方法的开始和结束前插入代码
     * @param path class文件路径
     * @param methodName 方法名
     * @param addInfo 方法修改信息
     */
    public static void addEnterInfo4Method(String path, String methodName, String addInfo) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        // 自动计算操作数栈和局部变量表大小
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            //删除原方法
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                if (!name.equals(methodName)) {
                    return methodVisitor;
                }
                return new AdviceAdapter(ASM5, methodVisitor, access, name, desc) {
                    @Override
                    protected void onMethodEnter() {
                        // 进入方法新增System.out.println("enter {name} : {addInfo}")
                        super.onMethodEnter();
                        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream");
                        methodVisitor.visitLdcInsn("enter " + name + " : " + addInfo);
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    }

                    @Override
                    protected void onMethodExit(int opCode) {
                        // 退出方法新增System.out.println("normal/error exist : {name}")
                        super.onMethodExit(opCode);
                        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream");
                        if (opCode == Opcodes.ATHROW) {
                            methodVisitor.visitLdcInsn("error exist : " + name);
                        } else {
                            methodVisitor.visitLdcInsn("normal exist : " + name);
                        }
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    }
                };
            }
        };
        cr.accept(classVisitor, 0);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//enterMethodDemo.class"), bytesModified);
    }

    public static void addTryCatchBlock(String path, String methodName, String addInfo) throws Exception {
        byte[] bytes = getBytes(path);
        ClassReader cr = new ClassReader(bytes);
        // 自动计算操作数栈和局部变量表大小
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            //删除原方法
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                if (!name.equals(methodName)) {
                    return methodVisitor;
                }
                // 标签用于执行语句跳转
                Label startLabel = new Label();
                return new AdviceAdapter(ASM5, methodVisitor, access, name, desc) {
                    @Override
                    protected void onMethodEnter() {
                        // 进入方法新增System.out.println("enter {name} : {addInfo}")
                        super.onMethodEnter();
                        methodVisitor.visitLabel(startLabel);
                        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream");
                        methodVisitor.visitLdcInsn("enter " + name + " : " + addInfo);
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    }

                    @Override
                    public void visitMaxs(int maxStack, int maxLocals) {
                        // 生成异常表
                        Label endLabel = new Label();
                        methodVisitor.visitTryCatchBlock(startLabel, endLabel, endLabel, null);
                        methodVisitor.visitLabel(endLabel);
                        finallyBlock(ATHROW);
                        methodVisitor.visitInsn(ATHROW);
                        super.visitMaxs(maxStack, maxLocals);
                    }

                    private void finallyBlock(int opcode){
                        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream");
                        if (opcode == Opcodes.ATHROW) {
                            methodVisitor.visitLdcInsn("error exist : " + name);
                        } else {
                            methodVisitor.visitLdcInsn("normal exist : " + name);
                        }
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                    }

                    @Override
                    protected void onMethodExit(int opCode) {
                        // 退出方法新增System.out.println("normal/error exist : {name}")
                        super.onMethodExit(opCode);
                        if (opCode != Opcodes.ATHROW) {
                            finallyBlock(opCode);
                        }
                    }
                };
            }
        };
        cr.accept(classVisitor, 0);
        byte[] bytesModified = cw.toByteArray();
        FileUtils.writeByteArrayToFile(new File("D://file//tryCatchMethodDemo.class"), bytesModified);
    }

    public static void main(String[] args) throws Exception {
        // printFieldAndMethod("D://file//MyDemo.class");
        // addField("D://file//MyDemo.class", Opcodes.ACC_PUBLIC, "xyz", "Ljava/lang/String", null, null);
        // printFieldAndMethod("D://file//newMyDemo.class");
        // addMethod("D://file//MyDemo.class", Opcodes.ACC_PUBLIC, "newMethod", "(ILjava/lang/String;)V", null, new String[]{"Exception"});
        // printFieldAndMethod("D://file//newMyDemo2.class");
        // removeField("D://file//MyDemo.class", "a");
        // removeMethod("D://file//MyDemo.class", "test01");
        // updateMethod("D://file//MyDemo.class", "test01");

        // addEnterInfo4Method("D://file//MyDemo.class", "test01", "asm add info");
        addTryCatchBlock("D://file//MyDemo.class", "test01", "asm add info");
    }
}
