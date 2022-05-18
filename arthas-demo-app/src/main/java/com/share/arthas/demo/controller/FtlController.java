package com.share.arthas.demo.controller;

import com.share.arthas.demo.model.User;
import com.share.arthas.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class FtlController {

    @Autowired
    private UserService userService;

    @RequestMapping("/success")
    public String toSuccess(HttpServletRequest request, HttpServletResponse response, ModelMap model){
        long user_id = ServletRequestUtils.getLongParameter(request, "user_id", 0);
        User user = userService.getUserById(user_id);
        if (user != null) {
            model.put("user", user);
            return "success";
        } else {
            model.put("errorMsg", "user not exist!");
            return "fail";
        }
    }

}
