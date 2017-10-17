package com.liaison.service.akka.core;

import akka.actor.ActorSystem;

/**
 * This interface provides a generic method where all service components initializations should happen.
 *
 * All classes implementing this interface should provide an empty constructor.
 * Otherwise, it won't be initialized during service bootstrap.
 *
 * Implementation of {@link #configure(ActorSystem)} must be blocking.
 * If not, system will exit once it completes bootstrapping.
 */
public interface BootstrapModule {

    /**
     * Configures and initializes all necessary service components.
     *
     * Any blocking component initialization (i.e. HttpApp) MUST be declared at the very end.
     * Otherwise, rest of the initialization will not be reached and initialized.
     *
     * @param system {@link ActorSystem} for the service
     */
    void configure(ActorSystem system);
}
