-- Puzzles: what a game of a puzzle was built with, what it scored, and whether it solved the puzzle in a way its
-- author did not list.  scenario_progress.best_score (already there) keeps each player's best score.

ALTER TABLE scenario_run
  ADD COLUMN picks TEXT NULL AFTER journal,                  -- JSON: slot id -> blueprint, the cards the solver picked
  ADD COLUMN score INT NULL AFTER picks,                     -- the puzzle's score as the game ended
  ADD COLUMN unlisted TINYINT(1) NOT NULL DEFAULT 0 AFTER score,  -- solved with picks none of the author's solutions has
  ADD KEY scenario_run_unlisted (scenario_id, unlisted);
