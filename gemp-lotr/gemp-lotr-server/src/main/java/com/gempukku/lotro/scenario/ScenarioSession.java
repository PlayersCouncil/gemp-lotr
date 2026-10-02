package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.DecisionArbiter;
import com.gempukku.lotro.game.DecisionObserver;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One play-through of a scenario: the table, the director that keeps the scenario's place in its script, and what
 * the script has to say about each decision the game is waiting on.
 *
 * The session follows the game by itself.  Each time the game comes to rest, the session looks at the decisions it
 * is waiting on, asks the director what the script says about them, and keeps the result (a "ruling") for whoever
 * is going to answer.  The moment the game accepts an answer, the session tells the director, so that the script has
 * moved on before the game does.  Nobody has to remember to do either, and a headless run and a live game cannot do
 * them differently.
 *
 * A script is a list of steps taken one at a time, but a game can ask for two things before it stops: both players
 * at once (each is shown a revealed card), or a second question from inside the answering of the first (choose a
 * card from the deck you have just been shown).  So the session never rules on a decision the moment it is asked.
 * It waits until the game is at rest and then gives the current step to whichever waiting decision it fits.  Any
 * other decision waits its turn: it is not ruled on, and not put to its player, until the step in hand has been
 * answered and the script has moved on.  Only when no waiting decision fits the current step are they ruled to be
 * outside the script.
 *
 * The session answers nothing itself.  A headless run (ScenarioRunner) reads each ruling and answers both seats from
 * it; a live game (LiveScenario) shows the human's ruling to the human and plays the opponent's.
 */
public class ScenarioSession implements DecisionObserver, DecisionArbiter {
    /**
     * What the script says about one decision.
     * @param step The step the decision belongs to; null if the script has nothing to say about it.
     * @param shown What the step shows the player.  Null if there is no step.  If the step does not fit the decision
     *              (see problem) this still carries the step's messages, with no restriction on the answer.
     * @param answer The answer the script gives, in the game's own terms; null if it gives none.
     * @param problem Why the script could not be applied to the decision; null if it could.  This is always a
     *                mistake in the scenario: an answer the decision does not offer, a card that is not where the
     *                step expects.
     */
    public record Ruling(String player, AwaitingDecision decision, Step step, ScenarioDirector.Presentation shown,
                         String answer, String problem) { }

    /** Somebody who wants to know as soon as there is a ruling, without waiting to be the one who answers. */
    public interface Listener {
        /**
         * Called while the game is at rest, before the decision is put to its player.  Do not answer the decision
         * from here.  A decision whose answer the game rejected is ruled on again, with the same ruling.
         */
        void ruled(Ruling ruling);

        /** Called once the decision has been answered and the script has moved on, before the game carries on. */
        void answered(Ruling ruling, String answer);

        /**
         * Called each time the game has stopped working with something waiting to be decided, before any of it is
         * ruled on.  This is the moment to end the game, if it is time to: what is waiting is then never asked.
         */
        void atRest();
    }

    private final ScenarioTable _table;
    private final ScenarioDirector _director;
    /** By player: the ruling on the decision that player is being asked for. */
    private final Map<String, Ruling> _rulings = new LinkedHashMap<>();
    private Listener _listener;

    /**
     * @param table A table that has not been started yet.
     */
    public ScenarioSession(ScenarioTable table) {
        _table = table;
        _director = new ScenarioDirector(table);
        table.Observe(this);
        table.Arbitrate(this);
    }

    public ScenarioTable Table() { return _table; }
    public ScenarioDirector Director() { return _director; }
    public ScenarioDefinition Definition() { return _table.Definition(); }

    public void Listen(Listener listener) {
        _listener = listener;
    }

    /**
     * @return The ruling on the decision a player is being asked for.  Null if they are not being asked for one, or
     * if their decision is waiting for a step in hand to be answered first.
     */
    public Ruling Ruling(String player) {
        return _rulings.get(player);
    }

    @Override
    public List<String> release(Map<String, AwaitingDecision> waiting) {
        //The bid and the choice of seat come before the scenario's board exists, and the table answers them itself
        if (!_table.Arranged())
            return new ArrayList<>(waiting.keySet());
        if (_listener != null)
            _listener.atRest();
        //Whatever a finished game was still going to ask no longer matters, and nor does anything a game asks after
        // the step that ends its scenario
        if (_table.GameIsFinished() || _director.end() != null)
            return List.of();

        List<String> released = new ArrayList<>();
        Map<String, AwaitingDecision> fresh = new LinkedHashMap<>();
        for (Map.Entry<String, AwaitingDecision> entry : waiting.entrySet()) {
            Ruling before = _rulings.get(entry.getKey());
            if (before != null && before.decision() == entry.getValue()) {
                //Asked again because its answer was rejected: the ruling stands
                tell(before);
                released.add(entry.getKey());
            } else {
                fresh.put(entry.getKey(), entry.getValue());
            }
        }

        boolean stepInHand = _rulings.values().stream().anyMatch(ruling -> ruling.step() != null);
        List<Ruling> outsideTheScript = new ArrayList<>();
        for (Map.Entry<String, AwaitingDecision> entry : fresh.entrySet()) {
            if (stepInHand)
                break;
            Ruling ruling = rule(entry.getKey(), entry.getValue());
            if (ruling.step() != null) {
                stepInHand = true;
                accept(ruling, released);
            } else {
                outsideTheScript.add(ruling);
            }
        }
        //A decision that does not fit the current step may still fit the next one, so nothing is said about it
        // while a step is in hand.  It will be looked at again once that step has been answered.
        if (!stepInHand)
            for (Ruling ruling : outsideTheScript)
                accept(ruling, released);
        return released;
    }

    private void accept(Ruling ruling, List<String> released) {
        _rulings.put(ruling.player(), ruling);
        tell(ruling);
        released.add(ruling.player());
    }

    private void tell(Ruling ruling) {
        if (_listener != null)
            _listener.ruled(ruling);
    }

    private Ruling rule(String player, AwaitingDecision decision) {
        Step step = null;
        ScenarioDirector.Presentation shown = null;
        String answer = null;
        String problem = null;
        try {
            //Once a step has ended the scenario, the script has nothing more to say
            if (_director.end() == null)
                step = _director.match(player, decision);
            if (step != null) {
                try {
                    shown = _director.present(step, player, decision);
                    answer = _director.scriptedAnswer(step, decision);
                } catch (ScenarioScriptException exp) {
                    problem = exp.getMessage();
                    shown = _director.presentUnrestricted(step);
                    answer = null;
                }
            }
        } catch (RuntimeException exp) {
            //A mistake in the scenario must not take the game down with it
            StackTraceElement[] trace = exp.getStackTrace();
            problem = "the script could not be applied: " + exp + (trace.length > 0 ? " at " + trace[0] : "");
            step = null;
            shown = null;
            answer = null;
        }
        return new Ruling(player, decision, step, shown, answer, problem);
    }

    @Override
    public void decisionIssued(String player, AwaitingDecision decision) { }

    @Override
    public void decisionAnswered(String player, AwaitingDecision decision, String answer) {
        Ruling ruling = _rulings.get(player);
        if (ruling == null || ruling.decision() != decision)
            return;
        _rulings.remove(player);
        if (ruling.step() != null)
            _director.answered(ruling.step());
        if (_listener != null)
            _listener.answered(ruling, answer);
    }
}
