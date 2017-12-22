package com.liaison.service.akka.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class EntryActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create(getClass().getSimpleName());
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testValidMessageType() {
        new TestKit(system) {{
            ActorMessageConsumer<WorkTicketOuterClass.WorkTicket> workTicketConsumer = (message, context, sender) ->
                    sender.tell(message.getGlobalProcessId(), ActorRef.noSender());
            final ActorRef subject = system.actorOf(Props.create(EntryActor.class, WorkTicketOuterClass.WorkTicket.class, workTicketConsumer));
            within(duration("3 seconds"), () -> {
                final String globalProcessId = UUID.randomUUID().toString();
                WorkTicketOuterClass.WorkTicket workTicket = WorkTicketOuterClass.WorkTicket.newBuilder().setGlobalProcessId(globalProcessId).build();
                subject.tell(workTicket, getRef());
                expectMsg(globalProcessId);
                return null;
            });
        }};
    }

    @Test
    public void testInvalidMessageType() {
        new TestKit(system) {{
            ActorMessageConsumer<WorkTicketOuterClass.WorkTicket> workTicketConsumer = (message, context, sender) ->
                    sender.tell(message.getGlobalProcessId(), ActorRef.noSender());
            final ActorRef subject = system.actorOf(Props.create(EntryActor.class, WorkTicketOuterClass.WorkTicket.class, workTicketConsumer));
            within(duration("3 seconds"), () -> {
                subject.tell("random_string", getRef());
                expectMsgClass(Status.Failure.class);
                return null;
            });
        }};
    }
}
