package com.liaison.service.akka.core.circuitbreaker;

import akka.pattern.CircuitBreaker;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Factory class for {@link CircuitBreaker}.
 *
 * Main use case for this class would be to have a single CircuitBreaker for the same key (i.e. host, URI).
 * With multiple {@link akka.actor.ActorRef} instances, users are susceptible to have multiple instances of CircuitBreaker for the same host.
 * Having multiple CircuitBreaker defeats its purpose as each instance has its own counter.
 */
public class CircuitBreakerFactory {

    private CircuitBreakerFactory() {

    }

    private final static Map<String, CircuitBreaker> map = new ConcurrentHashMap<>();

    /**
     *
     *
     * @param key CircuitBreaker key
     * @param function CircuitBreaker generating {@link Function}
     * @return Existing or newly created CircuitBreaker
     */
    public static CircuitBreaker getInstance(@Nonnull String key, @Nonnull Function<String, CircuitBreaker> function) {
        return map.computeIfAbsent(key, function);
    }
}
