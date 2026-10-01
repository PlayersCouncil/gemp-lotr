package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioDefinition.Allow;
import com.gempukku.lotro.scenario.ScenarioDefinition.Answer;
import com.gempukku.lotro.scenario.ScenarioDefinition.AnswerKind;
import com.gempukku.lotro.scenario.ScenarioDefinition.DoOp;
import com.gempukku.lotro.scenario.ScenarioDefinition.Message;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;
import com.gempukku.lotro.scenario.ScenarioDefinition.When;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps a scenario's place in its script.
 *
 * A scenario's steps are worked through strictly in order, and only the current step is ever looked at.  Each time
 * the game asks either seat for a decision, the director is asked which step (if any) that decision belongs to; once
 * the decision has been answered, it is told so and moves on.  It never answers anything itself: whoever is running
 * the game (the headless runner, or a live game's mediator) asks it what the script says and does the answering.
 */
public class ScenarioDirector {
    public enum StepStatus {
        /** The script never got this far. */
        UNREACHED,
        /** The step applied to at least one decision. */
        MATCHED,
        /** The step was given up on: its skipWhen came true, or it was a repeating step whose until was already true. */
        SKIPPED
    }

    /**
     * What a step shows the player for one decision, with everything filled in for the game as it stands.
     * @param messages The step's messages, templates filled in.  Empty after the first decision of a repeating step.
     * @param allowed The answers the player may give, in the game's own terms; null if the step does not restrict.
     * @param passAllowed Whether passing is among them.
     */
    public record Presentation(Step step, List<RenderedMessage> messages, List<String> allowed, boolean passAllowed) { }

    public record RenderedMessage(Message message, String title, String text) { }

    private final ScenarioTable _table;
    private final ScenarioDefinition _def;
    private final Map<String, StepStatus> _statuses = new LinkedHashMap<>();

    private int _index = 0;
    /** How many decisions the current step has applied to so far. */
    private int _timesApplied = 0;
    private ScenarioDefinition.End _end;

    private AwaitingDecision _lastDecision;
    private Step _lastMatch;

    private Object _lastSkirmish;
    private int _skirmishOrdinal = 0;
    private int _skirmishTurn = -1;

    public ScenarioDirector(ScenarioTable table) {
        _table = table;
        _def = table.Definition();
        for (Step step : _def.steps)
            _statuses.put(step.label(), StepStatus.UNREACHED);
    }

    public Map<String, StepStatus> statuses() {
        return _statuses;
    }

    /** The step the script is waiting on, or null if every step has been used. */
    public Step currentStep() {
        return _index < _def.steps.size() ? _def.steps.get(_index) : null;
    }

    /** Set once a step that ends the scenario has been answered. */
    public ScenarioDefinition.End end() {
        return _end;
    }

    /**
     * Finds the step that a decision belongs to, giving up on steps whose moment has passed along the way.  The first
     * time a step applies, its direct changes to the board ("do") are carried out.
     * @return The step, or null if the decision does not belong to the current step.
     */
    public Step match(String player, AwaitingDecision decision) {
        //Asking twice about the same decision (a player reconnecting, say) must not move the script on twice
        if (decision == _lastDecision)
            return _lastMatch;
        _lastDecision = decision;
        _lastMatch = findStep(player, decision);
        return _lastMatch;
    }

    private Step findStep(String player, AwaitingDecision decision) {
        observeSkirmish();
        while (_index < _def.steps.size()) {
            Step step = _def.steps.get(_index);

            if (step.repeats() && matches(step.until, player, decision)) {
                //A repeating step that never got to apply was simply not needed
                if (_timesApplied == 0)
                    _statuses.put(step.label(), StepStatus.SKIPPED);
                advance();
                continue;
            }

            if (matches(step.when, player, decision)) {
                if (_timesApplied == 0) {
                    _statuses.put(step.label(), StepStatus.MATCHED);
                    for (DoOp op : step.doOps)
                        _table.Apply(op);
                }
                return step;
            }

            if (_timesApplied == 0 && step.skipWhen != null && matches(step.skipWhen, player, decision)) {
                _statuses.put(step.label(), StepStatus.SKIPPED);
                advance();
                continue;
            }

            return null;
        }
        return null;
    }

    /**
     * Tells the director that the decision a step applied to has been answered.
     */
    public void answered(Step step) {
        if (step != currentStep())
            return;
        _timesApplied++;
        if (step.end != null)
            _end = step.end;
        if (!step.repeats())
            advance();
    }

    private void advance() {
        _index++;
        _timesApplied = 0;
    }

    /**
     * @return Whether the current step has already applied to an earlier decision (only a repeating step can have).
     */
    public boolean isContinuing(Step step) {
        return step == currentStep() && _timesApplied > 0;
    }

    private void observeSkirmish() {
        int turn = _table.gameState().getTurnNumber();
        if (turn != _skirmishTurn) {
            _skirmishTurn = turn;
            _skirmishOrdinal = 0;
            _lastSkirmish = null;
        }
        Object skirmish = _table.gameState().getSkirmish();
        if (skirmish != null && skirmish != _lastSkirmish)
            _skirmishOrdinal++;
        if (skirmish != null)
            _lastSkirmish = skirmish;
    }

    public boolean matches(When when, String player, AwaitingDecision decision) {
        if (when.seat != null && when.seat != _table.SeatOf(player))
            return false;
        if (when.decision != null && when.decision != decision.getDecisionType())
            return false;
        if (when.text != null && !decision.getText().toLowerCase().contains(when.text.toLowerCase()))
            return false;
        if (when.phase != null && when.phase != _table.GetCurrentPhase())
            return false;
        if (when.turn != null && when.turn != _table.gameState().getTurnNumber())
            return false;
        if (when.skirmish != null && (_table.gameState().getSkirmish() == null || when.skirmish != _skirmishOrdinal))
            return false;
        if (when.offers != null && !offers(decision, when.offers))
            return false;
        return when.require == null || _table.Holds(when.require, player);
    }

    private boolean offers(AwaitingDecision decision, String alias) {
        PhysicalCard card = _table.Card(alias);
        String cardId = String.valueOf(card.getCardId());
        Map<String, String[]> params = decision.getDecisionParameters();
        if (decision instanceof ArbitraryCardsSelectionDecision arbitrary && arbitrary.getPhysicalCards() != null)
            return arbitrary.getPhysicalCards().contains(card);
        for (String name : List.of("cardId", "freeCharacters", "minions"))
            if (params.containsKey(name) && Arrays.asList(params.get(name)).contains(cardId))
                return true;
        return false;
    }

    /**
     * @return The answer the script gives for this decision, in the game's own terms, or null if the script does not
     * say (the step leaves the choice to whoever is answering).
     * @throws ScenarioScriptException if the script's answer does not fit the decision.
     */
    public String scriptedAnswer(Step step, AwaitingDecision decision) throws ScenarioScriptException {
        Answer answer = null;
        if (!step.answers.isEmpty()) {
            if (_timesApplied < step.answers.size())
                answer = step.answers.get(_timesApplied);
            else if (step.answers.size() == 1)
                answer = step.answers.getFirst();
            else
                throw new ScenarioScriptException("step " + step.label() + " lists " + step.answers.size()
                        + " answers but has been asked for another.  The decision is: " + AnswerCompiler.describe(_table, decision));
        } else if (step.allow != null && step.allow.answers.size() == 1 && step.allow.passWhen == null) {
            answer = step.allow.answers.getFirst();
        }
        return answer == null ? null : AnswerCompiler.compile(_table, decision, answer);
    }

    /**
     * Works out what a step shows the player for this decision.  Doing so also checks the step against the game:
     * every answer the step allows has to be one the decision really offers.
     * @throws ScenarioScriptException if the step allows something the decision does not offer.
     */
    public Presentation present(Step step, String player, AwaitingDecision decision) throws ScenarioScriptException {
        List<RenderedMessage> messages = new ArrayList<>();
        if (!isContinuing(step))
            for (Message message : step.messages)
                messages.add(new RenderedMessage(message,
                        message.title == null ? null : Templates.render(_table, message.title),
                        Templates.render(_table, message.text)));

        List<String> allowed = null;
        boolean passAllowed = false;
        Allow allow = step.allow;
        if (allow != null) {
            allowed = new ArrayList<>();
            for (Answer answer : allow.answers) {
                if (answer.kind() == AnswerKind.PASS) {
                    AnswerCompiler.compile(_table, decision, answer);
                    passAllowed = true;
                    continue;
                }
                try {
                    allowed.add(AnswerCompiler.compile(_table, decision, answer));
                } catch (ScenarioScriptException exp) {
                    //In a step that spans several decisions, an allowed answer is expected to stop being on offer
                    // once it has been used (a card that has been played cannot be played again)
                    if (!step.repeats())
                        throw exp;
                }
            }
            if (allow.passWhen != null && _table.Holds(allow.passWhen, player))
                passAllowed = true;
            if (allowed.isEmpty() && !passAllowed)
                throw new ScenarioScriptException("step " + step.label() + " allows nothing that this decision offers.  The decision is: "
                        + AnswerCompiler.describe(_table, decision));
        }
        return new Presentation(step, messages, allowed, passAllowed);
    }

    /**
     * @return Where things stand, for a report: the decision in the scenario's terms, with the game's clock.
     */
    public String describe(String player, AwaitingDecision decision) {
        return _table.SeatOf(player).key() + ", " + AnswerCompiler.describe(_table, decision)
                + "  [turn " + _table.gameState().getTurnNumber() + ", " + _table.GetCurrentPhase().name().toLowerCase()
                + (_table.gameState().getSkirmish() != null ? ", skirmish " + _skirmishOrdinal : "") + "]";
    }
}
