package com.yanhuan.arthas.demo.service;

import com.yanhuan.arthas.demo.model.User;

import java.util.List;

public interface UserService {

    User getUserById(long id);

    boolean insert(User user);

    List<User> listUsers();

    boolean update(User user);

    boolean delete(long id);

    String testInvokePrivateMethod(long id);

}
