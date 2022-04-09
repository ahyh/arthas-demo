package com.yanhuan.arthas.demo.dao;

import com.yanhuan.arthas.demo.model.UserChange;

import java.util.List;

public interface UserChangeMapper {

    int insert(UserChange userChange);

    List<UserChange> listUserChangeByUserId(long userId);
}
