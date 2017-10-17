package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FailActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testFailActor() {
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(Props.create(FailActor.class));
            within(duration("3 seconds"), () -> {
                subject.tell("hello", getRef());
                expectMsgClass(Status.Failure.class);
                return null;
            });
        }};
    }
}
