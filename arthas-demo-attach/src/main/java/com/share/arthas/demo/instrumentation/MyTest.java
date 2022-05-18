package com.share.arthas.demo.instrumentation;

/**
 * for instrumentation test
 *
 * @author share
 */
public class MyTest {

    public static void main(String[] args) {
        new MyTest().foo();
    }

    public void foo(){
        method1();
        method2();
    }

    public void method1(){

    }

    public void method2(){

    }
}
