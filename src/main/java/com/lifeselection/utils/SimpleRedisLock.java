package com.lifeselection.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {

    private String name;
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        // 鑾峰彇绾跨▼鏍囩ず
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 鑾峰彇閿?
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        // 璋冪敤lua鑴氭湰
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }
    /*@Override
    public void unlock() {
        // 鑾峰彇绾跨▼鏍囩ず
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 鑾峰彇閿佷腑鐨勬爣绀?
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        // 鍒ゆ柇鏍囩ず鏄惁涓€鑷?
        if(threadId.equals(id)) {
            // 閲婃斁閿?
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }*/
}
