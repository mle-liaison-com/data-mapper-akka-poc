package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.core.route.PerRequestActorRouteProvider;
import com.liaison.service.akka.nucleus.actor.AsyncActor;
import com.liaison.service.akka.nucleus.actor.FailActor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.util.UUID;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.route;

@Api(value = "sample/", produces = "text/plain")
@Path("/sample")
public class SampleRouteProvider implements PerRequestActorRouteProvider {

    @Override
    public Route create(final ActorSystem system) {
        return pathPrefix("sample", () -> route(asyncGet(system), failGet(system)));
    }

    @Path("/async")
    @ApiOperation(value = "simple", code = 204, nickname = "async", httpMethod = HttpMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") })
    public Route asyncGet(final ActorSystem system) {
        return path("async", () -> get(() -> {
            createActorRef(system, AsyncActor.class, UUID.randomUUID().toString()).tell("", ActorRef.noSender());
            return complete(StatusCodes.NO_CONTENT);
        }));
    }

    @Path("/fail")
    @ApiOperation(value = "sync", nickname = "sync", httpMethod = HttpMethod.GET, response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") })
    public Route failGet(final ActorSystem system) {
        return path("fail", () -> get(() -> invokeActorWithExceptionHandler(
                system,
                createActorRef(system, FailActor.class, UUID.randomUUID().toString()),
                "message",
                t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                o -> HttpResponse.create().withEntity(o.toString()))));
    }
}
