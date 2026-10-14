package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.logic.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps a scenario's place in its script.
 *
 * A scenario's steps are worked through strictly in order, and only the current step is ever looked at.  Each time
 * the game asks either seat for a decision, the director is asked which step (if any) that decision belongs to; once
 * the decision has been answered, it is told so and moves on.  It never answers anything itself: whoever is running
 * the game (the headless runner, or a live game's mediator) asks it what the script says and does the answering.
 *
 * Two things can change which steps there are to work through.  A step marked as a pause is read, not answered: it
 * applies to a decision, and once it has been read the script moves on and the same decision is looked at again.  And
 * a pause can end in a choice, which gives the game a name ("help"); steps that say 'if: help' or 'unless: help' are
 * part of the game or not accordingly, and the ones that are not are passed over as if they were not in the file.
 *
 * One kind of step is not taken in order: a block ("anyOrder").  A block stands in the script like any step, and the
 * script stays at it until a decision arrives that fits its 'until'.  Meanwhile each decision goes to the first of
 * the block's own steps that fits it, wherever in the block that step comes and however often it has applied
 * already (a step marked 'once', or a pause, applies once).  This is for the stretches of a game whose order is the
 * game's and the player's to decide: which skirmish is fought first, how many times each side acts in it.
 */
public class ScenarioDirector {
    public enum StepStatus {
        /** The script never got this far. */
        UNREACHED,
        /** The step applied to at least one decision. */
        MATCHED,
        /**
         * The step was given up on: its skipWhen came true, or it was a repeating step whose until was already true,
         * or it is a block's step and the block was over before any decision went to it.
         */
        SKIPPED,
        /** The step belongs to a way through the scenario that this game did not take (its 'if' or 'unless'). */
        BYPASSED
    }

    /**
     * What a step shows the player for one decision, with everything filled in for the game as it stands.
     * @param messages The step's messages, templates filled in.  Empty after the first decision of a repeating step.
     * @param allowed The answers the player may give, in the game's own terms; null if the step does not restrict.
     * @param passAllowed Whether passing is among them.
     */
    public record Presentation(Step step, List<RenderedMessage> messages, List<String> allowed, boolean passAllowed) { }

    public record RenderedMessage(Message message, String header, String text) { }

    private final ScenarioTable _table;
    private final ScenarioDefinition _def;
    private final Map<String, StepStatus> _statuses = new LinkedHashMap<>();

    /** The names chosen so far (see Message.choices). */
    private final Set<String> _flags = new LinkedHashSet<>();

    private int _index = 0;
    /** How many decisions the current step has applied to so far (a block: its steps between them, readings included). */
    private int _timesApplied = 0;
    /** In the block the script is at: how many decisions each of its steps has applied to. */
    private final Map<Step, Integer> _applied = new java.util.IdentityHashMap<>();
    /** In the block the script is at: the steps that are done with (a 'once' that has applied, a pause that has been read). */
    private final Set<Step> _spent = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private ScenarioDefinition.End _end;

    private AwaitingDecision _lastDecision;
    private Step _lastMatch;

    private Object _lastSkirmish;
    private int _skirmishOrdinal = 0;
    private int _skirmishTurn = -1;

    public ScenarioDirector(ScenarioTable table) {
        _table = table;
        _def = table.Definition();
        for (Step step : _def.allSteps())
            _statuses.put(step.label(), StepStatus.UNREACHED);
    }

    public Map<String, StepStatus> statuses() {
        return _statuses;
    }

    /** The step the script is waiting on, or null if every step has been used. */
    public Step currentStep() {
        return _index < _def.steps.size() ? _def.steps.get(_index) : null;
    }

    /** The names chosen so far, in the order they were chosen. */
    public Set<String> flags() {
        return _flags;
    }

    /**
     * @return Whether the script has nothing more to say: every step that is part of this game has been used.
     * Whatever the game asks from here on is past the end of the script.
     */
    public boolean atFrontier() {
        for (int index = _index; index < _def.steps.size(); index++)
            if (partOfTheGame(_def.steps.get(index)))
                return false;
        return true;
    }

    /**
     * Authoring: writes a step into the script at the point the script has reached, as a step that has just been
     * used.  This is how a decision that the script did not cover becomes part of it: the script now reads as if it
     * had covered it all along, and stands where it stood.
     */
    public void record(Step step) {
        restructure(() -> _def.steps.add(_index, step), null);
        _statuses.put(step.label(), StepStatus.MATCHED);
        _index++;
    }

    /**
     * Authoring: writes a step in after the step the script is waiting on, and has the script wait on the new one.
     * This is for a step that repeats and is under way when a decision arrives that it neither applies to nor ends
     * at: the repeating step is thereby done (its author sees to it that its 'until' says so for the next game).
     */
    public void insertAfterCurrent(Step step) {
        int at = Math.min(_index + 1, _def.steps.size());
        restructure(() -> _def.steps.add(at, step), step);
    }

    /**
     * Authoring: a block has been made to end where it was (see insertAfterCurrent).  It is done with as it is when
     * it ends by itself: the steps in it that no decision went to were not needed.
     */
    public void doneWith(Step block) {
        for (Step child : block.children)
            if (_statuses.get(child.label()) == StepStatus.UNREACHED)
                _statuses.put(child.label(), StepStatus.SKIPPED);
    }

    /** Authoring: the same, for a decision that has already been answered: the script stands after the new step. */
    public void recordAfterCurrent(Step step) {
        Step done = currentStep();
        insertAfterCurrent(step);
        if (done != null && done.block)
            doneWith(done);
        _statuses.put(step.label(), StepStatus.MATCHED);
        advance();
    }

    /**
     * Authoring: writes a step into the block the script is at, as the last of its steps.  (The decision it is for
     * fitted none of the others, so nothing that comes before it in the block takes that decision from it.)
     * @param used Whether the decision has been answered already, so that the step has applied to it.
     */
    public void addToBlock(Step step, boolean used) {
        Step block = currentStep();
        if (block == null || !block.block)
            throw new IllegalStateException("The script is not at a block");
        step.parent = block;
        restructure(() -> block.children.add(step), null);
        if (used) {
            _statuses.put(step.label(), StepStatus.MATCHED);
            _statuses.put(block.label(), StepStatus.MATCHED);
            _applied.merge(step, 1, Integer::sum);
            _timesApplied++;
            if (step.once)
                _spent.add(step);
        }
    }

    /**
     * Authoring: writes a step into the block the script is at, in front of one of its steps, so that what would
     * have gone to that one goes to the new step first.
     */
    public void insertInBlock(Step step, Step before) {
        Step block = currentStep();
        if (block == null || !block.block || before.parent != block)
            throw new IllegalStateException("The script is not at that step's block");
        step.parent = block;
        restructure(() -> block.children.add(block.children.indexOf(before), step), null);
    }

    /** Authoring: takes a step that was written into a block out of it again. */
    public void removeFromBlock(Step step) {
        Step block = step.parent;
        if (block == null)
            return;
        restructure(() -> block.children.remove(step), null);
        _applied.remove(step);
        _spent.remove(step);
    }

    /**
     * Authoring: the steps have been changed by somebody else (words reworded in place, steps added or removed
     * after the one the script is waiting on).  The steps are numbered afresh, what became of each is kept, and the
     * script goes on waiting on the same step it was waiting on.
     * @param change Makes the change.  It must leave the current step, and every step before it, in the list.
     * @param current The step the script is to wait on afterwards; null to stay at the same place in the list.
     */
    public void restructure(Runnable change, Step current) {
        Map<Step, StepStatus> before = new java.util.IdentityHashMap<>();
        for (Step step : _def.allSteps())
            before.put(step, _statuses.getOrDefault(step.label(), StepStatus.UNREACHED));
        change.run();
        _statuses.clear();
        for (int index = 0; index < _def.steps.size(); index++) {
            Step step = _def.steps.get(index);
            step.number = index + 1;
            step.parent = null;
            step.sub = 0;
            _statuses.put(step.label(), before.getOrDefault(step, StepStatus.UNREACHED));
            for (int place = 0; place < step.children.size(); place++) {
                Step child = step.children.get(place);
                child.number = step.number;
                child.parent = step;
                child.sub = place + 1;
                _statuses.put(child.label(), before.getOrDefault(child, StepStatus.UNREACHED));
            }
        }
        if (current != null) {
            int index = _def.steps.indexOf(current);
            if (index >= 0)
                _index = index;
            //(a step named as the one to wait on is waited on from its beginning)
            _timesApplied = 0;
        }
        _index = Math.min(_index, _def.steps.size());
        _lastDecision = null;
        _lastMatch = null;
    }

    /** How many decisions the step the script is waiting on has applied to so far. */
    public int timesApplied() {
        return _timesApplied;
    }

    /**
     * @return How many decisions the steps of the block the script is at have had answered.  (What was only read is
     * not counted: a pause is over a decision that then goes to another of the block's steps.)
     */
    public int answeredInBlock() {
        Step block = currentStep();
        int answered = 0;
        if (block != null)
            for (Step child : block.children)
                answered += _applied.getOrDefault(child, 0);
        return answered;
    }

    /** How many decisions a step has applied to so far: the step the script is at, or one of the steps in it. */
    private int times(Step step) {
        if (step.parent != null)
            return step.parent == currentStep() ? _applied.getOrDefault(step, 0) : 0;
        return step == currentStep() ? _timesApplied : 0;
    }

    /**
     * @return Whether a decision that goes to this step is the first thing the script has had to do with the place
     * it is at: the step's own first decision or, in a block, the first decision any of the block's steps takes.
     */
    public boolean arrives(Step step) {
        return step.parent != null ? step.parent == currentStep() && _timesApplied == 0 : !isContinuing(step);
    }

    /** Whether, with what has been chosen so far, a step is part of this game at all. */
    private boolean partOfTheGame(Step step) {
        for (String name : step.onlyIf)
            if (!_flags.contains(name))
                return false;
        for (String name : step.unless)
            if (_flags.contains(name))
                return false;
        return true;
    }

    /**
     * Whether a player who has nothing to play in a phase has to be asked to pass all the same, because the script's
     * next step is about exactly that: their passing in that phase.  This is what lets a scenario pass most phases
     * for its player and still stop at the one it wants to talk about; and it is the only way the scripted opponent,
     * which is never otherwise asked when it has nothing to do, gets to stop and have something said.
     */
    public boolean asksToPass(String player, Phase phase) {
        for (int index = _index; index < _def.steps.size(); index++) {
            Step step = _def.steps.get(index);
            if (!partOfTheGame(step))
                continue;
            observeSkirmish();
            if (step.block) {
                //If the block would be over at this decision, it is the steps after it that have a say.  If not,
                // it is the first of its own steps that the decision could go to.
                if ((index != _index || _timesApplied == 0) && step.skipWhen != null && couldFit(step.skipWhen, player, phase, false))
                    continue;
                if (couldFit(step.until, player, phase, false))
                    continue;
                if (!couldFit(step.when, player, phase, false))
                    return false;
                for (Step child : step.children) {
                    if ((index == _index && _spent.contains(child)) || !partOfTheGame(child))
                        continue;
                    //(the phase is said for it if the block says it for all its steps)
                    if (couldFit(child.when, player, phase, !(saysPhase(child.when, phase) || (child.when.phase == null && step.when.phase == phase))))
                        return isAboutPassing(child);
                }
                return false;
            }
            //A step that repeats and has already begun takes what comes and asks for nothing more.  What may be
            // wanted is the pass that ends it: that is for the step after it to say.
            if (index == _index && step.repeats() && _timesApplied > 0)
                continue;
            return couldFit(step.when, player, phase, !saysPhase(step.when, phase)) && isAboutPassing(step);
        }
        return false;
    }

    /**
     * Everything about a 'when' that can be known before the decision exists: a player's "play something or pass"
     * in a phase.  (What the decision will say and offer cannot be: 'text' and 'offers' are left to the author to
     * get right.)
     * @param phaseSaid Whether the 'when' has to name the phase itself.  A step that is about a pass in a phase
     *                  says which; a block's 'until' need not.
     */
    /** Whether a 'when' that names no phase says which it is all the same: a card is only ever fighting in a skirmish. */
    private static boolean saysPhase(When when, Phase phase) {
        return when.phase == null && when.fighting != null && phase == Phase.SKIRMISH;
    }

    private boolean couldFit(When when, String player, Phase phase, boolean phaseSaid) {
        if (when.opening)
            return false;
        return (when.phase == phase || (!phaseSaid && when.phase == null))
                && (when.seat == null || when.seat == _table.SeatOf(player))
                && (when.decision == null || when.decision == AwaitingDecisionType.CARD_ACTION_CHOICE)
                && (when.turn == null || when.turn == _table.gameState().getTurnNumber())
                && (when.skirmish == null || (_table.gameState().getSkirmish() != null && when.skirmish == _skirmishOrdinal))
                && (when.fighting == null || isFighting(when.fighting))
                && (when.require == null || _table.Holds(when.require, player));
    }

    /**
     * A step that plays a card is waiting for the moment the card can be played, and being asked with nothing to
     * play is not that moment.  A step whose answer is to pass, or which is only there to be read, is.
     */
    private static boolean isAboutPassing(Step step) {
        if (step.pause)
            return true;
        if (!step.answers.isEmpty())
            return step.answers.getFirst().kind() == AnswerKind.PASS;
        if (step.allow == null)
            return false;
        return step.allow.passWhen != null || step.allow.answers.stream().anyMatch(answer -> answer.kind() == AnswerKind.PASS);
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

            if (!partOfTheGame(step)) {
                _statuses.put(step.label(), StepStatus.BYPASSED);
                for (Step child : step.children)
                    _statuses.put(child.label(), StepStatus.BYPASSED);
                advance();
                continue;
            }

            if (step.block) {
                boolean over = matches(step.until, player, decision);
                if (over || (_timesApplied == 0 && step.skipWhen != null && matches(step.skipWhen, player, decision))) {
                    //The block is done.  Its steps that no decision went to were not needed this time.
                    if (_timesApplied == 0)
                        _statuses.put(step.label(), StepStatus.SKIPPED);
                    for (Step child : step.children)
                        if (_statuses.get(child.label()) == StepStatus.UNREACHED)
                            _statuses.put(child.label(), StepStatus.SKIPPED);
                    advance();
                    continue;
                }
                if (!matches(step.when, player, decision))
                    return null;
                for (Step child : step.children) {
                    if (_spent.contains(child))
                        continue;
                    if (!partOfTheGame(child)) {
                        _statuses.put(child.label(), StepStatus.BYPASSED);
                        continue;
                    }
                    if (!matches(child.when, player, decision))
                        continue;
                    if (_applied.getOrDefault(child, 0) == 0) {
                        _statuses.put(child.label(), StepStatus.MATCHED);
                        for (DoOp op : child.doOps)
                            _table.Apply(op);
                    }
                    _statuses.put(step.label(), StepStatus.MATCHED);
                    return child;
                }
                return null;
            }

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
                    //(the opening is read on the board as it was laid out: what it does, it does once it has been read)
                    if (!step.when.opening)
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
        if (step.parent != null) {
            //One of a block's steps: the script stays at the block
            if (step.parent != currentStep())
                return;
            _applied.merge(step, 1, Integer::sum);
            _timesApplied++;
            if (step.end != null)
                _end = step.end;
            if (step.once)
                _spent.add(step);
            return;
        }
        if (step != currentStep())
            return;
        if (step.when.opening)
            for (DoOp op : step.doOps)
                _table.Apply(op);
        _timesApplied++;
        if (step.end != null)
            _end = step.end;
        if (!step.repeats())
            advance();
    }

    /**
     * Tells the director that a pause has been read.
     * @param choice The name the reader chose, if the pause ended in a choice; null otherwise.
     */
    public void read(Step step, String choice) {
        if (!step.pause || (step.parent == null ? step != currentStep() : step.parent != currentStep()))
            return;
        if (choice != null)
            _flags.add(choice);
        if (step.end != null)
            _end = step.end;
        if (step.parent != null) {
            //A block's pause has been read: the decision goes to whichever of the block's other steps fits it
            _spent.add(step);
            _timesApplied++;
        } else {
            advance();
        }
        //The decision the pause was read over is looked at afresh, by whichever step comes next
        _lastDecision = null;
        _lastMatch = null;
    }

    /**
     * @return What the opponent's standing orders say to a decision no step covers, in the game's own terms; null
     * if none of them fits it.
     */
    public String order(String player, AwaitingDecision decision) {
        for (ScenarioDefinition.Order order : _def.orders) {
            if (!matches(order.when(), player, decision))
                continue;
            for (Answer answer : order.answers()) {
                try {
                    return AnswerCompiler.compile(_table, decision, answer);
                } catch (ScenarioScriptException exp) {
                    //Not on offer this time; an order is for whenever it is
                }
            }
        }
        return null;
    }

    private void advance() {
        _index++;
        _timesApplied = 0;
        _applied.clear();
        _spent.clear();
    }

    /**
     * @return Whether the step has already applied to an earlier decision (only a repeating step, or one of a
     * block's, can have).
     */
    public boolean isContinuing(Step step) {
        return times(step) > 0;
    }

    /** Whether a card is in the skirmish being fought, on either side of it. */
    private boolean isFighting(String alias) {
        var skirmish = _table.gameState().getSkirmish();
        if (skirmish == null)
            return false;
        PhysicalCard card = _table.Card(alias);
        return skirmish.getFellowshipCharacter() == card || skirmish.getShadowCharacters().contains(card);
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
        //The opening is not one of the game's decisions, and nothing but a step that waits for it has it
        if (when.opening != _table.IsOpening(decision))
            return false;
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
        if (when.fighting != null && !isFighting(when.fighting))
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
        //The opening is the table's own question, and has one answer
        if (_table.IsOpening(decision))
            return "0";
        Answer answer = null;
        if (!step.answers.isEmpty()) {
            int times = times(step);
            if (times < step.answers.size())
                answer = step.answers.get(times);
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
        List<RenderedMessage> messages = render(step);

        List<String> allowed = null;
        boolean passAllowed = false;
        Allow allow = step.allow;
        if (allow != null) {
            allowed = new ArrayList<>();
            for (Answer answer : allow.answers) {
                if (answer.kind() == AnswerKind.PASS) {
                    try {
                        AnswerCompiler.compile(_table, decision, answer);
                        passAllowed = true;
                    } catch (ScenarioScriptException exp) {
                        if (!answer.ifOffered())
                            throw exp;
                    }
                    continue;
                }
                try {
                    allowed.add(AnswerCompiler.compile(_table, decision, answer));
                } catch (ScenarioScriptException exp) {
                    //In a step that spans several decisions, an allowed answer is expected to stop being on offer
                    // once it has been used (a card that has been played cannot be played again); and an answer
                    // written with a ? is one its author knows may not be on offer
                    if (!step.recurs() && !answer.ifOffered())
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
     * What a step shows when its restrictions cannot be worked out: its messages, and nothing about the answer.
     */
    public Presentation presentUnrestricted(Step step) {
        return new Presentation(step, render(step), null, false);
    }

    private List<RenderedMessage> render(Step step) {
        List<RenderedMessage> messages = new ArrayList<>();
        if (!isContinuing(step))
            for (Message message : step.messages)
                messages.add(new RenderedMessage(message,
                        message.header == null ? null : Templates.render(_table, message.header),
                        Templates.render(_table, message.text)));
        return messages;
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
