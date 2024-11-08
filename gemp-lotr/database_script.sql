-- MySQL dump 10.13  Distrib 8.0.11, for Win64 (x86_64)
--
-- Host: 161.35.10.4    Database: gemp_db
-- ------------------------------------------------------
-- Server version	5.5.5-10.5.25-MariaDB-ubu2004

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `collection`
--

DROP TABLE IF EXISTS `collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `collection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `collection` mediumblob DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  `extra_info` varchar(5000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_collection_player_type` (`player_id`,`type`),
  KEY `player_collection` (`player_id`,`type`),
  CONSTRAINT `fk_collection_player_id` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=88474 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `collection_entries`
--

DROP TABLE IF EXISTS `collection_entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `collection_entries` (
  `collection_id` int(11) NOT NULL,
  `quantity` int(2) DEFAULT 0,
  `product_type` varchar(50) NOT NULL,
  `product_variant` varchar(50) DEFAULT NULL,
  `product` varchar(50) NOT NULL,
  `source` varchar(100) NOT NULL,
  `created_date` datetime DEFAULT current_timestamp(),
  `modified_date` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  `notes` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`collection_id`,`product`),
  CONSTRAINT `collection_entries_ibfk_1` FOREIGN KEY (`collection_id`) REFERENCES `collection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `deck`
--

DROP TABLE IF EXISTS `deck`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `deck` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `target_format` varchar(50) NOT NULL DEFAULT 'Anything Goes',
  `type` varchar(45) NOT NULL DEFAULT 'Default',
  `contents` text NOT NULL,
  `notes` varchar(5000) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `player_deck` (`player_id`,`name`),
  KEY `player_id` (`player_id`),
  CONSTRAINT `deck_ibfk_1` FOREIGN KEY (`player_id`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=301268 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_history`
--

DROP TABLE IF EXISTS `game_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `game_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `winner` varchar(45) NOT NULL,
  `winnerId` int(11) NOT NULL DEFAULT 0,
  `loser` varchar(45) NOT NULL,
  `loserId` int(11) NOT NULL DEFAULT 0,
  `win_reason` varchar(255) NOT NULL,
  `lose_reason` varchar(255) NOT NULL,
  `win_recording_id` varchar(45) DEFAULT NULL,
  `lose_recording_id` varchar(45) DEFAULT NULL,
  `start_date` datetime NOT NULL DEFAULT current_timestamp(),
  `end_date` datetime NOT NULL DEFAULT current_timestamp(),
  `format_name` varchar(45) DEFAULT NULL,
  `winner_deck_name` varchar(255) DEFAULT NULL,
  `loser_deck_name` varchar(255) DEFAULT NULL,
  `tournament` varchar(255) DEFAULT NULL,
  `replay_version` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `index3` (`winner`),
  KEY `index4` (`loser`),
  KEY `game_history_tournament_IDX` (`tournament`) USING HASH,
  KEY `game_history_win_id_index` (`win_recording_id`),
  KEY `game_history_lose_id_index` (`lose_recording_id`),
  KEY `fk_winnerId` (`winnerId`),
  KEY `fk_loserId` (`loserId`),
  CONSTRAINT `fk_loserId` FOREIGN KEY (`loserId`) REFERENCES `player` (`id`),
  CONSTRAINT `fk_winnerId` FOREIGN KEY (`winnerId`) REFERENCES `player` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=986971 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ignores`
--

DROP TABLE IF EXISTS `ignores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `ignores` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playerName` varchar(30) DEFAULT NULL,
  `ignoredName` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `PLAYER_IGNORES` (`playerName`)
) ENGINE=MyISAM AUTO_INCREMENT=1359 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ip_ban`
--

DROP TABLE IF EXISTS `ip_ban`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `ip_ban` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) NOT NULL,
  `prefix` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=244 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `league`
--

DROP TABLE IF EXISTS `league`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `league` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `code` bigint(20) unsigned NOT NULL DEFAULT 0,
  `type` varchar(255) NOT NULL,
  `parameters` varchar(5000) DEFAULT NULL,
  `start_date` date NOT NULL DEFAULT current_timestamp(),
  `end_date` date NOT NULL DEFAULT current_timestamp(),
  `status` int(11) NOT NULL,
  `cost` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=478 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `league_match`
--

DROP TABLE IF EXISTS `league_match`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `league_match` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `league_type` varchar(45) NOT NULL,
  `season_type` varchar(45) NOT NULL,
  `winner` varchar(45) NOT NULL,
  `loser` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `league_match_type` (`league_type`)
) ENGINE=InnoDB AUTO_INCREMENT=174464 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `league_participation`
--

DROP TABLE IF EXISTS `league_participation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `league_participation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `league_type` varchar(45) NOT NULL,
  `player_name` varchar(45) NOT NULL,
  `join_ip` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `league_participation_type` (`league_type`)
) ENGINE=InnoDB AUTO_INCREMENT=43090 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `player`
--

DROP TABLE IF EXISTS `player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `player` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `password` varchar(64) NOT NULL,
  `type` varchar(5) NOT NULL DEFAULT 'u',
  `last_login_reward` int(11) DEFAULT NULL,
  `last_ip` varchar(45) DEFAULT NULL,
  `create_ip` varchar(45) DEFAULT NULL,
  `banned_until` decimal(20,0) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=32845 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scheduled_tournament`
--

DROP TABLE IF EXISTS `scheduled_tournament`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `scheduled_tournament` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tournament_id` varchar(45) NOT NULL,
  `name` varchar(255) NOT NULL,
  `format` varchar(45) NOT NULL,
  `start_date` datetime NOT NULL DEFAULT current_timestamp(),
  `type` varchar(45) NOT NULL DEFAULT 'CONSTRUCTED',
  `parameters` varchar(5000) NOT NULL DEFAULT '{}',
  `started` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tournament`
--

DROP TABLE IF EXISTS `tournament`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tournament` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tournament_id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `start_date` datetime NOT NULL DEFAULT current_timestamp(),
  `type` varchar(45) NOT NULL DEFAULT 'CONSTRUCTED',
  `parameters` varchar(5000) NOT NULL DEFAULT '{}',
  `stage` varchar(45) DEFAULT NULL,
  `round` int(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_tournament_id` (`tournament_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1451 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tournament_match`
--

DROP TABLE IF EXISTS `tournament_match`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tournament_match` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tournament_id` varchar(255) NOT NULL,
  `round` int(11) NOT NULL DEFAULT 0,
  `player_one` varchar(45) NOT NULL,
  `player_two` varchar(45) NOT NULL,
  `winner` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14304 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tournament_player`
--

DROP TABLE IF EXISTS `tournament_player`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tournament_player` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tournament_id` varchar(255) NOT NULL,
  `player` varchar(30) DEFAULT NULL,
  `deck_name` varchar(100) NOT NULL,
  `deck` text NOT NULL,
  `dropped` binary(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10377 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `transfer`
--

DROP TABLE IF EXISTS `transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `transfer` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `notify` int(11) NOT NULL,
  `player` varchar(45) NOT NULL,
  `reason` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `currency` int(11) NOT NULL,
  `collection` text NOT NULL,
  `transfer_date` decimal(20,0) NOT NULL,
  `direction` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `player` (`player`,`notify`)
) ENGINE=InnoDB AUTO_INCREMENT=3644 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'gemp_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-10-31 22:50:04
