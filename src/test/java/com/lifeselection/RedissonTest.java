package com.lifeselection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    private RLock lock;

    @BeforeEach
    void setUp() {
        lock = redissonClient.getLock("order");
    }

    @Test
    void method1() throws InterruptedException {
        // 灏濊瘯鑾峰彇閿?
        boolean isLock = lock.tryLock(1L, TimeUnit.SECONDS);
        if (!isLock) {
            log.error("鑾峰彇閿佸け璐?.... 1");
            return;
        }
        try {
            log.info("鑾峰彇閿佹垚鍔?.... 1");
            method2();
            log.info("寮€濮嬫墽琛屼笟鍔?... 1");
        } finally {
            log.warn("鍑嗗閲婃斁閿?.... 1");
            lock.unlock();
        }
    }
    void method2() {
        // 灏濊瘯鑾峰彇閿?
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("鑾峰彇閿佸け璐?.... 2");
            return;
        }
        try {
            log.info("鑾峰彇閿佹垚鍔?.... 2");
            log.info("寮€濮嬫墽琛屼笟鍔?... 2");
        } finally {
            log.warn("鍑嗗閲婃斁閿?.... 2");
            lock.unlock();
        }
    }
}
