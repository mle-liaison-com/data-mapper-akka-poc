package com.liaison.service.akka.core.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * This class provides an eagerly initialized singleton {@link Config} that is used by the whole service.
 * As configurations must be provided to create an {@link akka.actor.ActorSystem}, there is no need to lazily initialize.
 *
 * Configuration files loaded by this class should conform to existing configuration hierarchy used by Alloy platform.
 * See <a href="https://github.com/LiaisonTechnologies/g2-lib-configuration/blob/master/library/README.md">g2-lib-configuration</a>
 * Failure to provide any one of the files will result in bootstrap failure.
 *
 * Also, most of configuration changes go through a full SDLC, and even in-place changes are assumed to require a restart.
 * Thus, it will NOT support dynamic configuration loading.
 */
public final class ConfigManager {

    private ConfigManager() {

    }

    private static final String ENVIRONMENT_VARIABLE_APPLICATION_ID = "APPLICATION_ID";
    private static final String ENVIRONMENT_VARIABLE_STACK = "STACK";
    private static final String ENVIRONMENT_VARIABLE_ENVIRONMENT = "ENVIRONMENT";
    private static final String ENVIRONMENT_VARIABLE_REGION = "REGION";
    private static final String ENVIRONMENT_VARIABLE_DATACENTER = "DATACENTER";
    private static final String ENVIRONMENT_VARIABLE_ADDITIONAL_URLS = "ADDITIONAL_URLS";

    private static final Config COMPLETE;
    static {
        final String applicationId = System.getenv(ENVIRONMENT_VARIABLE_APPLICATION_ID);
        final String stack = System.getenv(ENVIRONMENT_VARIABLE_STACK);
        final String environment = System.getenv(ENVIRONMENT_VARIABLE_ENVIRONMENT);
        final String region = System.getenv(ENVIRONMENT_VARIABLE_REGION);
        final String datacenter = System.getenv(ENVIRONMENT_VARIABLE_DATACENTER);

        Config combined = ConfigFactory.load();

        if (applicationId != null) {
            combined = combine(loadConfigByName(applicationId), combined);
            combined = combine(loadConfigByName(applicationId, stack), combined);
            combined = combine(loadConfigByName(applicationId, environment), combined);
            combined = combine(loadConfigByName(applicationId, environment, region), combined);
            combined = combine(loadConfigByName(applicationId, environment, datacenter), combined);
        }

        String additionalUrls = System.getenv(ENVIRONMENT_VARIABLE_ADDITIONAL_URLS);
        if (additionalUrls != null) {
            String[] split = additionalUrls.split(",");
            for (String url : split) {
                combined = combine(loadConfigFromUrl(url), combined);
            }
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
        String configName = Arrays.stream(names).filter(Objects::nonNull).reduce(applicationId, (a, b) -> a + "-" + b);
        return ConfigFactory.parseResourcesAnySyntax(configName, ConfigParseOptions.defaults().setAllowMissing(true));
    }

    private static Config loadConfigFromUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            return ConfigFactory.parseURL(url, ConfigParseOptions.defaults().setAllowMissing(true));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to convert property into URL", e);
        }
    }

    private static Config combine(Config base, Config fallback) {
        Config combined = base.withFallback(fallback);
        return ConfigFactory.load(combined);
    }

    /**
     * Getter for service {@link Config}
     *
     * @return eagerly initialized singleton {@link Config} instance
     */
    public static Config getConfig() {
        return COMPLETE;
    }

    /**
     * Helper method to support loading configuration with default value.
     * Function can simply be a method reference from {@link Config} instance (i.e. config::getString, etc.).
     *
     * @param function function to call any get configuration method from {@link Config}
     * @param key configuration key
     * @param def default value
     * @param <T> return type
     * @return configuration value or default value if missing
     */
    public static <T> T loadConfigWithDefault(Function<String, T> function, String key, T def) {
        try {
            return function.apply(key);
        } catch (ConfigException.Missing e) {
            return def;
        }
    }
}
