package com.yanhuan.arthas.demo.utils;

import com.yanhuan.arthas.demo.model.User;
import com.yanhuan.arthas.demo.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * UserUtil
 *
 * @author yanhuan
 */
public class UserUtil {

    private static final String USER_TYPE_PRIVATE = "private";
    public static final String USER_TYPE_PUBLIC = "public";

    private static UserService userService;

    public static UserService getUserService(){
        if (userService == null) {
            userService = SpringUtil.getBean(UserService.class);
        }
        return userService;
    }


    public static String getTypeStr(int type) {
        UserService userService = getUserService();
        if (userService == null) {
            return "";
        }
        if (type == 0) {
            return "owner";
        } else if (type == 1) {
            return "admin";
        } else if (type == 2) {
            return "member";
        }
        return "default";
    }

    public static String fromMap(Map<Integer, String> map) {
        if (map == null || map.size() == 0) {
            return "empty";
        }
        return map.get(0);
    }

    public static String fromUserMap(Map<String, User> map) {
        if (map == null || map.size() == 0) {
            return "empty";
        }
        User user = map.get("0");
        if (user == null) {
            return "empty";
        }
        return user.getName();
    }

    public static String fromList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return "empty";
        }
        User user = userList.get(0);
        if (!StringUtils.startsWith(user.getName(), "name")) {
            return "empty";
        }
        return user.getName();
    }

    public static String fromSet(Set<User> userSet) {
        if (CollectionUtils.isEmpty(userSet)) {
            return "empty";
        }
        for (User user : userSet) {
            if (StringUtils.startsWith(user.getName(), "name")) {
                return user.getName();
            }
        }
        return "empty";
    }

}
