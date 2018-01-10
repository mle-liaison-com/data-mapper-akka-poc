package com.liaison.service.akka.http.client;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.pattern.CircuitBreaker;
import akka.stream.ActorMaterializer;
import akka.testkit.javadsl.TestKit;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.liaison.service.akka.core.circuitbreaker.CircuitBreakerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpClientTest {

    static class HttpClientActor extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(HttpRequest.class, request -> {
                        final ActorRef sender = getSender();
                        ActorSystem system = getContext().getSystem();
                        HttpClient client = new HttpClient(system, req ->
                                new CircuitBreaker(system.dispatcher(), system.scheduler(), 5,
                                        FiniteDuration.apply(5000, TimeUnit.MILLISECONDS),
                                        FiniteDuration.apply(15000, TimeUnit.MILLISECONDS)),
                                (response, throwable) -> {
                                    final ActorMaterializer materializer = ActorMaterializer.create(system);
                                    CompletionStage<HttpEntity.Strict> strictEntity = response.entity().toStrict(5000, materializer);
                                    strictEntity.whenComplete((strict, t)-> {
                                        // send response as string back to original sender
                                        sender.tell(strict.getData().utf8String(), ActorRef.noSender());
                                        materializer.shutdown();
                                    });
                                });
                        client.call(request);
                    })
                    .build();
        }
    }

    private ActorSystem system;

    @Before
    public void setup() {
        CircuitBreakerFactory.clear();
        system = ActorSystem.create(getClass().getSimpleName());
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testHttpGet() {
        String method = "GET";
        String uri = "https://httpbin.org/get";
        String name = "Header-Name";
        String value = "Header-Value";

        HttpRequestBuilder builder = new HttpRequestBuilder(method, uri).withHeader(name, value);
        HttpRequest httpRequest = builder.build();

        new TestKit(system) {{
            String response = within(duration("5 seconds"), () -> {
                ActorRef ref = system.actorOf(Props.create(HttpClientActor.class));
                ref.tell(httpRequest, getRef());
                return expectMsgClass(String.class);
            });
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(response).getAsJsonObject();
            assertEquals(uri, root.get("url").getAsString());

            // make sure inserted headers are found in the response
            JsonObject headers = root.get("headers").getAsJsonObject();
            assertNotNull(headers.get(name));
            assertEquals(value, headers.get(name).getAsString());
        }};
    }

    @Test
    public void testHttpPost() {
        String method = "POST";
        String uri = "https://httpbin.org/post";
        String body = "body";

        HttpRequestBuilder builder = new HttpRequestBuilder(method, uri).withEntity(HttpEntities.create(body));
        HttpRequest httpRequest = builder.build();

        new TestKit(system) {{
            String response = within(duration("5 seconds"), () -> {
                ActorRef ref = system.actorOf(Props.create(HttpClientActor.class));
                ref.tell(httpRequest, getRef());
                return expectMsgClass(String.class);
            });
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(response).getAsJsonObject();
            assertEquals(uri, root.get("url").getAsString());
            // make sure inserted headers are found in the response
            assertEquals(body, root.get("data").getAsString());
        }};
    }
}
