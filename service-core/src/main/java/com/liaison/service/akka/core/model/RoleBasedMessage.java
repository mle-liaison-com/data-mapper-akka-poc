package com.liaison.service.akka.core.model;

import akka.annotation.ApiMayChange;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Base message class for service-to-service communications.
 * Client service needs to attach its roles to this message class,
 * and roles will be verified by server service for authorization.
 */
@ApiMayChange
public abstract class RoleBasedMessage implements Serializable {

    private final Set<String> roles;

    /**
     * Base constructor for message with roles
     *
     * @param roles Set of roles defined from client service
     */
    public RoleBasedMessage(Set<String> roles) {
        this.roles = Collections.unmodifiableSet(roles);
    }

    /**
     * Getter for roles
     *
     * @return Set of client roles
     */
    public Set<String> getRoles() {
        return roles;
    }
}
