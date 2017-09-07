package com.liaison.service.akka.core.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.server.Route;

public interface RouteProvider {

    Route create(ActorSystem system);
}
