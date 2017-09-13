package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SampleRouteProviderTest extends JUnitRouteTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    private TestRoute testRoute = testRoute(new SampleRouteProvider(system).create());

    @Test
    public void testSampleAsync() {
        testRoute.run(HttpRequest.GET("/sample/async")).assertStatusCode(204);
    }

    @Test
    public void testSampleFail() {
        testRoute.run(HttpRequest.GET("/sample/fail")).assertStatusCode(500).assertEntity("java.lang.RuntimeException: test exception");
    }
}
