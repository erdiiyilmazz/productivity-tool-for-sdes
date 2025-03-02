package com.erdidev.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.beans.factory.DisposableBean;
import javax.sql.DataSource;

@TestConfiguration
public class BaseTestConfiguration implements DisposableBean {

    private static final int REDIS_PORT = 6379;
    private static final GenericContainer<?> redisContainer;
    private static final GenericContainer<?> postgresContainer;

    static {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(REDIS_PORT);
        
        postgresContainer = new GenericContainer<>(DockerImageName.parse("postgres:latest"))
            .withExposedPorts(5432)
            .withEnv("POSTGRES_DB", "testdb")
            .withEnv("POSTGRES_USER", "test")
            .withEnv("POSTGRES_PASSWORD", "test");

        redisContainer.start();
        postgresContainer.start();
    }

    @Override
    public void destroy() {
        redisContainer.close();
        postgresContainer.close();
    }

    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url(String.format("jdbc:postgresql://%s:%d/testdb",
                postgresContainer.getHost(),
                postgresContainer.getMappedPort(5432)))
            .username("test")
            .password("test")
            .build();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisContainer.getHost());
        config.setPort(redisContainer.getMappedPort(REDIS_PORT));
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
} 