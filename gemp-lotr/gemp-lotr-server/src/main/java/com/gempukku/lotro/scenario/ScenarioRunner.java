package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.logic.decisions.AwaitingDecision;
import com.gempukku.lotro.scenario.ScenarioDefinition.Coverage;
import com.gempukku.lotro.scenario.ScenarioDefinition.EndResult;
import com.gempukku.lotro.scenario.ScenarioDefinition.Fallback;
import com.gempukku.lotro.scenario.ScenarioDefinition.RngEntry;
import com.gempukku.lotro.scenario.ScenarioDefinition.RunnerLevel;
import com.gempukku.lotro.scenario.ScenarioDefinition.Step;

import java.util.List;
import java.util.Map;

/**
 * Plays a scenario through with nobody at the table: every decision, for both seats, is answered from the script.
 *
 * This is how a scenario is checked against the game it describes.  The run fails if the script and the game
 * disagree anywhere: a decision arrives that the current step does not expect, a scripted answer does not fit its
 * decision, a step allows something the game does not offer, a listed random outcome goes unused.  How far a run
 * can get depends on how fully the scenario is scripted, so the report also says how much was proved:
 *
 *     full       every decision was scripted and the scenario reached its end
 *     prefix     the run was correct as far as it went, then met a decision the script leaves open
 *     setup      the board was laid out and the first decision arrived
 *
 * A scenario whose reader is offered a choice of ways through it (a pause that ends in buttons) is played once for
 * every way there is, and has to be right on all of them.  A step that only some ways reach is no mistake; a step
 * that none reaches is.
 *
 * Nothing here needs a server: only the card library and the scenario's format.
 */
public final class ScenarioRunner {
    private ScenarioRunner() { }

    /** A scenario that asks for more decisions than this has a loop in it. */
    private static final int DECISION_LIMIT = 5000;

    public static final String HUMAN = "Player";
    public static final String OPPONENT = "~Scenario";

    public static ScenarioRunReport run(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format) {
        //Every way through: wherever a run met a choice, the ways it did not take are queued to be played as well
        java.util.ArrayDeque<List<String>> waiting = new java.util.ArrayDeque<>();
        waiting.add(List.of());
        List<ScenarioRunReport> runs = new java.util.ArrayList<>();
        while (!waiting.isEmpty() && runs.size() < WAYS_LIMIT) {
            List<String> forced = waiting.poll();
            ScenarioRunReport one = run(def, library, format, false, forced);
            runs.add(one);
            for (int i = forced.size(); i < one.taken.size(); i++)
                for (String other : one.offered.get(i))
                    if (!other.equals(one.taken.get(i))) {
                        List<String> way = new java.util.ArrayList<>(one.taken.subList(0, i));
                        way.add(other);
                        waiting.add(way);
                    }
        }
        for (ScenarioRunReport one : runs)
            if (one.completed && one.failures.isEmpty())
                one.reached = RunnerLevel.FULL;
        ScenarioRunReport report = runs.size() == 1 ? runs.getFirst() : ScenarioRunReport.together(def.id, runs);
        if (!waiting.isEmpty())
            report.failures.add("There are more than " + WAYS_LIMIT + " ways through this scenario; not all of them were played");
        if (report.completed) {
            for (Map.Entry<String, ScenarioDirector.StepStatus> entry : report.steps.entrySet()) {
                //A step of a block that no decision went to is nothing out of the ordinary, however the block (or
                // the game) ended: a game played in another order would have used it.  What is wrong with a block
                // that was never come to is said of the block.
                boolean inBlock = def.findStep(entry.getKey()).parent != null;
                if (inBlock && (entry.getValue() == ScenarioDirector.StepStatus.UNREACHED || entry.getValue() == ScenarioDirector.StepStatus.SKIPPED))
                    continue;
                if (entry.getValue() == ScenarioDirector.StepStatus.UNREACHED || entry.getValue() == ScenarioDirector.StepStatus.BYPASSED)
                    report.failures.add(def.findStep(entry.getKey()).describe() + " was never reached");
                else if (entry.getValue() == ScenarioDirector.StepStatus.SKIPPED)
                    report.warnings.add(def.findStep(entry.getKey()).describe() + " was skipped");
            }
        }
        report.warnings.addAll(shadowedByTheirBlock(def));
        //(a step no way reaches is a failure, and a scenario with a failure in it has not been proved)
        if (!report.failures.isEmpty() && report.reached == RunnerLevel.FULL)
            report.reached = RunnerLevel.SETUP;
        solutions(def, library, format, report);
        return report;
    }

    /**
     * What a run is played with: the cards in a puzzle's slots, and, for a run of one of its solutions, that solution,
     * whose answers are the person's wherever the script gives the runner none.  The scenario's own run is played with
     * its first solution's picks (and none of its answers): it is the script that it proves.
     */
    private record Playing(Map<String, String> picks, ScenarioDefinition.Solution solution) {
        static Playing script(ScenarioDefinition def) {
            return new Playing(def.solutions.isEmpty() ? Map.of() : def.solutions.getFirst().picks(), null);
        }
    }

