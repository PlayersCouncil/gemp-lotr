-- tournament_player.deck_name was created as varchar(45) by update-tournament-tables.sql, while a deck's name
-- (deck.name) may be 100 characters.  A player signing up for a tournament with a longer deck name made the
-- tournament's start fail with "Data too long for column 'deck_name'".  The queue then tried again on every
-- server pass (about once a second), each time recording again the players that came before the failing one, and
-- while it kept failing no other queue started and no running tournament advanced.
--
-- Run with the server stopped if you can; it is also safe on a running one.  Every statement may be run again.

-- What the column is now (45 on an affected database, 100 on one built from database_script.sql).
SHOW COLUMNS FROM tournament_player LIKE 'deck_name';

-- 1. Let the column hold any deck name.
ALTER TABLE tournament_player MODIFY deck_name varchar(100) NOT NULL;

-- 2. Which tournaments have a player recorded more than once (the failed starts), and how badly.
SELECT tp.tournament_id, COUNT(*) AS player_rows, COUNT(DISTINCT tp.player) AS players,
       (SELECT COUNT(*) FROM tournament t WHERE t.tournament_id = tp.tournament_id) AS tournament_exists
FROM tournament_player tp
GROUP BY tp.tournament_id
HAVING COUNT(*) > COUNT(DISTINCT tp.player);

-- 3. A start that never succeeded has player rows and no tournament: remove all of them.  Only tournament ids
--    that have a player recorded more than once are touched, so nothing else without a tournament row is.
DELETE tp FROM tournament_player tp
JOIN (
    SELECT tournament_id
    FROM tournament_player
    GROUP BY tournament_id
    HAVING COUNT(*) > COUNT(DISTINCT player)
) failed ON failed.tournament_id = tp.tournament_id
LEFT JOIN tournament t ON t.tournament_id = tp.tournament_id
WHERE t.tournament_id IS NULL;

-- 4. A start that succeeded after failed attempts (the server was running when step 1 was applied) has the
--    tournament and each player several times: keep each player's newest row.  Only (tournament, player) pairs
--    recorded more than once are touched.
DELETE tp FROM tournament_player tp
JOIN (
    SELECT tournament_id, player, MAX(id) AS keep_id
    FROM tournament_player
    GROUP BY tournament_id, player
    HAVING COUNT(*) > 1
) dup ON dup.tournament_id = tp.tournament_id AND dup.player = tp.player
WHERE tp.id <> dup.keep_id;

-- Should now return nothing.
SELECT tournament_id, player, COUNT(*) AS player_rows
FROM tournament_player
GROUP BY tournament_id, player
HAVING COUNT(*) > 1;
