package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class HelloWorldActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("HelloWorldActorTest", ConfigFactory.parseString("akka.loggers = [\"akka.testkit.TestEventListener\"]"));
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testSyncActorMessage() {
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(Props.create(HelloWorldActor.class, UUID.randomUUID().toString()));
            within(duration("3 seconds"), () -> {
                subject.tell("hello", getRef());
                expectMsg("Hello, World!");
                return null;
            });
        }};
    }
}
