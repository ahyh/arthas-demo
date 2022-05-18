package com.share.arthas.demo.dao;

import com.share.arthas.demo.model.UserChange;

import java.util.List;

public interface UserChangeMapper {

    int insert(UserChange userChange);

    List<UserChange> listUserChangeByUserId(long userId);
}
