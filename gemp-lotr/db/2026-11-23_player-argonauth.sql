-- Which Gemp accounts belong to which PC account (PC-ACCOUNT.md). Argonauth is the authority; this is Gemp's copy,
-- brought up to date at every PC sign-in. Empty until someone signs in with a PC account, and never read in the
-- default (test) mode, so it is safe to create ahead of switching argonauth.mode to optional.

CREATE TABLE player_argonauth (
  player_id INT NOT NULL PRIMARY KEY,           -- player.id: an account has at most one PC account
  argonauth_sub CHAR(36) NOT NULL,              -- the PC account's permanent id at Argonauth; never its username
  proof VARCHAR(16) NOT NULL,                   -- password | session | created | admin, as Argonauth has it
  linked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- when Gemp first heard of this link
  INDEX player_argonauth_sub (argonauth_sub),
  CONSTRAINT fk_player_argonauth_player_id FOREIGN KEY (player_id) REFERENCES player (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
