package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.http.route.RouteProvider;

import static akka.http.javadsl.server.Directives.route;

public class RouteProviderImpl implements RouteProvider {

    private final ActorSystem system;
    private final ActorRef helloRef;

    public RouteProviderImpl(ActorSystem system, ActorRef helloRef) {
        this.system = system;
        this.helloRef = helloRef;
    }

    @Override
    public Route create() {
        return route(
                new SampleRouteProvider(system).create(),
                new HelloRouteProvider(system, helloRef).create()
        );
    }
}
