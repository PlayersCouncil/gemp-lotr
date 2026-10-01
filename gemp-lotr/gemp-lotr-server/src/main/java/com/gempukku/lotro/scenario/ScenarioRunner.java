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
 * Nothing here needs a server: only the card library and the scenario's format.
 */
public final class ScenarioRunner {
    private ScenarioRunner() { }

    /** A scenario that asks for more decisions than this has a loop in it. */
    private static final int DECISION_LIMIT = 5000;

    public static final String HUMAN = "Player";
    public static final String OPPONENT = "~Scenario";

    public static ScenarioRunReport run(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format) {
        return run(def, library, format, false);
    }

    /**
     * Plays a scenario as far as its script goes and then keeps going, answering every decision the script does not
     * cover by doing as little as possible (passing, choosing the minimum).  The report's list of decisions is then
     * a map of what the game will ask for beyond the script, which is what somebody writing the next step needs.
     * A report from here says nothing about whether the scenario is correct.
     */
    public static ScenarioRunReport explore(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format) {
        return run(def, library, format, true);
    }

    /** In an exploring run, how many decisions to answer beyond the script before stopping. */
    private static final int EXPLORE_LIMIT = 60;

    private static ScenarioRunReport run(ScenarioDefinition def, LotroCardBlueprintLibrary library, LotroFormat format, boolean explore) {
        ScenarioRunReport report = new ScenarioRunReport(def.id);
        report.required = def.runnerLevel != null ? def.runnerLevel
                : def.coverage == Coverage.STRICT ? RunnerLevel.FULL : RunnerLevel.PREFIX;

        List<String> invalid = ScenarioValidator.validate(def, library);
        if (!invalid.isEmpty()) {
            report.failures.addAll(invalid);
            report.stopped = "the scenario does not fit the card library, so it was not played";
            return report;
        }

        ScenarioTable table = new ScenarioTable(def, library, format, HUMAN, OPPONENT);
        ScenarioDirector director;
        try {
            table.Start();
            director = new ScenarioDirector(table);
        } catch (RuntimeException exp) {
            report.failures.add("The board could not be laid out: " + exp);
            report.stopped = "before the first decision";
            return report;
        }

        boolean completed = false;
        try {
            completed = play(def, table, director, report, explore);
        } catch (RuntimeException exp) {
            report.failures.add("The game threw " + exp + rootCause(exp));
            report.stopped = "the game threw an exception";
        }

        report.steps.putAll(director.statuses());
        for (Step step : def.steps)
            report.stepDescriptions.put(step.label(), step.when.source);
        for (String problem : table.Problems())
            report.failures.add(problem);

        if (completed) {
            for (RngEntry entry : table.Random().remaining())
                report.failures.add("rng: " + entry.kind().name().toLowerCase() + "(" + entry.seat().key() + "." + entry.zone()
                        + ": " + String.join(", ", entry.result()) + ") was never used");
            for (Map.Entry<String, ScenarioDirector.StepStatus> entry : director.statuses().entrySet()) {
                if (entry.getValue() == ScenarioDirector.StepStatus.UNREACHED)
                    report.failures.add(def.findStep(entry.getKey()).describe() + " was never reached");
                else if (entry.getValue() == ScenarioDirector.StepStatus.SKIPPED)
                    report.warnings.add(def.findStep(entry.getKey()).describe() + " was skipped");
            }
            if (report.failures.isEmpty())
                report.reached = RunnerLevel.FULL;
        }
        return report;
    }

    /**
     * @return Whether the scenario reached its end.
     */
    private static boolean play(ScenarioDefinition def, ScenarioTable table, ScenarioDirector director, ScenarioRunReport report, boolean explore) {
        boolean strict = def.coverage == Coverage.STRICT;
        int unscripted = 0;

        for (int number = 1; number <= DECISION_LIMIT; number++) {
            if (table.GameIsFinished()) {
                String winner = table.game().getWinnerPlayerId();
                report.stopped = "the game ended" + (winner == null ? " with no winner" : ": the " + table.SeatOf(winner).key() + " seat won");
                if (def.outcome.win == null && director.end() == null) {
                    report.failures.add("The game ended before the scenario did (" + report.stopped + ")");
                    return false;
                }
                return winner != null && winner.equals(table.HumanPlayer());
            }

            String player = table.FreepsAnyDecisionsAvailable() ? table.P1() : table.ShadowAnyDecisionsAvailable() ? table.P2() : null;
            if (player == null) {
                report.failures.add("The game is waiting on nobody, and has not ended");
                report.stopped = "the game stalled";
                return false;
            }
            AwaitingDecision decision = table.GetAwaitingDecision(player);
            boolean human = player.equals(table.HumanPlayer());

            Step step = director.match(player, decision);
            String description = director.describe(player, decision);
            String answer;
            String note = null;

            try {
                if (step != null) {
                    ScenarioDirector.Presentation shown = director.present(step, player, decision);
                    if (shown.allowed() != null && decision.getDecisionType() != com.gempukku.lotro.logic.decisions.AwaitingDecisionType.ASSIGN_MINIONS) {
                        //Worth knowing when writing the step's text: what the player could do here but is asked not to
                        int withheld = AnswerCompiler.options(table, decision).size() - shown.allowed().size();
                        if (withheld > 0)
                            note = "(" + withheld + " thing(s) on offer that the step does not allow)";
                    }
                    answer = director.scriptedAnswer(step, decision);
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
                } else if (def.fallback == Fallback.PASS) {
                    answer = AnswerCompiler.leastAnswer(decision);
                    note = "(opponent fallback: pass)";
                } else {
                    report.decisions.add(new ScenarioRunReport.Decision(number, description, null, null, null));
                    report.stopped = "a decision for the opponent that no step covers";
                    if (def.fallback == Fallback.BOT && !strict)
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
            if (step != null)
                director.answered(step);

            if (director.end() != null) {
                report.stopped = "the scenario ended (" + director.end().result.name().toLowerCase() + ")";
                if (director.end().result == EndResult.FAIL) {
                    report.failures.add("The scripted path ends in failure");
                    return false;
                }
                return true;
            }
            if (def.outcome.lose != null && table.Holds(def.outcome.lose, table.HumanPlayer())) {
                report.stopped = "the scenario's lose condition came true";
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

    private static String rootCause(Throwable exp) {
        Throwable cause = exp;
        while (cause.getCause() != null && cause.getCause() != cause)
            cause = cause.getCause();
        StackTraceElement[] trace = cause.getStackTrace();
        return (cause == exp ? "" : " (caused by " + cause + ")") + (trace.length > 0 ? " at " + trace[0] : "");
    }
}
