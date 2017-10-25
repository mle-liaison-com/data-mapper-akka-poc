package com.liaison.service.akka.bootstrap;

import akka.actor.ActorSystem;
import com.liaison.service.akka.bootstrap.config.ConfigBootstrap;
import com.liaison.service.akka.core.ActorSystemWrapper;
import com.liaison.service.akka.core.BootstrapModule;
import com.typesafe.config.Config;

import java.util.List;

import static com.liaison.service.akka.core.ActorSystemWrapper.CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS;
import static com.liaison.service.akka.core.ActorSystemWrapper.CONFIG_AKKA_REMOTE_UNTRUSTED_MODE;

/**
 * This class is responsible for bootstrapping the whole service.
 *
 * Bootstrapping steps are
 * 1. Load {@link Config} from {@link ConfigBootstrap}
 * 2. Lookup BootstrapModule using {@value CONFIG_BOOTSTRAP_MODULE_CLASS} value
 * 3. Run BootstrapModule
 *
 * Failure in any one of those steps will result in service bootstrap failure.
 */
public final class ServiceBootstrap {

    public static final String CONFIG_BOOTSTRAP_MODULE_CLASS = "com.liaison.service.akka.bootstrap.class";
    public static final String CONFIG_ACTOR_SYSTEM_NAME = "com.liaison.service.akka.actor.system.name";

    public static void main(String[] args) {
        Config complete = ConfigBootstrap.getConfig();

        String className = complete.getString(CONFIG_BOOTSTRAP_MODULE_CLASS);
        if (className == null || className.isEmpty()) {
            throw new IllegalStateException(String.format("%s is not configured or left empty.", CONFIG_BOOTSTRAP_MODULE_CLASS));
        }

        boolean untrustedMode = complete.getBoolean(CONFIG_AKKA_REMOTE_UNTRUSTED_MODE);
        List<String> trustedSelectionPaths = complete.getStringList(CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS);
        if (!untrustedMode || trustedSelectionPaths == null || trustedSelectionPaths.size() == 0) {
            throw new IllegalStateException(String.format("%s must be turned on and %s must be non-empty list to enable Actor authorization", CONFIG_AKKA_REMOTE_UNTRUSTED_MODE, CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS));
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

        ActorSystem system = new ActorSystemWrapper(ActorSystem.create(complete.getString(CONFIG_ACTOR_SYSTEM_NAME), complete));
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