    /**
     * The runner level 'solution': each of the scenario's solutions is played, its picks in the slots and its answers
     * given wherever the script leaves the person's decision open, and has to reach the scenario's win (or, for a
     * scored puzzle with no win, its end).  Each one's verdict goes in the report (solutions), its failures among the
     * report's, and its decisions after the scenario's own.  A puzzle with solutions is required to reach this level
     * (unless its options say otherwise), and reaches it once every solution does.
     */
    private static void solutions(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, ScenarioRunReport report) {
        if (def.solutions.isEmpty()) {
            if (!def.slots.isEmpty())
                report.failures.add("The puzzle has slots and no solution, so it was played with its slots empty.  List a solution (solutions: "
                        + "its picks, and the answers the script leaves to the person) for the runner to play it with");
            if (def.runnerLevel == RunnerLevel.SOLUTION)
                report.failures.add("options.runnerLevel is 'solution', and the scenario lists no solution to prove");
            return;
        }
        if (def.runnerLevel == null && def.kind == ScenarioDefinition.Kind.PUZZLE && report.required.compareTo(RunnerLevel.SOLUTION) < 0)
            report.required = RunnerLevel.SOLUTION;
        boolean all = true;
        for (ScenarioDefinition.Solution solution : def.solutions) {
            ScenarioRunReport run = run(def, library, format, false, List.of(), null, new Playing(solution.picks(), solution));
            boolean solved = run.completed && run.failures.isEmpty();
            String verdict = solved ? "solved (" + run.stopped + ")"
                    : run.failures.isEmpty() ? "does not solve it: " + run.stopped : "does not solve it: " + run.failures.getFirst();
            //(the likeliest reason a recorded solution stops fitting: the script came to answer one of its decisions)
            if (!solved && run.stepTookSolutionsDecision != null)
                verdict += ".  Probably because " + run.stepTookSolutionsDecision;
            report.solutions.put(solution.name(), solved ? null : verdict);
            report.decisions.add(new ScenarioRunReport.Decision(-1, "Solution " + solution.name() + ": " + verdict, null, null, null));
            report.decisions.addAll(run.decisions);
            if (!solved) {
                all = false;
                if (run.failures.isEmpty())
                    report.failures.add("solution '" + solution.name() + "' does not reach the scenario's win: " + run.stopped);
                for (String failure : run.failures)
                    report.failures.add("solution '" + solution.name() + "': " + failure);
                if (run.stepTookSolutionsDecision != null)
                    report.failures.add("solution '" + solution.name() + "': " + run.stepTookSolutionsDecision);
            }
        }
        if (all && report.failures.isEmpty() && report.reached.compareTo(RunnerLevel.SOLUTION) < 0)
            report.reached = RunnerLevel.SOLUTION;
    }

    /**
     * The steps of a block that wait for nothing but what ends the block: every decision they could take fits the
     * block's 'until' as well.  The decision that fits a block's 'until' is
     * the next step's, not the block's (that is what lets a block end), so such a step is never come to; except one
     * about a skirmish that is over ('after'), which is said at the block's last decision all the same (see
     * ScenarioDirector.lingering).  As designed, then, but a step written so is a mistake, and is said to be one.
     */
    static List<String> shadowedByTheirBlock(ScenarioDefinition def) {
        List<String> said = new java.util.ArrayList<>();
        for (Step block : def.steps) {
            if (!block.block)
                continue;
            for (Step child : block.children)
                if (child.when.after == null && coveredBy(child.when, block.until))
                    said.add(child.describe() + " waits for exactly what ends its block (until: " + block.until.source
                            + "), and that decision is the step after the block's: it is never come to.  Put it after the block, as a step of its own.");
        }
        return said;
    }

    /** Whether every decision that fits one 'when' fits another: it says all the other says, and maybe more. */
    private static boolean coveredBy(ScenarioDefinition.When one, ScenarioDefinition.When other) {
        return !other.opening
                && (other.seat == null || other.seat == one.seat)
                && (other.role == null || other.role == one.role)
                && (other.decision == null || other.decision == one.decision)
                && (other.text == null || (one.text != null && one.text.toLowerCase().contains(other.text.toLowerCase())))
                && (other.phase == null || other.phase == one.phase)
                && (other.turn == null || other.turn.equals(one.turn))
                && (other.skirmish == null || other.skirmish.equals(one.skirmish))
                && (other.offers == null || other.offers.equals(one.offers))
                && (other.fighting == null || other.fighting.equals(one.fighting))
                && (other.after == null || other.after.equals(one.after))
                && one.kinds.containsAll(other.kinds) && one.notKinds.containsAll(other.notKinds)
                && (other.require == null || other.require.equals(one.require));
    }

    /** A scenario with more ways through it than this is not played through all of them. */
    private static final int WAYS_LIMIT = 64;

    /** How many other answers are tried at one decision, and in all. */
    private static final int OTHERS_AT_ONE = 8;
    private static final int OTHERS_IN_ALL = 200;

