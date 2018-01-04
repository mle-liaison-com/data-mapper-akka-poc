package com.liaison.service.akka.http.route.metrics;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaison.service.akka.core.config.ConfigManager;
import com.liaison.service.akka.core.metrics.MetricsManager;
import com.liaison.service.akka.http.route.RouteProvider;
import com.typesafe.config.Config;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static com.liaison.service.akka.core.config.ConfigManager.loadConfigWithDefault;

/**
 * This class is responsible for providing a {@link Route} for Dropwizard metrics collected within the service.
 * Response will be formatted as a prettified JSON.
 * For more information about Dropwizard metrics, please visit <a href="http://metrics.dropwizard.io/4.0.0/index.html">Dropwizard Metrics</a>
 */
public class MetricsRouteProvider implements RouteProvider {

    private static final String PATH_METRICS = "metrics";

    private static final String CONFIG_RATE_UNIT = "com.liaison.service.akka.metrics.unit.rate";
    private static final String CONFIG_DURATION_UNIT = "com.liaison.service.akka.metrics.unit.duration";
    private static final String CONFIG_SHOW_SAMPLES = "com.liaison.service.akka.metrics.show";

    private static final ObjectMapper MAPPER;
    static {
        Config config = ConfigManager.getConfig();
        final TimeUnit rateUnit = parseTimeUnit(config, CONFIG_RATE_UNIT);
        final TimeUnit durationUnit = parseTimeUnit(config, CONFIG_DURATION_UNIT);
        final boolean showSamples = loadConfigWithDefault(config::getBoolean, CONFIG_SHOW_SAMPLES, false);
        MetricFilter filter = MetricFilter.ALL;
        MAPPER = new ObjectMapper().registerModule(new MetricsModule(rateUnit, durationUnit, showSamples, filter));
    }

    private static TimeUnit parseTimeUnit(Config config, String key) {
        String timeUnit = loadConfigWithDefault(config::getString, key, TimeUnit.SECONDS.toString());
        return TimeUnit.valueOf(String.valueOf(timeUnit).toUpperCase(Locale.US));
    }

    /**
     * Provides a {@link akka.http.javadsl.model.HttpMethods#GET} {@link Route} for metrics collected via
     * {@link MetricsManager#getRegistry()}
     *
     * @return {@link Route} to Dropwizard metrics report
     */
    @Override
    public Route create() {
        return path(PATH_METRICS, () -> get(() -> {
            try {
                return complete(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MetricsManager.getRegistry()));
            } catch (JsonProcessingException e) {
                return complete(StatusCodes.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }));
    }
}
