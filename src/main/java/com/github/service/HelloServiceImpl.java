package com.github.service;

import com.github.annotation.RemoteService;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:53 2018/6/30
 * @desc
 */
@RemoteService(HelloService.class)
@NoArgsConstructor
public class HelloServiceImpl implements HelloService {



    @Override
    public String hello(String name) {
        return "hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
