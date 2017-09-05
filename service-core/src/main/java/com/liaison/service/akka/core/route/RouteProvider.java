package com.liaison.service.akka.core.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.core.BaseActor;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static akka.http.javadsl.server.Directives.completeWithFuture;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.pattern.PatternsCS.ask;

public interface RouteProvider {

    String CONFIG_ACTOR_TIMEOUT = "com.liaison.service.akka.actor.timeout";

    Route get(ActorSystem system);

    default ActorRef createActorRef(final ActorSystem system, Class<? extends BaseActor> actorClass, Object... args) {
        return system.actorOf(Props.create(actorClass, args));
    }

    default Route invokeActorWithExceptionHandler(final ActorSystem system, final ActorRef ref, Object message,
                                                  Function<Throwable, Route> exceptionFunction,
                                                  Function<Object, HttpResponse> responseFunction) {
        long actorTimeout = system.settings().config().getLong(CONFIG_ACTOR_TIMEOUT);
        final ExceptionHandler handler = ExceptionHandler.newBuilder().matchAny(
                throwable -> stopActorAfterUse(
                        exceptionFunction,
                        throwable,
                        system,
                        ref)).build();
        final CompletionStage<HttpResponse> future = ask(ref, message, actorTimeout).thenApplyAsync(
                returned -> stopActorAfterUse(
                        responseFunction,
                        returned,
                        system,
                        ref));
        return handleExceptions(handler, () -> completeWithFuture(future));
    }

    default <E, T> T stopActorAfterUse(Function<E, T> function, E o, final ActorSystem system, final ActorRef ref) {
        try {
            return function.apply(o);
        } finally {
            system.stop(ref);
        }
    }
}
