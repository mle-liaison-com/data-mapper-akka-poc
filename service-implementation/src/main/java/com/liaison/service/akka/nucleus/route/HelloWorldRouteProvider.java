package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
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

public class HelloWorldRouteProvider implements RouteProvider {

    // TODO actor vs future
    @Override
    public Route get(final ActorSystem system, final AllDirectives directives) {
        return directives.route(
                directives.path("simple", () -> directives.complete("Hello, World!")),
                directives.path("sync", () -> invokeActorWithExceptionHandler(system, directives, createActorRef(system, HelloWorldSyncActor.class, UUID.randomUUID().toString()))),
                directives.path("async", () -> {
                    ActorRef async = system.actorOf(Props.create(HelloWorldAsyncActor.class, UUID.randomUUID().toString()));
                    async.tell("", async);
                    return directives.complete(StatusCodes.NO_CONTENT);
                }),
                directives.path("fail", () -> invokeActorWithExceptionHandler(system, directives, createActorRef(system, FailActor.class, UUID.randomUUID().toString())))
        );
    }

    private ActorRef createActorRef(ActorSystem system, Class<? extends BaseActor> actorClass, Object... args) {
        return system.actorOf(Props.create(actorClass, args));
    }

    private Route invokeActorWithExceptionHandler(ActorSystem system, AllDirectives directives, ActorRef ref) {
        final ExceptionHandler handler = ExceptionHandler.newBuilder().matchAny(
                throwable -> stopActorAfterUse(
                        t -> directives.complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                        throwable,
                        system,
                        ref)).build();
        final CompletionStage<HttpResponse> future = PatternsCS.ask(ref, "", 5000).thenApplyAsync(
                returned -> stopActorAfterUse(
                        o -> HttpResponse.create().withEntity(o.toString()),
                        returned,
                        system,
                        ref));
        return directives.handleExceptions(handler, () -> directives.completeWithFuture(future));
    }

    private <E, T> T stopActorAfterUse(Function<E, T> function, E o, ActorSystem system, ActorRef ref) {
        try {
            return function.apply(o);
        } finally {
            system.stop(ref);
        }
    }
}
