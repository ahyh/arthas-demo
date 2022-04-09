package com.yanhuan.arthas.demo.service.impl;

import com.yanhuan.arthas.demo.dao.UserChangeMapper;
import com.yanhuan.arthas.demo.dao.UserMapper;
import com.yanhuan.arthas.demo.events.AddUserEvent;
import com.yanhuan.arthas.demo.model.User;
import com.yanhuan.arthas.demo.service.UserChangeService;
import com.yanhuan.arthas.demo.service.UserService;
import com.yanhuan.arthas.demo.utils.RoleUtil;
import com.yanhuan.arthas.demo.utils.SpringUtil;
import com.yanhuan.arthas.demo.utils.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Service for user implementation
 *
 * @author yanhuan
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private static final int TYPE_LIST = 0;
    private static final int TYPE_SET = 1;
    private static final int TYPE_MAP = 2;

    @Resource
    private UserMapper userMapper;

    @Override
    public User getUserById(long id) {
        User user = userMapper.getUserById(id);
        String roleType = RoleUtil.getRoleType((int) (id % 3));
        user.setRole(roleType);

        String typeStr = UserUtil.getTypeStr((int) (id % 3));
        user.setType(typeStr);
        return user;
    }

    @Override
    public boolean insert(User user) {
        int insert = userMapper.insert(user);
        if (insert <= 0) {
            logger.error("insert error");
            return false;
        }
        AddUserEvent addUserEvent = new AddUserEvent();
        addUserEvent.setId(user.getId());
        addUserEvent.setAge(user.getAge());
        addUserEvent.setName(user.getName());
        addUserEvent.setCreateTime(new Date());
        SpringUtil.getApplicationContext().publishEvent(addUserEvent);
        return true;
    }

    @Override
    public List<User> listUsers() {
        return userMapper.listAllUsers();
    }

    @Override
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean delete(long id) {
        return userMapper.delete(id) > 0;
    }

    @Override
    public String testInvokePrivateMethod(long id) {
        User user = getUserById(1L);
        if (user != null) {
            return user.getName() + getFromUserUtil(TYPE_SET);
        }
        return "non-exist-user";
    }

    private String getFromUserUtil(int type) {
        if (type == TYPE_LIST) {
            return UserUtil.fromList(new ArrayList<>());
        } else if (type == TYPE_SET) {
            return UserUtil.fromSet(new HashSet<>());
        } else if (type == TYPE_MAP) {
            return UserUtil.fromMap(new HashMap<>());
        }
        return StringUtils.EMPTY;
    }

    public String getFromRoleUtil(int type) {
        return RoleUtil.getRoleType(type);
    }


}
