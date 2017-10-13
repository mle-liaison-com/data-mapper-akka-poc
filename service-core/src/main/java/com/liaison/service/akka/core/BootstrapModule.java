package com.liaison.service.akka.core;

import akka.actor.ActorSystem;

public interface BootstrapModule {

    void configure(ActorSystem system);
}
