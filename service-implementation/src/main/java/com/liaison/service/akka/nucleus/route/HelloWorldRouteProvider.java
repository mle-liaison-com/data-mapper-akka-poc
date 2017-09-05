package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.core.route.RouteProvider;
import com.liaison.service.akka.nucleus.actor.FailActor;
import com.liaison.service.akka.nucleus.actor.HelloWorldAsyncActor;
import com.liaison.service.akka.nucleus.actor.HelloWorldSyncActor;

import java.util.UUID;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.route;

public class HelloWorldRouteProvider implements RouteProvider {

    // TODO actor vs future
    @Override
    public Route get(final ActorSystem system) {
        return route(
                path("simple", () -> complete("Hello, World!")),
                path("sync", () -> invokeActorWithExceptionHandler(
                        system,
                        createActorRef(system, HelloWorldSyncActor.class, UUID.randomUUID().toString()),
                        "message",
                        t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                        o -> HttpResponse.create().withEntity(o.toString()))),
                path("async", () -> {
                    createActorRef(system, HelloWorldAsyncActor.class, UUID.randomUUID().toString()).tell("", ActorRef.noSender());
                    return complete(StatusCodes.NO_CONTENT);
                }),
                path("fail", () -> invokeActorWithExceptionHandler(
                        system,
                        createActorRef(system, FailActor.class, UUID.randomUUID().toString()),
                        "message",
                        t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                        o -> HttpResponse.create().withEntity(o.toString())))
        );
    }
}
