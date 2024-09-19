

SELECT *
FROM scheduled_tournament st 
ORDER BY ID desc

UPDATE scheduled_tournament 
SET start_date = '2023-10-14 20:35:00'#playoff = 'wc-swiss' #start_date = '2023-10-14 20:30:00', started = false
WHERE id IN (17)

INSERT INTO gemp_db.scheduled_tournament
(tournament_id, name, format, start_date, cost, playoff, tiebreaker, prizes, minimum_players, manual_kickoff, started)
VALUES('2024-wc-champ-pc-expanded', '2024 PC-Expanded Championship', 'pc_expanded', '2024-08-24 17:00:00', 0, 'swiss', 'owr', 'daily', 2, true, false);
VALUES('2023-wc-group-stage-pc-movie', '2023 WC PC-Movie Group Stage', 'pc_movie', '2023-10-14 17:00:00', 0, 'swiss', 'owr', 'daily', 2, true, false);

DELETE 
FROM scheduled_tournament st 
WHERE id = 18



SELECT *
FROM player p 
WHERE name IN ('ketura', 'Tunadan', 'Ringbearer', 'johnec', 'stephan77', 'GeriGeli', 'Tonio', 'thedast7', 'Pokec', 'Beezey', 'balrog69', 'sempolPL', 'neergreve', 'dstaley', 'thefaker', 'Chadwick537', 'scyld', 'bign19', 'Pizdec', 'Axter', 'LukasSchor', 'talial', 'rbranco', 'Raelag', 'olga06', 'Yk1030', 'enolen', 'Aaron_Brutcher', 'MockingbirdME', 'basmelis', 'fnlgroove', 'Icarus')

UPDATE player 
SET type = 'upca'
WHERE id = 31537


SELECT *
FROM deck d 
where player_id = 20214
AND name LIKE 'Nine Walkers%'

#Nine Walkers fotr pc


SELECT *
FROM league l 
ORDER BY id DESC

SELECT *
FROM league_participation lp 
WHERE lp.league_type = '1723768400434'
ORDER BY id DESC

SELECT *
FROM league_match lm
-- LEFT JOIN league_participation lp 
--  	ON lp.player_name = lm.winner AND lp.league_type = lm.league_type 
-- LEFT JOIN player p
-- 	ON p.name = lm.loser
-- WHERE lm.league_type = '1723768400434'
ORDER BY ID DESC;


SELECT *
FROM league l 
WHERE code = 1723768400434


SELECT *
FROM league_participation lp 
INNER JOIN league l
	ON l.code = lp.league_type 
WHERE player_name = 'fnlgroove'
ORDER BY lp.id DESC


SELECT *
FROM game_history gh 
WHERE tournament = '2024 Decipher Movie Championship'
	-- AND (winner = 'GeriGeli' OR loser = 'GeriGeli')
ORDER BY id DESC;

SELECT *
FROM game_history gh 
WHERE win_recording_id = '3b355remokn628wz'
ORDER BY id DESC;

-- DELETE
-- FROM league_match
-- WHERE id IN (220214, 220213)

SELECT *
FROM league_match lm
ORDER BY id DESC

SELECT *
FROM player p
WHERE name IN ('sandro96', 'sempolPL', 'basmelis', 'liboras', 'dstaley', 'Joebers', 'MaChal', 'Chadwick537', 'WeakSauce', 'balrog69', 'Kainbr')

