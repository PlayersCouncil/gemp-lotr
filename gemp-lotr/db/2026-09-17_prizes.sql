-- Configurable event prizes: promised prizes handed out as set-404 placeholder cards until an admin resolves them,
-- and a log of every prize tier awarded (which doubles as the "never award the same tier twice" record).

CREATE TABLE prize_placeholder (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  label VARCHAR(255) NOT NULL,                  -- what was promised, e.g. "2026 WC Champion promo"
  count INT NOT NULL DEFAULT 1,                 -- copies of the placeholder each winner received
  event_kind VARCHAR(20) NOT NULL,              -- league | tournament | campaign | manual
  event_id VARCHAR(45) NULL,                    -- league code / tournament id / campaign tag; NULL for manual
  event_name VARCHAR(255) NULL,
  tier_index INT NULL,                          -- index of the tier within the event definition; NULL for campaign / manual
  created DATETIME NOT NULL,
  created_by VARCHAR(45) NULL,                  -- admin, or NULL when awarded automatically at event end
  resolved_blueprint VARCHAR(45) NULL,          -- the real card every holder's placeholder was swapped for
  resolved_on DATETIME NULL,
  resolved_by VARCHAR(45) NULL,
  notes VARCHAR(1000) NULL,
  INDEX prize_placeholder_event (event_kind, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE prize_award (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  event_kind VARCHAR(20) NOT NULL,              -- league | tournament | campaign
  event_id VARCHAR(45) NOT NULL,                -- league code / tournament id / campaign tag
  event_name VARCHAR(255) NULL,
  tier_index INT NULL,
  tier_label VARCHAR(255) NULL,                 -- for campaign awards this (with event_id) is the dedup key
  player VARCHAR(45) NOT NULL,
  items TEXT NOT NULL,                          -- one "<count>x <blueprint>" per line, as awarded (placeholders included)
  awarded_on DATETIME NOT NULL,
  INDEX prize_award_event (event_kind, event_id, player)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
