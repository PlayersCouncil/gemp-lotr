-- League scheduler: recurring league definitions that the server materialises into league rows.

CREATE TABLE league_schedule (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(45) NOT NULL,                    -- series name, e.g. "Sealed", "Draft", "Constructed - Fellowship Progressive"
  league_type VARCHAR(45) NOT NULL,             -- CONSTRUCTED / SEALED / SOLODRAFT / RTMD
  template TEXT NOT NULL,                       -- LeagueParams JSON shared by every event (name/start/code are filled in per event)
  events TEXT NOT NULL,                         -- JSON array: [{"name": "Fellowship Block", "params": {...LeagueParams overrides...}}, ...]
  name_pattern VARCHAR(255) NOT NULL DEFAULT '{series} - {event}',
  next_event_date DATE NOT NULL,                -- start date of the next league to be created
  next_event_index INT NOT NULL DEFAULT 0,      -- index into events of the next league to be created
  interval_months DECIMAL(6,3) NOT NULL DEFAULT 1.000,  -- whole part in calendar months; fraction in weeks of a 4-week month (0.25 = 1 week)
  lead_days INT NOT NULL DEFAULT 7,             -- create the league this many days before its start date
  active BIT(1) NOT NULL DEFAULT b'1',
  last_created_league_id INT NULL,
  last_run DATETIME NULL,
  last_error VARCHAR(1000) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
