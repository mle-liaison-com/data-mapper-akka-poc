package com.liaison.service.akka.core.metrics;

import com.codahale.metrics.MetricRegistry;

/**
 * Class that holds eagerly initialized singleton {@link MetricRegistry}.
 */
public class MetricsManager {

    private static final MetricRegistry REGISTRY = new MetricRegistry();

    /**
     * returns singleton {@link MetricRegistry} instance.
     *
     * @return singleton {@link MetricRegistry}
     */
    public static MetricRegistry getRegistry() {
        return REGISTRY;
    }
}
