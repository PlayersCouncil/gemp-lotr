

SELECT *
FROM league l 
ORDER BY ID DESC




ALTER TABLE gemp_db.league  
ADD COLUMN start_date DATE NOT NULL DEFAULT now() AFTER start;

ALTER TABLE gemp_db.league  
ADD COLUMN end_date DATE NOT NULL DEFAULT now() AFTER end;

ALTER TABLE gemp_db.league  
ADD COLUMN code BIGINT UNSIGNED NOT NULL DEFAULT 0 AFTER type;

ALTER TABLE gemp_db.league  
MODIFY COLUMN parameters VARCHAR(5000);

UPDATE gemp_db.league  
SET start_date = date_format(`start`,'%Y-%m-%d'), end_date = date_format(`end`,'%Y-%m-%d');

UPDATE gemp_db.league  
SET class = 'CONSTRUCTED'
WHERE class LIKE '%Constructed%';

UPDATE gemp_db.league  
SET class = 'SEALED'
WHERE class LIKE '%Sealed%';

UPDATE gemp_db.league  
SET class = 'SOLODRAFT'
WHERE class LIKE '%Draft%';

DELETE 
FROM gemp_db.league
WHERE id < 18;

UPDATE gemp_db.league  
SET code = CAST(`type` AS UNSIGNED);


ALTER TABLE gemp_db.league 
DROP COLUMN `start`;

ALTER TABLE gemp_db.league 
DROP COLUMN `end`;

ALTER TABLE gemp_db.league 
DROP COLUMN `type`;

ALTER TABLE gemp_db.league 
RENAME COLUMN class TO `type`;





-- For testing existing leagues with.
-- INSERT INTO gemp_db.league (name,code,`type`,parameters,`start_date`,`end_date`,status,cost) VALUES
-- 	 ('WC July Weekend League: PC-Movie','1721884965287','CONSTRUCTED','20240726,default,0.7,1,1,pc_movie,3,6','2024-07-26','2024-07-30',1,0),
-- 	 ('Sealed - Towers Standard','1722368865315','SEALED','ts_special_sealed,20240731,10,10,1722368865315,Sealed - Towers Standard','2024-07-31','2024-09-10',2,0),
-- 	 ('Draft - Towers Block','1722989352076','SOLODRAFT','ttt_draft,20240807,12,10,1722989352076,Draft - Towers Block','2024-08-07','2024-08-20',0,0),
-- 	('Constructed - TS Progressive','1721568048978','CONSTRUCTED','20240722,default,0.25,1,4,ttt_standard,7,5,bohd_standard,7,5,ts_no_fotr,7,5,towers_standard,7,5','2024-07-22','2024-08-20',0,0);

