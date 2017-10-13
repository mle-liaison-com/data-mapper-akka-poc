package com.liaison.service.akka.bootstrap.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public final class ConfigBootstrap {

    private ConfigBootstrap() {

    }

    public static final String CONFIG_AKKA_DEPLOYMENT_APPLICATIONID = "akka.deployment.applicationId";
    public static final String CONFIG_AKKA_DEPLOYMENT_STACK = "akka.deployment.stack";
    public static final String CONFIG_AKKA_DEPLOYMENT_ENVIRONMENT = "akka.deployment.environment";
    public static final String CONFIG_AKKA_DEPLOYMENT_REGION = "akka.deployment.region";
    public static final String CONFIG_AKKA_DEPLOYMENT_DATACENTER = "akka.deployment.datacenter";
    public static final String CONFIG_AKKA_ADDITIONAL_URLS = "akka.configurationSource.additionalUrls";

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
        combined = combine(loadConfigByName(applicationId), combined);
        combined = combine(loadConfigByName(applicationId, stack), combined);
        combined = combine(loadConfigByName(applicationId, environment), combined);
        combined = combine(loadConfigByName(applicationId, environment, region), combined);
        combined = combine(loadConfigByName(applicationId, environment, datacenter), combined);

        String additionalUrls = System.getProperty(CONFIG_AKKA_ADDITIONAL_URLS);
        if (additionalUrls != null) {
            combined = combine(loadConfigFromUrl(additionalUrls), combined);
        }

        COMPLETE = combined;
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
     * @param applicationId name of the application
     * @param names additional config names
     * @return Config object containing configs defined in specified file only
     */
    private static Config loadConfigByName(String applicationId, String... names) {
        String configName = Arrays.stream(names).reduce(applicationId, (a, b) -> a + "-" + b);
        return ConfigFactory.parseResourcesAnySyntax(configName, ConfigParseOptions.defaults().setAllowMissing(false));
    }

    private static Config loadConfigFromUrl(String urlStr) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to convert property into URL", e);
        }

        return ConfigFactory.parseURL(url, ConfigParseOptions.defaults().setAllowMissing(false));
    }

    private static Config combine(Config base, Config fallback) {
        Config combined = base.withFallback(fallback);
        return ConfigFactory.load(combined);
    }

    public static Config getConfig() {
        return COMPLETE;
    }
}
