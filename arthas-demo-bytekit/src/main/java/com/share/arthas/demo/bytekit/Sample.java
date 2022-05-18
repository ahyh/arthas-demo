package com.share.arthas.demo.bytekit;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 用于测试增强的类
 *
 * @author share
 */
public class Sample {

    long longField;
    String strField;
    static int intField;

    public int hello(String str, boolean exception) {
        if (exception) {
            throw new RuntimeException("test exception");
        }
        return str.length();
    }

    public long toBeInvoke(int i, long l, String s, long ll) {
        return l + ll;
    }

    public void testInvokeArgs() {
        toBeInvoke(1, 123L, "abc", 100L);
    }

    public String testIf(String arg) {
        if (arg == null) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.length(arg) <= 0) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.startsWith(arg, "test")) {
            return "test";
        }
        if (StringUtils.contains(arg, "test")) {
            return "test";
        }
        return "args: " + arg;
    }

    public void testThrowException(String info){
        if (info == null) {
            throw new RuntimeException("null info");
        }
        if (Objects.equals("", info)) {
            throw new RuntimeException("empty info");
        }
        if (StringUtils.contains(info, "test")) {
            throw new RuntimeException("test info");
        }
        if (StringUtils.contains(info, "harvey")) {
            throw new RuntimeException("invalid info");
        }
    }
}
