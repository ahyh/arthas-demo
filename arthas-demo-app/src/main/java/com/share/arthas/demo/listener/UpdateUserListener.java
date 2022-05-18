package com.share.arthas.demo.listener;

import com.share.arthas.demo.service.UserChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserListener {

    @Autowired
    private UserChangeService userChangeService;


}
