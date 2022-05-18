package com.share.arthas.demo.controller;

import com.share.arthas.demo.model.User;
import com.share.arthas.demo.model.condition.UserCondition;
import com.share.arthas.demo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RestController
public class ArthasDemoController {

    private static final Logger log = LoggerFactory.getLogger(ArthasDemoController.class);

    @Autowired
    private UserService userService;

    @GetMapping("list/user")
    public List<User> listAllUsers() {
        return userService.listUsers();
    }

    @GetMapping("get/user/{id}")
    public User getUserById(@PathVariable("id") long id) {
        if (id <= 0) {
            throw new RuntimeException("Invalid params");
        }
        return userService.getUserById(id);
    }

    @GetMapping("get/info")
    public User getUserInfo(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        String id = map.get("id");
        String name = map.get("name");
        if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
            return null;
        }
        User user = userService.getUserById(Long.parseLong(id));
        if (user == null) {
            return null;
        }
        if (!StringUtils.equals(name, user.getName())) {
            return null;
        }
        return user;
    }

    @PostMapping("user/create")
    public boolean createUser(@RequestBody UserCondition userCondition) {
        if (userCondition == null) {
            return false;
        }
        if (StringUtils.isBlank(userCondition.getName())
                || StringUtils.isBlank(userCondition.getBirthday())) {
            return false;
        }
        if (userCondition.getAge() <= 0 || userCondition.getAge() >= 150) {
            throw new RuntimeException("Invalid params");
        }
        try {
            User user = new User();
            user.setAge(userCondition.getAge());
            user.setName(userCondition.getName());
            user.setType(userCondition.getType());
            user.setRole(userCondition.getRole());
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(userCondition.getBirthday()));
            return userService.insert(user);
        } catch (Exception e) {
            log.error("insert user error:{}", e);
        }
        return false;
    }

    @PutMapping("user/update")
    public boolean updateUser(@RequestBody UserCondition userCondition){
        if (userCondition == null) {
            return false;
        }
        long id = userCondition.getId();
        if (id <= 0) {
            throw new RuntimeException("Invalid param");
        }
        if (StringUtils.isBlank(userCondition.getName())
                || StringUtils.isBlank(userCondition.getBirthday())) {
            return false;
        }
        if (userCondition.getAge() <= 0 || userCondition.getAge() >= 150) {
            throw new RuntimeException("Invalid params");
        }
        User user = userService.getUserById(id);
        if (user == null) {
            throw new RuntimeException("User not exist, id: " + id);
        }
        user.setAge(userCondition.getAge());
        user.setName(userCondition.getName());
        return userService.update(user);
    }
}
