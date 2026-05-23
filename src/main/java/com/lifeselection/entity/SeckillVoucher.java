package com.lifeselection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 绉掓潃浼樻儬鍒歌〃锛屼笌浼樻儬鍒告槸涓€瀵逛竴鍏崇郴
 * </p>
 *
 * @author 铏庡摜
 * @since 2022-01-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_seckill_voucher")
public class SeckillVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鍏宠仈鐨勪紭鎯犲埜鐨刬d
     */
    @TableId(value = "voucher_id", type = IdType.INPUT)
    private Long voucherId;

    /**
     * 搴撳瓨
     */
    private Integer stock;

    /**
     * 鍒涘缓鏃堕棿
     */
    private LocalDateTime createTime;

    /**
     * 鐢熸晥鏃堕棿
     */
    private LocalDateTime beginTime;

    /**
     * 澶辨晥鏃堕棿
     */
    private LocalDateTime endTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private LocalDateTime updateTime;


}
