-- create table user
CREATE TABLE `yh_user` (
  `id` bigint(13) NOT NULL AUTO_INCREMENT,
  `age` int(4) NOT NULL,
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `birthday` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `role` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `option1` bigint(20) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `modify_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci


-- create table user_change
CREATE TABLE `yh_user_change` (
  `id` bigint(13) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(13) NOT NULL,
  `type` int(4) NOT NULL,
  `old_val` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `new_val` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci