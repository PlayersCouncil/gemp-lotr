-- SELECT *
-- FROM transfer
-- ORDER BY id DESC
-- 
-- SELECT *
-- FROM tournament t 
-- ORDER BY id DESC
-- 
-- 
-- SELECT * 
-- FROM tournament_match tm 
-- WHERE tournament_id = '2023-wc-pc-fotr-format-championship'
-- 
-- 
-- 
-- SELECT *
-- FROM tournament_player tp 
-- ORDER BY id DESC
-- 
-- 
-- SELECT 
-- 	*
-- 	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=', REPLACE(winner, '_', '%5F'), '$', win_recording_id) AS WinnerReplay
-- 	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=', REPLACE(loser, '_', '%5F'), '$', lose_recording_id) AS LoserReplay
-- FROM game_history gh 
-- WHERE tournament = '2023 Decipher Fellowship Block Format Championship'
-- 
-- 
-- 
-- SELECT *
-- FROM tournament_player tp 
-- ORDER BY id DESC



SET @player = 'Axter', @rank=0;

SELECT 
	 @player AS player
	,CONCAT('[url=https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=',
		CASE 
			WHEN winner = @player THEN CONCAT(REPLACE(winner, '_', '%5F'), '$', win_recording_id)  
			ELSE CONCAT(REPLACE(loser, '_', '%5F'), '$', lose_recording_id) 
		END, ']',  @rank:=@rank+1,'[/url] • ') AS URL
FROM game_history gh 
INNER JOIN player p 
	ON p.name = @player
INNER JOIN deck d 
	ON d.player_id = p.id 
	AND (d.name = winner_deck_name OR d.name = loser_deck_name OR d.name = 'denethor / tentacle V2') 
WHERE tournament = '2023 PC-Expanded Championship'
	AND start_date > '2023-07-20'
	AND (winner = @player OR loser = @player)
ORDER BY gh.id;




SELECT winner, loser, win_reason, lose_reason, start_date, end_date
	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=',REPLACE(winner, '_', '%5F'), '$', win_recording_id) AS winner_replay
	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=',REPLACE(loser, '_', '%5F'), '$', lose_recording_id) AS loser_replay
FROM game_history gh 
WHERE tournament = '2023 WC Group Stage'



