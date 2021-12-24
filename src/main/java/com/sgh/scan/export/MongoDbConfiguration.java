package com.sgh.scan.export;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mongodb")
@Setter
class MongoDbConfiguration {
    String host;
    String port;
    String db;
    String user;
    String pass;
}
