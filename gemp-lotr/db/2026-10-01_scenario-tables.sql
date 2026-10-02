-- Scenarios (tutorials, puzzles, sandboxes): a row for every game of one, and each player's progress through them.
-- Scenario games write no game_history row; a scenario game's replay is found through scenario_run.recording_id.

CREATE TABLE scenario_run (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  player_id INT NOT NULL,
  scenario_id VARCHAR(191) NOT NULL,            -- a canonical id, or <player name>/<id> for a player's own scenario
  version INT NOT NULL,                         -- the scenario file's version when the game was played
  started DATETIME NOT NULL,                    -- UTC
  ended DATETIME NULL,                          -- NULL while the game is going (or if the server stopped under it)
  result VARCHAR(16) NULL,                      -- complete / failed / off_script / abandoned / error
  resumed_at INT NULL,                          -- the checkpoint step the game started at; NULL if it started at the beginning
  last_step INT NULL,                           -- the last step of the script that applied
  step_report TEXT NULL,                        -- JSON: every step, by label -> matched / skipped / unreached
  problems TEXT NULL,                           -- one per line: where the script and the game disagreed
  recording_id VARCHAR(64) NULL,                -- the replay
  journal MEDIUMTEXT NULL,                      -- every answer and random outcome (GameJournal); re-runs the game exactly
  KEY scenario_run_player (player_id, scenario_id),
  KEY scenario_run_scenario (scenario_id, version),
  KEY scenario_run_recording (recording_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE scenario_progress (
  player_id INT NOT NULL,
  scenario_id VARCHAR(191) NOT NULL,
  version INT NOT NULL,                         -- the version last played; once completed, the version completed
  status VARCHAR(16) NOT NULL,                  -- started / complete
  attempts INT NOT NULL DEFAULT 0,              -- games started, including ones picked up at a checkpoint
  best_score INT NULL,                          -- for puzzles; not used yet
  first_completed DATETIME NULL,
  last_played DATETIME NOT NULL,
  PRIMARY KEY (player_id, scenario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
