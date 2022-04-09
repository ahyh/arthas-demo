package com.yanhuan.arthas.demo.model;


import com.yanhuan.arthas.demo.service.UserService;
import com.yanhuan.arthas.demo.utils.SpringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;


public class User implements Serializable {

    private long id;
    private int age;
    private String name;
    private Date birthday;
    private long option1;
    private String type;
    private String role;
    private Date createTime;
    private Date modifyTime;
    /**
     * for test add SpringBean method for get/set field
     */
    private String name2;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public long getOption1() {
        return option1;
    }

    public void setOption1(long option1) {
        this.option1 = option1;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName2() {
        if (StringUtils.isBlank(name2)) {
            UserService userService = SpringUtil.getBean(UserService.class);
            User user = userService.getUserById(id + 1);
            if (user != null) {
                name2 = user.getName();
                return name2;
            }
        } else {
            if (StringUtils.length(name2) > 3) {
                name2 = name2.substring(0, 3);
                return name2;
            }
        }
        return "empty-name2";
    }

    public void setName2(String name2){
        this.name2 = name2;
    }
}
