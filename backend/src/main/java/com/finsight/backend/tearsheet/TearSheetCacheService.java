package com.finsight.backend.tearsheet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
public class TearSheetCacheService {

    private static final Logger log = LoggerFactory.getLogger(TearSheetCacheService.class);
    private static final String KEY_PREFIX = "finsight:tearsheet:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public TearSheetCacheService(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            @Value("${finsight.cache.tearsheet-ttl-hours:6}") long ttlHours
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofHours(ttlHours);
    }

    private String buildKey(String userId, String company, Integer year) {
        String cleanCompany = company.trim().toLowerCase();
        String period = (year != null) ? String.valueOf(year) : "latest";
        return KEY_PREFIX + userId + ":" + cleanCompany + ":" + period;
    }

    public Optional<TearSheetResponse> get(String userId, String company, Integer year) {
        String key = buildKey(userId, company, year);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("Redis cache HIT for key={}", key);
                TearSheetResponse response = objectMapper.convertValue(cached, TearSheetResponse.class);
                return Optional.of(response);
            }
            log.info("Redis cache MISS for key={}", key);
        } catch (Exception ex) {
            log.warn("Redis read failed for key={}: {}. Falling back to live LLM generation.", key, ex.getMessage());
        }
        return Optional.empty();
    }

    public void put(String userId, String company, Integer year, TearSheetResponse response) {
        String key = buildKey(userId, company, year);
        try {
            redisTemplate.opsForValue().set(key, response, ttl);
            log.info("Stored tear sheet in Redis for key={}, ttl={}", key, ttl);
        } catch (Exception ex) {
            log.warn("Redis write failed for key={}: {}", key, ex.getMessage());
        }
    }

    public void evictUserCompany(String userId, String company) {
        String pattern = KEY_PREFIX + userId + ":" + company.trim().toLowerCase() + ":*";
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Evicted {} cached tear sheet entries for pattern={}", keys.size(), pattern);
            }
        } catch (Exception ex) {
            log.warn("Redis eviction failed for pattern={}: {}", pattern, ex.getMessage());
        }
    }
}