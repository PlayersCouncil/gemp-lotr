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
 * A step can be a pause: something for the player to read before the decision is theirs to answer.  The decision a
 * pause applies to is ruled on but still not put to its player.  When whoever is running the game says the pause
 * has been read (Read), the script moves on and the decision is ruled on afresh, for the step behind the pause.
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
     * @param answer The answer the script gives, in the game's own terms; null if it gives none.  With no step, this is
     *               what the opponent's standing orders say.
     * @param problem Why the script could not be applied to the decision; null if it could.  This is always a
     *                mistake in the scenario: an answer the decision does not offer, a card that is not where the
     *                step expects.
     * @param noAnswer For the person's own decision: why the answer the script has for it could not be given; null
     *                 if it could, or the script has none.  That answer is only there for a run with nobody at the
     *                 table, and a person is free to have got here by another allowed route (playing the second
     *                 copy of a card first), so this is a mistake only for such a run.  What the step shows and
     *                 allows stands regardless.
     */
    public record Ruling(String player, AwaitingDecision decision, Step step, ScenarioDirector.Presentation shown,
                         String answer, String problem, String noAnswer) { }

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
        table.AskToPass(_director::asksToPass);
        table.OnNote(this::follow);
    }

    /**
     * Authoring: what a step says or allows has been changed while a decision it applies to is waiting.  Each such
     * decision is ruled on again for the same step (the script does not move), and whoever is listening is told.
     */
    public void Refresh() {
        for (Ruling ruling : new ArrayList<>(_rulings.values())) {
            Step step = ruling.step();
            if (step == null)
                continue;
            ScenarioDirector.Presentation shown;
            String answer = null;
            String problem = null;
            String noAnswer = null;
            try {
                shown = _director.present(step, ruling.player(), ruling.decision());
                try {
                    answer = _director.scriptedAnswer(step, ruling.decision());
                } catch (ScenarioScriptException exp) {
                    if (ruling.player().equals(_table.HumanPlayer()))
                        noAnswer = exp.getMessage();
                    else
                        problem = exp.getMessage();
                }
            } catch (ScenarioScriptException exp) {
                problem = exp.getMessage();
                shown = _director.presentUnrestricted(step);
            } catch (RuntimeException exp) {
                problem = "the script could not be applied: " + exp;
                shown = _director.presentUnrestricted(step);
            }
            Ruling again = new Ruling(ruling.player(), ruling.decision(), step, shown, answer, problem, noAnswer);
            _rulings.put(ruling.player(), again);
            tell(again);
        }
    }

    /**
     * Authoring: a step has been written for a decision that is already in front of its player and that the script
     * had nothing to say about.  Each such decision is ruled on afresh, the first of them that the step fits takes
     * it, and whoever is listening is told.  (A pause cannot be put in this way: its decision would have had to be
     * held back, and it has been asked.)
     * @return Whether a step took a decision.
     */
    public boolean Reconsider() {
        for (Ruling ruling : new ArrayList<>(_rulings.values())) {
            if (ruling.step() != null)
                return false;
        }
        for (Ruling ruling : new ArrayList<>(_rulings.values())) {
            if (_table.userFeedback().getAwaitingDecision(ruling.player()) != ruling.decision())
                continue;
            Ruling again = rule(ruling.player(), ruling.decision());
            if (again.step() == null)
                continue;
            _rulings.put(ruling.player(), again);
            tell(again);
            return true;
        }
        return false;
    }

    private static final String READ = "read";

    /**
     * The person has read a pause (a step that is read, not answered).  The script moves on, and the decision the
     * pause was read over, which has been held back all this time, is ruled on again and put to them: it belongs to
     * whichever step comes next.
     * @param choice The name they chose, if the pause ended in a choice.  Anything that is not one of its choices
     *               (null included) counts as the first of them.
     * @return Whether that player had such a pause in front of them.
     */
    public boolean Read(String player, int stepNumber, String choice) {
        Ruling ruling = _rulings.get(player);
        if (ruling == null || ruling.step() == null || !ruling.step().pause || ruling.step().number != stepNumber)
            return false;
        pass(ruling, choice);
        //(nothing in the game has changed, but as far as its decisions go this is the game coming to rest again)
        _table.userFeedback().atRest();
        return true;
    }

    /**
     * @return The choices a pause ends in, as name -> what its button says; empty if it does not end in one.
     */
    public static Map<String, String> ChoicesOf(Step step) {
        return step.pause && !step.messages.isEmpty() ? step.messages.getLast().choices : Map.of();
    }

    private void pass(Ruling ruling, String choice) {
        Step step = ruling.step();
        Map<String, String> choices = ChoicesOf(step);
        String chosen = choices.isEmpty() ? null : choices.containsKey(choice) ? choice : choices.keySet().iterator().next();
        //Written down so that a game following this one's journal reads the same pause the same way
        _table.Journal().note(ruling.player(), READ, step.number + (chosen == null ? "" : ":" + chosen));
        _director.read(step, chosen);
        _rulings.remove(ruling.player());
    }

    private void follow(com.gempukku.lotro.game.GameJournal.Entry note) {
        if (!note.detail().equals(READ))
            return;
        String[] parts = note.value().split(":", 2);
        Read(_table.Player(note.player().equals(Seat.FREEPS.key()) ? Seat.FREEPS : Seat.SHADOW),
                Integer.parseInt(parts[0]), parts.length > 1 ? parts[1] : null);
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
                //Still being read over: the decision stays where it is until the pause has been read
                if (before.step() != null && before.step().pause)
                    continue;
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
        //A pause is read before the decision it is over is put to anybody: the decision is held back, and comes
        // round again once the pause has been read (see Read)
        if (ruling.step() == null || !ruling.step().pause)
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
        String noAnswer = null;
        try {
            //Once a step has ended the scenario, the script has nothing more to say
            if (_director.end() == null)
                step = _director.match(player, decision);
            if (step != null) {
                try {
                    shown = _director.present(step, player, decision);
                } catch (ScenarioScriptException exp) {
                    problem = exp.getMessage();
                    shown = _director.presentUnrestricted(step);
                }
                if (problem == null) {
                    try {
                        answer = _director.scriptedAnswer(step, decision);
                    } catch (ScenarioScriptException exp) {
                        //The opponent has nothing but the script's answer to go on.  The person has their own.
                        if (player.equals(_table.HumanPlayer()))
                            noAnswer = exp.getMessage();
                        else
                            problem = exp.getMessage();
                    }
                }
            } else if (_director.end() == null && !player.equals(_table.HumanPlayer())) {
                //No step covers it: the opponent's standing orders may
                answer = _director.order(player, decision);
            }
        } catch (RuntimeException exp) {
            //A mistake in the scenario must not take the game down with it
            StackTraceElement[] trace = exp.getStackTrace();
            problem = "the script could not be applied: " + exp + (trace.length > 0 ? " at " + trace[0] : "");
            step = null;
            shown = null;
            answer = null;
            noAnswer = null;
        }
        return new Ruling(player, decision, step, shown, answer, problem, noAnswer);
    }

    @Override
    public void decisionIssued(String player, AwaitingDecision decision) { }

    @Override
    public void decisionAnswered(String player, AwaitingDecision decision, String answer) {
        Ruling ruling = _rulings.get(player);
        if (ruling == null || ruling.decision() != decision)
            return;
        //Answered over a pause that was never read: the answer belongs to the step behind it
        while (ruling.step() != null && ruling.step().pause) {
            pass(ruling, null);
            if (_director.end() != null)
                break;
            ruling = rule(player, decision);
        }
        _rulings.remove(player);
        if (ruling.step() != null)
            _director.answered(ruling.step());
        if (_listener != null)
            _listener.answered(ruling, answer);
    }
}
