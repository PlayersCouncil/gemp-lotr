package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.GameJournal;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioDefinition.Advance;
import com.gempukku.lotro.scenario.ScenarioDefinition.Allow;
import com.gempukku.lotro.scenario.ScenarioDefinition.Coverage;
import com.gempukku.lotro.scenario.ScenarioDefinition.EndResult;
import com.gempukku.lotro.scenario.ScenarioDefinition.Fallback;
import com.gempukku.lotro.scenario.ScenarioDefinition.Message;
import com.gempukku.lotro.scenario.ScenarioDefinition.OffScriptResult;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;
import com.gempukku.lotro.scenario.ScenarioDirector.RenderedMessage;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import com.gempukku.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A scenario with a person in one of its seats.
 *
 * The session rules on every decision; this acts on the rulings.  A ruling on the person's decision becomes a
 * "step event" on their screen: the step's messages, and which answers the step allows.  (If the step is a pause,
 * the person reads it and says so, and the decision is ruled on again for the step behind it.)  A ruling on the other
 * seat's decision becomes that seat's answer, given after a short pause so that the person can follow what happened
 * (and, if the step has something to say first, not until the person has read it).  When the script says the
 * scenario is over, this ends the game.
 *
 * Everything here runs on whichever thread is running the game, inside the game's lock, with one exception: the
 * opponent's answers are handed to the host from a timer, exactly as a bot's are.
 */
public class LiveScenario implements ScenarioSession.Listener {
    private static final Logger LOG = LogManager.getLogger(LiveScenario.class);

    /** The one account every scenario's opponent plays as.  What it is called on screen is up to the scenario. */
    public static final String OPPONENT_PLAYER = "~Scenario";

    /** The kinds of thing a scenario puts on the person's screen (see GameState.showScenarioEvent). */
    public static final String INFO = "info";
    public static final String STEP = "step";
    public static final String END = "end";

    public enum Result {
        /** The scenario ran to an end that counts as done. */
        COMPLETE,
        /** It ran to an end that does not: the script said so, or the person lost the game. */
        FAILED,
        /** The game went somewhere the script does not cover. */
        OFF_SCRIPT,
        /** The person left: conceded, or stopped answering. */
        ABANDONED,
        /** The game broke and was called off. */
        ERROR
    }

    /** The part of a live game that a scenario needs: a way to answer for the opponent. */
    public interface Host {
        /**
         * Answers a decision on the opponent's behalf.  Called from a timer thread, never from inside the game.
         * @param decision The decision the answer is for.  If the opponent is no longer being asked exactly this,
         *                 the answer is to be dropped.
         */
        void opponentAnswers(String player, AwaitingDecision decision, String answer);
    }

    /** Turns a message's text (Markdown, card links) into what the client displays. */
    public interface TextRenderer {
        String render(String text);
    }

    /**
     * Somebody writing the scenario while playing it (see ScenarioAuthoring).  A game with an author never ends for
     * leaving its script: where the script has nothing to say, the person plays on, and what they do can become
     * script.  The opponent's seat is theirs to answer as well, wherever the script does not answer for it.
     */
    public interface Author {
        /**
         * The opponent is being asked something the script gives no answer to.
         * @return True to leave the decision waiting for the author to answer (see Held and AuthorAnswers); false to
         * have the opponent do what it would do in a game with no author (its fallback).
         */
        boolean holds(Ruling ruling);

        /** A decision, of either seat's, has been answered.  Called inside the game's lock. */
        void answered(Ruling ruling, String answer);
    }

    private final ScenarioDefinition _def;
    private final LotroCardBlueprintLibrary _library;
    private final ScenarioTable _table;
    private final ScenarioSession _session;
    private final String _human;
    private final String _opponent;
    private final TextRenderer _renderer;
    private final ScheduledExecutorService _timer;
    private Host _host;

