



SELECT *
FROM deck d 
WHERE contents RLIKE '(7|8)\\d_'
	
	
SELECT REGEXP_REPLACE(REGEXP_REPLACE('7_94,7_94,87_13,81_30,72_32,51_80,1_83,71_240,71_240,71_240,71_240,72_32', '7(\\d)_', '5\\1_'), '8(\\d)_', '6\\1_')

SELECT REGEXP_REPLACE('51_404,7_94,87_13,81_30,72_32,51_80,1_83,71_240,71_240,71_240,71_240,72_32,51_402,51_403', '51_40\\d', '51_70')

SELECT REGEXP_REPLACE('51_404,7_94,87_13,81_30,72_32,51_80,1_83,71_240,71_240,71_240,71_240,72_32,51_402,51_403,51_401', '53_401', '53_13')



UPDATE deck 
SET contents = REGEXP_REPLACE(REGEXP_REPLACE(contents, '7(\\d)_', '5\\1_'), '8(\\d)_', '6\\1_')
WHERE contents RLIKE '(7|8)_'

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '51_40\\d', '51_70')
WHERE contents RLIKE '51_40\\d'

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '53_401', '53_13')
WHERE contents RLIKE '53_401'

UPDATE deck
SET contents = CONCAT(contents,'|100_4')
WHERE target_format = 'Movie Block (PC)'

UPDATE deck
SET contents = CONCAT(contents,'|100_4')
WHERE target_format = 'PLAYTEST - Movie Block (PC)'
	AND contents RLIKE '(7_335|7_336|7_337)'
	
UPDATE deck
SET contents = CONCAT(contents,'|100_2')
WHERE target_format = 'PLAYTEST - Movie Block (PC)'
	AND contents RLIKE '(1_349)'
	
UPDATE deck
SET contents = CONCAT(contents,'|100_3')
WHERE target_format = 'PLAYTEST - Movie Block (PC)'
	AND contents RLIKE '(4_360|4_361|4_362|4_363|5_120|6_120)'

SELECT  *
FROM deck d
WHERE target_format = 'PLAYTEST - Movie Block (PC)'







