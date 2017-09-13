package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.FromConfig;
import com.liaison.service.akka.core.BaseActor;

public class HelloWorldActor extends BaseActor {

    private final ActorRef childRef;

    public HelloWorldActor(String gpuid) {
        super(gpuid);
        this.childRef = getContext().actorOf(FromConfig.getInstance().props(Props.create(HelloWorldChildActor.class, "child")), "child");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(any -> {
                    getLogger().info("receive sync message {}, {}", any, this);
                    childRef.tell("child", ActorRef.noSender());
                    getSender().tell("Hello, World!", ActorRef.noSender());
                })
                .build();
    }
}
