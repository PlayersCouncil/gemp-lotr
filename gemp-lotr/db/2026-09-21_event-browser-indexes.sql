-- Indexes for the completed-event browser (GET /eventHistory).
--
-- The browser buckets one month at a time: tournaments by tournament.start_date (there is no finish column,
-- only stage = 'FINISHED'), leagues by league.end_date.  Neither date column was indexed.
--
-- tournament_player / tournament_match are hit by every detail expansion and by the browser's player counts;
-- neither had any index on tournament_id, so both were full scans.  Verified missing against database_script.sql
-- (tournament: PRIMARY + UQ_tournament_id only; league: PRIMARY only; the two child tables: PRIMARY only).

ALTER TABLE tournament        ADD INDEX tournament_stage_start (stage, start_date);
ALTER TABLE league            ADD INDEX league_end_date (end_date);
ALTER TABLE tournament_player ADD INDEX tournament_player_tid (tournament_id);
ALTER TABLE tournament_match  ADD INDEX tournament_match_tid (tournament_id);
