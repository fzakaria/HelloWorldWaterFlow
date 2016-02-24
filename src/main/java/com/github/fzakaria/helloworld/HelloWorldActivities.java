package com.github.fzakaria.helloworld;

import com.github.fzakaria.waterflow.Activities;
import com.github.fzakaria.waterflow.activity.ActivityMethod;

/**
 * Created by fzakaria on 2/24/16.
 */
public class HelloWorldActivities extends Activities {

    @ActivityMethod(name = "Hello World" , version = "1.0")
    public String helloWorld(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        return String.format("Hello World %s!",name);
    }
}
