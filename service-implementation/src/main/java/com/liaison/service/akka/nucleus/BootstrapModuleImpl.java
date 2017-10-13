package com.liaison.service.akka.nucleus;

import akka.actor.ActorSystem;
import akka.http.javadsl.settings.ServerSettings;
import com.liaison.service.akka.core.BootstrapModule;
import com.liaison.service.akka.http.BaseHttpApp;
import com.liaison.service.akka.nucleus.route.RouteProviderImpl;
import com.typesafe.config.Config;

import java.util.concurrent.ExecutionException;

import static com.liaison.service.akka.http.BaseHttpApp.CONFIG_HTTP_SERVER_HOST;
import static com.liaison.service.akka.http.BaseHttpApp.CONFIG_HTTP_SERVER_PORT;

public class BootstrapModuleImpl implements BootstrapModule {

    public void configure(ActorSystem system) {
        Config config = system.settings().config();

        BaseHttpApp app = new BaseHttpApp(system, new RouteProviderImpl(system));
        try {
            app.startServer(config.getString(CONFIG_HTTP_SERVER_HOST),
                    config.getInt(CONFIG_HTTP_SERVER_PORT),
                    ServerSettings.create(config));
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException("Unable to start HTTP server.", e);
        }
    }
}
