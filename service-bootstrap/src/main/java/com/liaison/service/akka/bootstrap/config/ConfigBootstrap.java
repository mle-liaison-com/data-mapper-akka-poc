package com.liaison.service.akka.bootstrap.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;

import java.util.Arrays;

// TODO file-based configuration hierarchy ???
public final class ConfigBootstrap {

    private ConfigBootstrap() {

    }

    public static final String CONFIG_AKKA_DEPLOYMENT_APPLICATIONID = "akka.deployment.applicationId";
    public static final String CONFIG_AKKA_DEPLOYMENT_STACK = "akka.deployment.stack";
    public static final String CONFIG_AKKA_DEPLOYMENT_ENVIRONMENT = "akka.deployment.environment";
    public static final String CONFIG_AKKA_DEPLOYMENT_REGION = "akka.deployment.region";
    public static final String CONFIG_AKKA_DEPLOYMENT_DATACENTER = "akka.deployment.datacenter";

    private static final String CONFIG_LOGGING = "logging.conf";
    private static final Config COMPLETE;
    static {
        final String applicationId = System.getProperty(CONFIG_AKKA_DEPLOYMENT_APPLICATIONID);
        final String stack = System.getProperty(CONFIG_AKKA_DEPLOYMENT_STACK);
        final String environment = System.getProperty(CONFIG_AKKA_DEPLOYMENT_ENVIRONMENT);
        final String region = System.getProperty(CONFIG_AKKA_DEPLOYMENT_REGION);
        final String datacenter = System.getProperty(CONFIG_AKKA_DEPLOYMENT_DATACENTER);
        if (applicationId == null || stack == null || environment == null || region == null || datacenter == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Missing required system properties. %s = %s, %s = %s, %s = %s, %s = %s, %s = %s",
                            CONFIG_AKKA_DEPLOYMENT_APPLICATIONID,
                            applicationId,
                            CONFIG_AKKA_DEPLOYMENT_STACK,
                            stack,
                            CONFIG_AKKA_DEPLOYMENT_ENVIRONMENT,
                            environment,
                            CONFIG_AKKA_DEPLOYMENT_REGION,
                            region,
                            CONFIG_AKKA_DEPLOYMENT_DATACENTER,
                            datacenter
                    ));
        }

        Config combined = getStrictConfig(getConfigName(applicationId));
        combined = combine(getConfigName(applicationId, stack), combined);
        combined = combine(getConfigName(applicationId, environment), combined);
        combined = combine(getConfigName(applicationId, environment, region), combined);
        combined = combine(getConfigName(applicationId, environment, datacenter), combined);
        // TODO logger configs can only be read from top of config stack. check if this is a bug
        combined = combine(CONFIG_LOGGING, combined);
        COMPLETE = ConfigFactory.load(combined);
    }

    private static String getConfigName(String applicationId, String... names) {
        return Arrays.stream(names).reduce(applicationId, (a, b) -> a + "-" + b);
    }

    private static Config combine(String baseConfigName, Config fallback) {
        Config base = getStrictConfig(baseConfigName);
        Config combined = base.withFallback(fallback);
        return ConfigFactory.load(combined);
    }

    private static Config getStrictConfig(String configName) {
        return ConfigFactory.load(configName,
                ConfigParseOptions.defaults().setAllowMissing(false),
                ConfigResolveOptions.defaults());
    }

    public static Config getConfig() {
        return COMPLETE;
    }
}
