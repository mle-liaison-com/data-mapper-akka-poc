package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.routing.FromConfig;
import com.liaison.service.akka.core.route.RouteProvider;
import com.liaison.service.akka.nucleus.actor.AsyncActor;
import com.liaison.service.akka.nucleus.actor.FailActor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import scala.concurrent.duration.Duration;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.route;

@Api(value = "sample/", produces = "text/plain")
@Path("/sample")
public class SampleRouteProvider implements RouteProvider {

    private final ActorSystem system;
    private final ActorRef failRef;

    SampleRouteProvider(ActorSystem system) {
        this.system = system;

        SupervisorStrategy failStrategy = new OneForOneStrategy(5, Duration.create(1, TimeUnit.MINUTES), Collections.singletonList(Exception.class));
        this.failRef = system.actorOf(
                FromConfig.getInstance().withSupervisorStrategy(failStrategy).props(Props.create(FailActor.class, "test")),
                "fail");
    }

    @Override
    public Route create() {
        return pathPrefix("sample", () -> route(asyncGet(), failGet()));
    }

    @Path("/async")
    @ApiOperation(value = "simple", code = 204, nickname = "async", httpMethod = HttpMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") })
    public Route asyncGet() {
        return path("async", () -> get(() -> {
            system.actorOf(Props.create(AsyncActor.class, UUID.randomUUID().toString())).tell("", ActorRef.noSender());
            return complete(StatusCodes.NO_CONTENT);
        }));
    }

    @Path("/fail")
    @ApiOperation(value = "sync", nickname = "sync", httpMethod = HttpMethod.GET, response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") })
    public Route failGet() {
        return path("fail", () -> get(() -> invokeActorWithExceptionHandler(
                system,
                failRef,
                "message",
                t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                o -> HttpResponse.create().withEntity(o.toString()))));
    }
}
