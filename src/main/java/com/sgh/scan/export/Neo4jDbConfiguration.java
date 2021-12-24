package com.sgh.scan.export;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "neo4j")
@Setter
class Neo4jDbConfiguration {
    String host;
    String user;
    String pass;
}