INSERT INTO gemp_db.league_match
(league_type, season_type, winner, loser)
VALUES
('1723768400434', 'Serie 1', 'Everett', 'doly96'),
('1723768400434', 'Serie 1', 'doly96', 'Everett'),
('1723768400434', 'Serie 1', 'Dbix', 'Cwianacus'),
('1723768400434', 'Serie 1', 'Cwianacus', 'Dbix'),
('1723768400434', 'Serie 1', 'bign19', 'cmndante89'),
('1723768400434', 'Serie 1', 'cmndante89', 'bign19'),
('1723768400434', 'Serie 1', 'Axter', 'LukasSchor'),
('1723768400434', 'Serie 1', 'LukasSchor', 'Axter'),
('1723768400434', 'Serie 1', 'domgam', 'fnlgroove'),
('1723768400434', 'Serie 1', 'fnlgroove', 'domgam'),
('1723768400434', 'Serie 1', 'Amser', 'Quiniscal'),
('1723768400434', 'Serie 1', 'Quiniscal', 'Amser'),
('1723768400434', 'Serie 1', 'shuler', 'Wend'),
('1723768400434', 'Serie 1', 'Wend', 'shuler'),
('1723768400434', 'Serie 1', 'tony784', 'dmaz'),
('1723768400434', 'Serie 1', 'dmaz', 'tony784'),
('1723768400434', 'Serie 1', 'thefaker', 'Pizdec'),
('1723768400434', 'Serie 1', 'Pizdec', 'thefaker'),
('1723768400434', 'Serie 1', 'Sickofpalantirs', 'macheteman'),
('1723768400434', 'Serie 1', 'macheteman', 'Sickofpalantirs'),
('1723768400434', 'Serie 1', 'dstaley', 'balrog69'),
('1723768400434', 'Serie 1', 'balrog69', 'dstaley'),
('1723768400434', 'Serie 1', 'sempolPL', 'Modrzew'),
('1723768400434', 'Serie 1', 'Modrzew', 'sempolPL'),
('1723768400434', 'Serie 1', 'hrothlackin', 'Joebers'),
('1723768400434', 'Serie 1', 'Joebers', 'hrothlackin'),
('1723768400434', 'Serie 1', 'tristelune', 'Tunadan'),
('1723768400434', 'Serie 1', 'Tunadan', 'tristelune'),
('1723768400434', 'Serie 1', 'miortolan', 'bebpc'),
('1723768400434', 'Serie 1', 'bebpc', 'miortolan'),
('1723768400434', 'Serie 1', 'Cleston', 'rbranco'),
('1723768400434', 'Serie 1', 'rbranco', 'Cleston'),
('1723768400434', 'Serie 1', 'basmelis', 'olga06'),
('1723768400434', 'Serie 1', 'olga06', 'basmelis'),
('1723768400434', 'Serie 1', 'm_scarpato', 'Kainbr'),
('1723768400434', 'Serie 1', 'Kainbr', 'm_scarpato'),
('1723768400434', 'Serie 1', 'Thayli', 'Deets'),
('1723768400434', 'Serie 1', 'Deets', 'Thayli'),
('1723768400434', 'Serie 1', 'tauri_p-90', 'daisukeman'),
('1723768400434', 'Serie 1', 'daisukeman', 'tauri_p-90'),


SELECT class, count(*)
FROM league l
group by class

UPDATE league 
SET parameters = '{"code":1723768400434,"collectionName":"Sealed Team Gala – Reflections Multipath","cost":0,"description":"This is a team event that is only open to players who have registered on the sign-up sheet: <a href=\\\"https://docs.google.com/spreadsheets/d/1uPqJvqlNXRIr62amSaBb89l-elXlllOJJE27y477H8M/edit?usp=sharing\\\">here</a>. <br><br>Each player will be given the following sealed items: <br>12 Reflections Boosters,<br>2 Shotgun Enquea,<br>1 Choice of Starter Deck from sets 1-8<br><br>Once players have opened their Reflections boosters, they will need to consult with their teammate on which starter deck they want to use. Teammates <b>MUST</b> choose different starter decks.<br><br>The league uses the multipath site-path, so Ring-bearer skirmishes <b> CANNOT</b> be cancelled. The total number of games is 10; each team will have 2 fake games entered into the database (a win and a loss for each) to prevent you from accidentally facing your teammate.<br><br> Have fun everyone!","inviteOnly":true,"maxRepeatMatches":1,"name":"Sealed Team Gala – Reflections Multipath","series":[{"duration":20,"format":"ref_sealed","matches":12}],"start":"2024-08-16 00:00:00"}'
WHERE id = 728

SELECT *
FROM game_history gh 
-- WHERE format_name = 'Movie Block (PC)'
-- WHERE 	
-- 	win_recording_id IN ('u11taxzjzjwpsn3n', 'yp820x67i18y9k3w', 'a6qxc3mlj1l3g028', 'e59555pgksduaoqi')
ORDER BY id DESC