    /**
     * What a player might do instead of what the script does: wherever a step would take another answer than the
     * script's (one it allows, or anything, where it allows anything), the scenario is played again, the same way up
     * to there, with that answer instead, and then on until the script comes to its next step.  What the script
     * cannot handle on the way there is a fault: a decision no step covers, an answer of the opponent's that no
     * longer fits, a step that allows nothing that is on offer, the game going wrong.  The scenario ending (as it
     * says, or with the game won or lost) counts as handled.  Once answered another way, the player's later answers
     * are the script's where they still fit, and otherwise what the step allows, or as little as can be done.
     * @return One line for each fault, with the other answers that came to it.
     */
    public static OffScriptReport offScript(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format) {
        OffScriptReport result = new OffScriptReport();
        //Every way through, as the script plays it
        java.util.ArrayDeque<List<String>> waiting = new java.util.ArrayDeque<>();
        waiting.add(List.of());
        List<ScenarioRunReport> runs = new java.util.ArrayList<>();
        while (!waiting.isEmpty() && runs.size() < WAYS_LIMIT) {
            List<String> forced = waiting.poll();
            ScenarioRunReport one = run(def, library, format, false, forced);
            runs.add(one);
            for (int i = forced.size(); i < one.taken.size(); i++)
                for (String other : one.offered.get(i))
                    if (!other.equals(one.taken.get(i))) {
                        List<String> way = new java.util.ArrayList<>(one.taken.subList(0, i));
                        way.add(other);
                        waiting.add(way);
                    }
        }
        Map<String, List<String>> faults = new java.util.LinkedHashMap<>();
        java.util.Set<String> tried = new java.util.HashSet<>();
        for (ScenarioRunReport way : runs) {
            //(a way through that does not play through as it is can still be tried another way up to where it stops)
            if (!way.failures.isEmpty())
                result.notTried.add("A way through stops (" + way.stopped + "): nothing after that was tried");
            for (ScenarioRunReport.OffScriptPoint point : way.offScriptPoints) {
                int atThis = 0;
                for (String[] other : point.others()) {
                    //(the same decision on two ways through, answered the same other way, is one try)
                    if (!tried.add(point.step() + "|" + point.decision() + "|" + other[1]))
                        continue;
                    if (atThis++ >= OTHERS_AT_ONE) {
                        result.notTried.add("step " + point.step() + ": only " + OTHERS_AT_ONE + " of the " + point.others().size() + " other answers to \""
                                + point.decision() + "\" were tried");
                        break;
                    }
                    if (result.tries >= OTHERS_IN_ALL) {
                        result.notTried.add("More than " + OTHERS_IN_ALL + " other answers in all: the rest were not tried");
                        return result.with(faults);
                    }
                    result.tries++;
                    ScenarioRunReport run = run(def, library, format, false, way.taken, new Deviation(point.number(), other[0], point.top(), point.decision()),
                            Playing.script(def));
                    //(nor is what goes wrong on the way through as the script plays it: that is said by Check)
                    if (run.failures.isEmpty() || way.failures.contains(run.failures.getFirst()))
                        continue;
                    if ("not replayed".equals(run.stopped)) {
                        result.notTried.add("step " + point.step() + ": " + other[1] + " could not be tried (the run did not come to the same decision again)");
                        continue;
                    }
                    //By the decision answered another way: what each other answer came to
                    String where = "step " + point.step() + ", " + brief(point.decision()) + ", where the script answers " + point.answer() + ":";
                    faults.computeIfAbsent(where, x -> new java.util.ArrayList<>()).add(other[1] + ": " + brief(run.failures.getFirst()));
                }
            }
        }
        return result.with(faults);
    }

    /** A decision as it is described, without the list of what it offers (which the other answer says enough of). */
    private static String brief(String described) {
        return described.replaceAll(" offering: .*?(  \\[turn)", "$1").replaceAll(" offering nothing but Pass", "");
    }

    /** What offScript found. */
    public static final class OffScriptReport {
        /** How many other answers were tried. */
        public int tries;
        /** For each decision where another answer came to a fault: each such answer, and the fault. */
        public final Map<String, List<String>> faults = new java.util.LinkedHashMap<>();
        /** What could not be tried, and why. */
        public final List<String> notTried = new java.util.ArrayList<>();

        private OffScriptReport with(Map<String, List<String>> found) {
            faults.putAll(found);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(tries).append(" other answer(s) tried; ").append(faults.isEmpty() ? "the script handled them all."
                    : "at " + faults.size() + " decision(s), some were not handled:");
            for (Map.Entry<String, List<String>> fault : faults.entrySet()) {
                sb.append("\n\n").append(fault.getKey());
                for (String how : fault.getValue())
                    sb.append("\n  - ").append(how);
            }
            for (String one : notTried)
                sb.append("\n").append(one);
            return sb.toString();
        }
    }

    /**
     * Plays a scenario as far as its script goes and then keeps going, answering every decision the script does not
     * cover by doing as little as possible (passing, choosing the minimum).  The report's list of decisions is then
     * a map of what the game will ask for beyond the script, which is what somebody writing the next step needs.
     * A report from here says nothing about whether the scenario is correct.
     */
    public static ScenarioRunReport explore(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format) {
        return explore(def, library, format, List.of());
    }

    /**
     * @param choices What to choose at each choice the run comes to, in order; where this runs out, the first thing
     *                on offer is chosen.
     */
    public static ScenarioRunReport explore(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, List<String> choices) {
        ScenarioRunReport explored = run(def, library, format, true, choices);
        //An exploring run plays on where the script would have stopped, so what it proves is nothing: the verdict
        // that means something is the scenario's own run, as its test plays it, and that is what the report gives
        explored.exploring = run(def, library, format);
        return explored;
    }

    /** In an exploring run, how many decisions to answer beyond the script before stopping. */
    private static final int EXPLORE_LIMIT = 60;

    private static ScenarioRunReport run(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, boolean explore, List<String> choices) {
        return run(def, library, format, explore, choices, null, Playing.script(def));
    }

    /**
     * A run that follows the script up to one of the player's decisions, answers that one another way, and then
     * plays on until the script has come to its next step.
     * @param at The decision's place in the run.
     * @param answer The other answer, as the game takes it.
     * @param top The number of the step of the script the decision belongs to: coming to a later one is the end of it.
     * @param decision The decision as the run that followed the script described it, to be sure this run is at the same.
     */
    private record Deviation(int at, String answer, int top, String decision) { }

