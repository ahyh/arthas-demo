package com.share.arthas.demo.listener;

import com.google.gson.Gson;
import com.share.arthas.demo.contants.Constants;
import com.share.arthas.demo.events.AddUserEvent;
import com.share.arthas.demo.model.User;
import com.share.arthas.demo.model.UserChange;
import com.share.arthas.demo.service.UserChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AddUserListener {

    private static final Logger logger = LoggerFactory.getLogger(AddUserListener.class);

    @Autowired
    private UserChangeService userChangeService;

    @EventListener(value = AddUserEvent.class, condition = "#event != null && #event.age > 20")
    public void onAddUserGt20(AddUserEvent event) {
        int age = event.getAge();
        long userId = event.getId();
        String name = event.getName();
        User user = new User();
        user.setAge(age);
        user.setName(name);
        UserChange userChange = new UserChange();
        userChange.setType(Constants.ADD_USER);
        userChange.setUserId(userId);
        userChange.setName(name);
        userChange.setNewVal(new Gson().toJson(user));
        userChangeService.insert(userChange);
    }

    @Async
    @EventListener(value = AddUserEvent.class, condition = "#event != null && #event.age <= 20")
    public void onAddUserLt20Async(AddUserEvent event) {
        int age = event.getAge();
        long userId = event.getId();
        String name = event.getName();
        User user = new User();
        user.setAge(age);
        user.setName(name);
        UserChange userChange = new UserChange();
        userChange.setType(Constants.ADD_USER);
        userChange.setUserId(userId);
        userChange.setName(name);
        userChange.setNewVal(new Gson().toJson(user));
        userChangeService.insert(userChange);
    }


}
