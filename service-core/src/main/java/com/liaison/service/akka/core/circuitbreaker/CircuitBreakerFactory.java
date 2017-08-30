package com.liaison.service.akka.core.circuitbreaker;

import akka.pattern.CircuitBreaker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HPark on 8/2/2017.
 */
public class CircuitBreakerFactory {

    private CircuitBreakerFactory() {

    }

    private final static Map<String, CircuitBreaker> map = new HashMap<>();

    public static CircuitBreaker getInstance(String key, CircuitBreakerBuilder builder) {
        if (!map.containsKey(key)) {
            synchronized (CircuitBreakerFactory.class) {
                if (!map.containsKey(key)) {
                    map.put(key, builder.build());
                }
            }
        }
        return map.get(key);
    }
}
