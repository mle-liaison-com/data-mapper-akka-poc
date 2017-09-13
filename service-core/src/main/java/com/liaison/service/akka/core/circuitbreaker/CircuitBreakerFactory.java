package com.liaison.service.akka.core.circuitbreaker;

import akka.pattern.CircuitBreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by HPark on 8/2/2017.
 */
public class CircuitBreakerFactory {

    private CircuitBreakerFactory() {

    }

    private final static Map<String, CircuitBreaker> map = new ConcurrentHashMap<>();

    public static CircuitBreaker getInstance(String key, CircuitBreakerBuilder builder) {
        if (!map.containsKey(key)) {
            map.putIfAbsent(key, builder.build());
        }
        return map.get(key);
    }
}
