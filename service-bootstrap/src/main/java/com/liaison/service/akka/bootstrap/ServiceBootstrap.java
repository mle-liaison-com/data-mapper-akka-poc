package com.liaison.service.akka.bootstrap;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.ExtendedActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import com.liaison.service.akka.bootstrap.cluster.SimpleClusterListener;
import com.liaison.service.akka.core.ActorSystemWrapper;
import com.liaison.service.akka.core.BootstrapModule;
import com.liaison.service.akka.core.config.ConfigManager;
import com.typesafe.config.Config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.liaison.service.akka.core.ActorSystemWrapper.CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS;
import static com.liaison.service.akka.core.ActorSystemWrapper.CONFIG_AKKA_REMOTE_UNTRUSTED_MODE;

/**
 * This class is responsible for bootstrapping the whole service.
 *
 * Bootstrapping steps are
 * 1. Load {@link Config} from {@link ConfigManager}
 * 2. Lookup BootstrapModule using {@value CONFIG_BOOTSTRAP_MODULE_CLASS} value
 * 3. Set up cluster if SEED_NODES environment value is set
 * 4. Run BootstrapModule
 *
 * Failure in any one of those steps will result in service bootstrap failure.
 */
public final class ServiceBootstrap {

    private static final String TRANSPORT_NETTY_SSL = "akka.remote.netty.ssl";
    private static final String TRANSPORT_NETTY_TCP = "akka.remote.netty.tcp";
    private static final String CONFIG_NETTY_SSL_PORT = TRANSPORT_NETTY_SSL + ".port";
    private static final String CONFIG_NETTY_TCP_PORT = TRANSPORT_NETTY_TCP + ".port";
    private static final String CONFIG_AKKA_REMOTE_ENABLED_TRANSPORTS = "akka.remote.enabled-transports";
    private static final String CONFIG_ACTOR_SYSTEM_NAME = "com.liaison.service.akka.actor.system.name";
    private static final String CONFIG_BOOTSTRAP_MODULE_CLASS = "com.liaison.service.akka.bootstrap.class";
    private static final String ENV_KEY_SEED_NODES = "SEED_NODES";
    private static final String SCHEME_AKKA_SSL_TCP = "akka.ssl.tcp";
    private static final String SCHEME_AKKA_TCP = "akka.tcp";

    public static void main(String[] args) {
        Config complete = ConfigManager.getConfig();

        String className = complete.getString(CONFIG_BOOTSTRAP_MODULE_CLASS);
        if (className == null || className.isEmpty()) {
            throw new IllegalStateException(String.format("%s is not configured or left empty.", CONFIG_BOOTSTRAP_MODULE_CLASS));
        }

        boolean untrustedMode = complete.getBoolean(CONFIG_AKKA_REMOTE_UNTRUSTED_MODE);
        List<String> trustedSelectionPaths = complete.getStringList(CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS);
        // TODO untrusted mode is currently turned off to make cluster working.
        // TODO look for cluster message path, add the path to trusted paths, then turn untrusted mode back on
        if (!untrustedMode || trustedSelectionPaths == null || trustedSelectionPaths.size() == 0) {
            // throw new IllegalStateException(String.format("%s must be turned on and %s must be non-empty list to enable Actor authorization", CONFIG_AKKA_REMOTE_UNTRUSTED_MODE, CONFIG_AKKA_REMOTE_TRUSTED_SELECTION_PATHS));
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

        String actorSystemName = complete.getString(CONFIG_ACTOR_SYSTEM_NAME);
        ActorSystem system = new ActorSystemWrapper((ExtendedActorSystem) ActorSystem.create(actorSystemName, complete));

        // setup cluster
        String seedNodes = System.getenv(ENV_KEY_SEED_NODES);
        if (seedNodes != null && !seedNodes.trim().isEmpty()) {
            List<String> enabledTransports = complete.getStringList(CONFIG_AKKA_REMOTE_ENABLED_TRANSPORTS);
            boolean isSsl = enabledTransports.contains(TRANSPORT_NETTY_SSL);
            String scheme = isSsl ? SCHEME_AKKA_SSL_TCP : SCHEME_AKKA_TCP;
            int port = isSsl ? complete.getInt(CONFIG_NETTY_SSL_PORT) : complete.getInt(CONFIG_NETTY_TCP_PORT);

            system.actorOf(Props.create(SimpleClusterListener.class));

            Cluster cluster = Cluster.get(system);
            List<Address> addresses = Arrays.stream(seedNodes.split(","))
                    .map(host -> new Address(scheme, actorSystemName, host, port))
                    .collect(Collectors.toList());
            cluster.joinSeedNodes(addresses);
        }

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
