package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtExceptionExit;

/**
 * 异常退出方法拦截器
 */
public class ExceptionExitInterceptor {

    /**
     * 将方法体中的代码增强到目标方法中
     */
    @AtExceptionExit(inline = true)
    public static void atExceptionExit(@Binding.This Object object,
                                       @Binding.Class Object clazz,
                                       @Binding.Args Object[] args,
                                       @Binding.Throwable Throwable throwable,
                                       @Binding.Line int line
    ) {
        System.err.println("atExceptionExit: this" + object);
        System.err.println("atExceptionExit: clazz" + clazz.toString());
        System.err.println("atExceptionExit: args" + args);
        System.err.println("atExceptionExit: exception" + throwable);
        if (throwable != null) {
            String firstLine = throwable.getStackTrace()[0].toString();
            String[] split = firstLine.split(":");
            String s = split[1];
            line = Integer.parseInt(s.substring(0, s.length()-1));
        }
        System.err.println("atExceptionExit: line" + line);
    }

}
