package com.liaison.service.akka.http.client;

import akka.http.javadsl.model.HttpMethod;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.headers.RawHeader;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequestBuilder {

    private final String method;
    private final String uri;
    private final Map<String, String> headerMap = new HashMap<>();

    public HttpRequestBuilder(@Nonnull String method, @Nonnull String uri) {
        this.method = method;
        this.uri = uri;
    }

    public HttpRequestBuilder withHeader(String key, String value) {
        headerMap.put(key, value);
        return this;
    }

    public HttpRequest build() {
        Optional<HttpMethod> optional = HttpMethods.lookup(method.toUpperCase());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("invalid http method " + method);
        }
        HttpRequest request = HttpRequest.create(uri).withMethod(optional.get());
        headerMap.forEach((key, value) -> request.addHeader(RawHeader.create(key, value)));
        return request;
    }
}
