package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtExit;

/**
 * 自定义Interceptor
 * 在这个类中描述字节码增强的具体信息
 */
public class ExitInterceptor {

    /**
     * 将方法体中的代码增强到目标方法中
     */
    @AtExit(inline = true)
    public static void atExit(@Binding.This Object object,
                              @Binding.Class Object clazz,
                              @Binding.Return Object ret,
                              @Binding.Line int line
    ) {
        System.err.println("AtExit: this" + object);
        System.err.println("AtExit: clazz" + clazz.toString());
        System.err.println("AtExit: return" + ret);
        System.err.println("AtExit: line" + line);
    }
}
