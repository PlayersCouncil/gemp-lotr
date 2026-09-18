-- Links leagues back to the league schedule that created them (NULL for leagues an admin created by hand).
-- Deliberately no foreign key: the base league table predates the schedule table and its engine is not guaranteed
-- to be InnoDB; DbLeagueScheduleDAO.deleteSchedule nulls the link itself.

ALTER TABLE league
  ADD COLUMN schedule_id INT NULL AFTER cost;
