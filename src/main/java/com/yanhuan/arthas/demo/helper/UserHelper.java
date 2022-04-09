package com.yanhuan.arthas.demo.helper;

import com.yanhuan.arthas.demo.dao.UserMapper;
import com.yanhuan.arthas.demo.model.User;
import com.yanhuan.arthas.demo.model.condition.UserCondition;
import com.yanhuan.arthas.demo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * helper for user
 *
 * @author yanhuan
 */
@Component
public class UserHelper {

    private static final Logger logger = LoggerFactory.getLogger(UserHelper.class);

    @Resource
    private UserMapper userMapper;

    public int setAge4User(long id, int age) {
        if (age <= 0) {
            return 0;
        }
        if (age >= 150) {
            return 0;
        }
        User user = new User();
        user.setId(id);
        user.setAge(age);
        int update = userMapper.update(user);
        return update;
    }

    /**
     * test for empty method
     * <p>
     * watch com.yanhuan.arthas.demo.helper.UserHelper testEmptyMethod '{params,returnObj,throwExp,endLine}' -n 5 -x 3
     */
    public void testEmptyMethod() {

    }

    /**
     * test for empty method with param
     * <p>
     * watch com.yanhuan.arthas.demo.helper.UserHelper testEmptyWithArg '{params,returnObj,throwExp,endLine}' -n 5 -x 3
     */
    public void testEmptyWithArg(String info) {

    }

    /**
     * test return void type method
     * <p>
     * watch com.yanhuan.arthas.demo.helper.UserHelper testReturnEmpty '{params,returnObj,throwExp,endLine}' -n 5 -x 3
     */
    public void testReturnEmpty(String info) {
        if (info == null) {
            // test null
            logger.error("null string");
            return;
        }
        if (StringUtils.EMPTY.equals(info)) {
            // test empty
            logger.error("empty string");
            return;
        }
        if (StringUtils.contains(info, "test")) {
            /**
             * test info test
             */

            logger.info("contains test string");
            return;
        }
        if (StringUtils.contains(info, "harvey")) {
            logger.info("contains harvey string");
            return;
        }
        logger.info("valid string");
    }

    /**
     * test String type return
     * <p>
     * watch com.yanhuan.arthas.demo.helper.UserHelper testReturn '{params,returnObj,throwExp,endLine}' -n 5 -x 3
     */
    public String testReturn(String info) {
        if (info == null) {
            // test null
            logger.error("null string");
            return StringUtils.EMPTY;
        }
        if (StringUtils.EMPTY.equals(info)) {
            // test empty
            logger.error("empty string");
            return StringUtils.EMPTY;
        }
        if (StringUtils.contains(info, "test")) {
            // test

            logger.info("contains test string");
            return "test";
        }
        if (StringUtils.contains(info, "harvey")) {
            logger.info("contains harvey string");
            return "harvey";
        }
        logger.info("valid string");
        return "valid";
    }

    /**
     * test throw exception
     * <p>
     * watch com.yanhuan.arthas.demo.helper.UserHelper checkThrowException '{params,returnObj,throwExp,endLine}' -n 5 -x 3
     */
    public void checkThrowException(String info) {
        if (info == null) {
            // test null exception
            logger.error("null string");
            throw new RuntimeException("invalid info");
        }
        if (StringUtils.EMPTY.equals(info)) {
            // test empty exception
            logger.error("empty string");
            throw new RuntimeException("invalid info");
        }
        if (StringUtils.contains(info, "test")) {
            logger.info("contains test string");
            throw new RuntimeException("invalid info");
        }
        if (StringUtils.contains(info, "harvey")) {
            logger.info("contains harvey string");
            throw new RuntimeException("invalid info");
        }
        logger.info("valid string");
    }

    /**
     * test throw exception
     * <p>
     * watch com.yanhuan.arthas.demo.helper.UserHelper testLambda '{params,returnObj,throwExp,endLine}' -n 5 -x 3
     */
    public String testLambda(String info) {
        if (StringUtils.isBlank(info)) {
            return "empty info";
        }
        // test
        if (StringUtils.contains(info, "test")) {
            List<Character> list = new ArrayList<>();
            for (char c : info.toCharArray()) {
                list.add(c);
            }
            StringBuilder sb = new StringBuilder();
            list.stream().filter(x -> x % 2 == 0).map(x -> x + 1).forEach(
                    x -> sb.append(x)
            );
            return sb.toString();
        }
        // harvey
        if (StringUtils.contains(info, "harvey")) {
            String result = "";
            for (char c : info.toCharArray()) {
                if (c == 'e') {
                    throw new RuntimeException("contains e");
                }
                if (c == 'd') {
                    return "contains d";
                }
                result += c;
            }
            return result;
        }
        return "lambda";
    }
}
