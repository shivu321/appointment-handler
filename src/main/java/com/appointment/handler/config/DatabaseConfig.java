package com.appointment.handler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
public class DatabaseConfig {

    @Configuration
    @Profile("jpa")
    @EnableJpaRepositories(basePackages = "com.appointment.handler")
    public static class JpaConfig {
    }

    @Configuration
    @Profile("mongodb")
    @EnableMongoRepositories(basePackages = "com.appointment.handler")
    public static class MongoConfig {
    }
}
