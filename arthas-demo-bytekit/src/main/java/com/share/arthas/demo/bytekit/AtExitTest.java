package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.utils.Decompiler;
import com.alibaba.bytekit.utils.FileUtils;
import com.share.arthas.demo.asm.AsmDemo;

import java.io.File;
import java.net.URL;

/**
 * 测试ExitInterceptor
 *
 * @author yanhuan
 */
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

        // 输出到文件, 从输出的class文件可以看到, 目标方法的字节码被修改了, 根据拦截器中加了@AtExit方法中@Binding**定义, 做了相应的增强，
        // 可以获取到想要的信息
        URL resource = AsmDemo.class.getResource("/com/share/arthas/demo/asm/");
        FileUtils.writeByteArrayToFile(new File(resource.getFile() + "ExitEnhanceSample.class"), bytes);
    }
}
