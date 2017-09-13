package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloRouteProviderTest extends JUnitRouteTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    private TestRoute testRoute = testRoute(new HelloRouteProvider(system).create());

    @Test
    public void testHelloSimple() {
        testRoute.run(HttpRequest.GET("/hello/simple")).assertStatusCode(200).assertEntity("Hello, World!");
    }

    @Test
    public void testHelloSync() {
        testRoute.run(HttpRequest.GET("/hello/sync")).assertStatusCode(200).assertEntity("Hello, World!");
    }
}