    /** The opponent's next answer, from when its decision arrives until the answer is given. */
    private Move _move;
    /** Set when the script has lost its place, to be dealt with as soon as the game stops working. */
    private String _offScript;
    /** Set once the game has left its script and been allowed to carry on: from then on it is an ordinary game. */
    private boolean _carryingOn;
    private boolean _botFallbackReported;
    private int _lastCheckpoint;
    private Integer _resumedAt;
    private boolean _begun;
    /** Where the scenario stands in its campaign, as the client is to be told; null if it is in none. */
    private Map<String, Object> _campaign;
    /** The decision whose answer takes the step now on the person's screen off it again; null if there is none. */
    private Object _stepShownFor;
    /** Stands in for "whatever the person answers next". */
    private static final Object ANY_ANSWER = new Object();
    /** A game that has gone wrong can go on going wrong; this many problems are kept and the rest only counted. */
    private static final int PROBLEM_LIMIT = 100;
    private int _problemsDropped;
    private Result _result;
    private final List<String> _problems = new ArrayList<>();
    private final List<Runnable> _whenSettled = new ArrayList<>();

    private Author _author;
    /** The opponent's decision that is waiting for the author to answer it; null if none is. */
    private Ruling _held;
    /** Set while a changed step is being shown afresh (see Reshow). */
    private boolean _reshowing;
    /** If set, the script plays itself up to this step before anybody is shown anything. */
    private boolean _draftTest;
    private Integer _runTo;
    /** In a block, how far into it the script plays itself: to one of its steps (from 1), or past so many decisions. */
    private int _runToSub;
    private int _runToTimes;
    private java.util.function.Function<Step, String> _runChoices;
    private String _runProblem;

    private static final class Move {
        final AwaitingDecision decision;
        final String answer;
        /** The step whose messages the person has to dismiss first; 0 if there is nothing to wait for. */
        final int awaiting;
        boolean released;

        Move(AwaitingDecision decision, String answer, int awaiting) {
            this.decision = decision;
            this.answer = answer;
            this.awaiting = awaiting;
        }
    }

    /**
     * @param humanPlayer The name of the person playing.
     * @param timer Runs the opponent's answers after their pause.
     */
    public LiveScenario(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, String humanPlayer,
                        TextRenderer renderer, ScheduledExecutorService timer) {
        this(def, library, format, humanPlayer, renderer, timer, null);
    }

    /**
     * @param earlier The journal of an earlier game of this scenario for this game to repeat before anybody
     *                connects, so that it starts where that journal ends; null to start at the beginning.  See
     *                CheckpointJournal for the usual source of one.
     */
    public LiveScenario(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, String humanPlayer,
                        TextRenderer renderer, ScheduledExecutorService timer, GameJournal earlier) {
        _def = def;
        _library = library;
        _human = humanPlayer;
        _opponent = OPPONENT_PLAYER;
        _renderer = renderer;
        _timer = timer;
        _table = new ScenarioTable(def, library, format, humanPlayer, OPPONENT_PLAYER);
        _session = new ScenarioSession(_table);
        _session.Listen(this);
        if (earlier != null)
            _table.Follow(earlier);
        _table.Prepare();
    }

    public ScenarioDefinition Definition() { return _def; }
    public ScenarioTable Table() { return _table; }
    public ScenarioSession Session() { return _session; }
    public String HumanPlayer() { return _human; }
    public String OpponentPlayer() { return _opponent; }

    /** How the scenario ended; null while it is still going. */
    public Result Result() { return _result; }

    /**
     * Everything that went wrong with the script during this run.  A scenario that passes the headless runner
     * produces none on its scripted path; anything here is either a path the runner did not take or a bug.
     */
    public List<String> Problems() {
        List<String> all = new ArrayList<>(_problems);
        if (_problemsDropped > 0)
            all.add("... and " + _problemsDropped + " more");
        all.addAll(_table.Problems());
        return all;
    }

    public void SetHost(Host host) {
        _host = host;
    }

    /**
     * Gives the game an author.  To be called before Begin.
     */
    public void SetAuthor(Author author) {
        _author = author;
    }

    /**
     * Has the script play itself, both seats, from the start up to a step: Begin leaves the game waiting on the
     * first decision that step applies to, with nothing of the way there shown to anybody.  If the script cannot get
     * that far (a decision it has no answer for, an answer the game will not take) the game is left where the script
     * stopped, and RunProblem says why.  To be called before Begin.
     * @param step The number of the step to stop at; anything past the last step runs the whole script.
     * @param choices Says which of a pause's choices to take; null (or a null answer) takes the first.
     */
    public void RunTo(int step, java.util.function.Function<Step, String> choices) {
        RunTo(step, 0, 0, choices);
    }

