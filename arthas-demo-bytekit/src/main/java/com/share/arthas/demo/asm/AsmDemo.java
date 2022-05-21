package com.share.arthas.demo.asm;

import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.lang.String;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static jdk.internal.org.objectweb.asm.Opcodes.ASM5;

/**
 * 使用ASM core API来解析class文件的结构并改变class文件，添加field, method等
 *
 * @author yanhuan
 */
public class AsmDemo {

    /**
     * 文件是一个class文件
     *
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
     */
    public static void printFieldAndMethod(byte[] bytes) throws Exception {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(0);
        Map<String, Map<Integer, Integer>> OFFSET_LINE_MAP = new HashMap<>();
        ClassVisitor classVisitor = new ClassVisitor(ASM5, cw) {

            @Override
            public void visitAttribute(Attribute attribute) {
                System.out.println(attribute);
                super.visitAttribute(attribute);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                System.out.println("field name: " + name);
                System.out.println("field access: " + access);
                System.out.println("field desc: " + desc);
                System.out.println("field signature: " + signature);
                System.out.println("field value: " + value);
                return super.visitField(access, name, desc, signature, value);
            }

            /**
             * 通过visitMethod方法收集，方法的code属性字节码偏移量与行号的对应关系
             */
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                System.out.println("method name:" + name);
                MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
                Map<Integer, Integer> lineMap = new LinkedHashMap<>();
                return new AdviceAdapter(ASM5, methodVisitor, access, name, desc) {

                    @Override
                    public void visitLineNumber(int i, Label label) {
                        lineMap.put(label.getOffset(), i);
                        super.visitLineNumber(i, label);
                    }

                    @Override
                    public void visitEnd() {
                        OFFSET_LINE_MAP.put(name, lineMap);
                        super.visitEnd();
                    }
                };
            }
        };

        // SKIP_CODE: 跳过方法中的Code属性
        // SKIP_DEBUG: 跳过类文件中的调试信息
        // SKIP_FRAMES: 跳过StackMapTable属性
        // EXPAND_FRAMES: 展开StackMapTable属性
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        System.out.println(OFFSET_LINE_MAP);
    }

    /**
     * 新增一个Filed
     *
     * @param bytes     源class byte数组
     * @param access    新增加的属性的访问标记，public/private/static
     * @param name      新增加的属性名
     * @param desc      新增加的属性类型
     * @param signature 新增加的属性类型的泛型类型
     * @param value     新增加的属性的值
     */
    public static void addField(byte[] bytes, int access, String name, String desc, String signature, Object value) throws Exception {
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
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        byte[] bytesModified = cw.toByteArray();
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "addFieldDemo.class"), bytesModified);
    }

    /**
     * 删除一个Filed
     */
    public static void removeField(byte[] bytes, String fieldName) throws Exception {
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
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        byte[] bytesModified = cw.toByteArray();
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "removeFieldDemo.class"), bytesModified);
    }

    /**
     * 新增一个Filed
     */
    public static void addMethod(byte[] bytes, int access, String name, String desc, String signature, String[] exceptions) throws Exception {
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
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        byte[] bytesModified = cw.toByteArray();
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "addMethodDemo.class"), bytesModified);
    }

    /**
     * 删除一个method
     */
    public static void removeMethod(byte[] bytes, String methodName) throws Exception {
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
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "removeMethodDemo.class"), bytesModified);
    }

    /**
     * 修改方法内容
     *
     * @param bytes      class byte数组
     * @param methodName 需要修改的方法名称
     */
    public static void updateMethod(byte[] bytes, String methodName) throws Exception {
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
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "updateMethodDemo.class"), bytesModified);
    }

    /**
     * 通过AdviceAdapter在方法的开始和结束前插入代码
     *
     * @param bytes      class文件
     * @param methodName 方法名
     * @param addInfo    方法修改信息
     */
    public static void addEnterInfo4Method(byte[] bytes, String methodName, String addInfo) throws Exception {
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
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "enterMethodDemo.class"), bytesModified);
    }

    /**
     * add try-catch block
     */
    public static void addTryCatchBlock(byte[] bytes, String methodName, String addInfo) throws Exception {
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

                    private void finallyBlock(int opcode) {
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
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "tryCatchMethodDemo.class"), bytesModified);
    }

    public static void main(String[] args) throws Exception {
        // 获取class文件的字节数组
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/MyDemo.class");
        byte[] bytes = getBytes(resource.getFile());
        printFieldAndMethod(bytes);

        // addField(bytes, Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, "xyz", "Ljava/lang/String;", null, "ABC");
        // removeField(bytes, "a");
        // addMethod(bytes, Opcodes.ACC_PUBLIC, "newMethod", "(ILjava/lang/String;)Ljava/util/List;", null, new String[]{"Exception"});
        // removeMethod(bytes, "test01");
        // updateMethod(bytes, "test01");
        // addEnterInfo4Method(bytes, "test01", "asm add info");
        // addTryCatchBlock(bytes, "test01", "asm add info");
    }
}
