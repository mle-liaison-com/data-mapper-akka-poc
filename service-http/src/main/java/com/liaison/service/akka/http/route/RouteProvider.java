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

/**
 * This interface provides a common {@link Route} generation method, as well as helper methods
 * for creating routes using different directives.
 *
 * Use of this interface is completely optional, and user may decide to create a {@link Route} on its own.
 */
public interface RouteProvider {

    String CONFIG_ACTOR_TIMEOUT = "com.liaison.service.akka.actor.timeout";

    /**
     * Returns a Route
     *
     * @return {@link Route}
     */
    Route create();

    /**
     * Returns a {@link Route} that invokes an {@link akka.actor.Actor}
     * with error handling via {@link akka.http.javadsl.server.Directives#handleExceptions} directive
     *
     * When using this default method, exception needs to be properly wrapped with {@link akka.actor.Status.Failure}.
     * Otherwise, response will not be sent until Actor timeout is reached.
     *
     * @param system {@link ActorSystem} of the service
     * @param ref {@link ActorRef} of an {@link akka.actor.Actor} to be invoked
     * @param message Message to actor
     * @param exceptionFunction Exception handler
     * @param responseFunction Response handler
     * @return {@link Route} that invokes specified {@link akka.actor.Actor}
     */
    default Route invokeActorWithExceptionHandler(final ActorSystem system, final ActorRef ref, Object message,
                                                  Function<Throwable, Route> exceptionFunction,
                                                  Function<Object, HttpResponse> responseFunction) {
        long actorTimeout = system.settings().config().getLong(CONFIG_ACTOR_TIMEOUT);
        final ExceptionHandler handler = ExceptionHandler.newBuilder().matchAny(exceptionFunction::apply).build();
        final CompletionStage<HttpResponse> future = ask(ref, message, actorTimeout).thenApplyAsync(responseFunction);
        return handleExceptions(handler, () -> completeWithFuture(future));
    }
}