    /**
     * The same, to somewhere inside a block (where the step is one; otherwise the two say nothing).
     * @param sub One of the block's steps, counting from 1: the script stops at the first decision that goes to it
     *            (or where the block ends, if none does).  0 for none in particular.
     * @param times With no step named: how many decisions the block's steps are to have answered before the script
     *              stops.  0 stops at the block's first.
     */
    public void RunTo(int step, int sub, int times, java.util.function.Function<Step, String> choices) {
        _runTo = step;
        _runToSub = Math.max(0, sub);
        _runToTimes = Math.max(0, times);
        _runChoices = choices;
    }

    /** Whether a decision that goes to the step the script was sent to is where it was sent. */
    private boolean farEnough(Step step, ScenarioDirector director) {
        if (step.parent == null)
            return true;
        return _runToSub > 0 ? step.sub == _runToSub : director.answeredInBlock() >= _runToTimes;
    }

    /**
     * Says that this is a player's game of a draft somebody is writing: the person's screen is told, so that it can
     * show them the way back to writing it.  To be called before Begin.
     */
    public void SetDraftTest() {
        _draftTest = true;
    }

    /** Why the script could not play itself as far as it was asked to; null if it did (or was not asked). */
    public String RunProblem() { return _runProblem; }

    /** The opponent's decision that is waiting for the author; null if none is. */
    public Ruling Held() { return _held; }

    /**
     * The author answers the opponent's waiting decision.  Like any answer of the opponent's, it is handed to the
     * host, so this is to be called from outside the game's lock.
     */
    public void AuthorAnswers(String answer) {
        Ruling held = _held;
        if (held == null || _result != null)
            return;
        _host.opponentAnswers(_opponent, held.decision(), answer);
    }

    /**
     * Authoring: the steps in hand have been changed, and are put on the person's screen again as they now are.  To
     * be called inside the game's lock.
     */
    public void Reshow() {
        _reshowing = true;
        try {
            _session.Refresh();
        } finally {
            _reshowing = false;
        }
    }

    private void runScript() {
        ScenarioDirector director = _session.Director();
        for (int guard = 0; guard < 5000; guard++) {
            if (_table.GameIsFinished() || director.end() != null) {
                //Fair enough if the whole script was asked for.  Short of that, the script did not get where it was sent.
                Step reached = director.currentStep();
                if (_table.GameIsFinished())
                    _runProblem = "the game was over (" + (_human.equals(_table.game().getWinnerPlayerId()) ? "won" : "lost")
                            + " by its own rules) " + (reached == null ? "after the last step" : "before " + reached.describe());
                else if (reached != null && reached.number <= _runTo)
                    _runProblem = "the step before " + reached.describe() + " ended the scenario";
                return;
            }
            Ruling ruling = _session.Ruling(_human) != null ? _session.Ruling(_human) : _session.Ruling(_opponent);
            if (ruling == null)
                return;
            Step step = ruling.step();
            if (step != null && (step.number > _runTo || (step.number == _runTo && farEnough(step, director))))
                return;
            if (step == null && director.atFrontier())
                return;
            //(sent so far into a block, to a decision that none of the block's steps covers: that is where it is)
            Step at = director.currentStep();
            if (step == null && at != null && at.block && at.number == _runTo && _runToSub == 0 && _runToTimes > 0
                    && director.answeredInBlock() >= _runToTimes)
                return;
            if (ruling.problem() != null) {
                _runProblem = (step == null ? "" : step.describe() + ": ") + ruling.problem();
                return;
            }
            if (step != null && step.pause) {
                _session.Read(ruling.player(), step.number, _runChoices == null ? null : _runChoices.apply(step));
                continue;
            }
            String answer = ruling.answer();
            if (answer == null && step == null && !ruling.player().equals(_human) && _def.fallback != Fallback.FAIL)
                answer = AnswerCompiler.leastAnswer(ruling.decision());
            if (answer == null) {
                _runProblem = ruling.noAnswer() != null ? step.describe() + ": " + ruling.noAnswer()
                        : step != null ? step.describe() + " has no answer for the script to play"
                        : "no step covers " + director.describe(ruling.player(), ruling.decision());
                return;
            }
            try {
                _table.PlayerDecided(ruling.player(), answer);
            } catch (RuntimeException exp) {
                _runProblem = (step == null ? "" : step.describe() + ": ") + "the game would not take the answer \"" + answer + "\": " + exp;
                return;
            }
        }
        _runProblem = "the script went on for more than 5000 decisions";
    }

