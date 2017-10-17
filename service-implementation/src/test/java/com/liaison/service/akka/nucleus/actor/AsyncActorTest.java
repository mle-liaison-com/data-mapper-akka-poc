package com.liaison.service.akka.nucleus.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.testkit.javadsl.EventFilter;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AsyncActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create("AsyncActorTest", ConfigFactory.parseString("akka.loggers = [\"akka.testkit.TestEventListener\"]"));
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testAsyncActorLogging() {
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(Props.create(AsyncActor.class));
            final boolean result = new EventFilter(Logging.Info.class, system)
                    .message("received async message test")
                    .occurrences(1)
                    .intercept(() -> {
                        subject.tell("test", ActorRef.noSender());
                        return true;
                    });
            assertTrue(result);
        }};
    }

    @Test
    public void testAsyncActorMessage() {
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(Props.create(AsyncActor.class));
            within(duration("3 seconds"), () -> {
                subject.tell("hello", getRef());
                expectNoMsg();
                return null;
            });
        }};
    }
}
