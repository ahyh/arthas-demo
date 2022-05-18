package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.AtEnter;

/**
 * Enter拦截器
 */
public class EnterInterceptor {

    /**
     * 通过拦截器处理，将enter目标方法对象的类的对象，field，method，methodName，methodDesc都收集起来
     */
    @AtEnter(inline = true
            , suppress = RuntimeException.class, suppressHandler = TestPrintSuppressHandler.class
    )
    public static long onEnter(
            @Binding.This Object object, @Binding.Class Object clazz,
            @Binding.Field(name = "longField") long longField,
            @Binding.Field(name = "longField") Object longFieldObject,
            @Binding.Field(name = "intField") int intField,
            @Binding.Field(name = "strField") String strField,
            @Binding.Field(name = "intField") Object intFielObject,
            @Binding.Method java.lang.reflect.Method method,
            @Binding.MethodName String methodName,
            @Binding.MethodDesc String methodDesc
    ) {
        System.err.println("onEnter, object:" + object);
        System.err.println("onEnter, methodObject: " + method);
        System.err.println("onEnter, methodName:" + methodName);
        System.err.println("onEnter, methodDesc:" + methodDesc);
        return 123L;
    }
}
