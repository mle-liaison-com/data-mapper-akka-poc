package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HelloWorldActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create("HelloWorldActorTest");
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testSyncActorMessage() {
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(FromConfig.getInstance().props(Props.create(HelloWorldActor.class)), "hello");
            within(duration("3 seconds"), () -> {
                subject.tell("hello", getRef());
                expectMsg("Hello, World!");
                return null;
            });
        }};
    }
}