    /**
     * Says where the scenario stands in a campaign ({id, title, position, count, next}; see
     * ScenarioLibrary.campaignInfo), for the person's screen to offer the next one when this one is done.  To be
     * called before Begin.
     */
    public void SetCampaign(Map<String, Object> campaign) {
        _campaign = campaign;
    }

    /**
     * Has something done as soon as the scenario has settled how it ended.  That can be just before its game ends
     * (the scenario ended the game) or just after (the game ended the scenario).  It is done inside the game's lock.
     */
    public void WhenSettled(Runnable action) {
        _whenSettled.add(action);
    }

    private void settled(Result result) {
        _result = result;
        for (Runnable action : _whenSettled) {
            try {
                action.run();
            } catch (RuntimeException exp) {
                LOG.error("Scenario " + _def.id + ": something watching for its end failed", exp);
            }
        }
    }

    /**
     * Sets the game going.  To be called once, inside the game's lock, before anybody has connected.
     */
    public void Begin() {
        //Until the game has got to where the person comes in (the first decision, or the end of an earlier game it
        // is repeating), nothing is shown and the opponent does not act for itself
        _table.Begin();
        if (_runTo != null)
            runScript();
        _begun = true;
        if (_table.FollowedAnything() && _session.Director().currentStep() != null)
            _resumedAt = _session.Director().currentStep().number;
        _table.gameState().showScenarioEvent(_human, INFO, JsonUtils.SerializeWithNulls(infoEvent()));
        for (String player : List.of(_opponent, _human)) {
            Ruling ruling = _session.Ruling(player);
            if (ruling != null)
                act(ruling);
        }
        AfterActions();
    }

    /**
     * @return The number of the checkpoint step this game started at; null if it started at the beginning.
     */
    public Integer ResumedAt() { return _resumedAt; }

    /**
     * @return The journal to give a new game of this scenario so that it starts at the last checkpoint this game
     * reached; null if it has not reached one.
     */
    public GameJournal CheckpointJournal() {
        return _lastCheckpoint == 0 ? null : _table.Journal().upTo(stepMark(_lastCheckpoint));
    }

    /** The number of the last checkpoint step reached; 0 if none has been. */
    public int LastCheckpoint() { return _lastCheckpoint; }

    private static String stepMark(int stepNumber) {
        return "step:" + stepNumber;
    }

    @Override
    public void ruled(Ruling ruling) {
        //Each step's first decision is marked in the journal, so that a later game can be brought to that point
        //(a block is reached once, by whichever of its steps takes its first decision; a checkpoint is the block's)
        Step reached = ruling.step();
        if (reached != null && _session.Director().arrives(reached)) {
            _table.Journal().mark(stepMark(reached.number));
            if (reached.parent != null ? reached.parent.checkpoint : reached.checkpoint)
                _lastCheckpoint = reached.number;
        }
        if (_begun)
            act(ruling);
    }

