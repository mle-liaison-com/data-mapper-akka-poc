package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.core.route.RouteProvider;

import static akka.http.javadsl.server.Directives.route;

public class RouteProviderImpl implements RouteProvider {

    // TODO actor vs future
    // TODO route test
    @Override
    public Route create(final ActorSystem system) {
        return route(
                new SampleRouteProvider().create(system),
                new HelloRouteProvider().create(system)
        );
    }
}
