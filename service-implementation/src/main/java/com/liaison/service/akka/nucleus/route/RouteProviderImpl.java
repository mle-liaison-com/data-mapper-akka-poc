package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.core.route.RouteProvider;

import static akka.http.javadsl.server.Directives.route;

public class RouteProviderImpl implements RouteProvider {

    private final ActorSystem system;

    public RouteProviderImpl(ActorSystem system) {
        this.system = system;
    }

    @Override
    public Route create() {
        return route(
                new SampleRouteProvider(system).create(),
                new HelloRouteProvider(system).create()
        );
    }
}