    private void act(Ruling ruling) {
        //Once the scenario is over, or about to be ended (by its last step, or for leaving its script), nothing more
        // is shown or played: the game is ended as soon as it has stopped working
        if (_result != null || _session.Director().end() != null || (_offScript != null && !carriesOnOffScript()))
            return;
        Step step = ruling.step();
        if (ruling.problem() != null)
            problem((step == null ? "" : step.describe() + ": ") + ruling.problem());

        if (ruling.player().equals(_human)) {
            if (step != null) {
                showStep(stepEvent(ruling, false), ruling.decision());
            } else {
                clearStep();
                //(with an author at the table, this is not the end of anything: it is where the writing happens)
                if (_def.coverage == Coverage.STRICT && _author == null)
                    offScript("no step covers the player's decision: " + _session.Director().describe(ruling.player(), ruling.decision()));
            }
            return;
        }

        //Ruled on again (it can happen to any decision whose answer is rejected): the move already stands.  Unless
        // its step has been rewritten meanwhile, and what it says and does is being put up afresh.
        if (_move != null && _move.decision == ruling.decision()) {
            if (!_reshowing || _move.released)
                return;
            _move = null;
        }

        //(ruled on again, a decision that was left for the author may no longer be)
        if (_held != null && _held.decision() == ruling.decision())
            _held = null;
        String answer = ruling.answer();
        if (answer == null && _author != null && _author.holds(ruling)) {
            //Left for the author.  Anything the step has to say is shown, with nothing waiting on its being read.
            _held = ruling;
            if (step != null && !ruling.shown().messages().isEmpty())
                showStep(stepEvent(ruling, true), ruling.decision());
            return;
        }
        if (answer == null) {
            if (step == null && _def.fallback == Fallback.FAIL && _author != null) {
                //An author who is not answering for the opponent has it do the least it can, and is told that a
                // player's game would have stopped here
                problem("no step covers the opponent's decision (" + _session.Director().describe(ruling.player(), ruling.decision())
                        + "), and opponent.fallback is 'fail': a player's game leaves its script here.  The opponent has done as little as it can.");
            } else if (step == null && _def.fallback == Fallback.FAIL) {
                offScript("no step covers the opponent's decision: " + _session.Director().describe(ruling.player(), ruling.decision()));
                if (!carriesOnOffScript())
                    return;
            }
            if (step == null && _def.fallback == Fallback.BOT && !_carryingOn && !_botFallbackReported) {
                _botFallbackReported = true;
                problem("opponent.fallback is 'bot', which a live game cannot do yet; the opponent does as little as it can instead");
            }
            answer = AnswerCompiler.leastAnswer(ruling.decision());
        }

        boolean hasMessages = step != null && !ruling.shown().messages().isEmpty();
        if (hasMessages)
            showStep(stepEvent(ruling, true), ruling.decision());
        _move = new Move(ruling.decision(), answer, hasMessages ? step.number : 0);
        if (!hasMessages)
            release(_move);
    }

    private void showStep(Map<String, Object> event, Object shownFor) {
        show(STEP, event);
        _stepShownFor = shownFor;
    }

    private void clearStep() {
        _table.gameState().clearScenarioEvent(_human, STEP);
        _stepShownFor = null;
    }

    @Override
    public void answered(Ruling ruling, String answer) {
        if (_held != null && _held.decision() == ruling.decision())
            _held = null;
        //(the script playing itself to where its author wants to be is not the author doing anything)
        if (_author != null && _begun) {
            try {
                _author.answered(ruling, answer);
            } catch (RuntimeException exp) {
                LOG.error("Scenario " + _def.id + ": its author could not be told about an answer", exp);
                problem("an answer could not be written into the script: " + exp);
            }
        }
        //Only the answer the step on screen was waiting for takes it off the screen
        if (_stepShownFor == ruling.decision() || (_stepShownFor == ANY_ANSWER && ruling.player().equals(_human)))
            clearStep();
        if (_move != null && _move.decision == ruling.decision()) {
            if (!_move.answer.equals(answer))
                problem((ruling.step() == null ? "" : ruling.step().describe() + ": ") + "the game would not take the opponent's answer \""
                        + _move.answer + "\" and was given \"" + answer + "\" instead");
            _move = null;
        }
    }

    /**
     * The person has read a step that waits to be read: the messages of an opponent's step, so that the opponent may
     * go ahead; or a pause over one of their own decisions, so that the script moves on to the step that decision
     * really belongs to.  To be called inside the game's lock, and followed by AfterActions (a pause can be the end
     * of the scenario).
     * @param stepNumber The step whose messages they were.
     * @param choice The name of the button they pressed, if the step ended in a choice; null otherwise.
     */
    public void Acknowledge(String player, int stepNumber, String choice) {
        if (!player.equals(_human) || _result != null)
            return;
        if (_move != null && !_move.released && _move.awaiting == stepNumber) {
            clearStep();
            release(_move);
            return;
        }
        //(the session tells this about the ruling that follows, and that puts the next step on the screen)
        if (_session.Read(_human, stepNumber, choice) && _session.Director().end() != null)
            clearStep();
    }

