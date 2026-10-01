package com.gempukku.lotro.scenario;

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
         */
        void opponentAnswers(String player, int decisionId, String answer);
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
    private boolean _offScriptNoticeShown;
    private Result _result;
    private final List<String> _problems = new ArrayList<>();

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
        _def = def;
        _human = humanPlayer;
        _opponent = OPPONENT_PLAYER;
        _renderer = renderer;
        _timer = timer;
        _table = new ScenarioTable(def, library, format, humanPlayer, OPPONENT_PLAYER);
        _session = new ScenarioSession(_table);
        _session.Listen(this);
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
        all.addAll(_table.Problems());
        return all;
    }

    public void SetHost(Host host) {
        _host = host;
    }

    /**
     * Sets the game going.  To be called once, inside the game's lock, before anybody has connected.
     */
    public void Begin() {
        _table.gameState().showScenarioEvent(_human, INFO, JsonUtils.Serialize(infoEvent()));
        _table.Begin();
        AfterActions();
    }

    @Override
    public void ruled(Ruling ruling) {
        //Once the scenario is over, or about to be ended for leaving its script, nothing more is shown or played
        if (_result != null || (_offScript != null && !carriesOnOffScript()))
            return;
        Step step = ruling.step();
        if (ruling.problem() != null)
            problem((step == null ? "" : step.describe() + ": ") + ruling.problem());

        if (ruling.player().equals(_human)) {
            if (step != null) {
                show(STEP, stepEvent(ruling, false));
            } else {
                _table.gameState().clearScenarioEvent(_human, STEP);
                if (_def.coverage == Coverage.STRICT)
                    offScript("no step covers the player's decision: " + _session.Director().describe(ruling.player(), ruling.decision()));
            }
            return;
        }

        String answer = ruling.answer();
        if (answer == null) {
            if (step == null && _def.fallback == Fallback.FAIL) {
                offScript("no step covers the opponent's decision: " + _session.Director().describe(ruling.player(), ruling.decision()));
                if (!carriesOnOffScript())
                    return;
            }
            if (step == null && _def.fallback == Fallback.BOT)
                problem("opponent.fallback is 'bot', which a live game cannot do yet; the opponent did as little as it could instead");
            answer = AnswerCompiler.leastAnswer(ruling.decision());
        }

        boolean hasMessages = step != null && !ruling.shown().messages().isEmpty();
        if (hasMessages)
            show(STEP, stepEvent(ruling, true));
        _move = new Move(ruling.decision(), answer, hasMessages ? step.number : 0);
        if (!hasMessages)
            release(_move);
    }

    @Override
    public void answered(Ruling ruling, String answer) {
        _table.gameState().clearScenarioEvent(_human, STEP);
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
        _table.gameState().clearScenarioEvent(_human, STEP);
        release(_move);
    }

    private void release(Move move) {
        move.released = true;
        String player = _opponent;
        int decisionId = move.decision.getAwaitingDecisionId();
        _timer.schedule(() -> {
            try {
                _host.opponentAnswers(player, decisionId, move.answer);
            } catch (RuntimeException exp) {
                LOG.error("Scenario " + _def.id + ": the opponent could not answer", exp);
            }
        }, Math.max(0, _def.opponentDelayMs), TimeUnit.MILLISECONDS);
    }

    /**
     * To be called, inside the game's lock, each time the game has stopped working and is waiting again.  This is
     * where the scenario ends, if it is time for it to.
     */
    public void AfterActions() {
        if (_result != null)
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
                //The person carries on with an ordinary game.  The script is still followed if the game finds its
                // way back to it.
                if (!_offScriptNoticeShown) {
                    _offScriptNoticeShown = true;
                    Map<String, Object> notice = new LinkedHashMap<>();
                    notice.put("type", "offscript");
                    notice.put("message", text(_def.outcome.offScriptMessage));
                    show(STEP, notice);
                }
                _offScript = null;
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
        finish(Result.ERROR, List.of(), null, false, false);
    }

    private void offScript(String what) {
        _offScript = what;
        problem("off script: " + what + ".  The script was waiting on "
                + (_session.Director().currentStep() == null ? "nothing: every step has been used" : _session.Director().currentStep().describe()));
    }

    private void finish(Result result, List<Map<String, Object>> messages, String message, boolean restart, boolean endGame) {
        _result = result;
        _move = null;
        _table.gameState().clearScenarioEvent(_human, STEP);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "end");
        event.put("result", result.name().toLowerCase());
        event.put("messages", messages);
        event.put("message", message);
        event.put("restart", restart);
        show(END, event);

        if (endGame) {
            if (result == Result.COMPLETE)
                _table.game().playerWon(_human, "Scenario complete");
            else
                _table.game().playerWon(_opponent, result == Result.OFF_SCRIPT ? "Scenario left its script" : "Scenario failed");
        }
    }

    private void problem(String problem) {
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
