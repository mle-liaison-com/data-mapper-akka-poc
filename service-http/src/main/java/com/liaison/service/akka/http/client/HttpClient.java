package com.liaison.service.akka.http.client;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.pattern.CircuitBreaker;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * HTTP Client that utilizes Akka {@link Http}, with customizable response handler.
 * All requests made with this class will be protected by {@link CircuitBreaker}.
 * {@link CircuitBreaker} is expected to be created based on {@link HttpRequest} being sent out.
 */
public class HttpClient {

    private final ActorSystem system;
    private final Function<HttpRequest, CircuitBreaker> circuitBreakerFunction;
    private final BiConsumer<HttpResponse, Throwable> responseHandler;

    /**
     * HttpClient constructor. All parameters should be non-null
     *
     * @param system {@link ActorSystem} to create {@link Http}
     * @param circuitBreakerFunction {@link Function} to create {@link CircuitBreaker} from {@link HttpRequest}
     * @param responseHandler {@link BiConsumer} to handle {@link HttpResponse} or {@link Throwable} from the request
     */
    public HttpClient(@Nonnull ActorSystem system,
                      @Nonnull Function<HttpRequest, CircuitBreaker> circuitBreakerFunction,
                      @Nonnull BiConsumer<HttpResponse, Throwable> responseHandler) {
        this.system = system;
        this.circuitBreakerFunction = circuitBreakerFunction;
        this.responseHandler = responseHandler;
    }

    /**
     * Sends {@link HttpRequest}.
     *
     * @param request {@link HttpRequest} to send
     */
    public void call(@Nonnull HttpRequest request) {
        CircuitBreaker circuitBreaker = circuitBreakerFunction.apply(request);
        Http http = Http.get(system);
        circuitBreaker.callWithCircuitBreakerCS(() -> http.singleRequest(request)).whenComplete(responseHandler);
    }
}