UPDATE game_history 
SET tournament = '2024 PC-Movie Championship'
WHERE win_recording_id IN ('u11taxzjzjwpsn3n', 'yp820x67i18y9k3w', 'a6qxc3mlj1l3g028', 'e59555pgksduaoqi')


INSERT INTO gemp_db.tournament_match
(tournament_id, round, player_one, player_two, winner)
VALUES('2024-wc-champ-decipher-movie', 1, 'GeriGeli', 'bye', NULL);

SELECT *
FROM tournament_match tm 
ORDER BY id DESC



UPDATE tournament_match
SET winner = 'Foehammer80'
WHERE id = 14905

INSERT INTO gemp_db.tournament_match
(tournament_id, round, player_one, player_two, winner)
VALUES
('2024-wc-champ-pc-movie', 2, 'Axter', 'Rutil', 'Axter'),
('2024-wc-champ-pc-movie', 2, 'Mock', 'paunovic-', 'Mock'),
('2024-wc-champ-pc-movie', 2, 'bign19', 'CrisSilva', 'bign19'),
('2024-wc-champ-pc-movie', 2, 'MaChal', 'GeriGeli', 'GeriGeli'),
('2024-wc-champ-pc-movie', 2, 'talial', 'neergreve', 'neergreve'),
('2024-wc-champ-pc-movie', 2, 'kylejwx93', 'bye', NULL),
('2024-wc-champ-pc-movie', 3, 'Axter', 'Mock', 'Axter'),
('2024-wc-champ-pc-movie', 3, 'bign19', 'bye', NULL),
('2024-wc-champ-pc-movie', 3, 'CrisSilva', 'kylejwx93', 'CrisSilva'),
('2024-wc-champ-pc-movie', 3, 'neergreve', 'GeriGeli', 'GeriGeli'),
('2024-wc-champ-pc-movie', 3, 'MaChal', 'talial', 'MaChal'),
('2024-wc-champ-pc-movie', 4, 'bign19', 'Axter', 'Axter'),
('2024-wc-champ-pc-movie', 4, 'GeriGeli', 'Mock', 'Mock'),
('2024-wc-champ-pc-movie', 4, 'CrisSilva', 'talial', 'talial'),
('2024-wc-champ-pc-movie', 4, 'neergreve', 'kylejwx93', 'kylejwx93'),

-- 
-- DELETE 
-- FROM tournament_match
-- WHERE id >= 14908


SELECT 
	 gh.winner 
	,gh.loser
	,'' AS round
	,gh.win_reason 
	,gh.lose_reason 
	,gh.start_date AS `Start`
	,gh.end_date AS `End`
	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=',REPLACE(winner, '_', '%5F'), '$', win_recording_id) AS winner_replay
	,CONCAT('https://play.lotrtcgpc.net/gemp-lotr/game.html?replayId=',REPLACE(loser, '_', '%5F'), '$', lose_recording_id) AS loser_replay
FROM game_history gh 
WHERE tournament = '2024 PC-FOTR Championship'
	#AND (winner = 'Chadwick537' OR loser = 'Chadwick537')


SELECT *
FROM game_history gh 
WHERE gh.tournament = '2024 PC-Expanded Championship'
ORDER BY id DESC


SELECT *
FROM scheduled_tournament
ORDER BY ID DESC


SELECT *
FROM tournament t 
WHERE tournament_id = '2024-wc-champ-pc-expanded'
ORDER BY id DESC


UPDATE tournament 
SET stage = 'Playing Games'
WHERE id = 1435

UPDATE tournament 
SET round = '4'
WHERE id = 1387


UPDATE tournament 
SET collection = 'default'
WHERE id IN (1411, 1412, 1413, 1414, 1415)


SELECT *
FROM tournament_player tp 
WHERE tournament_id = '2024-wc-champ-pc-expanded'
ORDER BY ID DESC

-- DELETE
-- FROM tournament_match
-- WHERE id IN (14315)

UPDATE tournament_player 
SET dropped = 1
WHERE id = 11304

