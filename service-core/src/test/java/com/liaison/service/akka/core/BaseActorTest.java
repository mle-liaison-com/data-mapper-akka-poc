package com.liaison.service.akka.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.testkit.javadsl.EventFilter;
import akka.testkit.javadsl.TestKit;
import com.liaison.service.akka.core.config.ConfigManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BaseActorTest {

    private static class ConfigActor extends BaseActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, str -> {
                        String config = getConfig().getString(str);
                        getSender().tell(config, ActorRef.noSender());
                        getLogger().info(config);
                    })
                    .build();
        }
    }

    private static final String CONFIG_TEST = "com.liaison.service.akka.test";

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
    public void testGetLogger() {
        final String value = ConfigManager.getConfig().getString(CONFIG_TEST);
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(Props.create(ConfigActor.class));
            final String path = subject.path().address() + subject.path().toStringWithoutAddress();
            final boolean result = new EventFilter(Logging.Info.class, system)
                    .message(value)
                    .from(path)
                    .occurrences(1)
                    .intercept(() -> {
                        subject.tell(CONFIG_TEST, ActorRef.noSender());
                        return true;
                    });
            assertTrue(result);
        }};
    }

    @Test
    public void testGetConfig() {
        final String value = ConfigManager.getConfig().getString(CONFIG_TEST);
        new TestKit(system) {{
            final ActorRef subject = system.actorOf(Props.create(ConfigActor.class));
            within(duration("3 seconds"), () -> {
                subject.tell(CONFIG_TEST, getRef());
                expectMsg(value);
                return null;
            });
        }};
    }
}
