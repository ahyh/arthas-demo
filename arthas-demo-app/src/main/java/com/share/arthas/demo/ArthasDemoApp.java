package com.share.arthas.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class
 *
 * @author share
 */
@EnableAsync
@SpringBootApplication
@MapperScan("com.share.arthas.demo.dao")
public class ArthasDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(ArthasDemoApp.class, args);
    }
}