    private static ScenarioRunReport run(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, boolean explore, List<String> choices,
                                         Deviation deviation, Playing playing) {
        ScenarioRunReport report = new ScenarioRunReport(def.id);
        report.required = def.runnerLevel != null ? def.runnerLevel
                : def.coverageInForce() == Coverage.STRICT ? RunnerLevel.FULL : RunnerLevel.PREFIX;

        List<String> invalid = ScenarioValidator.validate(def, library);
        if (!invalid.isEmpty()) {
            report.failures.addAll(invalid);
            report.stopped = "the scenario does not fit the card library, so it was not played";
            return report;
        }

        ScenarioTable table = new ScenarioTable(def, library, format, HUMAN, OPPONENT);
        ScenarioSession session = new ScenarioSession(table);
        ScenarioDirector director = session.Director();
        //(a sandbox whose other seat begins with the bot is played as fallback: bot is: see the opponent's decisions below)
        if (def.kind == ScenarioDefinition.Kind.SANDBOX && def.otherSeatInForce() == ScenarioDefinition.OtherSeat.BOT)
            director.FallbackInstead(Fallback.BOT);
        table.Picks(playing.picks());
        try {
            table.Start();
        } catch (RuntimeException exp) {
            report.failures.add("The board could not be laid out: " + exp);
            report.stopped = "before the first decision";
            return report;
        }

        boolean completed = false;
        try {
            completed = play(def, session, report, explore, choices, deviation, playing);
        } catch (RuntimeException exp) {
            report.failures.add("The game threw " + exp + rootCause(exp));
            report.stopped = "the game threw an exception";
        }

        report.steps.putAll(director.statuses());
        for (Step step : def.allSteps())
            report.stepDescriptions.put(step.label(), step.block
                    ? (step.when.source.isEmpty() ? "" : step.when.source + "; ") + "any order, until " + step.until.source : step.when.source);
        for (String problem : table.Problems())
            report.failures.add(problem);

        if (deviation == null && !explore && playing.solution() == null && report.failures.isEmpty())
            refusalsNotShown(def, report);
        if (completed && deviation == null)
            for (RngEntry entry : table.Random().remaining())
                report.failures.add("rng: " + entry.kind().name().toLowerCase() + "(" + entry.seat().key() + "." + entry.zone()
                        + ": " + String.join(", ", entry.result()) + ") was never used");
        //Which steps the scenario as a whole never reaches is judged once every way through it has been played
        report.completed = completed;
        return report;
    }

