package com.share.arthas.demo.attach;

/**
 * 演示attach API的demo
 */
public class TestDemo {

    public static void main(String[] args) throws Exception{
        while(true){
            Thread.sleep(5000);
            testMethod();
        }
    }

    public static void testMethod(){
        System.out.println("test method");
    }
}
