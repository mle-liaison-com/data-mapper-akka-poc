package com.liaison.service.akka.http.client.route.metrics;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.javadsl.TestKit;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liaison.service.akka.core.metrics.MetricsManager;
import com.liaison.service.akka.http.route.metrics.MetricsRouteProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricsRouteProviderTest extends JUnitRouteTest {

    private static final String PATH_METRICS = "/metrics";

    private ActorSystem system;
    private TestRoute testRoute;

    @Before
    public void setup() {
        MetricsManager.getRegistry().removeMatching(MetricFilter.ALL);
        system = ActorSystem.create(getClass().getSimpleName());
        testRoute = testRoute(new MetricsRouteProvider().create());
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testEmptyMetricsReport() throws IOException {
        HttpEntity.Strict strict = testRoute.run(HttpRequest.GET(PATH_METRICS)).assertStatusCode(200).entity();
        String raw = strict.getData().utf8String();
        ObjectMapper objectMapper = new ObjectMapper();
        MetricRegistry registry = objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .reader().forType(MetricRegistry.class).readValue(raw);
        assertTrue(registry.getMetrics().isEmpty());
    }

    @Test
    public void testMetricsReportWithOneTimer() throws IOException {
        String timerName = "test_timer";
        MetricsManager.getRegistry().timer(timerName);
        HttpEntity.Strict strict = testRoute.run(HttpRequest.GET(PATH_METRICS)).assertStatusCode(200).entity();
        String raw = strict.getData().utf8String();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tree = objectMapper.readTree(raw);
        assertEquals(tree.get("gauges").size(), 0);
        assertEquals(tree.get("counters").size(), 0);
        assertEquals(tree.get("histograms").size(), 0);
        assertEquals(tree.get("meters").size(), 0);
        assertEquals(tree.get("timers").size(), 1);
        assertTrue(tree.get("timers").hasNonNull(timerName));
    }
}
