package com.share.arthas.demo.controller;

import com.share.arthas.demo.task.PrintNumTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @PostMapping("/task/{type}/{seed}")
    public String startTask(@PathVariable("type") String type,
                            @PathVariable("seed") int seed){
        PrintNumTask task = new PrintNumTask(seed);
        new Thread(task, "task-print-number").start();
        return "success";
    }
}
