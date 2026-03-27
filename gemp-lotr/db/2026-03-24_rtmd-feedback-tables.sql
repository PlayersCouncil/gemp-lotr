-- RTMD Community Feedback tables: pairwise comparison voting and idea submissions.

CREATE TABLE rtmd_comparison_vote (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  blueprint_a VARCHAR(20) NOT NULL,
  blueprint_b VARCHAR(20) NOT NULL,
  winner VARCHAR(20) NOT NULL,
  ip_address VARCHAR(45) NOT NULL,
  voted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_pair (blueprint_a, blueprint_b),
  KEY idx_ip (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE rtmd_idea_submission (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  idea_text VARCHAR(250) NOT NULL,
  ip_address VARCHAR(45) NOT NULL,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  upvotes INT NOT NULL DEFAULT 0,
  downvotes INT NOT NULL DEFAULT 0,
  KEY idx_ip (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE rtmd_idea_vote (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  submission_id INT NOT NULL,
  ip_address VARCHAR(45) NOT NULL,
  vote TINYINT NOT NULL,
  voted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_submission_ip (submission_id, ip_address),
  FOREIGN KEY (submission_id) REFERENCES rtmd_idea_submission(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
