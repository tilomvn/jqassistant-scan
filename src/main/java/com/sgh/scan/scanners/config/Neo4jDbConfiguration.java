package com.sgh.scan.scanners.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@ConfigurationProperties(prefix = "neo4j")
@TestPropertySource("classpath:application.properties")
@Getter
@Setter
public class Neo4jDbConfiguration {
    String host;
    String user;
    String pass;
}
