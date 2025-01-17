package com.rus.nawm.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {
  @Value("${redis.host}")
  private String redisHost;
  @Value("${redis.port}")
  private int redisPort;

  public static final String REDIS_ALL_TOURISTS_CACHE_KEY = "allTourists";
  public static final String REDIS_TOURIST_BY_ID_CACHE_KEY = "tourists";
  public static final String REDIS_TOURIST_BY_EMAIL_CACHE_KEY = "touristsByEmail";
  public static final String REDIS_TOURIST_BY_PHONE_CACHE_KEY = "touristsByPhone";
  public static final String REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY = "touristsByNameAndSurname";

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  public RedisCacheManager cacheManager() {
    RedisCacheConfiguration cacheConfig = myDefaultCacheConfig(Duration.ofMinutes(10)).disableCachingNullValues();

    return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(cacheConfig)
            .withCacheConfiguration(REDIS_TOURIST_BY_NAME_AND_SURNAME_CACHE_KEY, myDefaultCacheConfig(Duration.ofMinutes(10)))
            .withCacheConfiguration(REDIS_TOURIST_BY_PHONE_CACHE_KEY, myDefaultCacheConfig(Duration.ofMinutes(10)))
            .withCacheConfiguration(REDIS_TOURIST_BY_EMAIL_CACHE_KEY, myDefaultCacheConfig(Duration.ofMinutes(10)))
            .withCacheConfiguration(REDIS_TOURIST_BY_ID_CACHE_KEY, myDefaultCacheConfig(Duration.ofMinutes(10)))
            .withCacheConfiguration(REDIS_ALL_TOURISTS_CACHE_KEY, myDefaultCacheConfig(Duration.ofMinutes(10)))
            .build();
  }

  private RedisCacheConfiguration myDefaultCacheConfig(Duration duration) {
    return RedisCacheConfiguration
            .defaultCacheConfig()
            .entryTtl(duration)
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
  }
}
