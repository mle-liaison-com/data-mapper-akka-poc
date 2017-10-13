package com.liaison.service.akka.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.http.route.RouteProvider;
import com.liaison.service.akka.http.route.swagger.SwaggerRouteProvider;

public final class BaseHttpApp extends HttpApp {

    private final ActorSystem system;
    private final RouteProvider routeProvider;

    public BaseHttpApp(ActorSystem system, RouteProvider routeProvider) {
        this.system = system;
        this.routeProvider = routeProvider;
    }

    @Override
    protected Route routes() {
        return route(routeProvider.create(), new SwaggerRouteProvider(system).create());
    }

    public static final String CONFIG_HTTP_SERVER_HOST = "com.liaison.service.akka.http.server.host";
    public static final String CONFIG_HTTP_SERVER_PORT = "com.liaison.service.akka.http.server.port";
}
