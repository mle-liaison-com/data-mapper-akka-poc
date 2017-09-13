package com.liaison.service.akka.core;

import akka.actor.AbstractActor;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActor extends AbstractActor {

    // TODO try using -Dlogback.configurationFile to specify log config file instead of default logback.xml
    // TODO no longer using actor per request pattern. move gpuid elsewhere (perhaps new logging utility methods ?)
    private final DiagnosticLoggingAdapter logger;

    protected BaseActor(String gpuid) {
        logger = Logging.withMarker(this);
        Map<String, Object> mdc = new HashMap<>();
        mdc.put("gpuid", gpuid);
        logger.setMDC(mdc);
    }

    protected Config getConfig() {
        return getContext().getSystem().settings().config();
    }

    protected DiagnosticLoggingAdapter getLogger() {
        return logger;
    }
}
