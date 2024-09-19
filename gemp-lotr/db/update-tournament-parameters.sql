SELECT *
FROM tournament t
ORDER BY id DESC;


DELETE 
FROM gemp_db.tournament
WHERE id < 19;

ALTER TABLE gemp_db.tournament 
ADD CONSTRAINT UQ_tournament_id UNIQUE (tournament_id);

ALTER TABLE gemp_db.tournament  
ADD COLUMN parameters VARCHAR(5000) NOT NULL DEFAULT '{}' AFTER start_date;

ALTER TABLE gemp_db.tournament 
DROP COLUMN `draft_type`;

ALTER TABLE gemp_db.tournament 
ADD COLUMN `type` VARCHAR(45) NOT NULL DEFAULT 'CONSTRUCTED' AFTER start_date;

UPDATE tournament 
SET parameters = CONCAT('{', '"tournament_id":"',tournament_id,
'", "name":"',name,
'", "format":"', `format`, 
'", "collection":"', collection, 
'", "pairing": "', pairing, 
'", "manual_kickoff":', CASE WHEN manual_kickoff = 0 THEN 'false' ELSE 'true' END, 
', "prizes":"', IFNULL(prizes, 'daily'), '"}');


ALTER TABLE gemp_db.tournament 
MODIFY COLUMN name VARCHAR(255) AFTER tournament_id;


ALTER TABLE gemp_db.tournament 
DROP COLUMN format;

ALTER TABLE gemp_db.tournament 
DROP COLUMN collection;

ALTER TABLE gemp_db.tournament 
DROP COLUMN pairing;

ALTER TABLE gemp_db.tournament 
DROP COLUMN manual_kickoff;

ALTER TABLE gemp_db.tournament 
DROP COLUMN prizes;

# DELETE FROM scheduled_tournament WHERE id < 20


SELECT *
FROM scheduled_tournament st 
ORDER BY id DESC;


ALTER TABLE gemp_db.scheduled_tournament 
ADD COLUMN `type` VARCHAR(45) NOT NULL DEFAULT 'CONSTRUCTED' AFTER start_date;

ALTER TABLE gemp_db.scheduled_tournament  
ADD COLUMN parameters VARCHAR(5000) NOT NULL DEFAULT '{}' AFTER `type`;


UPDATE scheduled_tournament 
SET parameters = CONCAT('{', '"tournament_id":"',tournament_id,
'", "name":"',name,
'", "format":"', `format`, 
'", "type":"CONSTRUCTED"', 
'", "pairing": "swiss"',
'", "manual_kickoff":', CASE WHEN manual_kickoff = 0 THEN 'false' ELSE 'true' END, 
'", "cost":"', cost, 
'", "tiebreaker":"', tiebreaker, 
'", "playoff":"', playoff, 
'", "minimum_players":"', minimum_players, 
', "prizes":"', IFNULL(prizes, 'daily'), '"}');

ALTER TABLE gemp_db.scheduled_tournament 
DROP COLUMN cost;

ALTER TABLE gemp_db.scheduled_tournament 
DROP COLUMN playoff;

ALTER TABLE gemp_db.scheduled_tournament 
DROP COLUMN tiebreaker;

ALTER TABLE gemp_db.scheduled_tournament 
DROP COLUMN prizes;

ALTER TABLE gemp_db.scheduled_tournament 
DROP COLUMN minimum_players;

ALTER TABLE gemp_db.scheduled_tournament 
DROP COLUMN manual_kickoff;
