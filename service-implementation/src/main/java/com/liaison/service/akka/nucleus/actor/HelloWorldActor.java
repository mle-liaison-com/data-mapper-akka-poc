package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import com.liaison.service.akka.core.BaseActor;

public class HelloWorldActor extends BaseActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> {
                    getSender().tell("Hello, World!", ActorRef.noSender());
                })
                .build();
    }
}
