package com.liaison.service.akka.nucleus.actor;

import com.liaison.service.akka.core.BaseActor;

/**
 * Child of HelloWorldActor
 * Number of instances generated is N (number of routees in /hello router) * M (number of routees in /hello/"*"/child router)
 * As each routee (HelloWorldActor) owns a separate child router
 */
public class HelloWorldChildActor extends BaseActor {

    public HelloWorldChildActor(String pguid) {
        super(pguid);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> getLogger().info("child actor invoked: {}", this))
                .build();
    }
}
