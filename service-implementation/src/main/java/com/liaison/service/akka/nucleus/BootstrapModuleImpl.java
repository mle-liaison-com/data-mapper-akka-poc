package com.liaison.service.akka.nucleus;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.http.javadsl.settings.ServerSettings;
import akka.routing.FromConfig;
import com.liaison.service.akka.core.ActorMessageConsumer;
import com.liaison.service.akka.core.BootstrapModule;
import com.liaison.service.akka.core.EntryActor;
import com.liaison.service.akka.core.WorkTicketOuterClass;
import com.liaison.service.akka.http.BaseHttpApp;
import com.liaison.service.akka.nucleus.actor.HelloWorldActor;
import com.liaison.service.akka.nucleus.route.RouteProviderImpl;
import com.typesafe.config.Config;

import java.util.concurrent.ExecutionException;

import static com.liaison.service.akka.http.BaseHttpApp.CONFIG_HTTP_SERVER_HOST;
import static com.liaison.service.akka.http.BaseHttpApp.CONFIG_HTTP_SERVER_PORT;

public class BootstrapModuleImpl implements BootstrapModule {

    @Override
    public void configure(ActorSystem system) {
        Config config = system.settings().config();

        ActorRef helloRef = system.actorOf(FromConfig.getInstance().props(Props.create(HelloWorldActor.class)), "hello");
        ActorMessageConsumer<WorkTicketOuterClass.WorkTicket> consumer = (msg, ctx, sender) -> {
            // check roles and route message to appropriate actor
            if (msg != null) {
                helloRef.forward(msg, ctx);
            } else {
                Exception exception = new IllegalStateException("Unknown Message Type.");
                sender.tell(new Status.Failure(exception), ActorRef.noSender());
                throw exception;
            }
        };
        // expose entry actor only in akka.remote.trusted-selection-paths
        system.actorOf(Props.create(EntryActor.class, WorkTicketOuterClass.WorkTicket.class, consumer), "entry");

        // HttpApp#startServer call MUST be at the end of the method as it is blocking
        BaseHttpApp app = new BaseHttpApp(system, new RouteProviderImpl(system, helloRef).create());
        try {
            app.startServer(config.getString(CONFIG_HTTP_SERVER_HOST),
                    config.getInt(CONFIG_HTTP_SERVER_PORT),
                    ServerSettings.create(config));
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException("Unable to start HTTP server.", e);
        }
    }
}
