package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import com.liaison.service.akka.core.route.RouteProvider;
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
                directives.path("sync", () -> directives.completeWithFuture(invokeSyncActor(system))),
                directives.path("async", () -> {
                    ActorRef async = system.actorOf(Props.create(HelloWorldAsyncActor.class, UUID.randomUUID().toString()));
                    async.tell("", async);
                    return directives.complete(StatusCodes.NO_CONTENT);
                })
        );
    }

    private CompletionStage<HttpResponse> invokeSyncActor(ActorSystem system) {
        ActorRef ref = system.actorOf(Props.create(HelloWorldSyncActor.class, UUID.randomUUID().toString()));
        return askAndStop(system, ref, "", 5000, o -> {
            if (o instanceof Exception) {
                return HttpResponse.create().withStatus(StatusCodes.INTERNAL_SERVER_ERROR).withEntity(((Exception) o).getMessage());
            } else {
                return HttpResponse.create().withEntity(o.toString());
            }
        });
    }

    private CompletionStage<HttpResponse> askAndStop(ActorSystem system, ActorRef ref, Object message, long timeout,
                                                      Function<Object, HttpResponse> function) {
        return PatternsCS.ask(ref, message, timeout).thenApplyAsync(o -> {
            try {
                return function.apply(o);
            } finally {
                system.stop(ref);
            }
        });
    }
}
