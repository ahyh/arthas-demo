package com.share.arthas.demo.dao;

import com.share.arthas.demo.model.User;

import java.util.List;

/**
 * mapper for user
 *
 * @author share
 */
public interface UserMapper {

    int insert(User user);

    int update(User user);

    User getUserById(long id);

    List<User> listAllUsers();

    int delete(long id);
}
