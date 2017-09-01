package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import com.liaison.service.akka.core.BaseActor;
import com.liaison.service.akka.core.route.RouteProvider;
import com.liaison.service.akka.nucleus.actor.FailActor;
import com.liaison.service.akka.nucleus.actor.HelloWorldAsyncActor;
import com.liaison.service.akka.nucleus.actor.HelloWorldSyncActor;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.completeWithFuture;
import static akka.http.javadsl.server.Directives.handleExceptions;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.route;

public class HelloWorldRouteProvider implements RouteProvider {

    // TODO actor vs future
    @Override
    public Route get(final ActorSystem system) {
        return route(
                path("simple", () -> complete("Hello, World!")),
                path("sync", () -> invokeActorWithExceptionHandler(system, createActorRef(system, HelloWorldSyncActor.class, UUID.randomUUID().toString()))),
                path("async", () -> {
                    ActorRef async = system.actorOf(Props.create(HelloWorldAsyncActor.class, UUID.randomUUID().toString()));
                    async.tell("", async);
                    return complete(StatusCodes.NO_CONTENT);
                }),
                path("fail", () -> invokeActorWithExceptionHandler(system, createActorRef(system, FailActor.class, UUID.randomUUID().toString())))
        );
    }

    private ActorRef createActorRef(final ActorSystem system, Class<? extends BaseActor> actorClass, Object... args) {
        return system.actorOf(Props.create(actorClass, args));
    }

    private Route invokeActorWithExceptionHandler(final ActorSystem system, final ActorRef ref) {
        final ExceptionHandler handler = ExceptionHandler.newBuilder().matchAny(
                throwable -> stopActorAfterUse(
                        t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                        throwable,
                        system,
                        ref)).build();
        final CompletionStage<HttpResponse> future = PatternsCS.ask(ref, "", 5000).thenApplyAsync(
                returned -> stopActorAfterUse(
                        o -> HttpResponse.create().withEntity(o.toString()),
                        returned,
                        system,
                        ref));
        return handleExceptions(handler, () -> completeWithFuture(future));
    }

    private <E, T> T stopActorAfterUse(Function<E, T> function, E o, final ActorSystem system, final ActorRef ref) {
        try {
            return function.apply(o);
        } finally {
            system.stop(ref);
        }
    }
}
