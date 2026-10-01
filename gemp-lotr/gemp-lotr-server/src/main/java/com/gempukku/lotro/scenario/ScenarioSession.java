package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.DecisionObserver;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;

import java.util.HashMap;
import java.util.Map;

/**
 * One play-through of a scenario: the table, the director that keeps the scenario's place in its script, and what
 * the script has to say about each decision the game is waiting on.
 *
 * The session follows the game by itself.  The moment the game puts a decision to either seat, the session asks the
 * director what the script says about it and keeps the result (a "ruling") for whoever is going to answer.  The
 * moment the game accepts an answer, the session tells the director, so that the script has moved on before the game
 * does.  Nobody has to remember to do either, and a headless run and a live game cannot do them differently.
 *
 * The session answers nothing itself.  A headless run (ScenarioRunner) reads each ruling and answers both seats from
 * it; a live game (LiveScenario) shows the human's ruling to the human and plays the opponent's.
 */
public class ScenarioSession implements DecisionObserver {
    /**
     * What the script says about one decision.
     * @param step The step the decision belongs to; null if the script has nothing to say about it.
     * @param shown What the step shows the player.  Null if there is no step.  If the step does not fit the decision
     *              (see problem) this still carries the step's messages, with no restriction on the answer.
     * @param answer The answer the script gives, in the game's own terms; null if it gives none.
     * @param problem Why the step does not fit the decision it matched; null if it does.  This is always a mistake in
     *                the scenario: an answer the decision does not offer, a card that is not where the step expects.
     */
    public record Ruling(String player, AwaitingDecision decision, Step step, ScenarioDirector.Presentation shown,
                         String answer, String problem) { }

    /** Somebody who wants to know as soon as there is a ruling, without waiting to be the one who answers. */
    public interface Listener {
        /** Called while the game is still working: do not answer the decision from here. */
        void ruled(Ruling ruling);

        /** Called once the decision has been answered and the script has moved on, before the game carries on. */
        void answered(Ruling ruling, String answer);
    }

    private final ScenarioTable _table;
    private final ScenarioDirector _director;
    private final Map<String, Ruling> _rulings = new HashMap<>();
    private Listener _listener;

    /**
     * @param table A table that has not been started yet.
     */
    public ScenarioSession(ScenarioTable table) {
        _table = table;
        _director = new ScenarioDirector(table);
        table.Observe(this);
    }

    public ScenarioTable Table() { return _table; }
    public ScenarioDirector Director() { return _director; }
    public ScenarioDefinition Definition() { return _table.Definition(); }

    public void Listen(Listener listener) {
        _listener = listener;
    }

    /**
     * @return The ruling on the decision a player is being asked for, or null if they are not being asked for one.
     */
    public Ruling Ruling(String player) {
        return _rulings.get(player);
    }

    @Override
    public void decisionIssued(String player, AwaitingDecision decision) {
        //The bid and the choice of seat come before the scenario's board exists, and the table answers them itself
        if (!_table.Arranged())
            return;

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
            //A mistake in the scenario must not take the game down with it: this is called from inside the game
            StackTraceElement[] trace = exp.getStackTrace();
            problem = "the script could not be applied: " + exp + (trace.length > 0 ? " at " + trace[0] : "");
            step = null;
            shown = null;
            answer = null;
        }
        Ruling ruling = new Ruling(player, decision, step, shown, answer, problem);
        _rulings.put(player, ruling);
        if (_listener != null)
            _listener.ruled(ruling);
    }

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
