package com.liaison.service.akka.core;

import akka.actor.ActorSystem;

public class NoopBootstrapModule implements BootstrapModule {

    @Override
    public void configure(ActorSystem system) {
        system.log().warning("Initializing " + this.getClass().getName());
        while (true) { }
    }
}
