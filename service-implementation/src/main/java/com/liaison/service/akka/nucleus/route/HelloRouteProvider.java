package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.liaison.service.akka.core.route.PerRequestActorRouteProvider;
import com.liaison.service.akka.nucleus.actor.HelloWorldActor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.util.UUID;

@Api(value = "/hello", produces = "text/plain")
@Path("/hello")
public class HelloRouteProvider extends AllDirectives implements PerRequestActorRouteProvider {

    @Override
    public Route create(final ActorSystem system) {
        return pathPrefix("hello", () -> route(simpleGet(), syncGet(system)));
    }

    @Path("/simple")
    @ApiOperation(value = "simple", nickname = "simple", httpMethod = HttpMethod.GET, response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") })
    public Route simpleGet() {
        return path("simple", () -> get(() -> complete("Hello, World!")));
    }

    @Path("/sync")
    @ApiOperation(value = "sync", nickname = "sync", httpMethod = HttpMethod.GET, response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Internal server error") })
    public Route syncGet(final ActorSystem system) {
        return path("sync", () -> get(() -> invokeActorWithExceptionHandler(
                        system,
                        createActorRef(system, HelloWorldActor.class, UUID.randomUUID().toString()),
                        "message",
                        t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                        o -> HttpResponse.create().withEntity(o.toString()))));
    }
}
