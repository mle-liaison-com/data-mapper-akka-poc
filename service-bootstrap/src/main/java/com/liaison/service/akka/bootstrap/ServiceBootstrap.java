package com.liaison.service.akka.bootstrap;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.settings.ServerSettings;
import akka.stream.ActorMaterializer;
import com.liaison.service.akka.bootstrap.config.ConfigBootstrap;
import com.liaison.service.akka.core.route.RouteProvider;
import com.liaison.service.akka.nucleus.route.HelloWorldRouteProvider;
import com.typesafe.config.Config;

public final class ServiceBootstrap extends HttpApp {

    private final ActorSystem system;
    private final RouteProvider routeProvider;

    private ServiceBootstrap(ActorSystem system, RouteProvider routeProvider) {
        this.system = system;
        this.routeProvider = routeProvider;
    }

    @Override
    protected Route routes() {
        return routeProvider.get(system, this);
    }

    @Override
    protected void postHttpBindingFailure(Throwable cause) {
        systemReference.get().log().info("I can't bind!");
    }

    public static final String CONFIG_AKKA_HTTP_SERVER_HOST = "akka.http.server.host";
    public static final String CONFIG_AKKA_HTTP_SERVER_PORT = "akka.http.server.port";

    public static void main(String[] args) throws Exception {
        // TODO for local test only. delete once system properties are correctly passed in
        System.setProperty(ConfigBootstrap.CONFIG_AKKA_DEPLOYMENT_APPLICATIONID, "akka-nucleus");
        System.setProperty(ConfigBootstrap.CONFIG_AKKA_DEPLOYMENT_STACK, "stack");
        System.setProperty(ConfigBootstrap.CONFIG_AKKA_DEPLOYMENT_ENVIRONMENT, "dev");
        System.setProperty(ConfigBootstrap.CONFIG_AKKA_DEPLOYMENT_REGION, "region");
        System.setProperty(ConfigBootstrap.CONFIG_AKKA_DEPLOYMENT_DATACENTER, "datacenter");
        Config complete = ConfigBootstrap.getConfig();

        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("akka-service", complete);
        ActorMaterializer materializer = ActorMaterializer.create(system);

        //In order to access all directives we need an instance where the routes are defined.
        ServiceBootstrap app = new ServiceBootstrap(system, new HelloWorldRouteProvider());
        app.startServer(complete.getString(CONFIG_AKKA_HTTP_SERVER_HOST),
                complete.getInt(CONFIG_AKKA_HTTP_SERVER_PORT),
                ServerSettings.create(complete));

        system.terminate();
    }
}
