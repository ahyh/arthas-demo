package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.utils.Decompiler;

public class AtExitTest {

    public static void main(String[] args) throws Exception {
        // 指定Interceptor和目标方法, 设置reTransform为true
        ByteKitHelper helper = ByteKitHelper.builder().interceptorClass(ExitInterceptor.class).methodMatcher("testIf")
                .reTransform(true);

        // 处理Sample类
        byte[] bytes = helper.process(Sample.class);

        // 调用增强后的方法
        new Sample().testIf(null);

        // 查看增强后的字节码
        System.err.println(Decompiler.decompile(bytes));
    }
}
