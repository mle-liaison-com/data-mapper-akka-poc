package com.liaison.service.akka.nucleus.actor;

import com.liaison.service.akka.core.BaseActor;

public class AsyncActor extends BaseActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> {
                    try {
                        getLogger().info("received async message {}", any);
                    } finally {
                        getContext().stop(getSelf());
                    }
                })
                .build();
    }
}
