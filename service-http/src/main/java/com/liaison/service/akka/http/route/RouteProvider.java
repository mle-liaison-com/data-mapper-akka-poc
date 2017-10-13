package com.liaison.service.akka.http.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Route;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static akka.http.javadsl.server.Directives.completeWithFuture;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.pattern.PatternsCS.ask;

public interface RouteProvider {

    Route create();

    String CONFIG_ACTOR_TIMEOUT = "com.liaison.service.akka.actor.timeout";

    default Route invokeActorWithExceptionHandler(final ActorSystem system, final ActorRef ref, Object message,
                                                  Function<Throwable, Route> exceptionFunction,
                                                  Function<Object, HttpResponse> responseFunction) {
        long actorTimeout = system.settings().config().getLong(CONFIG_ACTOR_TIMEOUT);
        final ExceptionHandler handler = ExceptionHandler.newBuilder().matchAny(exceptionFunction::apply).build();
        final CompletionStage<HttpResponse> future = ask(ref, message, actorTimeout).thenApplyAsync(responseFunction);
        return handleExceptions(handler, () -> completeWithFuture(future));
    }
}
