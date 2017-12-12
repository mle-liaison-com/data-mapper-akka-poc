package com.liaison.service.akka.http.client;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.CircuitBreaker;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class HttpClient {

    private final ActorSystem system;
    private final Function<HttpRequest, CircuitBreaker> circuitBreakerFunction;
    private final BiConsumer<HttpResponse, Throwable> responseHandler;

    public HttpClient(@Nonnull ActorSystem system,
                      @Nonnull Function<HttpRequest, CircuitBreaker> circuitBreakerFunction,
                      @Nonnull BiConsumer<HttpResponse, Throwable> responseHandler) {
        this.system = system;
        this.circuitBreakerFunction = circuitBreakerFunction;
        this.responseHandler = responseHandler;
    }

    public void call(HttpRequest request) {
        CircuitBreaker circuitBreaker = circuitBreakerFunction.apply(request);
        Http http = Http.get(system);
        circuitBreaker.callWithCircuitBreakerCS(() -> http.singleRequest(request)).whenComplete(responseHandler);
    }
}
