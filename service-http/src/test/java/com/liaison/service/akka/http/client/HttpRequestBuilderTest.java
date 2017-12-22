package com.liaison.service.akka.http.client;

import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.Uri;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpRequestBuilderTest {

    @Test
    public void testHttpRequestBuilder() throws Exception{
        String method = "GET";
        String uri = "https://httpbin.org/get";
        String name = "testName";
        String value = "testvalue";

        HttpRequestBuilder builder = new HttpRequestBuilder(method, uri);
        builder.withHeader(name, value);
        HttpRequest httpRequest = builder.build();

        assertEquals(httpRequest.method(), HttpMethods.lookup(method).get());
        assertEquals(httpRequest.getUri(), Uri.create(uri));
        assertEquals(httpRequest.getHeader(name).get().value(), value);
    }

}
