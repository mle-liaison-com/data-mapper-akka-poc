package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import com.liaison.service.akka.core.BaseActor;

public class HelloWorldSyncActor extends BaseActor {

    public HelloWorldSyncActor(String gpuid) {
        super(gpuid);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> {
                    getLogger().info("receive sync message {}", any);
                    getSender().tell("Hello, World!", ActorRef.noSender());
                })
                .build();
    }
}
