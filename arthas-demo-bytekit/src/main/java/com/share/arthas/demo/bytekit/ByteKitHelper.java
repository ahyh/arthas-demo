package com.share.arthas.demo.bytekit;

import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.utils.AgentUtils;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.bytekit.utils.MatchUtils;
import com.alibaba.bytekit.utils.VerifyUtils;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 将需要拦截的类，拦截的方法都封装起来
 *
 * @author share
 */
public class ByteKitHelper {

    private Class<?> interceptorClass;

    private boolean redefine;

    private boolean reTransform;

    private String methodMatcher = "*";

    private boolean asmVerity = true;

    public static ByteKitHelper builder() {
        return new ByteKitHelper();
    }

    public ByteKitHelper interceptorClass(Class<?> interceptorClass) {
        this.interceptorClass = interceptorClass;
        return this;
    }

    public ByteKitHelper redefine(boolean redefine) {
        this.redefine = redefine;
        return this;
    }

    public ByteKitHelper reTransform(boolean reTransform) {
        this.reTransform = reTransform;
        return this;
    }

    public ByteKitHelper methodMatcher(String methodMatcher) {
        this.methodMatcher = methodMatcher;
        return this;
    }

    public byte[] process(Class<?> transform) throws Exception {
        // 指定默认的拦截器解析器
        DefaultInterceptorClassParser defaultInterceptorClassParser = new DefaultInterceptorClassParser();

        // 调用parse方法，获取InterceptorProcessor，解析拦截器类中的@At*注解，获取到*InterceptorProcessorParser类，用于后续处理
        List<InterceptorProcessor> interceptorProcessors = defaultInterceptorClassParser.parse(interceptorClass);

        // 读取原始class，获取classNode对象
        ClassNode classNode = AsmUtils.loadClass(transform);

        // 遍历所有方法，找到需要进行增强的方法
        List<MethodNode> matchedMethods = new ArrayList<MethodNode>();
        for (MethodNode methodNode : classNode.methods) {
            if (MatchUtils.wildcardMatch(methodNode.name, methodMatcher)) {
                matchedMethods.add(methodNode);
            }
        }

        // 遍历所有需要增强的方法，调用拦截器处理器进行处理
        for (MethodNode methodNode : matchedMethods) {
            MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode);
            for (InterceptorProcessor interceptor : interceptorProcessors) {
                interceptor.process(methodProcessor);
            }
        }

        byte[] bytes = AsmUtils.toBytes(classNode);
        if (asmVerity) {
            VerifyUtils.asmVerify(bytes);
        }

        if (redefine) {
            AgentUtils.redefine(transform, bytes);
        }

        if (reTransform) {
            AgentUtils.reTransform(transform, bytes);
        }

        return bytes;
    }
}
