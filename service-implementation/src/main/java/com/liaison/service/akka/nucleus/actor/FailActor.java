package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.Status;
import com.liaison.service.akka.core.BaseActor;

public class FailActor extends BaseActor {

    public FailActor(String gpuid) {
        super(gpuid);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> {
                    try {
                        throw new RuntimeException("test exception");
                    } catch (Exception e) {
                        getSender().tell(new Status.Failure(e), ActorRef.noSender());
                        throw e;
                    }
                })
                .build();
    }
}
