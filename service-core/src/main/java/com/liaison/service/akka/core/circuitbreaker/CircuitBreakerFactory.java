package com.liaison.service.akka.core.circuitbreaker;

import akka.pattern.CircuitBreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by HPark on 8/2/2017.
 */
public class CircuitBreakerFactory {

    private CircuitBreakerFactory() {

    }

    private final static Map<String, CircuitBreaker> map = new ConcurrentHashMap<>();

    public static CircuitBreaker getInstance(String key, Function<String, CircuitBreaker> function) {
        return map.computeIfAbsent(key, function);
    }
}
