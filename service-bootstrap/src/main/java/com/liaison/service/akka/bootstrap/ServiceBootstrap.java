package com.liaison.service.akka.bootstrap;

import akka.actor.ActorSystem;
import com.liaison.service.akka.bootstrap.config.ConfigBootstrap;
import com.liaison.service.akka.core.BootstrapModule;
import com.typesafe.config.Config;

public final class ServiceBootstrap {

    public static final String CONFIG_BOOTSTRAP_MODULE_CLASS = "com.liaison.service.akka.bootstrap.class";
    public static final String CONFIG_ACTOR_SYSTEM_NAME = "com.liaison.service.akka.actor.system.name";

    public static void main(String[] args) {
        Config complete = ConfigBootstrap.getConfig();

        String className = complete.getString(CONFIG_BOOTSTRAP_MODULE_CLASS);
        if (className == null || className.isEmpty()) {
            throw new IllegalStateException(String.format("%s is not configured or left empty.", CONFIG_BOOTSTRAP_MODULE_CLASS));
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load bootstrap class.", e);
        }

        if (!BootstrapModule.class.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Bootstrap class must implement " + BootstrapModule.class.getName());
        }

        ActorSystem system = ActorSystem.create(complete.getString(CONFIG_ACTOR_SYSTEM_NAME), complete);
        try {
            BootstrapModule bootstrapModule = clazz.asSubclass(BootstrapModule.class).newInstance();
            bootstrapModule.configure(system);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception while trying to bootstrap the service.", e);
        } finally {
            system.terminate();
        }
    }
}
