package com.liaison.service.akka.nucleus.model;

import com.liaison.service.akka.core.model.RoleBasedMessage;

import java.io.Serializable;
import java.util.Set;

public class HelloMessage extends RoleBasedMessage implements Serializable {

    public HelloMessage(Set<String> roles) {
        super(roles);
    }
}
