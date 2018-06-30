package com.github.service;

import java.util.List;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:51 2018/6/30
 * @desc
 */
public interface PersonService {
    List<Person> GetTestPerson(String name, int num);
}
