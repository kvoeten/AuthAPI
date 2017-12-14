CREATE DATABASE IF NOT EXISTS `service` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `service`;

DROP TABLE IF EXISTS `accounts`;
CREATE TABLE IF NOT EXISTS `accounts` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(13) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `state` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `banned` tinyint(2) unsigned NOT NULL DEFAULT '0',
  `bantime` timestamp NOT NULL DEFAULT '1971-01-01 00:00:01',
  `birthday` timestamp NOT NULL DEFAULT '1971-01-01 00:00:01',
  `creation` timestamp NOT NULL DEFAULT '1971-01-01 00:00:01',
  `history` timestamp NOT NULL DEFAULT '1971-01-01 00:00:01',
  `admin` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `gender` tinyint(2) unsigned NOT NULL DEFAULT '10',
  `ip` varchar(15) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;