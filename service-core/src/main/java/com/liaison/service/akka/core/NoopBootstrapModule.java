package com.liaison.service.akka.core;

import akka.actor.ActorSystem;

/**
 * Default {@link BootstrapModule} if {@value CONFIG_BOOTSTRAP_CLASS} is not provided in {@link com.typesafe.config.Config}
 */
public final class NoopBootstrapModule implements BootstrapModule {

    @Override
    public void configure(ActorSystem system) {
        system.log().warning("Initializing " + this.getClass().getName() +
                ". It is required to specify bootstrap class via " + CONFIG_BOOTSTRAP_CLASS +
                " to make service functional.");
        while (true) { }
    }
}
