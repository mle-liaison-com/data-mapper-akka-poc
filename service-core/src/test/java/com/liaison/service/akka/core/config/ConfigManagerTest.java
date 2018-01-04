package com.liaison.service.akka.core.config;

import com.typesafe.config.Config;
import org.junit.Test;

import static com.liaison.service.akka.core.config.ConfigManager.loadConfigWithDefault;
import static org.junit.Assert.assertEquals;

public class ConfigManagerTest {

    @Test
    public void testLoadConfigWithDefault() {
        Config config = ConfigManager.getConfig();

        // test existing configuration
        assertEquals(loadConfigWithDefault(config::getString, "com.liaison.service.akka.test", "test_value"), "config_value");

        // test missing configuration. expected to return default value
        assertEquals(loadConfigWithDefault(config::getString, "com.liaison.servcie.akka.missing", "test_value"), "test_value");
    }
}
