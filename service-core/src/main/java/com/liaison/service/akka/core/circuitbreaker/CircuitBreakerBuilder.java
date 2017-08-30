package com.liaison.service.akka.core.circuitbreaker;

import akka.actor.Scheduler;
import akka.pattern.CircuitBreaker;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.FiniteDuration;

public class CircuitBreakerBuilder {

    private final ExecutionContext dispatcher;
    private final Scheduler scheduler;
    private final int maxFailures;
    private final FiniteDuration callTimeout;
    private final FiniteDuration resetTimeout;
    private FiniteDuration maxResetTimeout;

    public CircuitBreakerBuilder(ExecutionContext dispatcher, Scheduler scheduler, int maxFailures,
                                 FiniteDuration callTimeout, FiniteDuration resetTimeout) {
        this.dispatcher = dispatcher;
        this.scheduler = scheduler;
        this.maxFailures = maxFailures;
        this.callTimeout = callTimeout;
        this.resetTimeout = resetTimeout;
    }

    public CircuitBreakerBuilder withExponentialBackkoff(FiniteDuration maxResetTimeout) {
        this.maxResetTimeout = maxResetTimeout;
        return this;
    }

    public CircuitBreaker build() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(dispatcher, scheduler, maxFailures, callTimeout,
                resetTimeout);
        if (maxResetTimeout != null) {
            circuitBreaker.withExponentialBackoff(maxResetTimeout);
        }
        return circuitBreaker;
    }
}
