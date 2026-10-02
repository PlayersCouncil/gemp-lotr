package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.DBDefs;
import com.gempukku.lotro.common.DateUtils;
import com.gempukku.lotro.db.ScenarioDAO;
import com.gempukku.lotro.game.GameRecorder;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.logic.timing.GameResultListener;
import com.gempukku.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps the record of scenario games: a row for every game (who, which scenario, how it ended, how far the script
 * got, its replay and its journal) and each player's progress.
 *
 * The row is what tells a scenario's author that something is wrong with it once it is out in the world.  The
 * headless runner proves the scripted path; it cannot prove a path that depends on chance, on a bot, or on what a
 * player chooses to do.  A step that real games keep skipping or never reaching, or problems that keep being
 * written down, are the only sign of that.
 *
 * Nothing here is allowed to get in the way of the game.  If the database is unhappy (the tables have not been
 * created yet, say), the game is played anyway and the failure is logged.
 */
public class ScenarioRunLog {
    private static final Logger LOG = LogManager.getLogger(ScenarioRunLog.class);

    private final ScenarioDAO _dao;
    private final GameRecorder _recorder;

    public ScenarioRunLog(ScenarioDAO dao, GameRecorder recorder) {
        _dao = dao;
        _recorder = recorder;
        recorder.setScenarioRunLookup(this::startOfRunWithRecording);
    }

    private ZonedDateTime startOfRunWithRecording(String recordingId) {
        try {
            DBDefs.ScenarioRun run = _dao.getRunByRecording(recordingId);
            return run == null || run.started == null ? null : DateUtils.ParseDate(run.started);
        } catch (RuntimeException exp) {
            LOG.error("Could not look up the scenario run with recording " + recordingId, exp);
            return null;
        }
    }

    /**
     * Records that a scenario game has started, and sees to it that its end is recorded when it comes.
     * @param mediator The game, already started.
     * @param recording The game's replay in the making; null to keep none.
     */
    public void follow(Player player, LotroGameMediator mediator, GameRecorder.ScenarioRecording recording) {
        LiveScenario scenario = mediator.getScenario();
        ScenarioDefinition def = scenario.Definition();

        int runId;
        try {
            runId = _dao.startRun(player.getId(), def.id, def.version,
                    scenario.Table().AnswersFollowed() > 0 ? scenario.LastCheckpoint() : null);
        } catch (RuntimeException exp) {
            LOG.error("Could not record the start of scenario " + def.id + " for " + player.getName()
                    + ".  The game goes ahead; nothing about it will be recorded.", exp);
            return;
        }

        //The scenario settles how it ended either just before the game ends or just after, depending on which of
        // them ended the other.  The record is written when both have happened.
        //(The game does not count itself as finished until it has told everybody that it is, so that is kept track of
        // here instead of asked.)
        AtomicBoolean over = new AtomicBoolean();
        AtomicBoolean written = new AtomicBoolean();
        Runnable finish = () -> {
            if (scenario.Result() != null && over.get() && written.compareAndSet(false, true))
                finished(player, mediator, runId, recording);
        };
        scenario.WhenSettled(finish);
        mediator.addGameResultListener(new GameResultListener() {
            @Override
            public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserPlayerIdsWithReasons) {
                over.set(true);
                finish.run();
            }

            @Override
            public void gameCancelled() {
                over.set(true);
                finish.run();
            }
        });
        //A game that was already over by the time anybody was listening
        if (mediator.isFinished())
            over.set(true);
        finish.run();
    }

    private void finished(Player player, LotroGameMediator mediator, int runId, GameRecorder.ScenarioRecording recording) {
        LiveScenario scenario = mediator.getScenario();
        ScenarioDefinition def = scenario.Definition();
        try {
            DBDefs.ScenarioRun run = new DBDefs.ScenarioRun();
            run.id = runId;
            run.result = scenario.Result().name().toLowerCase();

            Map<String, String> steps = new LinkedHashMap<>();
            Integer lastStep = null;
            for (ScenarioDefinition.Step step : def.steps) {
                ScenarioDirector.StepStatus status = scenario.Session().Director().statuses().get(step.label());
                steps.put(step.label(), status.name().toLowerCase());
                if (status == ScenarioDirector.StepStatus.MATCHED)
                    lastStep = step.number;
                //In a game that got all the way to the end, every step should have had its turn
                if (scenario.Result() == LiveScenario.Result.COMPLETE && status != ScenarioDirector.StepStatus.MATCHED)
                    LOG.warn("Scenario " + def.id + " v" + def.version + ": " + step.describe() + " was "
                            + (status == ScenarioDirector.StepStatus.SKIPPED ? "skipped" : "never reached")
                            + " in a completed game (run " + runId + ", player " + player.getName() + ")");
            }
            run.last_step = lastStep;
            run.step_report = JsonUtils.Serialize(steps);

            List<String> problems = scenario.Problems();
            run.problems = problems.isEmpty() ? null : String.join("\n", problems);
            run.journal = scenario.Table().Journal().write();

            if (recording != null) {
                run.recording_id = recording.save();
                if (run.recording_id != null) {
                    String replay = GameRecorder.replayUrl(player.getName(), run.recording_id);
                    mediator.sendMessageToPlayers("Thank you for playing!  Please report any bugs or other feedback <a href= '"
                            + GameRecorder.BUG_REPORT_FORM + replay + "' target='_blank'>using this form.</a>"
                            + "<br><br>The above link will fill in the replay for you automatically.");
                }
            }
            _dao.finishRun(run);
        } catch (RuntimeException exp) {
            LOG.error("Could not record the end of scenario run " + runId + " (" + def.id + ", " + player.getName() + ")", exp);
        }
    }

    /**
     * @return A player's progress, by scenario id.  Empty if they have none, or if it could not be read.
     */
    public Map<String, DBDefs.ScenarioProgress> progressOf(Player player) {
        Map<String, DBDefs.ScenarioProgress> result = new HashMap<>();
        try {
            for (DBDefs.ScenarioProgress progress : _dao.getProgress(player.getId()))
                result.put(progress.scenario_id, progress);
        } catch (RuntimeException exp) {
            LOG.error("Could not read the scenario progress of " + player.getName(), exp);
        }
        return result;
    }
}
