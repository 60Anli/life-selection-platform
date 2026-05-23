package com.lifeselection.config;

import com.lifeselection.entity.SeckillVoucher;
import com.lifeselection.entity.Shop;
import com.lifeselection.service.ISeckillVoucherService;
import com.lifeselection.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lifeselection.utils.RedisConstants.SECKILL_STOCK_KEY;
import static com.lifeselection.utils.RedisConstants.SHOP_GEO_KEY;

@Slf4j
@Component
public class RedisDataInitializer implements ApplicationRunner {

    @Resource
    private IShopService shopService;
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        loadShopGeo();
        loadSeckillStock();
    }

    private void loadShopGeo() {
        List<Shop> shops = shopService.list();
        Map<Long, List<Shop>> shopsByType = shops.stream()
                .filter(shop -> shop.getTypeId() != null && shop.getX() != null && shop.getY() != null)
                .collect(Collectors.groupingBy(Shop::getTypeId));

        shopsByType.forEach((typeId, typedShops) -> {
            String key = SHOP_GEO_KEY + typeId;
            stringRedisTemplate.delete(key);
            for (Shop shop : typedShops) {
                stringRedisTemplate.opsForGeo().add(
                        key,
                        new RedisGeoCommands.GeoLocation<>(
                                shop.getId().toString(),
                                new org.springframework.data.geo.Point(shop.getX(), shop.getY())
                        )
                );
            }
        });
        log.info("Loaded {} shops into Redis GEO", shops.size());
    }

    private void loadSeckillStock() {
        List<SeckillVoucher> vouchers = seckillVoucherService.list();
        for (SeckillVoucher voucher : vouchers) {
            if (voucher.getVoucherId() != null && voucher.getStock() != null) {
                stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getVoucherId(), voucher.getStock().toString());
            }
        }
        log.info("Loaded {} seckill voucher stock records into Redis", vouchers.size());
    }
}
