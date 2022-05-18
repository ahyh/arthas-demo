package com.share.arthas.demo.controller;

import com.share.arthas.demo.helper.UserHelper;
import com.share.arthas.demo.model.User;
import com.share.arthas.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserHelper userHelper;

    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") Long id){
        User user = userService.getUserById(id);
        return user;
    }

    @GetMapping("/arthas-test/{info}")
    public User testArthas(@PathVariable("info") Integer info){
        Long id = 1L;
        if (info == null) {
            User user = userService.getUserById(id);
            return user;
        }
        if (info == 0) {
            userHelper.testEmptyMethod();
        }
        if (info == 1) {
            userHelper.testEmptyWithArg(String.valueOf(info));
        }
        if (info >= 20) {
            if (info == 20) {
                userHelper.testReturnEmpty(null);
            } else if (info == 21) {
                userHelper.testReturnEmpty("");
            } else if(info ==22){
                userHelper.testReturnEmpty("test");
            } else if(info == 23){
                userHelper.testReturnEmpty("harvey");
            } else if (info == 24) {
                userHelper.testReturnEmpty("abc");
            }
        }
        if (info >= 30) {
            if (info == 30) {
                userHelper.testReturn(null);
            } else if (info == 31) {
                userHelper.testReturn("");
            } else if(info ==32){
                userHelper.testReturn("test");
            } else if(info == 33){
                userHelper.testReturn("harvey");
            } else if (info == 34) {
                userHelper.testReturn("abc");
            }
        }
        if (info >= 40) {
            if (info == 40) {
                userHelper.checkThrowException(null);
            } else if (info == 41) {
                userHelper.checkThrowException("");
            } else if(info ==42){
                userHelper.checkThrowException("test");
            } else if(info == 43){
                userHelper.checkThrowException("harvey");
            } else if (info == 44) {
                userHelper.checkThrowException("abc");
            }
        }
        if (info >= 50) {
            if (info == 50) {
                userHelper.testLambda(null);
            } else if (info == 51) {
                userHelper.testLambda("abctestcba");
            } else if (info == 52) {
                userHelper.testLambda("abceharvey");
            } else if (info == 53) {
                userHelper.testLambda("abcdharvey");
            } else if (info == 54) {
                userHelper.testLambda("mnafe");
            }
        }
        User user = userService.getUserById(id);
        return user;
    }
}
