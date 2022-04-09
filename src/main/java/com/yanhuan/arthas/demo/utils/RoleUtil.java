package com.yanhuan.arthas.demo.utils;

import com.yanhuan.arthas.demo.model.User;
import com.yanhuan.arthas.demo.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class RoleUtil {

    private static final String ROLE_ADMIN = "private_role_admin";
    public static final String ROLE_MEMBER = "public_role_member";

    private static UserService userService;

    public static UserService getUserService() {
        if (userService == null) {
            userService = SpringUtil.getBean(UserService.class);
        }
        return userService;
    }

    public static List<Integer> filterRole(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.EMPTY_LIST;
        }
        return roleIds.stream().filter(x -> x % 2 == 0).map(x -> 2 * x).collect(Collectors.toList());
    }

    public static String getRoleType(int type) {
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
        return "no-role";
    }

    public static String getRoleTypeByUser(User user) {
        if (user == null) {
            return StringUtils.EMPTY;
        }
        String role = user.getRole();
        return "role-" + role;
    }

    public static List<User> getUsersByIds(List<String> userIds) {
        UserService userService = getUserService();
        if (userService == null) {
            return new ArrayList<>();
        }
        List<User> users = userService.listUsers();
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }
        return users.stream().filter(u -> userIds.contains(String.valueOf(u.getId()))).collect(Collectors.toList());
    }

    public static List<User> getUsersByMap(Map<String, String> map) {
        UserService userService = getUserService();
        if (userService == null) {
            return new ArrayList<>();
        }
        List<User> users = userService.listUsers();
        if (map == null || !map.containsKey("ids")) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }
        String ids = map.get("ids");
        String[] split = ids.split(",");
        List<String> userIds = Arrays.asList(split);
        return users.stream().filter(u -> userIds.contains(String.valueOf(u.getId()))).collect(Collectors.toList());
    }
}
