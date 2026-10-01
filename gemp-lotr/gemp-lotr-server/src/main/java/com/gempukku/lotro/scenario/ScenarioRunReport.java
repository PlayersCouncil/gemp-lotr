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
     * @param stepId The step it belonged to, or null if it belonged to none.
     * @param answer How it was answered, in the game's terms, or null if the run stopped here.
     * @param note Anything worth saying about it (e.g. "answered by fallback").
     */
    public record Decision(int number, String decision, String stepId, String answer, String note) { }

    public final String scenarioId;
    /** How much of the scenario the run proved. */
    public RunnerLevel reached = RunnerLevel.SETUP;
    /** How much it had to prove. */
    public RunnerLevel required = RunnerLevel.FULL;
    public final List<String> failures = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
    public final List<Decision> decisions = new ArrayList<>();
    public final Map<String, ScenarioDirector.StepStatus> steps = new LinkedHashMap<>();
    /** Why the run stopped, in a sentence. */
    public String stopped = "";

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
            sb.append("  ").append(String.format("%-10s", entry.getValue().name().toLowerCase())).append(entry.getKey()).append("\n");
        sb.append("Decisions:\n");
        for (Decision decision : decisions) {
            sb.append("  ").append(String.format("%3d", decision.number())).append(". ")
                    .append(decision.stepId() == null ? "(no step)" : "[" + decision.stepId() + "]")
                    .append(" ").append(decision.decision()).append("\n");
            sb.append("       -> ").append(decision.answer() == null ? "(not answered)" : "\"" + decision.answer() + "\"");
            if (decision.note() != null)
                sb.append("  ").append(decision.note());
            sb.append("\n");
        }
        return sb.toString();
    }
}
