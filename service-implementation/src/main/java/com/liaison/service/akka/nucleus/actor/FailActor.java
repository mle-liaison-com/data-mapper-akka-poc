package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.Status;
import com.liaison.service.akka.core.BaseActor;

public class FailActor extends BaseActor {

    public FailActor(String gpuid) {
        super(gpuid);
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        getLogger().info("started: {}", this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> {
                    try {
                        throw new RuntimeException("test exception");
                    } catch (Exception e) {
                        getLogger().error("{}: {}", e.getMessage(), this);
                        getSender().tell(new Status.Failure(e), ActorRef.noSender());
                        throw e;
                    }
                })
                .build();
    }
}
