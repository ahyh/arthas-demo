package com.yanhuan.arthas.demo.service;

import com.yanhuan.arthas.demo.model.UserChange;

import java.util.List;

public interface UserChangeService {

    boolean insert(UserChange userChange);

    List<UserChange> listChangesByUserId(long userId);
}
