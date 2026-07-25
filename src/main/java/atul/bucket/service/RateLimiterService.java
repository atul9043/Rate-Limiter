package atul.bucket.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import atul.bucket.dto.RateLimitResult;

@Service
public class RateLimiterService {

    @Autowired
    StringRedisTemplate redisTemplate;

    private int AUTH_MAX_TOKEN = 20;
    private int AUTH_REFILL_RATE = 10;

    private int ANON_MAX_TOKEN = 5;
    private int ANON_REFILL_RATE = 3;
    private int BUCKET_REFILL_INTERVAL = 30;
    

    private DefaultRedisScript<List> script;

    public RateLimiterService(){

        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("script/bucket.lua"));
        script.setResultType(List.class);
    }

    public RateLimitResult isAllowed(String clientId, boolean authenticated){

        int MAX_TOKEN = authenticated?AUTH_MAX_TOKEN:ANON_MAX_TOKEN;
        int REFILL_RATE = authenticated?AUTH_REFILL_RATE:ANON_REFILL_RATE;
        int REFILL_INTERVAL = BUCKET_REFILL_INTERVAL;

        String key = "RateLimit :" + clientId;
        long now = Instant.now().getEpochSecond();

        @SuppressWarnings("unchecked")
        List<Long> result = redisTemplate.execute(
            script,
            Collections.singletonList(key),
            String.valueOf(now),
            String.valueOf(MAX_TOKEN),
            String.valueOf(REFILL_RATE),
            String.valueOf(REFILL_INTERVAL));

        boolean allowed = result.get(0)==1L;
        long tokens = result.get(1);

        return new RateLimitResult(allowed, tokens);
    }

}
