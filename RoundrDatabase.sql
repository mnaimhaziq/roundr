-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.1.0.6537
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for game
CREATE DATABASE IF NOT EXISTS `game` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `game`;

-- Dumping structure for table game.chat
CREATE TABLE IF NOT EXISTS `chat` (
  `chat_id` int NOT NULL AUTO_INCREMENT,
  `game_id` int NOT NULL,
  `player_id` int NOT NULL,
  `message_content` varchar(255) NOT NULL DEFAULT '',
  `timestamp` date NOT NULL,
  PRIMARY KEY (`chat_id`),
  KEY `FK_chat_game` (`game_id`),
  KEY `FK_chat_player` (`player_id`),
  CONSTRAINT `FK_chat_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`game_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_chat_player` FOREIGN KEY (`player_id`) REFERENCES `player` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table game.chat: ~0 rows (approximately)
DELETE FROM `chat`;

-- Dumping structure for table game.game
CREATE TABLE IF NOT EXISTS `game` (
  `game_id` int NOT NULL AUTO_INCREMENT,
  `games_status` varchar(50) NOT NULL,
  `turn_rounds` int NOT NULL DEFAULT '0',
  `turn_time_limit` int NOT NULL DEFAULT '0',
  `word_length` int NOT NULL DEFAULT '0',
  `player_limit` int NOT NULL DEFAULT '0',
  `player_count` int NOT NULL DEFAULT '0',
  `game_status` enum('ready','not_ready') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'not_ready',
  `ip_address` varchar(50) NOT NULL,
  PRIMARY KEY (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table game.game: ~0 rows (approximately)
DELETE FROM `game`;

-- Dumping structure for table game.player
CREATE TABLE IF NOT EXISTS `player` (
  `player_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
 `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`player_id`) USING BTREE,
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table game.player: ~0 rows (approximately)
DELETE FROM `player`;

-- Dumping structure for table game.player_game
CREATE TABLE IF NOT EXISTS `player_game` (
  `player_game_id` int NOT NULL AUTO_INCREMENT,
  `game_id` int NOT NULL DEFAULT '0',
  `player_id` int NOT NULL DEFAULT '0',
  `is_host` tinyint NOT NULL DEFAULT '0',
  `player_color` int NOT NULL DEFAULT '0',
  `final_score` int NOT NULL DEFAULT '0',
  `status` enum('ready','not_ready') DEFAULT 'not_ready',
  PRIMARY KEY (`player_game_id`),
  KEY `FK_player_game_game` (`game_id`),
  KEY `FK_player_game_player` (`player_id`),
  CONSTRAINT `FK_player_game_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`game_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_player_game_player` FOREIGN KEY (`player_id`) REFERENCES `player` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table game.player_game: ~0 rows (approximately)
DELETE FROM `player_game`;

-- Dumping structure for table game.round
CREATE TABLE IF NOT EXISTS `round` (
  `round_id` int NOT NULL AUTO_INCREMENT,
  `game_id` int NOT NULL DEFAULT '0',
  `round_number` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`round_id`),
  KEY `FK_round_game` (`game_id`),
  CONSTRAINT `FK_round_game` FOREIGN KEY (`game_id`) REFERENCES `game` (`game_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table game.round: ~0 rows (approximately)
DELETE FROM `round`;

-- Dumping structure for table game.turn
CREATE TABLE IF NOT EXISTS `turn` (
  `turn_id` int NOT NULL AUTO_INCREMENT,
  `round_id` int NOT NULL DEFAULT '0',
  `player_id` int NOT NULL DEFAULT '0',
  `words` varchar(50) NOT NULL,
  `time_taken` time NOT NULL DEFAULT '00:00:00',
  `score` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`turn_id`),
  KEY `FK__round` (`round_id`),
  KEY `FK__player` (`player_id`),
  CONSTRAINT `FK__player` FOREIGN KEY (`player_id`) REFERENCES `player` (`player_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK__round` FOREIGN KEY (`round_id`) REFERENCES `round` (`round_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table game.turn: ~0 rows (approximately)
DELETE FROM `turn`;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
