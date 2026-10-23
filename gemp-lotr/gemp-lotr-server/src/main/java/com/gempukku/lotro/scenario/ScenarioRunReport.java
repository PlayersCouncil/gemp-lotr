package com.gempukku.lotro.scenario;

import com.gempukku.lotro.scenario.ScenarioDefinition.RunnerLevel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What happened when a scenario was played through headless.
 */
public class ScenarioRunReport {
    /**
     * One decision the game asked for.
     * @param decision The decision, in the scenario's terms.
     * @param step The label of the step it belonged to, or null if it belonged to none.
     * @param answer How it was answered, in the game's terms, or null if the run stopped here.
     * @param note Anything worth saying about it (e.g. "answered by fallback").
     */
    public record Decision(int number, String decision, String step, String answer, String note) { }

    public final String scenarioId;
    /** How much of the scenario the run proved. */
    public RunnerLevel reached = RunnerLevel.SETUP;
    /** How much it had to prove. */
    public RunnerLevel required = RunnerLevel.FULL;
    public final List<String> failures = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
    public final List<Decision> decisions = new ArrayList<>();
    /** Every step, by label, and what became of it. */
    public final Map<String, ScenarioDirector.StepStatus> steps = new LinkedHashMap<>();
    /** Every step, by label, and what it waits for (as written in the file). */
    public final Map<String, String> stepDescriptions = new LinkedHashMap<>();
    /** Why the run stopped, in a sentence. */
    public String stopped = "";
    /** Whether the scenario reached its end. */
    public boolean completed;
    /** What was chosen at each choice the run came to, in order. */
    public final List<String> taken = new ArrayList<>();
    /** What each of those choices offered. */
    public final List<List<String>> offered = new ArrayList<>();

    /**
     * A decision of the player's, in this run, where the step in hand would have taken another answer than the
     * script's: one it allows, or anything where it allows anything (see ScenarioRunner.offScript).
     * @param number The decision's place in the run.
     * @param step The step's label.
     * @param top The number of the step of the script it is (a block's, for one of a block's steps).
     * @param decision The decision, described.
     * @param answer What the script answered, in the scenario's terms.
     * @param others The other answers, as the game takes them, each with how the scenario would say it.
     */
    public record OffScriptPoint(int number, String step, int top, String decision, String answer, List<String[]> others) { }

    /** Where this run could have gone another way (only a run that follows the script records them). */
    public final List<OffScriptPoint> offScriptPoints = new ArrayList<>();

    /**
     * Several runs of the same scenario, one for each way through it, as one report: it passes if they all do, and
     * a step counts as reached if any of them reached it.
     */
    public static ScenarioRunReport together(String scenarioId, List<ScenarioRunReport> runs) {
        ScenarioRunReport all = new ScenarioRunReport(scenarioId);
        all.completed = true;
        all.reached = RunnerLevel.FULL;
        List<String> stops = new ArrayList<>();
        for (ScenarioRunReport run : runs) {
            String way = run.taken.isEmpty() ? "no choice" : "choosing " + String.join(", ", run.taken);
            all.required = run.required;
            all.completed &= run.completed;
            if (run.reached.compareTo(all.reached) < 0)
                all.reached = run.reached;
            for (String failure : run.failures)
                all.failures.add("[" + way + "] " + failure);
            for (String warning : run.warnings)
                all.warnings.add("[" + way + "] " + warning);
            stops.add("[" + way + "] " + run.stopped);
            all.decisions.add(new Decision(-1, "Way through: " + way, null, null, null));
            all.decisions.addAll(run.decisions);
            all.stepDescriptions.putAll(run.stepDescriptions);
            for (Map.Entry<String, ScenarioDirector.StepStatus> step : run.steps.entrySet())
                all.steps.merge(step.getKey(), step.getValue(), (a, b) -> rank(a) >= rank(b) ? a : b);
        }
        all.stopped = String.join("; ", stops);
        return all;
    }

    private static int rank(ScenarioDirector.StepStatus status) {
        return switch (status) {
            case MATCHED -> 3;
            case SKIPPED -> 2;
            case BYPASSED -> 1;
            case UNREACHED -> 0;
        };
    }

    public ScenarioRunReport(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public boolean passed() {
        return failures.isEmpty() && reached.compareTo(required) >= 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Scenario ").append(scenarioId).append(": ").append(passed() ? "PASSED" : "FAILED")
                .append(" (proved: ").append(reached.name().toLowerCase())
                .append(", required: ").append(required.name().toLowerCase()).append(")\n");
        sb.append("Stopped: ").append(stopped).append("\n");
        for (String failure : failures)
            sb.append("  FAILURE: ").append(failure).append("\n");
        for (String warning : warnings)
            sb.append("  warning: ").append(warning).append("\n");
        sb.append("Steps:\n");
        for (Map.Entry<String, ScenarioDirector.StepStatus> entry : steps.entrySet())
            sb.append("  ").append(String.format("%-10s", entry.getValue().name().toLowerCase()))
                    .append(String.format("%-4s", entry.getKey()))
                    .append(stepDescriptions.getOrDefault(entry.getKey(), "").isEmpty() ? "" : " when: " + stepDescriptions.get(entry.getKey()))
                    .append("\n");
        sb.append("Decisions:\n");
        for (Decision decision : decisions) {
            if (decision.number() < 0) {
                sb.append(" ").append(decision.decision()).append("\n");
                continue;
            }
            sb.append("  ").append(String.format("%3d", decision.number())).append(". ")
                    .append(decision.step() == null ? "(no step)" : "[step " + decision.step() + "]")
                    .append(" ").append(decision.decision()).append("\n");
            sb.append("       -> ").append(decision.answer() != null ? "\"" + decision.answer() + "\"" : decision.note() != null ? "" : "(not answered)");
            if (decision.note() != null)
                sb.append("  ").append(decision.note());
            sb.append("\n");
        }
        return sb.toString();
    }
}
