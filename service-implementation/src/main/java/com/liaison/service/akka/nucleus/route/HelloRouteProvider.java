package com.liaison.service.akka.nucleus.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.Route;
import akka.routing.FromConfig;
import com.liaison.service.akka.http.route.RouteProvider;
import com.liaison.service.akka.nucleus.actor.HelloWorldActor;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.pathPrefix;
import static akka.http.javadsl.server.Directives.route;

@Api(value = "/hello", produces = "text/plain")
@Path("/hello")
public class HelloRouteProvider implements RouteProvider {

    private final ActorSystem system;
    private final ActorRef helloRef;

    HelloRouteProvider(ActorSystem system) {
        this.system = system;
        this.helloRef = system.actorOf(FromConfig.getInstance().props(Props.create(HelloWorldActor.class, "test")), "hello");
    }

    @Override
    public Route create() {
        return pathPrefix("hello", () -> route(simpleGet(), syncGet()));
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
    public Route syncGet() {
        return path("sync", () -> get(() -> invokeActorWithExceptionHandler(
                system,
                helloRef,
                "message",
                t -> complete(StatusCodes.INTERNAL_SERVER_ERROR, t.getMessage()),
                o -> HttpResponse.create().withEntity(o.toString()))));
    }
}
