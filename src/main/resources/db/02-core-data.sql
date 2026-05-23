SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `tb_shop_type` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `sort` int(10) unsigned DEFAULT 0,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tb_shop_type` (`id`, `name`, `icon`, `sort`) VALUES
(1, '缇庨', '/types/ms.png', 1),
(2, 'KTV', '/types/KTV.png', 2),
(3, '涓戒汉路缇庡彂', '/types/lrmf.png', 3),
(10, '缇庣潾路缇庣敳', '/types/mjmj.png', 4),
(5, '鎸夋懇路瓒崇枟', '/types/amzl.png', 5),
(6, '缇庡SPA', '/types/spa.png', 6),
(7, '浜插瓙娓镐箰', '/types/qzyl.png', 7),
(8, '閰掑惂', '/types/jiuba.png', 8),
(9, '杞拌洞棣?, '/types/hpg.png', 9),
(4, '鍋ヨ韩杩愬姩', '/types/jsyd.png', 10)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `icon` = VALUES(`icon`), `sort` = VALUES(`sort`);

CREATE TABLE IF NOT EXISTS `tb_shop` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `type_id` bigint(20) unsigned NOT NULL,
  `images` varchar(2048) DEFAULT '',
  `area` varchar(128) DEFAULT '',
  `address` varchar(255) DEFAULT '',
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `avg_price` bigint(10) unsigned DEFAULT 0,
  `sold` int(10) unsigned DEFAULT 0,
  `comments` int(10) unsigned DEFAULT 0,
  `score` int(2) unsigned DEFAULT 0,
  `open_hours` varchar(64) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_id` (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tb_shop` (`id`, `name`, `type_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`) VALUES
(1, '103鑼堕鍘?, 1, '/imgs/shop/1.jpg', '澶у叧', '閲戝崕璺敠鏄屾枃鍗庤嫅29鍙?, 120.149192, 30.316078, 80, 4215, 3035, 37, '10:00-22:00'),
(2, '钄￠┈娲稕鐑よ倝', 1, '/imgs/shop/2.jpg', '鎷卞妗?涓婂', '涓婂璺?035鍙?, 120.151505, 30.333422, 85, 2160, 1460, 46, '11:30-03:00'),
(3, '鏂扮櫧楣块鍘?, 1, '/imgs/shop/3.jpg', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇僃5', 120.151954, 30.324970, 61, 12035, 8045, 47, '10:30-21:00'),
(4, 'Mamala', 1, '/imgs/shop/4.jpg', '鎷卞妗?涓婂', '涓芥按璺?6鍙疯繙娲嬩箰鍫ゆ腐鍟嗗煄', 120.146659, 30.312742, 290, 13519, 9529, 49, '11:00-22:00'),
(5, '娴峰簳鎹炵伀閿?, 1, '/imgs/shop/5.jpg', '澶у叧', '涓婂璺?58鍙锋按鏅跺煄璐墿涓績F6', 120.157780, 30.310633, 104, 4125, 2764, 49, '10:00-07:00'),
(10, '寮€涔愯开KTV', 2, '/imgs/shop/10.jpg', '杩愭渤涓婅', '鍙板窞璺?鍙疯繍娌充笂琛楄喘鐗╀腑蹇僃4', 120.149093, 30.324666, 67, 26891, 902, 37, '00:00-24:00')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

CREATE TABLE IF NOT EXISTS `tb_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL,
  `password` varchar(128) DEFAULT '',
  `nick_name` varchar(32) DEFAULT '',
  `icon` varchar(255) DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tb_user` (`id`, `phone`, `password`, `nick_name`, `icon`) VALUES
(1, '13686869696', '', '灏忛奔鍚屽', '/imgs/blogs/blog1.jpg'),
(2, '13838411438', '', '鍙彲浠婂ぉ涓嶅悆鑲?, '/imgs/icons/kkjtbcr.jpg')
ON DUPLICATE KEY UPDATE `nick_name` = VALUES(`nick_name`);

CREATE TABLE IF NOT EXISTS `tb_user_info` (
  `user_id` bigint(20) unsigned NOT NULL,
  `city` varchar(64) DEFAULT '',
  `introduce` varchar(128) DEFAULT '',
  `fans` int(10) unsigned DEFAULT 0,
  `followee` int(10) unsigned DEFAULT 0,
  `gender` tinyint(1) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `credits` int(10) unsigned DEFAULT 0,
  `level` tinyint(1) DEFAULT 0,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tb_voucher` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(20) unsigned NOT NULL,
  `title` varchar(255) NOT NULL,
  `sub_title` varchar(255) DEFAULT '',
  `rules` varchar(1024) DEFAULT '',
  `pay_value` bigint(10) unsigned DEFAULT 0,
  `actual_value` bigint(10) unsigned DEFAULT 0,
  `type` tinyint(1) unsigned DEFAULT 0,
  `status` tinyint(1) unsigned DEFAULT 1,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tb_voucher` (`id`, `shop_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`) VALUES
(1, 1, '50鍏冧唬閲戝埜', '鍛ㄤ竴鑷冲懆鏃ュ潎鍙娇鐢?, '鍏ㄥ満閫氱敤锛屼笉鍏戠幇銆佷笉鎵鹃浂锛屼粎闄愬爞椋?, 4750, 5000, 0, 1),
(2, 1, '100鍏冪鏉€鍒?, '闄愭椂鎶㈣喘', '绉掓潃鎴愬姛鍚庡埌搴椾娇鐢?, 1000, 10000, 1, 1)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

CREATE TABLE IF NOT EXISTS `tb_seckill_voucher` (
  `voucher_id` bigint(20) unsigned NOT NULL,
  `stock` int(10) unsigned DEFAULT 0,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `begin_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tb_seckill_voucher` (`voucher_id`, `stock`, `begin_time`, `end_time`) VALUES
(2, 100, '2022-01-01 00:00:00', '2037-12-31 23:59:59')
ON DUPLICATE KEY UPDATE `stock` = VALUES(`stock`);

CREATE TABLE IF NOT EXISTS `tb_voucher_order` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `voucher_id` bigint(20) unsigned NOT NULL,
  `pay_type` tinyint(1) unsigned DEFAULT 1,
  `status` tinyint(1) unsigned DEFAULT 1,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pay_time` timestamp NULL DEFAULT NULL,
  `use_time` timestamp NULL DEFAULT NULL,
  `refund_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_voucher` (`user_id`, `voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