    public void Acknowledge(String player, int stepNumber) {
        Acknowledge(player, stepNumber, null);
    }

    private void release(Move move) {
        move.released = true;
        String player = _opponent;
        _timer.schedule(() -> {
            try {
                _host.opponentAnswers(player, move.decision, move.answer);
            } catch (RuntimeException exp) {
                LOG.error("Scenario " + _def.id + ": the opponent could not answer", exp);
            }
        }, Math.max(0, _def.opponentDelayMs), TimeUnit.MILLISECONDS);
    }

    @Override
    public void atRest() {
        AfterActions();
    }

    /**
     * To be called, inside the game's lock, each time the game has stopped working and is waiting again.  This is
     * where the scenario ends, if it is time for it to.  (The session calls it too, before it rules on whatever is
     * waiting; the caller of the game still has to, because a game that ends with nothing waiting never gets as far
     * as the session.)
     */
    public void AfterActions() {
        if (_result != null || !_begun)
            return;

        if (_table.game().isFinished()) {
            //The game got there first: somebody won it by its own rules
            String winner = _table.game().getWinnerPlayerId();
            Result result = _table.game().isCancelled() ? Result.ERROR : _human.equals(winner) ? Result.COMPLETE : Result.FAILED;
            finish(result, List.of(), result == Result.FAILED ? text(_def.outcome.loseMessage) : null, false, false);
            return;
        }

        ScenarioDefinition.End end = _session.Director().end();
        if (end != null) {
            List<Map<String, Object>> messages = new ArrayList<>();
            for (Message message : end.messages)
                messages.add(messageJson(new RenderedMessage(message,
                        message.header == null ? null : Templates.render(_table, message.header),
                        Templates.render(_table, message.text)), Advance.NEXT));
            finish(end.result == EndResult.COMPLETE ? Result.COMPLETE : Result.FAILED, messages, null, false, true);
        } else if (_offScript != null) {
            if (carriesOnOffScript()) {
                //The person carries on with an ordinary game, and is told so once.  The script is still followed if
                // the game finds its way back to it.
                Map<String, Object> notice = new LinkedHashMap<>();
                notice.put("type", "offscript");
                notice.put("message", text(_def.outcome.offScriptMessage));
                showStep(notice, ANY_ANSWER);
                _offScript = null;
                _carryingOn = true;
            } else {
                finish(Result.OFF_SCRIPT, List.of(), text(_def.outcome.offScriptMessage),
                        _def.outcome.offScriptResult == OffScriptResult.RESTART, true);
            }
        } else if (_def.outcome.lose != null && _table.Holds(_def.outcome.lose, _human)) {
            finish(Result.FAILED, List.of(), text(_def.outcome.loseMessage), false, true);
        } else if (_def.outcome.win != null && _table.Holds(_def.outcome.win, _human)) {
            finish(Result.COMPLETE, List.of(), null, false, true);
        }
    }

    /**
     * The person has walked away from the scenario (conceded, or stopped answering).  To be called before the game
     * is told that they lost, so that the loss is not taken for a failed attempt.
     */
    public void Abandoned() {
        if (_result == null)
            finish(Result.ABANDONED, List.of(), null, false, false);
    }

    /**
     * The game is being thrown away before anybody has seen it.  Nothing is shown, and nothing that was waiting to
     * hear how the scenario ended is told anything.
     */
    public void Discard() {
        _whenSettled.clear();
        _move = null;
        if (_result == null)
            _result = Result.ABANDONED;
    }

    private boolean carriesOnOffScript() {
        return _def.outcome.offScriptResult == OffScriptResult.CONTINUE;
    }

    /**
     * Something outside the script went wrong with the game, and whoever is running it is about to end it.  To be
     * called before they do, so that however the game then ends is not taken for the scenario's result.
     */
    public void Broke(String what) {
        if (_result != null)
            return;
        problem(what);
        try {
            finish(Result.ERROR, List.of(), null, false, false);
        } catch (RuntimeException exp) {
            //Whatever broke may well break this too.  The result has to be settled regardless.
            LOG.error("Scenario " + _def.id + ": could not show how it ended", exp);
            if (_result == null)
                settled(Result.ERROR);
        }
    }

