package com.liaison.service.akka.http.route.swagger;

import com.github.swagger.akka.javadsl.Converter;
import com.github.swagger.akka.javadsl.SwaggerGenerator;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Scheme;
import io.swagger.models.auth.SecuritySchemeDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AkkaSwaggerGenerator implements SwaggerGenerator {

    private final Set<Class<?>> classes;

    public AkkaSwaggerGenerator(Set<Class<?>> classes) {
        this.classes = Collections.unmodifiableSet(classes);
    }

    @Override
    public Converter converter() {
        return new Converter(this);
    }

    @Override
    public Set<Class<?>> apiClasses() {
        return classes;
    }

    @Override
    public Info info() {
        return new Info();
    }

    @Override
    public String generateSwaggerYaml() {
        return converter().generateSwaggerYaml();
    }

    @Override
    public String generateSwaggerJson() {
        return converter().generateSwaggerJson();
    }

    @Override
    public List<String> unwantedDefinitions() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> vendorExtensions() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<ExternalDocs> externalDocs() {
        return Optional.empty();
    }

    @Override
    public Map<String, SecuritySchemeDefinition> securitySchemeDefinitions() {
        return Collections.emptyMap();
    }

    @Override
    public List<Scheme> schemes() {
        return Collections.emptyList();
    }

    @Override
    public String host() {
        return "localhost:8989";
    }

    @Override
    public String basePath() {
        return "/";
    }

    @Override
    public String apiDocsPath() {
        return "api-docs";
    }
}
