package com.liaison.service.akka.http.client.route;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.javadsl.TestKit;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.liaison.service.akka.core.WorkTicketOuterClass;
import com.liaison.service.akka.http.route.RouteProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RouteProviderTest extends JUnitRouteTest {

    private static final String PATH_PROTOBUF = "protobuf";
    private static final String PATH_PROTOBUF_PREFIXED = "/" + PATH_PROTOBUF;
    private static final String PATH_INVALID = "invalid";
    private static final String PATH_INVALID_PREFIXED = "/" + PATH_INVALID;

    private static class TestRouteProvider implements RouteProvider {

        private final ActorSystem system;

        private TestRouteProvider(ActorSystem system) {
            this.system = system;
        }

        @Override
        public Route create() {
            ActorRef ref = system.actorOf(Props.create(TestActor.class));
            return Directives.route(
                    Directives.path(PATH_PROTOBUF, () -> Directives.post(() ->
                            Directives.entity(protobufUnmarshaller(WorkTicketOuterClass.WorkTicket.newBuilder()), workTicket ->
                                    invokeActorWithExceptionHandler(system, ref, workTicket,
                                            t -> Directives.complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                                            o -> HttpResponse.create().withEntity(o.toString()))))),
                    Directives.path(PATH_INVALID, () -> Directives.post(() ->
                            Directives.extractStrictEntity(FiniteDuration.apply(1, TimeUnit.SECONDS), strict ->
                                    invokeActorWithExceptionHandler(system, ref, strict.getData().utf8String(),
                                            t -> Directives.complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                                            o -> HttpResponse.create().withEntity(o.toString())))))
            );
        }
    }

    private static class TestActor extends AbstractActor {

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(MessageOrBuilder.class, msg -> getSender().tell(JsonFormat.printer().print(msg), ActorRef.noSender()))
                    .match(String.class, str -> getSender().tell(new Status.Failure(generateIllegalArgumentException(str)), ActorRef.noSender()))
                    .build();
        }
    }

    private static Throwable generateIllegalArgumentException(String message) {
        return new IllegalArgumentException(message);
    }

    private ActorSystem system;
    private TestRoute testRoute;

    @Before
    public void setup() {
        system = ActorSystem.create(getClass().getSimpleName());
        testRoute = testRoute(new TestRouteProvider(system).create());
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testValidProtobufMessage() throws Exception {
        WorkTicketOuterClass.WorkTicket workTicket = WorkTicketOuterClass.WorkTicket.newBuilder()
                .setGlobalProcessId(UUID.randomUUID().toString())
                .putAdditionalContexts("addition_context_name", "additional_context_value")
                .build();
        String workTicketJson = JsonFormat.printer().print(workTicket);
        testRoute.run(HttpRequest.POST(PATH_PROTOBUF_PREFIXED).withEntity(MediaTypes.APPLICATION_JSON.toContentType(), workTicketJson))
                .assertStatusCode(200).assertEntity(workTicketJson);
    }

    @Test
    public void testNonJsonMessageWithUnmarshaller() {
        testRoute.run(HttpRequest.POST(PATH_PROTOBUF_PREFIXED).withEntity("test")).assertStatusCode(415);
    }

    @Test
    public void testNonProtobufMessageWithUnmarshaller() {
        testRoute.run(HttpRequest.POST(PATH_PROTOBUF_PREFIXED).withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\"name\":\"value\"}"))
                .assertStatusCode(400).assertEntity("Unable to Unmarshall JSON as " + WorkTicketOuterClass.WorkTicket.class.getSimpleName());
    }

    @Test
    public void testInvokeActorWithExceptionHandlerWithException() {
        String message = "invalid";
        testRoute.run(HttpRequest.POST(PATH_INVALID_PREFIXED).withEntity("invalid")).assertStatusCode(500)
                .assertEntity(generateIllegalArgumentException(message).toString());
    }
}
