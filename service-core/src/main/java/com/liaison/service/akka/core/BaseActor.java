package com.liaison.service.akka.core;

import akka.actor.AbstractActor;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import com.typesafe.config.Config;

/**
 * Wrapper class of {@link AbstractActor} with utility methods
 */
public abstract class BaseActor extends AbstractActor {

    // TODO try using -Dlogback.configurationFile to specify log config file instead of default logback.xml
    // TODO no longer using actor per request pattern. move gpuid elsewhere (perhaps new logging utility methods ?)
    private final DiagnosticLoggingAdapter logger;

    /**
     * Empty constructor that initializes instance-local logger
     */
    protected BaseActor() {
        logger = Logging.withMarker(this);
    }

    /**
     * Utility method that allows user to simply get {@link Config} from {@link akka.actor.AbstractActor.ActorContext}
     *
     * @return {@link Config} for this {@link akka.actor.Actor}
     */
    protected Config getConfig() {
        return getContext().getSystem().settings().config();
    }

    /**
     * Getter for logger
     *
     * @return {@link DiagnosticLoggingAdapter} for this {@link akka.actor.Actor}
     */
    protected DiagnosticLoggingAdapter getLogger() {
        return logger;
    }
}
