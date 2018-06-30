package com.github.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午4:51 2018/6/30
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    private String firstName;

    private String lastName;

    @Override
    public String toString(){
        return firstName + " " + lastName;
    }

    @Override
    public int hashCode() {
        return this.firstName.hashCode()^this.lastName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Person) ) return false;
        Person p = (Person)obj;
        return this.firstName.equals(p.firstName) && this.lastName.equals(p.lastName);
    }
}
