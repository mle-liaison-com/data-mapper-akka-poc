package com.liaison.service.akka.http.route.swagger;

import com.github.swagger.akka.javadsl.Converter;
import com.github.swagger.akka.javadsl.SwaggerGenerator;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Scheme;
import io.swagger.models.auth.SecuritySchemeDefinition;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link SwaggerGenerator}.
 */
@Immutable
public class AkkaSwaggerGenerator implements SwaggerGenerator {

    private final String host;
    private final Set<Class<?>> classes;

    private static final List<Scheme> schemes;
    static {
        List<Scheme> temp = new ArrayList<>();
        temp.add(Scheme.HTTP);
        temp.add(Scheme.HTTPS);
        schemes = Collections.unmodifiableList(temp);
    }

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

    @Override
    public String basePath() {
        return "/";
    }

    @Override
    public String apiDocsPath() {
        return "api-docs";
    }

    @Override
    public Info info() {
        return new Info();
    }

    @Override
    public List<Scheme> schemes() {
        return schemes;
    }

    @Override
    public Map<String, SecuritySchemeDefinition> securitySchemeDefinitions() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<ExternalDocs> externalDocs() {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> vendorExtensions() {
        return Collections.emptyMap();
    }

    @Override
    public List<String> unwantedDefinitions() {
        return Collections.emptyList();
    }

    @Override
    public Converter converter() {
        return new Converter(this);
    }

    @Override
    public String generateSwaggerJson() {
        return this.converter().generateSwaggerJson();
    }

    @Override
    public String generateSwaggerYaml() {
        return this.converter().generateSwaggerYaml();
    }
}
