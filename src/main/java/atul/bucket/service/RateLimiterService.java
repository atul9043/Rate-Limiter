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
    private int MAX_TOKEN = 3;
    private int REFILL_RATE = 5;
    private int REFILL_INTERVAL = 30;

    private  DefaultRedisScript<List> script;

    public RateLimiterService(){

        script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("script/bucket.lua"));
        script.setResultType(List.class);
    }

    public RateLimitResult isAllowed(String clientId){

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
