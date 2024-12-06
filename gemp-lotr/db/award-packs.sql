SELECT *
FROM player p
WHERE name = 'Neilpie'

SELECT *
FROM deck d 
INNER JOIN player p 
	ON p.id = d.player_id 
WHERE p.name = 'Neilpie'

SELECT *
FROM game_history gh 
WHERE winner = 'Neilpie' OR loser = 'Neilpie'



SELECT *
FROM deck d 
INNER JOIN player p 	
	ON p.id = d.player_id 
WHERE p.name IN ('Axter', 'balrog69', 'bign19', 'CoS', 'dstaley', 'enolen', 'fnlgroove', 'GeriGeli', 'Icarus', 'johnec', 'LukasSchor', 'MockingbirdME', 'neergreve', 'olga06', 'Pokec', 'Raelag', 'rbranco', 'Ringbearer', 'stephan77', 'talial', 'thedast7', 'Tonio', 'Yk1030')
AND contents LIKE '%1_382%'

SELECT *
FROM transfer t 
WHERE  collection LIKE '%1_382%'
ORDER BY id DESC


SELECT *
FROM collection c 
INNER JOIN collection_entries ce 
	ON ce.collection_id = c.id 
INNER JOIN player p 
	ON p.id = c.player_id 
WHERE #p.name = 'Chadwick537' AND 
	product IN('1_382', '1_382*') 
	AND p.name IN ('Axter', 'balrog69', 'bign19', 'CoS', 'dstaley', 'enolen', 'fnlgroove', 'GeriGeli', 'Icarus', 'johnec', 'LukasSchor', 'MockingbirdME', 'neergreve', 'olga06', 'Pokec', 'Raelag', 'rbranco', 'Ringbearer', 'stephan77', 'talial', 'thedast7', 'Tonio', 'Yk1030')
	AND ce.created_date <> ce.modified_date 
	AND (c.type <> 'trophy')
ORDER BY p.name, ce.modified_date, ce.created_date DESC

-- INSERT INTO gemp_db.collection_entries
-- (collection_id, quantity, product_type, product_variant, product, source, created_date, modified_date, notes)
-- VALUES
-- (38751, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (63570, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (22671, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (64330, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (167019, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (55829, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (167461, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (103237, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (65411, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (59343, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (62630, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (94793, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (58444, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (273741, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (59369, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision'),
-- (23215, 4, 'CARD', NULL, '1_386', 'Manual Administrator action', current_timestamp(), NULL, 'Fixing the 2023 WC Goblin Runner / FA Enquea collision')
-- ;


-- UPDATE collection_entries 
-- SET quantity = quantity - 4
-- WHERE product = '1_382'
-- 	AND collection_id IN (38751, 63570, 22671, 64330, 167019, 55829, 167461, 103237, 65411, 59343, 62630, 94793, 58444, 273741, 59369, 23215) 	


#SELECT *
#FROM collection_entries ce 
WHERE collection_id = 199929 AND product = '1_383'



