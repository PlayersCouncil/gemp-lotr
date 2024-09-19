

-- SELECT *
-- FROM game_history gh 
-- WHERE tournament LIKE '%Yuletide%'
-- AND (winner = 'm_scarpato' OR loser = 'm_scarpato')
-- 

# Get IDs from the appropriate yuletide leagues:

SELECT *
FROM league l 
WHERE name LIKE '%Yuletide%'
ORDER BY id DESC;


# Stick those IDs in both of these clauses: 

SELECT player, count(*) AS matches
FROM (
	SELECT winner AS player,
		CONCAT('[url=https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=', CONCAT(REPLACE(winner, '_', '%5F'), '$', win_recording_id), ']') AS ReplayURL
	FROM league_match w 
	WHERE w.league_type IN ('1702871717631', '1702871696832', '1702871668557')	
	
	UNION ALL
	
	SELECT loser AS player,
		CONCAT('[url=https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=', CONCAT(REPLACE(loser, '_', '%5F'), '$', lose_recording_id), ']') AS ReplayURL
	FROM league_match l
	WHERE l.league_type IN ('1702871717631', '1702871696832', '1702871668557')	
) players
GROUP BY players.player
ORDER BY COUNT(*) DESC;













