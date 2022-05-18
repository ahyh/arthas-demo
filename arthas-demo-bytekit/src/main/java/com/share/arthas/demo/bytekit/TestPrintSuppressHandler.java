package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.annotation.ExceptionHandler;

public class TestPrintSuppressHandler {

    @ExceptionHandler(inline = true)
    public static void onSuppress(@Binding.Throwable Throwable e, @Binding.Class Object clazz) {
        System.err.println("exception handler: " + clazz);
        e.printStackTrace();
    }

}
