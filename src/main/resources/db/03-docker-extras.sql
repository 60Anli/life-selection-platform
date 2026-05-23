SET NAMES utf8mb4;

INSERT INTO `tb_voucher`
  (`id`, `shop_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`)
VALUES
  (2, 1, '100 yuan seckill voucher', 'Limited time offer', 'Available after successful seckill order.', 1000, 10000, 1, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `sub_title` = VALUES(`sub_title`),
  `rules` = VALUES(`rules`),
  `pay_value` = VALUES(`pay_value`),
  `actual_value` = VALUES(`actual_value`),
  `type` = VALUES(`type`),
  `status` = VALUES(`status`);

INSERT INTO `tb_seckill_voucher`
  (`voucher_id`, `stock`, `begin_time`, `end_time`)
VALUES
  (2, 100, '2022-01-01 00:00:00', '2037-12-31 23:59:59')
ON DUPLICATE KEY UPDATE
  `stock` = VALUES(`stock`),
  `begin_time` = VALUES(`begin_time`),
  `end_time` = VALUES(`end_time`);

INSERT INTO `tb_shop`
  (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`)
VALUES
  (101, 'Cloud Style Studio', 3, '/imgs/types/lrmf.png', 'Canal Street', 'Canal Street Mall 3F', 120.151000, 30.325000, 128, 980, 268, 46, '10:00-21:30'),
  (102, 'Peach Nail Studio', 10, '/imgs/types/mjmj.png', 'Crystal City', 'Crystal City Mall 2F', 120.158200, 30.310400, 96, 1240, 356, 48, '10:30-22:00'),
  (103, 'Pine Foot Massage', 5, '/imgs/types/amzl.png', 'Daguan', 'Jinhua Road 31', 120.149500, 30.316500, 168, 760, 198, 45, '12:00-02:00'),
  (104, 'Clear Light SPA', 6, '/imgs/types/spa.png', 'Ocean Plaza', 'Lishui Road B120', 120.146900, 30.312900, 298, 540, 142, 47, '10:00-22:00'),
  (105, 'Orange Family Park', 7, '/imgs/types/qzyl.png', 'North City', 'Wanda Plaza 3F', 120.128900, 30.337000, 88, 2260, 618, 46, '09:30-21:00'),
  (106, 'Time Craft Bar', 8, '/imgs/types/jiuba.png', 'Canal Street', 'Canal Street Mall 1F', 120.150400, 30.325300, 118, 1860, 430, 45, '18:00-02:00'),
  (107, 'Forest Party House', 9, '/imgs/types/hpg.png', 'Gongshu', 'Shangtang Road 1035', 120.151700, 30.333700, 260, 430, 92, 44, '10:00-24:00'),
  (108, 'Jump Fitness Studio', 4, '/imgs/types/jsyd.png', 'D32 Mall', 'Huzhou Street 567', 120.130600, 30.327800, 199, 1520, 384, 47, '07:00-22:30')
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type_id` = VALUES(`type_id`),
  `images` = VALUES(`images`),
  `area` = VALUES(`area`),
  `address` = VALUES(`address`),
  `x` = VALUES(`x`),
  `y` = VALUES(`y`),
  `avg_price` = VALUES(`avg_price`),
  `sold` = VALUES(`sold`),
  `comments` = VALUES(`comments`),
  `score` = VALUES(`score`),
  `open_hours` = VALUES(`open_hours`);
