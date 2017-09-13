package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HelloRouteProviderTest extends JUnitRouteTest {

    private ActorSystem system;
    private TestRoute testRoute;

    @Before
    public void setup() {
        system = ActorSystem.create();
        testRoute = testRoute(new HelloRouteProvider(system).create());
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }


    @Test
    public void testHelloSimple() {
        testRoute.run(HttpRequest.GET("/hello/simple")).assertStatusCode(200).assertEntity("Hello, World!");
    }

    @Test
    public void testHelloSync() {
        testRoute.run(HttpRequest.GET("/hello/sync")).assertStatusCode(200).assertEntity("Hello, World!");
    }
}
