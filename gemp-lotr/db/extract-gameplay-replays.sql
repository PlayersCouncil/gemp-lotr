

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



SET @player = 'Chadwick537';

SELECT 
	 @player AS player
	,CASE WHEN winner = @player THEN loser ELSE winner END AS opponent
	,winner
	,win_reason
	,start_date
	,end_date
	,format_name
	,tournament
	,CASE WHEN winner = @player THEN winner_deck_name  ELSE loser_deck_name END AS deck_name
	,CONCAT('https://play.lotrtcgpc.net/share/deck?id=',
		CASE 
			WHEN winner = @player THEN TO_BASE64(CONCAT(winner, '|', winner_deck_name))
			ELSE TO_BASE64(CONCAT(loser, '|', loser_deck_name)) 
		END) AS DeckURL
	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=',
		CASE 
			WHEN winner = @player THEN CONCAT(REPLACE(winner, '_', '%5F'), '$', win_recording_id)  
			ELSE CONCAT(REPLACE(loser, '_', '%5F'), '$', lose_recording_id) 
		END) AS ReplayURL
	
FROM game_history gh 
WHERE (winner = @player OR loser = @player)
	AND (start_date > '2024-09-15')












