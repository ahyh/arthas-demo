package com.share.arthas.demo.attach;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * agentmain方法
 */
public class MyAttachAgent {

    /**
     * 调用instrumentation的addTransformer以及retransformClasses完成字节码的改写
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("Loading attach agent......");
        ClassFileTransformer myAttachTransformer = new MyAttachTransformer();
        instrumentation.addTransformer(myAttachTransformer, true);
        if (instrumentation.isRedefineClassesSupported()) {
            Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
            for (Class<?> clazz : allLoadedClasses) {
                if (clazz.getName().contains("TestDemo")) {
                    try {
                        instrumentation.retransformClasses(clazz);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
