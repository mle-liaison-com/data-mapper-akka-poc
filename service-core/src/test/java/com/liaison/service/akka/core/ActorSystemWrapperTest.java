package com.liaison.service.akka.core;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ActorSystemWrapperTest {

    static class InvalidActor extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder().build();
        }
    }

    private ActorSystem system;

    @Before
    public void setup() {
        system = new ActorSystemWrapper(ActorSystem.create(getClass().getSimpleName()));
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    // /user/entry is in akka.remote.trusted-selection-paths, but InvalidActor is not an instance of EntryActor
    @Test(expected = IllegalStateException.class)
    public void testNotEntryActor() {
        system.actorOf(Props.create(InvalidActor.class), "entry");
    }

    // /user/random is not in akka.remote.trusted-selection-path.
    // so, any actor can be created
    @Test
    public void testNotInTrustedSelectionPaths() {
        ActorRef ref = system.actorOf(Props.create(InvalidActor.class), "random");
        assertNotNull("ActorRef created successfully", ref);
    }

    // /user/entry is in akka.remote.trusted-selection-paths, and EntryActor is used for the actor path
    @Test
    public void testInTrustedSelectionPaths() {
        ActorMessageConsumer<Object> noop = (message, context, sender) -> { };
        ActorRef ref = system.actorOf(Props.create(EntryActor.class, Object.class, noop), "entry");
        assertNotNull("ActorRef created successfully", ref);
    }
}
