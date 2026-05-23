package com.lifeselection.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeselection.dto.Result;
import com.lifeselection.entity.SeckillVoucher;
import com.lifeselection.entity.Voucher;
import com.lifeselection.mapper.VoucherMapper;
import com.lifeselection.service.ISeckillVoucherService;
import com.lifeselection.service.IVoucherService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.lifeselection.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  鏈嶅姟瀹炵幇绫?
 * </p>
 *
 * @author 铏庡摜
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 鏌ヨ浼樻儬鍒镐俊鎭?
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 杩斿洖缁撴灉
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 淇濆瓨浼樻儬鍒?
        save(voucher);
        // 淇濆瓨绉掓潃淇℃伅
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 淇濆瓨绉掓潃搴撳瓨鍒癛edis涓?
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
