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
  (101, '浜戞湹閫犲瀷宸ヤ綔瀹?, 3, '/imgs/types/lrmf.png', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇?灞?, 120.151000, 30.325000, 128, 980, 268, 46, '10:00-21:30'),
  (102, '妗冩缇庣敳缇庣潾', 10, '/imgs/types/mjmj.png', '姘存櫠鍩?, '涓婂璺?58鍙锋按鏅跺煄璐墿涓績2灞?, 120.158200, 30.310400, 96, 1240, 356, 48, '10:30-22:00'),
  (103, '鏉鹃棿瓒抽亾', 5, '/imgs/types/amzl.png', '澶у叧', '閲戝崕璺敠鏄屾枃鍗庤嫅31鍙?, 120.149500, 30.316500, 168, 760, 198, 45, '12:00-02:00'),
  (104, '婢勫厜缇庡SPA', 6, '/imgs/types/spa.png', '杩滄磱涔愬牑娓?, '涓芥按璺?6鍙疯繙娲嬩箰鍫ゆ腐鍟嗗煄B120', 120.146900, 30.312900, 298, 540, 142, 47, '10:00-22:00'),
  (105, '灏忔浜插瓙涔愬洯', 7, '/imgs/types/qzyl.png', '鍖楅儴鏂板煄', '鏉璺?66鍙蜂竾杈惧箍鍦?灞?, 120.128900, 30.337000, 88, 2260, 618, 46, '09:30-21:00'),
  (106, '鎷惧厜绮鹃吙閰掑惂', 8, '/imgs/types/jiuba.png', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇?灞?, 120.150400, 30.325300, 118, 1860, 430, 45, '18:00-02:00'),
  (107, '妫笨杞拌洞棣?, 9, '/imgs/types/hpg.png', '鎷卞妗?涓婂', '涓婂璺?035鍙?骞?01瀹?, 120.151700, 30.333700, 260, 430, 92, 44, '10:00-24:00'),
  (108, '璺冨姩鍋ヨ韩宸ヤ綔瀹?, 4, '/imgs/types/jsyd.png', 'D32澶╅槼璐墿涓績', '婀栧窞琛?67鍙峰寳鍩庡ぉ鍦?灞?, 120.130600, 30.327800, 199, 1520, 384, 47, '07:00-22:30')
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
