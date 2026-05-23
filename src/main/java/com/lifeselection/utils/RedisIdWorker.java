package com.lifeselection.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {
    /**
     * 寮€濮嬫椂闂存埑
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    /**
     * 搴忓垪鍙风殑浣嶆暟
     */
    private static final int COUNT_BITS = 32;

    private StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        // 1.鐢熸垚鏃堕棿鎴?
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.鐢熸垚搴忓垪鍙?
        // 2.1.鑾峰彇褰撳墠鏃ユ湡锛岀簿纭埌澶?
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        // 2.2.鑷闀?
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 3.鎷兼帴骞惰繑鍥?
        return timestamp << COUNT_BITS | count;
    }
}
