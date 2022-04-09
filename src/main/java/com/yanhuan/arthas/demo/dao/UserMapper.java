package com.yanhuan.arthas.demo.dao;

import com.yanhuan.arthas.demo.model.User;

import java.util.List;

/**
 * mapper for user
 *
 * @author yanhuan
 */
public interface UserMapper {

    int insert(User user);

    int update(User user);

    User getUserById(long id);

    List<User> listAllUsers();

    int delete(long id);
}
