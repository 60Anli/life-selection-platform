package com.lifeselection.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.lifeselection.utils.RedisConstants.CACHE_NULL_TTL;
import static com.lifeselection.utils.RedisConstants.LOCK_SHOP_KEY;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private final LocalCacheClient localCacheClient;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate, LocalCacheClient localCacheClient) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.localCacheClient = localCacheClient;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
        localCacheClient.set(key, value, time, unit);
    }

    public void delete(String key) {
        localCacheClient.delete(key);
        stringRedisTemplate.delete(key);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 璁剧疆閫昏緫杩囨湡
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 鍐欏叆Redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        R localValue = localCacheClient.get(key, type);
        if (localValue != null) {
            return localValue;
        }
        // 1.浠巖edis鏌ヨ鍟嗛摵缂撳瓨
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.鍒ゆ柇鏄惁瀛樺湪
        if (StrUtil.isNotBlank(json)) {
            // 3.瀛樺湪锛岀洿鎺ヨ繑鍥?
            R value = JSONUtil.toBean(json, type);
            localCacheClient.set(key, value, time, unit);
            return value;
        }
        // 鍒ゆ柇鍛戒腑鐨勬槸鍚︽槸绌哄€?
        if (json != null) {
            // 杩斿洖涓€涓敊璇俊鎭?
            return null;
        }

        // 4.涓嶅瓨鍦紝鏍规嵁id鏌ヨ鏁版嵁搴?
        R r = dbFallback.apply(id);
        // 5.涓嶅瓨鍦紝杩斿洖閿欒
        if (r == null) {
            // 灏嗙┖鍊煎啓鍏edis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            // 杩斿洖閿欒淇℃伅
            return null;
        }
        // 6.瀛樺湪锛屽啓鍏edis
        this.set(key, r, time, unit);
        return r;
    }

    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.浠巖edis鏌ヨ鍟嗛摵缂撳瓨
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.鍒ゆ柇鏄惁瀛樺湪
        if (StrUtil.isBlank(json)) {
            // 3.瀛樺湪锛岀洿鎺ヨ繑鍥?
            return null;
        }
        // 4.鍛戒腑锛岄渶瑕佸厛鎶妀son鍙嶅簭鍒楀寲涓哄璞?
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5.鍒ゆ柇鏄惁杩囨湡
        if(expireTime.isAfter(LocalDateTime.now())) {
            // 5.1.鏈繃鏈燂紝鐩存帴杩斿洖搴楅摵淇℃伅
            return r;
        }
        // 5.2.宸茶繃鏈燂紝闇€瑕佺紦瀛橀噸寤?
        // 6.缂撳瓨閲嶅缓
        // 6.1.鑾峰彇浜掓枼閿?
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2.鍒ゆ柇鏄惁鑾峰彇閿佹垚鍔?
        if (isLock){
            // 6.3.鎴愬姛锛屽紑鍚嫭绔嬬嚎绋嬶紝瀹炵幇缂撳瓨閲嶅缓
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // 鏌ヨ鏁版嵁搴?
                    R newR = dbFallback.apply(id);
                    // 閲嶅缓缂撳瓨
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    // 閲婃斁閿?
                    unlock(lockKey);
                }
            });
        }
        // 6.4.杩斿洖杩囨湡鐨勫晢閾轰俊鎭?
        return r;
    }

    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.浠巖edis鏌ヨ鍟嗛摵缂撳瓨
        R localValue = localCacheClient.get(key, type);
        if (localValue != null) {
            return localValue;
        }
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        // 2.鍒ゆ柇鏄惁瀛樺湪
        if (StrUtil.isNotBlank(shopJson)) {
            // 3.瀛樺湪锛岀洿鎺ヨ繑鍥?
            R value = JSONUtil.toBean(shopJson, type);
            localCacheClient.set(key, value, time, unit);
            return value;
        }
        // 鍒ゆ柇鍛戒腑鐨勬槸鍚︽槸绌哄€?
        if (shopJson != null) {
            // 杩斿洖涓€涓敊璇俊鎭?
            return null;
        }

        // 4.瀹炵幇缂撳瓨閲嶅缓
        // 4.1.鑾峰彇浜掓枼閿?
        String lockKey = LOCK_SHOP_KEY + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2.鍒ゆ柇鏄惁鑾峰彇鎴愬姛
            if (!isLock) {
                // 4.3.鑾峰彇閿佸け璐ワ紝浼戠湢骞堕噸璇?
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            // 4.4.鑾峰彇閿佹垚鍔燂紝鏍规嵁id鏌ヨ鏁版嵁搴?
            r = dbFallback.apply(id);
            // 5.涓嶅瓨鍦紝杩斿洖閿欒
            if (r == null) {
                // 灏嗙┖鍊煎啓鍏edis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                // 杩斿洖閿欒淇℃伅
                return null;
            }
            // 6.瀛樺湪锛屽啓鍏edis
            this.set(key, r, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 7.閲婃斁閿?
            unlock(lockKey);
        }
        // 8.杩斿洖
        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
