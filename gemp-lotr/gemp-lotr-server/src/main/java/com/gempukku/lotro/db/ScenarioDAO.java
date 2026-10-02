package com.gempukku.lotro.db;

import com.gempukku.lotro.common.DBDefs;

import java.util.List;

/**
 * Scenario games (tutorials, puzzles, sandboxes): a row for every game of one (scenario_run), and each player's
 * progress through them (scenario_progress).  See db/2026-10-01_scenario-tables.sql.
 */
public interface ScenarioDAO {
    /**
     * Records that a player has started a game of a scenario, and counts it as an attempt in their progress.
     * @param resumedAt The checkpoint step the game starts at; null if it starts at the beginning.
     * @return The run's id.
     */
    int startRun(int playerId, String scenarioId, int version, Integer resumedAt);

    /**
     * Records how a game of a scenario ended.  If it was completed, the player's progress says so from now on.
     * @param run The row to write.  Its id says which run; started, player_id, scenario_id, version and resumed_at
     *            are left as they were when the run began.
     */
    void finishRun(DBDefs.ScenarioRun run);

    /** @return The run whose replay this is, without its journal; null if there is none. */
    DBDefs.ScenarioRun getRunByRecording(String recordingId);

    /** @return A player's progress in every scenario they have started. */
    List<DBDefs.ScenarioProgress> getProgress(int playerId);
}