    /**
     * @return Whether the scenario reached its end.
     */
    private static boolean play(ScenarioDefinition def, ScenarioSession session, ScenarioRunReport report, boolean explore, List<String> choices,
                                Deviation deviation, Playing playing) {
        ScenarioTable table = session.Table();
        ScenarioDirector director = session.Director();
        //(a sandbox is always guided: see ScenarioDefinition.coverageInForce)
        boolean strict = def.coverageInForce() == Coverage.STRICT;
        int unscripted = 0;
        //(a solution's answers, the next of which is the person's next open decision's)
        ScenarioDefinition.Solution solution = playing.solution();
        int taped = 0;

        for (int number = 1; number <= DECISION_LIMIT; number++) {
            if (table.GameIsFinished() && table.PathEnded() != null) {
                //Not the game's own ending: the scenario's path ran out (see ScenarioTable.EndOfThePath)
                report.stopped = "the game was stopped: " + table.PathEnded();
                if (!explore)
                    report.failures.add("The game was stopped before the scenario ended: " + table.PathEnded()
                            + ".  Give the path more sites (sites), or have the scenario end before then (end, or outcome.win)");
                return false;
            }
            if (table.SeatingProblem() != null) {
                //The game's own opening seated the players otherwise than the scenario declares them: from here on
                // nothing the script says of its seats is about the right chair
                report.stopped = "the opening seated the players the other way round";
                report.failures.add("The game's own opening (setup.opening: game) did not seat the players as the scenario declares them: "
                        + table.SeatingProblem());
                return false;
            }
            if (table.Breach() != null) {
                report.stopped = "the person broke the puzzle's rules: " + table.Breach();
                if (deviation != null)
                    return true;
                report.failures.add("The path breaks the puzzle's rules: " + table.Breach());
                return false;
            }
            if (table.Verdict() != null) {
                //The judging moment came (outcome.judge): that is the end, whatever the game did after it
                ScenarioTable.Verdict verdict = table.Verdict();
                report.stopped = "judged at " + verdict.moment() + ": " + (verdict.won() ? "won" : "failed")
                        + (verdict.score() == null ? "" : ", scoring " + verdict.score());
                if (verdict.won() || deviation != null)
                    return true;
                report.failures.add("The scripted path is judged failed at " + verdict.moment());
                return false;
            }
            if (table.GameIsFinished()) {
                String winner = table.game().getWinnerPlayerId();
                report.stopped = "the game ended" + (winner == null ? " with no winner" : ": the " + table.SeatOf(winner).key() + " seat won");
                //(answered another way, the game ending is the game handling it: the player is shown how it ended)
                if (deviation != null)
                    return true;
                //(the scenario can say what the game's own end counts as: outcome.gameWon, outcome.gameLost)
                boolean won = winner != null && winner.equals(table.HumanPlayer());
                ScenarioDefinition.End ending = won ? def.outcome.gameWon : def.outcome.gameLost;
                if (ending != null && director.end() == null) {
                    report.stopped += " (outcome." + (won ? "gameWon" : "gameLost") + ": " + ending.result.name().toLowerCase() + ")";
                    if (ending.result == EndResult.FAIL) {
                        report.failures.add("The scripted path ends in failure (" + report.stopped + ")");
                        return false;
                    }
                    return true;
                }
                //(a scored puzzle with no 'win' is complete once it has been played to its end, however that went)
                if (LiveScenario.scoredToTheEnd(def) && director.end() == null) {
                    Integer score = table.Score();
                    report.stopped += score == null ? " (a scored end)" : " (a scored end: " + score + ")";
                    return true;
                }
                if (def.outcome.win == null && director.end() == null) {
                    report.failures.add("The game ended before the scenario did (" + report.stopped + ")");
                    return false;
                }
                return winner != null && winner.equals(table.HumanPlayer());
            }

            //The session ruled on whatever the game is waiting on when the game stopped.  A decision with no ruling is
            // waiting for the step in hand to be answered first, so it is not this one's turn.
            String player = session.Ruling(table.P1()) != null ? table.P1() : session.Ruling(table.P2()) != null ? table.P2() : null;
            if (player == null) {
                report.failures.add(table.FreepsAnyDecisionsAvailable() || table.ShadowAnyDecisionsAvailable()
                        ? "The game is waiting on a decision the script never got round to ruling on"
                        : "The game is waiting on nobody, and has not ended");
                report.stopped = "the game stalled";
                return false;
            }
            ScenarioSession.Ruling ruling = session.Ruling(player);
            AwaitingDecision decision = ruling.decision();
            boolean human = player.equals(table.HumanPlayer());
            Step step = ruling.step();
            String description = director.describe(player, decision);
            String answer;
            String note = null;
            //Answered another way: done once the script has come to a later step (it handled what was done)
            boolean deviated = deviation != null && number > deviation.at();
            if (deviated && step != null && (step.parent != null ? step.parent.number : step.number) > deviation.top()) {
                report.stopped = "came to step " + step.label();
                return true;
            }

            //What the step says, with its numbers filled in from the game as it stands: worth reading against the
            // decisions around it, since nothing else checks that the words are true
            if (step != null && ruling.shown() != null)
                for (ScenarioDirector.RenderedMessage message : ruling.shown().messages())
                    report.decisions.add(new ScenarioRunReport.Decision(-1, "      says: " + message.text(), null, null, null));

            if (step != null && step.pause && ruling.problem() == null) {
                //Nothing is answered: the pause is read (choosing, if it ends in a choice), and the same decision
                // comes round again for the step behind it
                Map<String, String> offered = ScenarioSession.ChoicesOf(step);
                String choice = null;
                if (!offered.isEmpty()) {
                    int position = report.taken.size();
                    choice = position < choices.size() && offered.containsKey(choices.get(position)) ? choices.get(position)
                            : offered.keySet().iterator().next();
                    report.taken.add(choice);
                    report.offered.add(new java.util.ArrayList<>(offered.keySet()));
                }
                report.decisions.add(new ScenarioRunReport.Decision(number, description, step.label(), null,
                        choice == null ? "(a pause: read)" : "(a pause: read, choosing '" + choice + "')"));
                session.Read(player, step.number, choice);
                if (director.end() != null) {
                    report.stopped = "the scenario ended (" + director.end().result.name().toLowerCase() + ")";
                    //(answered another way, an ending the scenario has for it is the scenario handling it)
                    if (director.end().result == EndResult.FAIL && deviation == null) {
                        report.failures.add("The scripted path ends in failure");
                        return false;
                    }
                    return true;
                }
                continue;
            }

            if (deviation != null && number == deviation.at()) {
                if (!human || !description.equals(deviation.decision())) {
                    report.failures.add("The run did not come to the same decision again (" + description + "), so the other answer was not tried");
                    report.stopped = "not replayed";
                    return false;
                }
                report.decisions.add(new ScenarioRunReport.Decision(number, description, step == null ? null : step.label(), deviation.answer(),
                        "(answered another way)"));
                try {
                    table.PlayerDecided(player, deviation.answer());
                } catch (RuntimeException exp) {
                    report.failures.add("the game rejected the answer \"" + deviation.answer() + "\" to: " + description);
                    report.stopped = "the game rejected an answer";
                    return false;
                }
                if (director.end() != null) {
                    report.stopped = "the scenario ended (" + director.end().result.name().toLowerCase() + ")";
                    return true;
                }
                continue;
            }

            //A solution's run: the person's decision that the script gives no answer for takes the solution's next answer
            if (solution != null && human && ruling.problem() == null && (ruling.answer() == null || ruling.noAnswer() != null)) {
                if (taped < solution.answers().size()) {
                    ScenarioDefinition.Answer next = solution.answers().get(taped++);
                    try {
                        answer = AnswerCompiler.compile(table, decision, next);
                    } catch (ScenarioScriptException exp) {
                        report.decisions.add(new ScenarioRunReport.Decision(number, description, step == null ? null : step.label(), null, null));
                        report.failures.add("its answer " + taped + ", " + next.source() + ", does not fit the decision: " + exp.getMessage());
                        report.stopped = "the solution does not fit the game";
                        return false;
                    }
                    note = "(the solution's answer " + taped + ": " + next.source() + ")";
                } else {
                    answer = AnswerCompiler.leastAnswer(decision);
                    note = "(the solution's answers have run out: as little as can be done)";
                }
                if (step != null && ruling.shown() != null && !ScenarioDirector.allowsAnswer(ruling.shown(), decision, answer)) {
                    report.decisions.add(new ScenarioRunReport.Decision(number, description, step.label(), null, null));
                    report.failures.add("step " + step.label() + ": the solution's " + (note.contains("answer " + taped + ":")
                            ? "answer " + taped + " (" + solution.answers().get(taped - 1).source() + ")" : "pass") + " is not one the step allows, "
                            + "so the player's page would refuse it.  The step allows " + allowedSaid(table, decision, ruling.shown()));
                    report.stopped = "the solution does not fit the script";
                    return false;
                }
                report.decisions.add(new ScenarioRunReport.Decision(number, description, step == null ? null : step.label(), answer, note));
                try {
                    table.PlayerDecided(player, answer);
                } catch (RuntimeException exp) {
                    report.failures.add("the game rejected the answer \"" + answer + "\" (" + note + ") to: " + description);
                    report.stopped = "the game rejected an answer";
                    return false;
                }
                if (table.Breach() != null)
                    continue;
                if (endedHere(def, table, director, report, deviation))
                    return director.end() == null || director.end().result != EndResult.FAIL;
                if (def.outcome.lose != null && table.Holds(def.outcome.lose, table.HumanPlayer())) {
                    report.stopped = "the scenario's lose condition came true";
                    report.failures.add("The solution meets the scenario's lose condition");
                    return false;
                }
                if (def.outcome.win != null && table.Holds(def.outcome.win, table.HumanPlayer())) {
                    report.stopped = "the scenario's win condition came true";
                    return true;
                }
                continue;
            }

            try {
                if (ruling.problem() != null)
                    throw new ScenarioScriptException(ruling.problem());
                //Once answered another way, the player's later answers are whatever fits: what the script says if it
                // still can be, or else something the step allows, or else as little as can be done
                if (deviated && human && step != null && (ruling.noAnswer() != null || ruling.answer() == null)) {
                    ScenarioDirector.Presentation shown = ruling.shown();
                    answer = shown != null && shown.allowed() != null && !shown.allowed().isEmpty() ? shown.allowed().getFirst()
                            : AnswerCompiler.leastAnswer(decision);
                    report.decisions.add(new ScenarioRunReport.Decision(number, description, step.label(), answer, "(answered another way earlier: what fits)"));
                    table.PlayerDecided(player, answer);
                    continue;
                }
                //With nobody at the table, the script's answer is the only answer there is
                if (ruling.noAnswer() != null)
                    throw new ScenarioScriptException(ruling.noAnswer());
                if (step != null) {
                    ScenarioDirector.Presentation shown = ruling.shown();
                    if (shown.allowed() != null && decision.getDecisionType() != com.gempukku.lotro.logic.decisions.AwaitingDecisionType.ASSIGN_MINIONS) {
                        //Worth knowing when writing the step's text: what the player could do here but is asked not to
                        int withheld = AnswerCompiler.options(table, decision).size() - shown.allowed().size();
                        if (withheld > 0)
                            note = "(" + withheld + " thing(s) on offer that the step does not allow)";
                    }
                    answer = ruling.answer();
                    if (answer == null && explore) {
                        answer = AnswerCompiler.leastAnswer(decision);
                        note = "(exploring: the step gives no answer, so did as little as possible)";
                    } else if (answer == null) {
                        report.decisions.add(new ScenarioRunReport.Decision(number, description, step.label(), null, null));
                        report.stopped = "step " + step.label() + " leaves the choice open and gives the runner no answer";
                        if (strict)
                            report.failures.add("step " + step.label() + " has no answer for the runner.  Give it an 'answer', or an 'allow' with exactly one entry.");
                        else
                            report.reached = RunnerLevel.PREFIX;
                        return false;
                    }
                } else if (ruling.answer() != null) {
                    answer = ruling.answer();
                    ScenarioDirector.OrderMatch order = director.standingOrder(player, decision);
                    note = order != null ? "(no step covers this; one of the opponent's standing orders does: " + order.order().label(order.index() + 1) + ")"
                            : "(no step covers this; the opponent does it by rule: " + (decision.getDecisionType() == com.gempukku.lotro.logic.decisions.AwaitingDecisionType.ASSIGN_MINIONS
                            ? "its leftovers" : def.fallback == Fallback.EAGER ? "eager, taking all it can" : "the sandbox") + ")";
                } else if (explore) {
                    if (++unscripted > EXPLORE_LIMIT) {
                        report.stopped = "explored " + EXPLORE_LIMIT + " decisions beyond the script";
                        return false;
                    }
                    answer = AnswerCompiler.leastAnswer(decision);
                    note = "(exploring: no step covers this, so did as little as possible)";
                } else if (human) {
                    report.decisions.add(new ScenarioRunReport.Decision(number, description, null, null, null));
                    report.stopped = "a decision for the player that no step covers";
                    if (strict)
                        report.failures.add("No step covers this decision (the script is waiting on "
                                + (director.currentStep() == null ? "nothing: every step has been used" : director.currentStep().describe())
                                + "): " + description);
                    else
                        report.reached = RunnerLevel.PREFIX;
                    return false;
                } else if ((def.fallbackInForce() == Fallback.PASS || def.fallbackInForce() == Fallback.SANDBOX)
                        && (def.kind != ScenarioDefinition.Kind.SANDBOX || def.otherSeatInForce() == ScenarioDefinition.OtherSeat.SCRIPT)) {
                    answer = AnswerCompiler.leastAnswer(decision);
                    note = "(opponent fallback: " + (def.fallback == Fallback.PASS ? "pass" : "sandbox, with no rule for this: as little as it can") + ")";
                } else {
                    report.decisions.add(new ScenarioRunReport.Decision(number, description, null, null, null));
                    report.stopped = "a decision for the opponent that no step covers";
                    //(the bot, or a sandbox's person in the other seat, answers here: proved as far as the script can be)
                    if ((def.fallback == Fallback.BOT || (def.kind == ScenarioDefinition.Kind.SANDBOX
                            && def.otherSeatInForce() != ScenarioDefinition.OtherSeat.SCRIPT)) && !strict)
                        report.reached = RunnerLevel.PREFIX;
                    else
                        report.failures.add("No step covers this decision for the opponent (the script is waiting on "
                                + (director.currentStep() == null ? "nothing: every step has been used" : director.currentStep().describe())
                                + "): " + description);
                    return false;
                }
            } catch (ScenarioScriptException exp) {
                report.decisions.add(new ScenarioRunReport.Decision(number, description, step == null ? null : step.label(), null, null));
                report.failures.add((step == null ? "" : "step " + step.label() + ": ") + exp.getMessage());
                report.stopped = "the script does not fit the game";
                return false;
            }

            //A solution's run, at a decision of the person's that a step answers: the solution's next answer goes to
            // a later decision, which is worth saying if it then fails (see ScenarioRunReport.stepTookSolutionsDecision)
            if (solution != null && human && step != null && answer != null && taped < solution.answers().size()
                    && report.stepTookSolutionsDecision == null) {
                ScenarioDefinition.Answer next = solution.answers().get(taped);
                try {
                    String instead = AnswerCompiler.compile(table, decision, next);
                    if (!instead.equals(answer))
                        report.stepTookSolutionsDecision = step.describe() + " answered " + said(table, decision, answer)
                                + " to \"" + decision.getText() + "\" by itself, where the solution's answer " + (taped + 1) + ", " + next.source()
                                + ", would have gone.  A solution's answers go only to the decisions the script leaves open, so a step with "
                                + "one allowed answer takes its decision first (\"Write down what I do\" writes such a step for every pass): "
                                + "take the step out, or let it allow more than that one answer";
                } catch (ScenarioScriptException | RuntimeException exp) {
                    //(the solution's answer would not have fitted here either: nothing taken from it)
                }
            }

            //The player's scripted answer is held to what the page would let them do, and what the step refuses is
            // shown to be refused
            if (deviation == null && human && step != null && ruling.shown() != null && answer != null) {
                String refused = refusals(table, director, step, ruling, report);
                if (refused == null && !ScenarioDirector.allowsAnswer(ruling.shown(), decision, answer))
                    refused = "its answer " + said(table, decision, answer) + " (the " + ordinal(director.timesFor(step) + 1)
                            + " decision of the step) is not one its 'allow' lets the player give here, so the player's page would refuse it."
                            + "  The step allows " + allowedSaid(table, decision, ruling.shown()) + ".  The decision is: " + description;
                if (refused != null) {
                    report.decisions.add(new ScenarioRunReport.Decision(number, description, step.label(), null, null));
                    report.failures.add("step " + step.label() + ": " + refused);
                    report.stopped = "the script does not fit the game";
                    return false;
                }
            }

            //Where the player could have done otherwise, as the step has it: for trying those other ways (see offScript)
            if (deviation == null && !explore && solution == null && human && step != null && ruling.shown() != null && answer != null
                    && decision.getDecisionType() != com.gempukku.lotro.logic.decisions.AwaitingDecisionType.ASSIGN_MINIONS) {
                ScenarioDirector.Presentation shown = ruling.shown();
                List<String> others = new java.util.ArrayList<>();
                if (shown.allowed() == null) {
                    others.addAll(AnswerCompiler.alternatives(decision));
                } else {
                    for (String one : shown.allowed())
                        if (!others.contains(one))
                            others.add(one);
                    if (shown.passAllowed() && !others.contains(""))
                        others.add("");
                }
                final String given = answer;
                others.removeIf(one -> one.equals(given));
                if (!others.isEmpty()) {
                    List<String[]> said = new java.util.ArrayList<>();
                    for (String one : others) {
                        String term;
                        try {
                            term = AnswerCompiler.decompile(table, decision, one);
                        } catch (RuntimeException exp) {
                            term = "raw(" + one + ")";
                        }
                        said.add(new String[] {one, term});
                    }
                    String scripted;
                    try {
                        scripted = AnswerCompiler.decompile(table, decision, answer);
                    } catch (RuntimeException exp) {
                        scripted = "raw(" + answer + ")";
                    }
                    report.offScriptPoints.add(new ScenarioRunReport.OffScriptPoint(number, step.label(),
                            step.parent != null ? step.parent.number : step.number, description, scripted, said));
                }
            }

            report.decisions.add(new ScenarioRunReport.Decision(number, description, step == null ? null : step.label(), answer, note));
            try {
                table.PlayerDecided(player, answer);
            } catch (RuntimeException exp) {
                boolean rejected = exp.getCause() instanceof com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
                report.failures.add((step == null ? "" : "step " + step.label() + ": ")
                        + (rejected ? "the game rejected the answer \"" + answer + "\" to: " + description
                                : "the game threw " + exp + rootCause(exp) + " after the answer \"" + answer + "\" to: " + description));
                report.stopped = rejected ? "the game rejected an answer" : "the game threw an exception";
                return false;
            }

            //(what the puzzle's rules forbid comes before whatever else the answer led to: the loop's top says it)
            if (table.Breach() != null)
                continue;
            if (director.end() != null) {
                report.stopped = "the scenario ended (" + director.end().result.name().toLowerCase() + ")";
                if (director.end().result == EndResult.FAIL && deviation == null) {
                    report.failures.add("The scripted path ends in failure");
                    return false;
                }
                return true;
            }
            if (def.outcome.lose != null && table.Holds(def.outcome.lose, table.HumanPlayer())) {
                report.stopped = "the scenario's lose condition came true";
                //(answered another way, losing as the scenario says is the scenario handling it)
                if (deviation != null)
                    return true;
                report.failures.add("The scripted path meets the scenario's lose condition");
                return false;
            }
            if (def.outcome.win != null && table.Holds(def.outcome.win, table.HumanPlayer())) {
                report.stopped = "the scenario's win condition came true";
                return true;
            }
        }
        report.failures.add("More than " + DECISION_LIMIT + " decisions: the script is probably going round in circles");
        report.stopped = "too many decisions";
        return false;
    }

