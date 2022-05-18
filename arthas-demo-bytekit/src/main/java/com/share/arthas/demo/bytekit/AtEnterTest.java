package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.utils.Decompiler;

public class AtEnterTest {

    public static void main(String[] args) throws Exception {
        // 指定拦截器和拦截的方法
        ByteKitHelper helper = ByteKitHelper.builder().interceptorClass(EnterInterceptor.class).methodMatcher("hello")
                .reTransform(true);

        // 处理拦截的类
        byte[] bytes = helper.process(Sample.class);

        // 调用增强后的hello方法
        int result = new Sample().hello("abc", false);
        System.out.println("result:" + result);
        System.err.println(Decompiler.decompile(bytes));
    }

}