SELECT *
FROM tournament_match tm 
WHERE tournament_id = '2024-wc-champ-pc-expanded'
ORDER BY id DESC


SELECT *
FROM deck d
INNER JOIN player p
	ON p.id = d.player_id 
WHERE p.name = 'valentint'


INSERT INTO gemp_db.tournament_player
(tournament_id, player, deck_name, deck, dropped)
VALUES('2024-wc-champ-decipher-movie', 'Robert1712', 'Rohan 2023', '4_301|4_1|7_330,7_337,8_118,7_345,7_348,8_120,7_359,7_360,8_117|1_50,1_50,1_89,6_95,4_267,10_72,7_321,7_321,7_321,1_311,7_222,7_239,10_21,5_25,10_23,10_23,10_23,4_219,4_219,9_48,9_48,9_48,1_231,1_231,8_30,8_30,4_263,4_268,4_274,7_242,7_242,4_288,5_88,5_88,5_89,5_89,7_253,7_230,5_116,7_53,7_53,7_53,7_53,5_30,5_30,5_30,4_289,4_289,4_289,4_289,5_22,5_22,5_22,7_57,10_20,10_20,8_22,8_22,8_24,8_24,8_24,9_29,9_29,6_46,6_46,6_46,10_24,5_80,5_80,4_276,5_94,5_94,5_94,5_94,6_97,10_116', '0');

UPDATE gemp_db.tournament_player
SET deck_name= '(MOVIE-PC) FrodoDw- Gollum', deck='2_102|7_2|7_336,7_339,8_118,10_119,7_349,8_120,7_359,7_360,7_329|9_28,10_21,5_25,10_23,10_23,10_23,10_23,9_48,9_48,1_231,1_231,8_30,8_30,8_30,10_19,7_53,7_53,7_53,7_53,7_74,5_30,5_30,5_22,7_57,7_57,7_57,10_20,8_22,8_22,8_24,8_24,8_24,8_24,10_22,10_22,6_46,6_46,6_46,6_46,10_24,10_24,10_116,53_108,1_296,52_108,2_105,1_308,1_317,1_89,1_3,1_3,1_3,1_3,8_3,9_3,9_3,9_3,9_3,51_9,51_9,51_9,1_8,2_5,2_5,1_15,8_5,10_1,2_10,2_10,2_10,1_21,9_6,9_7,9_8,1_27,2_10,51_9,2_5,8_3,2_4,2_4,9_5|100_4'
WHERE id = 11409



SELECT *
FROM tournament_match tm 
ORDER BY id DESC


SELECT *
FROM game_history gh 
-- WHERE tournament IN ('2024 PC-Movie Championship')
WHERE format_name = 'Movie Block (PC)'
ORDER BY id DESC


UPDATE tournament_match 
SET winner = 'dstaley'
WHERE id = 14638

UPDATE tournament_match 
SET winner = stephan77
WHERE id = 14637


UPDATE game_history 
SET winner = 'dstaley', winnerId = 33114, loser = 'johnec', loserId = 35804
WHERE id = 1214409

UPDATE game_history 
SET winner = 'stephan77', winnerId = 30969, loser = 'balrog69', loserId = 35908
WHERE id = 1214410



SELECT FROM_UNIXTIME('1695509366664')

SELECT *, FROM_UNIXTIME(floor(transfer_date / 1000)) AS transfer_time
FROM transfer t 
WHERE player = 'Aaron_Brutcher'
	AND collection LIKE '%2_125%'
ORDER BY id DESC

SELECT *
FROM collection c 
INNER JOIN player p 
	ON p.id = c.player_id 
INNER JOIN collection_entries ce 
	ON ce.collection_id = c.id 
WHERE p.name = 'Aaron_Brutcher'
	AND c.id IN (199929, 202698)
	AND ce.product = '2_125'
ORDER BY IFNULL(ce.modified_date, ce.created_date)


UPDATE collection_entries 
SET quantity = 4
WHERE collection_id = 199929 AND product = '2_125'



SELECT c.*
FROM collection c 
INNER JOIN player p 
	ON p.id = c.player_id 
WHERE p.name = 'Aaron_Brutcher'

