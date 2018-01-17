package com.liaison.service.akka.http;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.settings.ServerSettings;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.liaison.service.akka.http.route.metrics.MetricsRouteProvider;
import com.liaison.service.akka.http.route.swagger.SwaggerRouteProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.route;

/**
 * Simple class for {@link HttpApp} with HTTPS enabled.
 *
 * Main purpose of this class is to provide Swagger documentations for each route exposed.
 */
public class BaseHttpApp {

    public static final String CONFIG_HTTP_SERVER_HOST = "com.liaison.service.akka.http.server.host";
    public static final String CONFIG_HTTP_SERVER_PORT = "com.liaison.service.akka.http.server.port";

    private final AtomicReference<ServerBinding> serverBinding = new AtomicReference<>();
    private final ActorSystem system;
    private final Route route;

    /**
     * Constructs {@link HttpApp} with provided route.
     *
     * @param system {@link ActorSystem} of the service
     * @param route Combination of all {@link Route} for the service
     */
    public BaseHttpApp(@Nonnull ActorSystem system, @Nonnull Route route) {
        Objects.requireNonNull(system, "ActorSystem must not be null");
        Objects.requireNonNull(route, "Route must not be null");

        this.system = system;
        this.route = pathPrefix(system.name(), () -> route(route, new SwaggerRouteProvider(system).create(), new MetricsRouteProvider().create()));
    }

    public void startServer(String host, int port, ServerSettings settings) throws ExecutionException, InterruptedException {
        ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> flow = route.flow(system, materializer);
        final Http http = Http.get(system);
        // HttpsConnectionContext https = HttpsContextBuilder.build(system);
        // http.setDefaultServerHttpContext(https);
        CompletionStage<ServerBinding> bindingFuture = http.bindAndHandle(flow, ConnectHttp.toHost(host, port),
                settings, system.log(), materializer);

        bindingFuture.handle((binding, exception) -> {
            if (exception != null) {
                system.log().error(exception, "Error starting the server: " + exception.getMessage());
            } else {
                //setting the server binding for possible future uses in the client
                serverBinding.set(binding);
                system.log().info("Server online at http://" + binding.localAddress().getHostName() + ":" + binding.localAddress().getPort() + "/");
            }
            return null;
        });

        try {
            bindingFuture.thenCompose(ignore -> waitForShutdownSignal(system)) // chaining both futures to fail fast
                    .toCompletableFuture()
                    .exceptionally(ignored -> Done.getInstance()) // If the future fails, we want to complete normally
                    .get(); // It's waiting forever because maybe there is never a shutdown signal
        } finally {
            bindingFuture.thenCompose(ServerBinding::unbind).handle((success, exception) -> {
                system.log().info("Shutting down the server");
                return null;
            });
        }
    }

    private CompletionStage<Done> waitForShutdownSignal (ActorSystem system) {
        final CompletableFuture<Done> promise = new CompletableFuture<>();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> promise.complete(Done.getInstance())));
        CompletableFuture.runAsync(() -> {
            System.out.println("Press RETURN to stop...");
            try {
                if (System.in.read() >= 0)
                    promise.complete(Done.getInstance());
            } catch (IOException e) {
                system.log().error(e, "Problem occurred! " + e.getMessage());
            }
        });
        return promise;
    }
}
