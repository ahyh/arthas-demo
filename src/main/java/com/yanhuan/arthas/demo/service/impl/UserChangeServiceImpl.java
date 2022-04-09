package com.yanhuan.arthas.demo.service.impl;

import com.yanhuan.arthas.demo.dao.UserChangeMapper;
import com.yanhuan.arthas.demo.model.UserChange;
import com.yanhuan.arthas.demo.service.UserChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * UserChange service implementation for user change
 *
 * @author yanhuan
 */
@Service
public class UserChangeServiceImpl implements UserChangeService {

    private static final Logger logger = LoggerFactory.getLogger(UserChangeServiceImpl.class);

    @Resource
    private UserChangeMapper userChangeMapper;

    @Override
    public boolean insert(UserChange userChange) {
        return userChangeMapper.insert(userChange) > 0;
    }

    @Override
    public List<UserChange> listChangesByUserId(long userId) {
        return userChangeMapper.listUserChangeByUserId(userId);
    }
}
