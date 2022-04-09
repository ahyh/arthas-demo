package com.yanhuan.arthas.demo.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PrintNumTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PrintNumTask.class);

    private int seed;

    public PrintNumTask(int seed) {
        this.seed = seed;
    }

    @Override
    public void run() {
        logger.info("PrintNumTask seed is:{}", seed);
        int i = 0;
        while (true) {
            logger.info("Loop :{} random number is:{}", i, new Random().nextInt(seed));
            try {
                TimeUnit.SECONDS.sleep(1);
                int result = geneTwoRandomIntAndAdd();
                logger.info("Loop :{} and result is:{}", i++, result);
            } catch (InterruptedException e) {
                logger.error("PrintNumberTask error:{}", e);
            }
        }
    }

    private int geneTwoRandomIntAndAdd(){
        Random random = new Random();
        int a = random.nextInt(seed);
        int b = random.nextInt(seed);
        return a + b;
    }
}
