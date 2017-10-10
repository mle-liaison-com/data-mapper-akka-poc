package com.liaison.service.akka.bootstrap.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import java.util.Arrays;

public final class ConfigBootstrap {

    private ConfigBootstrap() {

    }

    public static final String CONFIG_AKKA_DEPLOYMENT_APPLICATIONID = "akka.deployment.applicationId";
    public static final String CONFIG_AKKA_DEPLOYMENT_STACK = "akka.deployment.stack";
    public static final String CONFIG_AKKA_DEPLOYMENT_ENVIRONMENT = "akka.deployment.environment";
    public static final String CONFIG_AKKA_DEPLOYMENT_REGION = "akka.deployment.region";
    public static final String CONFIG_AKKA_DEPLOYMENT_DATACENTER = "akka.deployment.datacenter";

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

        Config combined = ConfigFactory.load();
        combined = combine(getConfigName(applicationId), combined);
        combined = combine(getConfigName(applicationId, stack), combined);
        combined = combine(getConfigName(applicationId, environment), combined);
        combined = combine(getConfigName(applicationId, environment, region), combined);
        COMPLETE = combine(getConfigName(applicationId, environment, datacenter), combined);
        // TODO secure.properties
    }

    private static String getConfigName(String applicationId, String... names) {
        return Arrays.stream(names).reduce(applicationId, (a, b) -> a + "-" + b);
    }

    private static Config combine(String baseConfigName, Config fallback) {
        Config base = getStrictConfig(baseConfigName);
        Config combined = base.withFallback(fallback);
        return ConfigFactory.load(combined);
    }

    /**
     * {@link ConfigFactory#load(String)} involves 3 steps
     *      1. loading {@link ConfigFactory#defaultOverrides()}
     *      2. loading user provided config via {@link ConfigFactory#parseResourcesAnySyntax(String)}
     *      3. loading {@link ConfigFactory#defaultReference(ClassLoader)}
     *  Step 1 loads all the default Akka configurations first,
     *  causing previously overwritten Akka configurations to be lost (custom configurations are fine).
     *  To preserve all user provided configurations,
     *  {@link ConfigFactory#parseResourcesAnySyntax(String)} needs to be used in isolation.
     *
     * @param configName name of the config file
     * @return Config object containing configs defined in specified file only
     */
    private static Config getStrictConfig(String configName) {
        return ConfigFactory.parseResourcesAnySyntax(configName,
                ConfigParseOptions.defaults().setAllowMissing(false));
    }

    public static Config getConfig() {
        return COMPLETE;
    }
}
