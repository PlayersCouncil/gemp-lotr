package com.gempukku.lotro.scenario;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Decipher's 2003 Playmat Puzzles, as scenario files (src/test/resources/scenarios/puzzles), with their published
 * keys as solutions: each key still solves its puzzle in Gemp, and the wrong answers the results pages describe still
 * fail (the opponent defending itself as the puzzle says it may).
 */
public class ScenarioDecipherPuzzlesTest {
    private static String file(String name) throws Exception {
        return Files.readString(new File("src/test/resources/scenarios/puzzles/" + name).toPath(), StandardCharsets.UTF_8);
    }

    private static ScenarioRunReport run(String text) throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(text);
        assertEquals(List.of(), ScenarioValidator.validate(def, ScenarioTesting.library()));
        return ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
    }

    /** The file with its solutions replaced by one: a wrong answer, to see it fail. */
    private static String withOnly(String text, String solution) {
        int at = text.indexOf("\tsolutions: [");
        return text.substring(0, at) + "\tsolutions: [\n" + solution + "\n\t]\n}\n";
    }

    @Test
    public void defendingTheKeepIsSolvedByItsPublishedKey() throws Exception {
        ScenarioRunReport report = run(file("10-defending-the-keep.hjson"));
        assertTrue(report.toString(), report.passed());
        assertEquals(ScenarioDefinition.RunnerLevel.SOLUTION, report.reached);
        assertNull(report.toString(), report.solutions.get("published"));
    }

    @Test
    public void defendingTheKeepIsNotSolvedByTheWrongAnswersTheResultsPageDescribes() throws Exception {
        String text = file("10-defending-the-keep.hjson");
        //Armory: two tokens off the Weapons of Isengard, so three arrows, and only two can go on Aldor
        ScenarioRunReport armory = run(withOnly(text, """
                		{
                			name: armory
                			picks: {
                				condition: 5_79
                				ally: 4_262
                			}
                			answers: [
                				use(condition)
                				choose(ally)
                				choose(ally)
                				choose(frodo)
                			]
                		}"""));
        assertFalse(armory.passed());
        //(Frodo takes the third: the game ends with the person losing it, before the end of archery is judged)
        assertTrue(armory.solutions.get("armory"), armory.solutions.get("armory").contains("the shadow seat won"));
        //Arrow-slits: the Siege Engine prevents the machine's discarding (the opponent answers as it may)
        ScenarioRunReport slits = run(withOnly(text, """
                		{
                			name: slits
                			picks: {
                				condition: 5_80
                				ally: 4_262
                			}
                			answers: [
                				use(condition)
                				choose(weapons)
                			]
                		}"""));
        assertFalse(slits.passed());
        assertTrue(slits.toString(), slits.toString().contains("use(engine"));
    }

    @Test
    public void ambushOfTheSouthronsIsSolvedByBothOfItsPublishedSolutions() throws Exception {
        ScenarioRunReport report = run(file("08-ambush-of-the-southrons.hjson"));
        assertTrue(report.toString(), report.passed());
        assertNull(report.solutions.get("trap-and-be-back-soon"));
        assertNull(report.solutions.get("trap-and-defend-it"));
    }

    @Test
    public void beBackSoonIsPreventedWhenTheCommanderCanStillExertTwice() throws Exception {
        //The Trap's exertions on the other two minions only: the Commander can still exert twice, and the eager
        // opponent does, so the Commander stays and Frodo dies
        ScenarioRunReport report = run(withOnly(file("08-ambush-of-the-southrons.hjson"), """
                		{
                			name: wrong
                			picks: {
                				support: 4_126
                				hand: 5_21
                			}
                			answers: [
                				use(support)
                				choose(lancers)
                				choose(legion)
                				choose(lancers)
                				play(hand)
                				choose(commander)
                				assign(faramir: lancers, ranger: legion)
                			]
                		}"""));
        assertFalse(report.passed());
        assertNotNull(report.solutions.get("wrong"));
    }

    @Test
    public void likeThisWouldHappenIsScoredAndItsPublishedKeyStillReaches1160() throws Exception {
        ScenarioRunReport report = run(file("03-like-this-would-happen-2b.hjson"));
        assertTrue(report.toString(), report.passed());
        assertNull(report.solutions.get("published"));
        assertTrue(report.toString(), report.toString().contains("Solution published: solved (judged at skirmishResolution: won, scoring 1160)"));
        //Fangorn Forest, the other answer for the site, is a site 2 its slot takes
        ScenarioDefinition def = ScenarioParser.parse(file("03-like-this-would-happen-2b.hjson"));
        assertNull(SlotBounds.why(def, "site2", "4_332", def.solution("published").picks(), ScenarioTesting.library()));
        assertNotNull(SlotBounds.why(def, "site2", "4_339", def.solution("published").picks(), ScenarioTesting.library()));
    }
}
