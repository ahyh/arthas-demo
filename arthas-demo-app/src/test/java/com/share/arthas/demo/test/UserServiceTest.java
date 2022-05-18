package com.share.arthas.demo.test;

import com.share.arthas.demo.model.User;
import com.share.arthas.demo.service.UserService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

public class UserServiceTest extends BaseTest {

    @Resource
    private UserService userService;

    @Test
    public void testInsert() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        User user = new User();
        user.setAge(32);
        user.setName("name-0");
        user.setBirthday(dateFormat.parse("1989-11-12 23:12:12"));
        boolean insert = userService.insert(user);
        Assert.assertTrue(insert);
    }
}
