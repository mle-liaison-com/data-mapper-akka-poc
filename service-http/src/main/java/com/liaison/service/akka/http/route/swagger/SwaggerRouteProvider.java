package com.liaison.service.akka.http.route.swagger;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import com.github.swagger.akka.javadsl.SwaggerGenerator;
import com.liaison.service.akka.http.route.RouteProvider;
import com.typesafe.config.Config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.route;
import static com.liaison.service.akka.http.BaseHttpApp.CONFIG_HTTP_SERVER_HOST;
import static com.liaison.service.akka.http.BaseHttpApp.CONFIG_HTTP_SERVER_PORT;

/**
 * {@link RouteProvider} dedicated to Swagger documentation.
 * This class provides a {@link Route} to Swagger JSON generated from all classes defined in
 * {@value #CONFIG_SWAGGER_CLASSES} {@link Config}.
 *
 * All configured classes MUST declare their Swagger documented methods as public.
 * Otherwise, it will not be visible in resulting Swagger documentation.
 *
 * This class does NOT provide Swagger UI
 */
// TODO look into using reflection utils to load packages, instead of classes
public class SwaggerRouteProvider implements RouteProvider {

    public static final String CONFIG_SWAGGER_CLASSES = "com.liaison.service.akka.http.swagger.classes";

    private final ActorSystem system;

    /**
     * Creates a {@link RouteProvider} with a provided {@link ActorSystem}
     *
     * @param system {@link ActorSystem} of the service
     */
    public SwaggerRouteProvider(ActorSystem system) {
        this.system = system;
    }

    /**
     * Provides a GET {@link Route} to Swagger documentation JSON generated from all classes defined in
     * {@value #CONFIG_SWAGGER_CLASSES}
     *
     * @return {@link Route} to Swagger documentation JSON
     */
    @Override
    public Route create() {
        Config config = system.settings().config();
        List<String> list = config.getStringList(CONFIG_SWAGGER_CLASSES);
        Set<Class<?>> set = list.stream().map(str -> {
            try {
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unable to load class.", e);
            }
        })
        .collect(Collectors.toSet());
        String host = config.getString(CONFIG_HTTP_SERVER_HOST) + ":" + config.getString(CONFIG_HTTP_SERVER_PORT);
        SwaggerGenerator swaggerGenerator = new AkkaSwaggerGenerator(host, set);
        return route(path(PathMatchers.segment(swaggerGenerator.apiDocsPath()).slash("swagger.json"),
                () -> get(() -> complete(swaggerGenerator.generateSwaggerJson()))));
    }
}
