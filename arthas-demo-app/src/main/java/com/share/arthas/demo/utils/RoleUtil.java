package com.share.arthas.demo.utils;

import com.share.arthas.demo.model.User;
import com.share.arthas.demo.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class RoleUtil {

    private static final Logger logger = LoggerFactory.getLogger(RoleUtil.class);

    private static final Object lock = new Object();

    /**
     * ognl获取private static属性
     * ognl '@com.share.arthas.demo.utils.RoleUtil@ROLE_ADMIN'
     */
    private static final String ROLE_ADMIN = "private_role_admin";

    /**
     * ognl获取private static属性
     * ognl '@com.share.arthas.demo.utils.RoleUtil@ROLE_MEMBER'
     */
    public static final String ROLE_MEMBER = "public_role_member";

    private static UserService userService;

    public static UserService getUserService() {
        if (userService == null) {
            userService = SpringUtil.getBean(UserService.class);
        }
        return userService;
    }

    /**
     * ognl执行静态方法，方法入参是一个List
     * ognl '@com.share.arthas.demo.utils.RoleUtil@filterRole({1,2,3,4})'
     */
    public static List<Integer> filterRole(List<Integer> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.EMPTY_LIST;
        }
        return roleIds.stream().filter(x -> x % 2 == 0).map(x -> 2 * x).collect(Collectors.toList());
    }

    /**
     * ognl执行静态方法
     * ognl '@com.share.arthas.demo.utils.RoleUtil@getRoleType(1)'
     */
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

    /**
     * ognl执行静态方法，入参是一个对象
     * ognl '#user=new com.share.arthas.demo.model.User(),#user.setId(9L),#user.setRole("3"),#user.setName("harvey"),\
     * @com.share.arthas.demo.utils.RoleUtil@getRoleTypeByUser(#user)' -x 3
     */
    private static String getRoleTypeByUser(User user) {
        if (user == null) {
            return StringUtils.EMPTY;
        }
        String role = user.getRole();
        return "role-" + role;
    }

    private static List<User> getUsersByIds(List<String> userIds) {
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

    /**
     * ognl执行静态方法，入参是一个Map
     * ognl -x 3 '@com.share.arthas.demo.utils.RoleUtil@getUsersByMap(#{"ids": "1,2,3","test":"abc"})'
     */
    private static List<User> getUsersByMap(Map<String, String> map) {
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

    private static String getRoleTypePrivate(int type, long id) {
        synchronized (lock){
            UserService userService = getUserService();
            if (userService == null) {
                return "";
            }
        }
        try {
            User user = userService.getUserById(id);
            Assert.isTrue(user != null);
        } catch (ArrayIndexOutOfBoundsException oe){
            logger.error("ArrayIndexOutOfBoundsException");
            throw oe;
        }
        catch (RuntimeException re){
            logger.error("RuntimeException");
        }catch (Exception e){
            logger.error("Exception");
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


}
