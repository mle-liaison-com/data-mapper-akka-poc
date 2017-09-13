package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SampleRouteProviderTest extends JUnitRouteTest {

    private ActorSystem system;
    private TestRoute testRoute;

    @Before
    public void setup() {
        system = ActorSystem.create();
        testRoute = testRoute(new SampleRouteProvider(system).create());
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testSampleAsync() {
        testRoute.run(HttpRequest.GET("/sample/async")).assertStatusCode(204);
    }

    @Test
    public void testSampleFail() {
        testRoute.run(HttpRequest.GET("/sample/fail")).assertStatusCode(500).assertEntity("java.lang.RuntimeException: test exception");
    }
}
