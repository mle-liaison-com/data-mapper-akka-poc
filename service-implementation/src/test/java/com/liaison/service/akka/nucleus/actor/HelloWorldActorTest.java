package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloWorldActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("HelloWorldActorTest");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testSyncActorMessage() {
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(FromConfig.getInstance().props(Props.create(HelloWorldActor.class, "test")), "hello");
            within(duration("3 seconds"), () -> {
                subject.tell("hello", getRef());
                expectMsg("Hello, World!");
                return null;
            });
        }};
    }
}
