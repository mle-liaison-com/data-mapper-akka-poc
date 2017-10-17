package com.liaison.service.akka.http.route.swagger;

import com.github.swagger.akka.javadsl.SwaggerGenerator;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Set;

/**
 * Implementation of {@link SwaggerGenerator}.
 */
@Immutable
public class AkkaSwaggerGenerator implements SwaggerGenerator {

    private final String host;
    private final Set<Class<?>> classes;

    /**
     * Constructs {@link SwaggerGenerator} with provided host and a set of classes
     *
     * @param host host of API
     * @param classes a set of Swagger documented classes
     */
    public AkkaSwaggerGenerator(String host, Set<Class<?>> classes) {
        this.host = host;
        this.classes = Collections.unmodifiableSet(classes);
    }

    /**
     * Getter for Swagger documented classes
     *
     * @return a set of Swagger documented classes
     */
    @Override
    public Set<Class<?>> apiClasses() {
        return classes;
    }

    /**
     * Getter for host
     *
     * @return host of API
     */
    @Override
    public String host() {
        return host;
    }
}