    /**
     * Shows what a step refuses at this decision to be refused (see ScenarioDefinition.Refusal), and notes each that
     * is, in the report.
     * @return Why one of them is not; null if they all are (or none is about this decision).
     */
    private static String refusals(ScenarioTable table, ScenarioDirector director, Step step, ScenarioSession.Ruling ruling, ScenarioRunReport report) {
        int at = director.timesFor(step) + 1;
        for (ScenarioDefinition.Refusal refusal : step.refuses) {
            if (refusal.at() != 0 && refusal.at() != at)
                continue;
            List<String> compiled;
            try {
                compiled = AnswerCompiler.compileAll(table, ruling.decision(), refusal.answer());
            } catch (ScenarioScriptException exp) {
                //(said of every decision of the step: it need only be on offer at one of them)
                if (refusal.at() == 0)
                    continue;
                return "it refuses '" + refusal.source() + "', but the game does not offer that at the step's " + ordinal(at)
                        + " decision, so there is nothing to refuse: " + exp.getMessage();
            }
            for (String one : compiled)
                if (ScenarioDirector.allowsAnswer(ruling.shown(), ruling.decision(), one))
                    return "it refuses '" + refusal.source() + "', but at its " + ordinal(at) + " decision its 'allow' lets the player give "
                            + said(table, ruling.decision(), one) + ".  The step allows " + allowedSaid(table, ruling.decision(), ruling.shown())
                            + ".  The decision is: " + director.describe(ruling.player(), ruling.decision());
            report.refusalsShown.add(step.label() + " " + refusal.source());
        }
        return null;
    }

