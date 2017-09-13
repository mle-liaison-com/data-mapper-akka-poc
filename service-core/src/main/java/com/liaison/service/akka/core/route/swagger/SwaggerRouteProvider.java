package com.liaison.service.akka.core.route.swagger;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import com.github.swagger.akka.javadsl.SwaggerGenerator;
import com.liaison.service.akka.core.route.RouteProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.route;

// TODO look into using reflection utils to load packages, instead of classes
public class SwaggerRouteProvider implements RouteProvider {

    public static final String CONFIG_SWAGGER_CLASSES = "com.liaison.service.akka.swagger.classes";

    private final ActorSystem system;

    public SwaggerRouteProvider(ActorSystem system) {
        this.system = system;
    }

    @Override
    public Route create() {
        List<String> list = system.settings().config().getStringList(CONFIG_SWAGGER_CLASSES);
        Set<Class<?>> set = list.stream().map(str -> {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unable to load class.", e);
            }
        })
        .collect(Collectors.toSet());
        SwaggerGenerator swaggerGenerator = new AkkaSwaggerGenerator(set);
        return route(path(PathMatchers.segment(swaggerGenerator.apiDocsPath()).slash("swagger.json"),
                () -> get(() -> complete(swaggerGenerator.generateSwaggerJson()))));
    }
}
