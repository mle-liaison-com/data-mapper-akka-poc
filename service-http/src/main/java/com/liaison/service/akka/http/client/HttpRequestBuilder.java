package com.liaison.service.akka.http.client;

import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.headers.RawHeader;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simplified builder class to build {@link HttpRequest} with fluent API.
 */
public class HttpRequestBuilder {

    private final String method;
    private final String uri;
    private final Map<String, String> headerMap = new HashMap<>();
    private RequestEntity requestEntity;

    /**
     * Constructor. User must provide HTTP method and URI as {@link String}
     *
     * @param method http method
     * @param uri target uri
     */
    public HttpRequestBuilder(@Nonnull String method, @Nonnull String uri) {
        this.method = method;
        this.uri = uri;
    }

    /**
     * Adds a HTTP header
     *
     * @param key header name
     * @param value header value
     * @return itself
     */
    public HttpRequestBuilder withHeader(String key, String value) {
        headerMap.put(key, value);
        return this;
    }

    public HttpRequestBuilder withEntity(RequestEntity requestEntity) {
        if (this.requestEntity != null) {
            throw new IllegalStateException("Cannot have multiple request entities");
        }
        this.requestEntity = requestEntity;
        return this;
    }

    /**
     * Builds {@link HttpRequest} with user provided values
     *
     * @return {@link HttpRequest} instance
     */
    public HttpRequest build() {
        Optional<HttpMethod> optional = HttpMethods.lookup(method.toUpperCase());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("invalid http method " + method);
        }
        HttpRequest request = HttpRequest.create(uri).withMethod(optional.get());
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            request = request.addHeader(RawHeader.create(entry.getKey(), entry.getValue()));
        }
        if (requestEntity != null) {
            request = request.withEntity(requestEntity);
        }
        return request;
    }
}
