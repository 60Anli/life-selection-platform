package com.lifeselection.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LocalCacheClient {

    private static final long MAX_CACHE_SIZE = 10_000L;

    private final Cache<String, CacheEntry> cache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

    public <T> T get(String key, Class<T> type) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.invalidate(key);
            return null;
        }
        Object value = entry.getValue();
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        if (value == null || time == null || unit == null) {
            return;
        }
        cache.put(key, new CacheEntry(value, System.nanoTime() + unit.toNanos(time)));
    }

    public void delete(String key) {
        cache.invalidate(key);
    }

    private static class CacheEntry {
        private final Object value;
        private final long expireAtNanos;

        private CacheEntry(Object value, long expireAtNanos) {
            this.value = value;
            this.expireAtNanos = expireAtNanos;
        }

        private Object getValue() {
            return value;
        }

        private boolean isExpired() {
            return System.nanoTime() >= expireAtNanos;
        }
    }
}
