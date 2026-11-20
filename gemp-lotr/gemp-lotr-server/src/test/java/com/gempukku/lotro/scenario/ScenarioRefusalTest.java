package com.gempukku.lotro.scenario;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * The headless runner holds the player's scripted answers to the step's own 'allow', as the player's page does, and
 * proves the answers a step says it refuses ('refuses').
 */
public class ScenarioRefusalTest {
    /**
     * Two archers fire at Frodo and Boromir, as in tutorial 1's archery step: Boromir may take an arrow only while he
     * is unwounded, so a second arrow on him is refused.
     */
    private static final String ARCHERY = """
            	options: {
            		autoPass: true
            	}
            	opponent: {
            		fallback: pass
            	}
            	setup: {
            		freeps: {
            			characters: boromir
            		}
            		shadow: {
            			minions: bowman, marksman
            		}
            	}
            	steps: [
            		{
            			when: freeps, cards, phase(archery)
            			until: freeps, assign
            			allow: choose(frodo), choose(boromir, unwounded)
            			answer: ANSWER
            			REFUSES
            		}
            		{
            			when: freeps, assign
            			answer: pass
            			end: complete
            		}
            	]
            """;

    private static ScenarioRunReport archery(String answer, String refuses) throws Exception {
        return ScenarioTesting.run(ARCHERY.replace("ANSWER", answer).replace("REFUSES", refuses));
    }

    @Test
    public void aScriptedAnswerTheStepDoesNotAllowFailsTheRun() throws Exception {
        //The request's case: before, the runner played both arrows on Boromir
        ScenarioRunReport report = archery("choose(boromir), choose(boromir)", "");
        assertFalse(report.toString(), report.passed());
        assertTrue(report.toString(), report.failures.stream().anyMatch(failure -> failure.startsWith("step 1: its answer 'choose(boromir)' (the 2nd decision of the step)")
                && failure.contains("its 'allow' lets the player give here") && failure.contains("The step allows 'choose(frodo)'")));

        assertTrue(archery("choose(boromir), choose(frodo)", "").toString(), archery("choose(boromir), choose(frodo)", "").passed());
    }

    @Test
    public void whatAStepRefusesIsProvedToBeRefused() throws Exception {
        //The second arrow on Boromir: on offer (he is still there to be chosen) and refused (he is wounded)
        ScenarioRunReport report = archery("choose(boromir), choose(frodo)", "refuses: \"2: choose(boromir)\"");
        assertTrue(report.toString(), report.passed());
        assertTrue(report.refusalsShown.contains("1 2: choose(boromir)"));

        //The first arrow on him is allowed, so a step that says it is refused is wrong
        report = archery("choose(boromir), choose(frodo)", "refuses: \"1: choose(boromir)\"");
        assertFalse(report.toString(), report.passed());
        assertTrue(report.toString(), report.failures.stream().anyMatch(failure ->
                failure.startsWith("step 1: it refuses '1: choose(boromir)', but at its 1st decision its 'allow' lets the player give 'choose(boromir)'")));

        //Without a number it is about every decision of the step, and holds where the game offers it: here not at the
        // first decision, where Boromir may be chosen
        report = archery("choose(boromir), choose(frodo)", "refuses: choose(boromir)");
        assertFalse(report.toString(), report.passed());

        //Something the game does not offer at that decision cannot be shown to be refused
        report = archery("choose(boromir), choose(frodo)", "refuses: \"2: choose(gandalf)\"");
        assertFalse(report.toString(), report.passed());
        assertTrue(report.toString(), report.failures.stream().anyMatch(failure -> failure.contains("but the game does not offer that at the step's 2nd decision")));

        //...nor one the step never comes to
        report = archery("choose(boromir), choose(frodo)", "refuses: \"3: choose(boromir)\"");
        assertFalse(report.toString(), report.passed());
        assertTrue(report.toString(), report.failures.stream().anyMatch(failure -> failure.contains("never came to its 3rd decision")));
    }

    @Test
    public void aPassCanBeRefused() throws Exception {
        String rest = """
                	setup: {
                		freeps: {
                			hand: aragorn
                		}
                	}
                	steps: [
                		{
                			when: freeps, action, phase(fellowship)
                			allow: play(aragorn)
                			refuses: REFUSED
                			end: complete
                		}
                	]
                """;
        assertTrue(ScenarioTesting.run(rest.replace("REFUSED", "pass")).toString(), ScenarioTesting.run(rest.replace("REFUSED", "pass")).passed());
        ScenarioRunReport report = ScenarioTesting.run(rest.replace("REFUSED", "play(aragorn)"));
        assertFalse(report.toString(), report.passed());
    }

    @Test
    public void refusalsAreReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse(ARCHERY.replace("ANSWER", "choose(boromir), choose(frodo)")
                .replace("REFUSES", "refuses: [\n\t\t\t\t\"2: choose(boromir)\"\n\t\t\t\tchoose(aragorn), pass\n\t\t\t]"));
        assertEquals(3, def.steps.getFirst().refuses.size());
        assertEquals(2, def.steps.getFirst().refuses.getFirst().at());
        assertEquals(0, def.steps.getFirst().refuses.get(1).at());
        String written = ScenarioWriter.toHjson(def, null);
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());
        //(on one line, if none is numbered)
        ScenarioDefinition plain = ScenarioTesting.parse(ARCHERY.replace("ANSWER", "choose(boromir), choose(frodo)").replace("REFUSES", "refuses: choose(aragorn), pass"));
        assertTrue(ScenarioWriter.toHjson(plain, null).contains("refuses: choose(aragorn), pass"));

        org.hjson.JsonObject json = ScenarioWriter.toJson(def);
        AliasRename.rename(json, "boromir", "captain");
        assertTrue(json.toString(), json.get("steps").asArray().get(0).asObject().get("refuses").toString().contains("2: choose(captain)"));

        for (String[] wrong : new String[][] {
                {"\t\t\tallow: choose(frodo), choose(boromir, unwounded)\n", "a step without an 'allow' lets the player give any answer"},
                {"\t\t\tuntil: freeps, assign\n", "this step answers one decision, so there is no decision 2"},
        }) {
            try {
                ScenarioTesting.parse(ARCHERY.replace("ANSWER", "choose(boromir)").replace("REFUSES", "refuses: \"2: choose(boromir)\"").replace(wrong[0], ""));
                fail(wrong[1]);
            } catch (ScenarioFormatException exp) {
                assertTrue(exp.getMessage(), exp.getMessage().startsWith("step 1.refuses: ") && exp.getMessage().contains(wrong[1]));
            }
        }
    }
}
