package com.lifeselection.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeselection.dto.Result;
import com.lifeselection.entity.VoucherOrder;
import com.lifeselection.mapper.VoucherOrderMapper;
import com.lifeselection.service.ISeckillVoucherService;
import com.lifeselection.service.IVoucherOrderService;
import com.lifeselection.utils.RedisIdWorker;
import com.lifeselection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

import static com.lifeselection.utils.MessageConstants.SECKILL_ORDER_GROUP;
import static com.lifeselection.utils.MessageConstants.SECKILL_ORDER_TOPIC;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString()
        );

        int r = result == null ? 1 : result.intValue();
        if (r != 0) {
            return Result.fail(r == 1 ? "\u5E93\u5B58\u4E0D\u8DB3" : "\u4E0D\u80FD\u91CD\u590D\u4E0B\u5355");
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        kafkaTemplate.send(SECKILL_ORDER_TOPIC, String.valueOf(orderId), JSONUtil.toJsonStr(voucherOrder))
                .addCallback(
                        success -> log.debug("Sent seckill order to Kafka, orderId={}", orderId),
                        failure -> log.error("Send seckill order to Kafka failed, orderId={}", orderId, failure)
                );
        return Result.ok(orderId);
    }

    @KafkaListener(topics = SECKILL_ORDER_TOPIC, groupId = SECKILL_ORDER_GROUP)
    public void handleVoucherOrder(String message) {
        try {
            VoucherOrder voucherOrder = JSONUtil.toBean(message, VoucherOrder.class);
            createVoucherOrder(voucherOrder);
        } catch (Exception e) {
            log.error("Handle seckill order message failed, message={}", message, e);
            throw e;
        }
    }

    private void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = redisLock.tryLock();
        if (!isLock) {
            log.error("Duplicate order blocked, userId={}, voucherId={}", userId, voucherId);
            return;
        }

        try {
            int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                log.error("Duplicate order blocked, userId={}, voucherId={}", userId, voucherId);
                return;
            }

            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId).gt("stock", 0)
                    .update();
            if (!success) {
                log.error("Voucher stock is not enough, voucherId={}", voucherId);
                return;
            }

            save(voucherOrder);
        } finally {
            redisLock.unlock();
        }
    }
}
