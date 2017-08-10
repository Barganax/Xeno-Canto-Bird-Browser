-- MySQL dump 10.13  Distrib 5.7.15, for Linux (x86_64)
--
-- Host: localhost    Database: bird_recording
-- ------------------------------------------------------
-- Server version       5.7.13-0ubuntu0.16.04.2

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
-- Table structure for table `cod_param`
--

drop table if exists `cod_param`;
CREATE TABLE `cod_param` (
  `cod_param_id` int(11) NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) DEFAULT NULL,
  `gain` double DEFAULT NULL,
  `silence_threshold` double DEFAULT NULL,
  `peak_threshold` double DEFAULT NULL,
  `min_interonset_interval` int(11) DEFAULT NULL,
  PRIMARY KEY (`cod_param_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1

LOCK TABLES `cod_param` WRITE;
/*!40000 ALTER TABLE `cod_param` DISABLE KEYS */;
INSERT INTO `cod_param` VALUES (1,'DEFAULT',0,-70,0.2,300);
/*!40000 ALTER TABLE `cod_param` ENABLE KEYS */;
UNLOCK TABLES;


--
-- Table structure for table `onset`
--

DROP TABLE IF EXISTS `onset`;
CREATE TABLE `onset` (
  `onset_id` int(11) NOT NULL AUTO_INCREMENT,
  `recording_id` varchar(255) NOT NULL,
  `cod_param_id` int(11) NOT NULL,
  `onset_time` int(11) DEFAULT NULL,
  `onset_salience` float DEFAULT NULL,
  PRIMARY KEY (`onset_id`),
  KEY `recording_id` (`recording_id`),
  KEY `cod_param_id` (`cod_param_id`),
  CONSTRAINT `onset_ibfk_1` FOREIGN KEY (`recording_id`) REFERENCES `recording` (`recording_id`),
  CONSTRAINT `onset_ibfk_2` FOREIGN KEY (`cod_param_id`) REFERENCES `cod_param` (`cod_param_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Table structure for table `recording`
--

DROP TABLE IF EXISTS `recording`;
 CREATE TABLE `recording` (
  `recording_id` varchar(255) NOT NULL,
  `species_id` int(11) NOT NULL,
  `subspecies` varchar(255) DEFAULT NULL,
  `format` varchar(255) NOT NULL,
  `length` int(11) DEFAULT NULL,
  `recordist` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `time` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `elevation` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `solitary` tinyint(1) DEFAULT NULL,
  `url` longtext,
  `remarks` longtext,
  PRIMARY KEY (`recording_id`),
  UNIQUE KEY `recording_id` (`recording_id`),
  KEY `species_id` (`species_id`),
  CONSTRAINT `recording_ibfk_1` FOREIGN KEY (`species_id`) REFERENCES `species` (`species_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

--
-- Table structure for table `recording_info`
--

DROP TABLE IF EXISTS `recording_info`;
CREATE TABLE `recording_info` (
  `recording_id` varchar(255) NOT NULL,
  `max_amplitude` double DEFAULT NULL,
  `avg_amplitude` double DEFAULT NULL,
  `var_amplitude` double DEFAULT NULL,
  PRIMARY KEY (`recording_id`),
  UNIQUE KEY `recording_id` (`recording_id`),
  CONSTRAINT `recording_info_ibfk_1` FOREIGN KEY (`recording_id`) REFERENCES `recording` (`recording_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

--
-- Table structure for table `sonogram_preference`
--

DROP TABLE IF EXISTS `sonogram_preference`;
CREATE TABLE `sonogram_preference` (
  `sonogram_preference_id` int(11) NOT NULL AUTO_INCREMENT,
  `tag` varchar(255) DEFAULT NULL,
  `buffer_size` int(11) DEFAULT NULL,
  `window_function` enum('none','rectangular','triangular','bartlett','bartlett_hann','blackman','blackman_harris_nuttall','cosine','hamming') DEFAULT NULL,
  `lo_cutoff` int(11) DEFAULT NULL,
  `hi_cutoff` int(11) DEFAULT NULL,
  `components` int(11) DEFAULT NULL,
  PRIMARY KEY (`sonogram_preference_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1

LOCK TABLES `sonogram_preference` WRITE;
/*!40000 ALTER TABLE `sonogram_preference` DISABLE KEYS */;
INSERT INTO `sonogram_preference` VALUES (1,'DEFAULT',2048,'none',100,18000,50);
/*!40000 ALTER TABLE `sonogram_preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sonogram`
--

DROP TABLE IF EXISTS `sonogram`;
CREATE TABLE `sonogram` (
  `sonogram_id` int(11) NOT NULL AUTO_INCREMENT,
  `sonogram_preference_id` int(11) NOT NULL,
  `onset_id` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `length` int(11) NOT NULL,
  `quality` enum('clean','other_bird','noisy','noisy_other_bird') DEFAULT NULL,
  PRIMARY KEY (`sonogram_id`),
  KEY `onset_id` (`onset_id`),
  KEY `sonogram_ibfk_2` (`sonogram_preference_id`),
  CONSTRAINT `sonogram_ibfk_1` FOREIGN KEY (`onset_id`) REFERENCES `onset` (`onset_id`),
  CONSTRAINT `sonogram_ibfk_2` FOREIGN KEY (`sonogram_preference_id`) REFERENCES `sonogram_preference` (`sonogram_preference_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1028 DEFAULT CHARSET=latin1

--
-- Table structure for table `sonogram`
--

DROP TABLE IF EXISTS `sonogram_data`;
CREATE TABLE `sonogram_data` (
  `sonogram_data_id` int(11) NOT NULL AUTO_INCREMENT,
  `time_stamp` double NOT NULL,
  `data` blob NOT NULL,
  PRIMARY KEY (`sonogram_data_id`),
  KEY `time_stamp` (`time_stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

--
-- Table structure for table `sonogram_data_intersect`
--

drop table if exists `sonogram_data_intersect`
CREATE TABLE `sonogram_data_intersect` (
  `sonogram_id` int(11) NOT NULL,
  `sonogram_data_id` int(11) NOT NULL,
  `time_stamp` double NOT NULL,
  PRIMARY KEY (`sonogram_id`,`sonogram_data_id`),
  KEY `sonogram_data_intersect_ibfk_2` (`sonogram_data_id`),
  CONSTRAINT `sonogram_data_intersect_ibfk_1` FOREIGN KEY (`sonogram_id`) REFERENCES `sonogram` (`sonogram_id`),
  CONSTRAINT `sonogram_data_intersect_ibfk_2` FOREIGN KEY (`sonogram_data_id`) REFERENCES `sonogram_data` (`sonogram_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1

--
-- Table structure for table `species`
--

DROP TABLE IF EXISTS `species`;
CREATE TABLE `species` (
  `species_id` int(11) NOT NULL AUTO_INCREMENT,
  `family` varchar(255) DEFAULT NULL,
  `genus` varchar(255) DEFAULT NULL,
  `species` varchar(255) DEFAULT NULL,
  `common_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`species_id`),
  KEY `genus` (`genus`,`species`),
  KEY `family` (`family`)
) ENGINE=InnoDB AUTO_INCREMENT=765 DEFAULT CHARSET=latin1