    private void offScript(String what) {
        //A game that has left its script and carried on is an ordinary game: nothing more is expected of it
        if (_carryingOn)
            return;
        //With an author at the table, where the script has nothing to say is where the writing happens
        if (_author != null)
            return;
        _offScript = what;
        problem("off script: " + what + ".  The script was waiting on "
                + (_session.Director().currentStep() == null ? "nothing: every step has been used" : _session.Director().currentStep().describe()));
    }

    private void finish(Result result, List<Map<String, Object>> messages, String message, boolean restart, boolean endGame) {
        _move = null;
        clearStep();

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "end");
        event.put("result", result.name().toLowerCase());
        event.put("messages", messages);
        event.put("message", message);
        event.put("restart", restart);
        //Whether there is a checkpoint to go back to, and if so which step it is
        event.put("checkpoint", _lastCheckpoint == 0 ? null : _lastCheckpoint);
        show(END, event);
        settled(result);

        if (endGame) {
            if (result == Result.COMPLETE)
                _table.game().playerWon(_human, "Scenario complete");
            else
                _table.game().playerWon(_opponent, result == Result.OFF_SCRIPT ? "Scenario left its script" : "Scenario failed");
        }
    }

    private void problem(String problem) {
        //(a step that is put up again, as an author's is each time they change it, says again whatever is wrong with it)
        if (!_problems.isEmpty() && _problems.getLast().equals(problem))
            return;
        if (_problems.size() >= PROBLEM_LIMIT) {
            _problemsDropped++;
            return;
        }
        _problems.add(problem);
        LOG.warn("Scenario " + _def.id + " v" + _def.version + " (player " + _human + "): " + problem);
    }

    private void show(String kind, Map<String, Object> event) {
        _table.gameState().showScenarioEvent(_human, kind, JsonUtils.SerializeWithNulls(event));
    }

    private String text(String text) {
        return text == null ? null : _renderer.render(Templates.render(_table, text));
    }

    private Map<String, Object> infoEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "info");
        event.put("id", _def.id);
        event.put("version", _def.version);
        event.put("kind", _def.kind.name().toLowerCase());
        event.put("title", _def.title);
        event.put("coverage", _def.coverage.name().toLowerCase());
        event.put("human", _human);
        event.put("humanSeat", _def.human.key());
        event.put("opponent", _opponent);
        event.put("opponentName", _def.opponentDisplayName());
        event.put("autoAccept", _def.autoAccept);
        //How far a bubble sits from what it points at where its message does not say; null for the stock distance
        event.put("bubbleGap", _def.bubbleGap);
        //Set if the game did not start at the beginning: the checkpoint step it was brought back to
        event.put("resumedAt", _resumedAt);
        event.put("campaign", _campaign);
        //Whether the person is writing the scenario as they play it (the authoring panel goes with this)
        event.put("author", _author != null);
        //...or playing a draft of it as a player would, to see what they will see
        event.put("draftTest", _draftTest);
        return event;
    }

    /**
     * @param opponents Whether the step is the opponent's: its messages are then all there is to it, and the person
     *                  is asked to say when they have read them.
     */
    private Map<String, Object> stepEvent(Ruling ruling, boolean opponents) {
        Step step = ruling.step();
        ScenarioDirector.Presentation shown = ruling.shown();
        Map<String, Integer> cards = new LinkedHashMap<>();
        //Where on its card each printed thing a message points at is: "frodo:strength" -> {left, top, width, height}
        Map<String, double[]> parts = new LinkedHashMap<>();

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "step");
        event.put("step", step.number);
        //(one of the steps of a block: which of them, counting from 1; 0 for a step of the script itself)
        event.put("sub", step.sub);
        event.put("name", step.id);
        event.put("seat", opponents ? "opponent" : "human");
        //A step that waits to be read: an opponent's (which then acts), or a pause over the person's own decision
        event.put("acknowledge", opponents || step.pause);
        event.put("pause", step.pause);
        event.put("checkpoint", step.checkpoint);

        //The last message shown when the decision arrives stays up while the person answers; the ones before it
        // each wait for "Next".  A message tied to a moment inside the answering is part of the answering.
        RenderedMessage lastAtStart = null;
        for (RenderedMessage message : shown.messages())
            if (message.message().on == null)
                lastAtStart = message;
        List<Map<String, Object>> messages = new ArrayList<>();
        for (RenderedMessage message : shown.messages()) {
            Advance advance = message.message().advance;
            if (opponents || step.pause) {
                //Nothing here waits for an answer: there is none to give until the step has been read
                if (advance == null || advance == Advance.DECISION)
                    advance = Advance.NEXT;
            } else if (advance == null) {
                advance = message == lastAtStart || message.message().on != null ? Advance.DECISION : Advance.NEXT;
            }
            messages.add(messageJson(message, advance));
            noteCards(message.message(), cards);
            noteParts(message.message(), parts);
        }
        event.put("messages", messages);
        event.put("parts", parts);

        Allow allow = step.allow;
        if (opponents || shown.allowed() == null || allow == null) {
            event.put("allow", null);
        } else {
            Map<String, Object> allowed = new LinkedHashMap<>();
            allowed.put("answers", shown.allowed());
            allowed.put("pass", shown.passAllowed());
            allowed.put("otherwise", text(allow.otherwise));
            Map<String, String> hints = new LinkedHashMap<>();
            for (Map.Entry<String, String> hint : allow.hints.entrySet()) {
                hints.put(hint.getKey(), text(hint.getValue()));
                noteCard(hint.getKey(), cards);
            }
            allowed.put("hints", hints);
            event.put("allow", allowed);
        }
        event.put("cards", cards);
        return event;
    }

    private Map<String, Object> messageJson(RenderedMessage rendered, Advance advance) {
        Message message = rendered.message();
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("header", rendered.header());
        json.put("html", _renderer.render(rendered.text()));
        //(the first of them by itself as well, which is all there was before a message could point at several things:
        // the recorded games of that time say it so, and the client reads either)
        json.put("anchor", message.anchor.isEmpty() ? null : message.anchor.getFirst());
        json.put("anchors", message.anchor);
        json.put("gap", message.gap);
        json.put("highlight", message.highlight);
        json.put("highlightStyle", message.highlightStyle);
        json.put("avoid", message.avoid);
        json.put("side", message.side);
        json.put("advance", advance.name().toLowerCase());
        json.put("advanceCard", message.advanceCard);
        List<Map<String, String>> choices = new ArrayList<>();
        for (Map.Entry<String, String> choice : message.choices.entrySet()) {
            Map<String, String> button = new LinkedHashMap<>();
            button.put("name", choice.getKey());
            button.put("label", choice.getValue());
            choices.add(button);
        }
        json.put("choices", choices);
        if (message.on == null) {
            json.put("on", null);
        } else {
            Map<String, Object> on = new LinkedHashMap<>();
            on.put("moment", message.on.kind().name().toLowerCase());
            on.put("card", message.on.card());
            json.put("on", on);
        }
        json.put("zoom", message.zoom == null ? null : CardFaces.describe(_def, message.zoom, _library));
        return json;
    }

    /** Collects the cards a message refers to, with the IDs they have at this moment. */
    private void noteCards(Message message, Map<String, Integer> cards) {
        for (String target : message.anchor)
            noteCard(Anchors.cardAlias(target), cards);
        for (String target : message.highlight)
            noteCard(Anchors.cardAlias(target), cards);
        for (String target : message.avoid)
            noteCard(Anchors.cardAlias(target), cards);
        if (message.on != null)
            noteCard(message.on.card(), cards);
        noteCard(message.advanceCard, cards);
    }

    private void noteParts(Message message, Map<String, double[]> parts) {
        List<String> anchors = new ArrayList<>(message.highlight);
        anchors.addAll(message.avoid);
        anchors.addAll(message.anchor);
        for (String anchor : anchors) {
            String part = Anchors.printedPart(anchor);
            if (part == null)
                continue;
            String alias = Anchors.cardAlias(anchor);
            double[] rect = CardFaces.region(_def, alias, part, _library);
            if (rect != null)
                parts.put(alias + ":" + part.toLowerCase(), rect);
        }
    }

    private void noteCard(String alias, Map<String, Integer> cards) {
        if (alias == null)
            return;
        PhysicalCard card = _table.Card(alias);
        cards.put(alias, card.getCardId());
    }
}
