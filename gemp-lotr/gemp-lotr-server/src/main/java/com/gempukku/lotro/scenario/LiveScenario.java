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
 * "step event" on their screen: the step's messages, and which answers the step allows.  A ruling on the other
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

    private final ScenarioDefinition _def;
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
        _begun = true;
        if (_table.AnswersFollowed() > 0 && _session.Director().currentStep() != null)
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
        Step reached = ruling.step();
        if (reached != null && !_session.Director().isContinuing(reached)) {
            _table.Journal().mark(stepMark(reached.number));
            if (reached.checkpoint)
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
                if (_def.coverage == Coverage.STRICT)
                    offScript("no step covers the player's decision: " + _session.Director().describe(ruling.player(), ruling.decision()));
            }
            return;
        }

        //Ruled on again (it can happen to any decision whose answer is rejected): the move already stands
        if (_move != null && _move.decision == ruling.decision())
            return;

        String answer = ruling.answer();
        if (answer == null) {
            if (step == null && _def.fallback == Fallback.FAIL) {
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
     * The person has dismissed the messages of an opponent's step, so the opponent may go ahead.  To be called
     * inside the game's lock.
     * @param stepNumber The step whose messages they were.
     */
    public void Acknowledge(String player, int stepNumber) {
        if (!player.equals(_human) || _move == null || _move.released || _move.awaiting != stepNumber)
            return;
        clearStep();
        release(_move);
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
            finish(_table.game().isCancelled() ? Result.ERROR : _human.equals(winner) ? Result.COMPLETE : Result.FAILED,
                    List.of(), null, false, false);
            return;
        }

        ScenarioDefinition.End end = _session.Director().end();
        if (end != null) {
            List<Map<String, Object>> messages = new ArrayList<>();
            for (Message message : end.messages)
                messages.add(messageJson(new RenderedMessage(message,
                        message.title == null ? null : Templates.render(_table, message.title),
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
            finish(Result.FAILED, List.of(), null, false, true);
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
        //Set if the game did not start at the beginning: the checkpoint step it was brought back to
        event.put("resumedAt", _resumedAt);
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

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "step");
        event.put("step", step.number);
        event.put("name", step.id);
        event.put("seat", opponents ? "opponent" : "human");
        event.put("acknowledge", opponents);
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
            if (opponents)
                advance = Advance.NEXT;
            else if (advance == null)
                advance = message == lastAtStart || message.message().on != null ? Advance.DECISION : Advance.NEXT;
            messages.add(messageJson(message, advance));
            noteCards(message.message(), cards);
        }
        event.put("messages", messages);

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
        json.put("title", rendered.title());
        json.put("html", _renderer.render(rendered.text()));
        json.put("anchor", message.anchor);
        json.put("highlight", message.highlight);
        json.put("highlightStyle", message.highlightStyle);
        json.put("avoid", message.avoid);
        json.put("side", message.side);
        json.put("advance", advance.name().toLowerCase());
        if (message.on == null) {
            json.put("on", null);
        } else {
            Map<String, Object> on = new LinkedHashMap<>();
            on.put("moment", message.on.kind().name().toLowerCase());
            on.put("card", message.on.card());
            json.put("on", on);
        }
        return json;
    }

    /** Collects the cards a message refers to, with the IDs they have at this moment. */
    private void noteCards(Message message, Map<String, Integer> cards) {
        if (message.anchor != null)
            noteCard(Anchors.cardAlias(message.anchor), cards);
        for (String target : message.highlight)
            noteCard(Anchors.cardAlias(target), cards);
        for (String target : message.avoid)
            noteCard(Anchors.cardAlias(target), cards);
        if (message.on != null)
            noteCard(message.on.card(), cards);
    }

    private void noteCard(String alias, Map<String, Integer> cards) {
        if (alias == null)
            return;
        PhysicalCard card = _table.Card(alias);
        cards.put(alias, card.getCardId());
    }
}
