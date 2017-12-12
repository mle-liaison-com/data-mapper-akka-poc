package com.liaison.service.akka.http.client;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.CircuitBreaker;
import com.liaison.service.akka.core.circuitbreaker.CircuitBreakerBuilder;
import com.liaison.service.akka.core.circuitbreaker.CircuitBreakerFactory;
import com.typesafe.config.Config;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class HttpClient {

    private static final String CONFIG_HTTP_MAX_FAILURE = "com.liaison.service.akka.http.client.failure.max";
    private static final String CONFIG_HTTP_TIMEOUT_CALL = "com.liaison.service.akka.http.client.failure.timeout.call";
    private static final String CONFIG_HTTP_TIMEOUT_RESET = "com.liaison.service.akka.http.client.failure.timeout.reset";

    private final ActorSystem system;
    private final BiConsumer<HttpResponse, Throwable> action;

    public HttpClient(@Nonnull ActorSystem system, @Nonnull BiConsumer<HttpResponse, Throwable> action) {
        this.system = system;
        this.action = action;
    }

    public void call(HttpRequest request) {
        CircuitBreaker circuitBreaker = CircuitBreakerFactory.getInstance(request.getUri().getHost().address(), str -> {
            Config config = system.settings().config();
            CircuitBreakerBuilder builder = new CircuitBreakerBuilder(system.dispatcher(),
                    system.scheduler(),
                    config.getInt(CONFIG_HTTP_MAX_FAILURE),
                    FiniteDuration.apply(config.getLong(CONFIG_HTTP_TIMEOUT_CALL), TimeUnit.MILLISECONDS),
                    FiniteDuration.apply(config.getLong(CONFIG_HTTP_TIMEOUT_RESET), TimeUnit.MILLISECONDS));
            return builder.build();
        });

        Http http = Http.get(system);
        circuitBreaker.callWithCircuitBreakerCS(() -> http.singleRequest(request)).whenComplete(action);
    }
}
