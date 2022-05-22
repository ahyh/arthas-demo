package com.share.arthas.demo.attach;

import com.alibaba.bytekit.utils.FileUtils;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.net.URL;
import java.security.ProtectionDomain;

/**
 * 自定义ClassFileTransformer
 */
public class MyAttachTransformer implements ClassFileTransformer {

    /**
     * 如果是testMethod方法，就通过改写字节码的方式改写此方法
     * 改写后的内容：System.out.println("update testMethod");
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
        for (MethodNode methodNode : classNode.methods) {
            if ("testMethod".equals(methodNode.name)) {
                InsnList insnList = new InsnList();
                insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                insnList.add(new LdcInsnNode("update testMethod"));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
                methodNode.instructions.insert(insnList);
                break;
            }
        }
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        // 加更改后的class写到文件中
        URL resource = MyAttachTransformer.class.getResource("/com/share/arthas/demo/attach/");
        byte[] bytes = classWriter.toByteArray();
        try {
            FileUtils.writeByteArrayToFile(new File(resource.getFile() + "EnhanceTestDemo.class"), bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return bytes;
    }
}
