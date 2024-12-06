

SELECT COUNT(*) 
FROM transfer 
WHERE player = 'wert' 
    AND notify = 1
	AND date_recorded < NOW();


SELECT *
FROM gemp_db.transfer t
# WHERE collection RLIKE('(101_65|101_66|101_67)')
ORDER BY id DESC;


UPDATE transfer 
SET notify = TRUE 
WHERE id = 5031

SELECT NOW(), NOW() + INTERVAL 24 HOUR




ALTER TABLE gemp_db.transfer  
ADD COLUMN date_recorded DATETIME NOT NULL DEFAULT now() AFTER transfer_date;

ALTER TABLE gemp_db.transfer  
ADD COLUMN notify2 BIT NOT NULL DEFAULT 0 AFTER notify;

ALTER TABLE gemp_db.transfer
MODIFY COLUMN currency INT(11) NOT NULL DEFAULT 0;

UPDATE gemp_db.transfer 
SET date_recorded = from_unixtime(floor(transfer_date/1000));

UPDATE gemp_db.transfer 
SET notify2 = CASE WHEN notify = '1' THEN TRUE ELSE FALSE END;



ALTER TABLE gemp_db.transfer 
RENAME COLUMN collection TO contents;

ALTER TABLE gemp_db.transfer 
RENAME COLUMN name TO collection;

ALTER TABLE gemp_db.transfer 
DROP COLUMN `transfer_date`;

ALTER TABLE gemp_db.transfer 
DROP COLUMN `notify`;

ALTER TABLE gemp_db.transfer 
RENAME COLUMN notify2 TO notify;



DROP TABLE IF EXISTS `announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `announcements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) COLLATE utf8_bin NOT NULL,
  `content` text COLLATE utf8_bin NOT NULL,
  `start` datetime NOT NULL DEFAULT NOW(),
  `until` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


SELECT *
FROM announcements a 
ORDER BY id DESC;