    /** What a step's refusals came to over a whole run: each that it never got to show is a failure. */
    private static void refusalsNotShown(ScenarioDefinition def, ScenarioRunReport report) {
        for (Step step : def.allSteps()) {
            if (step.refuses.isEmpty() || report.steps.get(step.label()) != ScenarioDirector.StepStatus.MATCHED)
                continue;
            for (ScenarioDefinition.Refusal refusal : step.refuses)
                if (!report.refusalsShown.contains(step.label() + " " + refusal.source()))
                    report.failures.add("step " + step.label() + ": it refuses '" + refusal.source() + "', which was never shown: "
                            + (refusal.at() == 0 ? "the game never offered it at any of the step's decisions"
                            : "the step never came to its " + ordinal(refusal.at()) + " decision (or the run stopped before it)"));
        }
    }

    private static String said(ScenarioTable table, AwaitingDecision decision, String answer) {
        try {
            return "'" + AnswerCompiler.decompile(table, decision, answer) + "'";
        } catch (RuntimeException exp) {
            return "\"" + answer + "\"";
        }
    }

    private static String allowedSaid(ScenarioTable table, AwaitingDecision decision, ScenarioDirector.Presentation shown) {
        List<String> all = new java.util.ArrayList<>();
        for (String one : shown.allowed())
            all.add(said(table, decision, one));
        if (shown.passAllowed())
            all.add("'pass'");
        return all.isEmpty() ? "nothing on offer" : String.join(", ", all);
    }

    private static String ordinal(int number) {
        int lastTwo = number % 100;
        String suffix = lastTwo >= 11 && lastTwo <= 13 ? "th" : switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
        return number + suffix;
    }

    private static String rootCause(Throwable exp) {
        Throwable cause = exp;
        while (cause.getCause() != null && cause.getCause() != cause)
            cause = cause.getCause();
        StackTraceElement[] trace = cause.getStackTrace();
        return (cause == exp ? "" : " (caused by " + cause + ")") + (trace.length > 0 ? " at " + trace[0] : "");
    }

    /**
     * Whether the scenario ended with that answer by a step of its own ('end'), saying so in the report; a step that
     * ends it in failure is a failure of the run.
     */
    private static boolean endedHere(ScenarioDefinition def, ScenarioTable table, ScenarioDirector director, ScenarioRunReport report, Deviation deviation) {
        if (director.end() == null)
            return false;
        report.stopped = "the scenario ended (" + director.end().result.name().toLowerCase() + ")";
        if (director.end().result == EndResult.FAIL && deviation == null)
            report.failures.add("The path ends in failure");
        return true;
    }
}
