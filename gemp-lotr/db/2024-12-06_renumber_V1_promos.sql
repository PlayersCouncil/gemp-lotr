
SELECT *
FROM transfer t
WHERE contents RLIKE('(101_65|101_66|101_67)')
ORDER BY id DESC;

SELECT *
FROM collection_entries ce
WHERE product IN ('101_65', '101_66', '101_67');

SELECT *
FROM deck d
WHERE contents RLIKE('(101_65|101_66|101_67)');


-- SELECT *
-- FROM collection c 
-- INNER JOIN player p
-- 	ON p.id = c.player_id 
-- WHERE c.id IN (59510, 64642, 64643)

UPDATE transfer 
SET contents = REGEXP_REPLACE(contents, '101_65', '101_101')
WHERE contents RLIKE('101_65');

UPDATE transfer 
SET contents = REGEXP_REPLACE(contents, '101_66', '101_102')
WHERE contents RLIKE('101_66');

UPDATE transfer 
SET contents = REGEXP_REPLACE(contents, '101_67', '101_103')
WHERE contents RLIKE('101_67');


UPDATE collection_entries 
SET product = REGEXP_REPLACE(product, '101_65', '101_101')
WHERE product RLIKE('101_65');

UPDATE collection_entries 
SET product = REGEXP_REPLACE(product, '101_66', '101_102')
WHERE product RLIKE('101_66');

UPDATE collection_entries 
SET product = REGEXP_REPLACE(product, '101_67', '101_103')
WHERE product RLIKE('101_67');


UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '101_65', '101_101')
WHERE contents RLIKE('101_65');

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '101_66', '101_102')
WHERE contents RLIKE('101_66');

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '101_67', '101_103')
WHERE contents RLIKE('101_67');




SELECT *
FROM transfer t
WHERE contents RLIKE('(101_101|101_102|101_103)')
ORDER BY id DESC;

SELECT *
FROM collection_entries ce
WHERE product IN ('101_101', '101_102', '101_103');

SELECT *
FROM deck d
WHERE contents RLIKE('(101_101|101_102|101_103)')
ORDER BY id DESC;
