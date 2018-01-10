package com.liaison.service.akka.core.circuitbreaker;

import akka.actor.ActorSystem;
import akka.pattern.CircuitBreaker;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class CircuitBreakerFactoryTest {

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
    public void testGetInstance() {
        new TestKit(system) {{
            final String key = "testKey";

            // create a new circuitbrekaer
            Function<String, CircuitBreaker> firstFunction = str -> new CircuitBreaker(system.dispatcher(), system.scheduler(),
                    10, FiniteDuration.apply(10, TimeUnit.SECONDS), FiniteDuration.apply(10, TimeUnit.SECONDS));
            CircuitBreaker firstCircuitBreaker = CircuitBreakerFactory.getInstance(key, firstFunction);
            assertNotNull("expected a non-null CircuitBreaker from CircuitBreakerFactory", firstCircuitBreaker);

            // attempt to provide a different function for the same key
            // this should result in the same CircuitBreaker instance regardless
            Function<String, CircuitBreaker> secondFunction = str -> new CircuitBreaker(system.dispatcher(), system.scheduler(),
                    20, FiniteDuration.apply(20, TimeUnit.SECONDS), FiniteDuration.apply(20, TimeUnit.SECONDS));
            CircuitBreaker secondCircuitBreaker = CircuitBreakerFactory.getInstance(key, secondFunction);
            assertEquals("CircuitBreakers should be the same", firstCircuitBreaker, secondCircuitBreaker);

            // attempt to provide the same first function, but with different key this time.
            // this should result in unique CircuitBreaker
            CircuitBreaker thirdCircuitBreaker = CircuitBreakerFactory.getInstance("different", firstFunction);
            assertNotEquals("CircuitBreakers should be different", firstCircuitBreaker, thirdCircuitBreaker);
        }};
    }
}
