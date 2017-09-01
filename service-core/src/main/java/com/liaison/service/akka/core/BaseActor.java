package com.liaison.service.akka.core;

import akka.actor.AbstractActor;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActor extends AbstractActor {

    // TODO try using -Dlogback.configurationFile to specify log config file instead of default logback.xml
    private final DiagnosticLoggingAdapter logger;
    private final String gpuid;

    protected BaseActor(String gpuid) {
        this.gpuid = gpuid;
        logger = Logging.withMarker(this);
        Map<String, Object> mdc = new HashMap<>();
        mdc.put("gpuid", gpuid);
        logger.setMDC(mdc);
    }

    protected final String getGpuid() {
        return gpuid;
    }

    protected Config getConfig() {
        return getContext().getSystem().settings().config();
    }

    protected DiagnosticLoggingAdapter getLogger() {
        return logger;
    }

    @Override
    public void postStop() {
        logger.clearMDC();
    }
}
