
SET @player = 'Temaraan';

SELECT *
FROM player p
WHERE name = @player;

SELECT *
FROM deck d
INNER JOIN player p	
	ON p.id = d.player_id 
WHERE p.name = @player;

SELECT *
FROM game_history gh 
WHERE winner = @player OR loser = @player;