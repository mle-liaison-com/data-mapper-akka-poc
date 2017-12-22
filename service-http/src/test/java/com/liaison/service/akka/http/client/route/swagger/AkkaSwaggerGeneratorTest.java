package com.liaison.service.akka.http.client.route.swagger;

import com.liaison.service.akka.http.route.swagger.AkkaSwaggerGenerator;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AkkaSwaggerGeneratorTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testApiClassesModification() {
        Set<Class<?>> classes = new HashSet<>();
        AkkaSwaggerGenerator swaggerGenerator = new AkkaSwaggerGenerator("localhost", classes);
        swaggerGenerator.apiClasses().add(String.class);
    }

    @Test
    public void testApiClasses() {
        Set<Class<?>> classes = new HashSet<>();
        AkkaSwaggerGenerator swaggerGenerator = new AkkaSwaggerGenerator("localhost", classes);
        assertTrue(swaggerGenerator.apiClasses().isEmpty());

        classes = new HashSet<>();
        classes.add(String.class);
        classes.add(Object.class);
        swaggerGenerator = new AkkaSwaggerGenerator("localhost", classes);
        assertTrue(swaggerGenerator.apiClasses().contains(String.class));
        assertTrue(swaggerGenerator.apiClasses().contains(Object.class));
        assertFalse(swaggerGenerator.apiClasses().contains(Integer.class));
    }

    @Test
    public void testHost() {
        String host = "localhost";
        Set<Class<?>> classes = new HashSet<>();
        AkkaSwaggerGenerator swaggerGenerator = new AkkaSwaggerGenerator(host, classes);
        assertEquals(swaggerGenerator.host(), host);
    }
}
