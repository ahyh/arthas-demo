package com.share.arthas.demo.asm;

public class MyDemo {

    private static final String DEMO_STATIC_STRING = "static_string";

    private int a = 0;
    private int b = 1;

    public void test01() {
        System.out.println("test01");
    }

    public void test02() {
        try {
            int num = 10;
            int div = 0;
            System.out.println(num / div);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int cal(int a, int b, char f) {
        if (f == '+') {
            return a + b;
        } else if (f == '-') {
            return a - b;
        } else if (f == '*') {
            return a * b;
        } else if (f == '/') {
            return a / b;
        }
        throw new RuntimeException("invalid char");
    }

    public static void main(String[] args) {
        System.out.println(1);
        System.out.println(cal(10,1, '+'));
    }
}
