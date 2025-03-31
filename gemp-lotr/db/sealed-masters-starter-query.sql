

SELECT LP.player_name, T.contents, T.date_recorded
FROM (
	SELECT player_name
	FROM league_participation lp 
	WHERE league_type = '1741327981753'
) LP
LEFT JOIN (
	SELECT player, contents, date_recorded
	FROM transfer t
	WHERE  t.collection = 'Sealed Masters Gala 1: Big Baddies Sealed'
		AND t.contents LIKE '%Starter%'
		AND t.contents NOT LIKE '%1x(S)Big Baddies - Starter,%'
		AND t.direction = 'to'
) T
ON T.player = LP.player_name;


SELECT contents, COUNT(*)
FROM transfer t
WHERE  t.collection = 'Sealed Masters Gala 1: Big Baddies Sealed'
	AND t.contents LIKE '%Starter%'
	AND t.contents NOT LIKE '%1x(S)Big Baddies - Starter,%'
	AND t.direction = 'to'
GROUP BY t.contents;

	

SELECT *
FROM league_participation lp 
WHERE league_type = '1741327981753'
ORDER BY id DESC;

SELECT * 
FROM league l 
ORDER BY 1 DESC;