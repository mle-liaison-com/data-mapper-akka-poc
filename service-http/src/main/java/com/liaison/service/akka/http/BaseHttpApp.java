package com.liaison.service.akka.http;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.http.route.swagger.SwaggerRouteProvider;

/**
 * Simple wrapper class for {@link HttpApp}.
 *
 * Main purpose of this class is to provide Swagger documentations for each route exposed.
 */
public class BaseHttpApp extends HttpApp {

    public static final String CONFIG_HTTP_SERVER_HOST = "com.liaison.service.akka.http.server.host";
    public static final String CONFIG_HTTP_SERVER_PORT = "com.liaison.service.akka.http.server.port";

    private final ActorSystem system;
    private final Route route;

    /**
     * Constructs {@link HttpApp} with provided route.
     *
     * @param system {@link ActorSystem} of the service
     * @param route Combination of all {@link Route} for the service
     */
    public BaseHttpApp(ActorSystem system, Route route) {
        this.system = system;
        this.route = route;
    }

    /**
     * Creates a {@link Route} that combines the provided route and Swagger documentation route
     *
     * @return Swagger documentation route in addition to {@link Route} provided in the constructor
     */
    @Override
    protected Route routes() {
        return route(route, new SwaggerRouteProvider(system).create());
    }
}
