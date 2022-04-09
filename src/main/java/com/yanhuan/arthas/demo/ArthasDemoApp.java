package com.yanhuan.arthas.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class
 *
 * @author yanhuan
 */
@SpringBootApplication
@MapperScan("com.yanhuan.arthas.demo.dao")
public class ArthasDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(ArthasDemoApp.class, args);
    }
}
