

SELECT *
FROM deck
WHERE contents LIKE '%100\_2%' OR contents LIKE '%100\_3%' OR contents LIKE '%100\_4%';

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '100_2', '102_74');

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '100_3', '102_75');

UPDATE deck 
SET contents = REGEXP_REPLACE(contents, '100_4', '102_76');

SELECT *
FROM deck
WHERE contents LIKE '%102\_74%' OR contents LIKE '%102\_75%' OR contents LIKE '%102\_76%';