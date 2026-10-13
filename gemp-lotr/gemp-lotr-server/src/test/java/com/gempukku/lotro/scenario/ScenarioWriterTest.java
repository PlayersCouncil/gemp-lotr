package com.gempukku.lotro.scenario;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Writing a scenario out and reading it back has to give the same scenario, whatever is in it: the authoring tool
 * saves by writing, and a file it had quietly changed would be a file nobody could trust.
 */
public class ScenarioWriterTest {
    private static List<Path> everyScenarioFile() throws IOException {
        List<Path> files = new ArrayList<>();
        for (File root : List.of(new File("src/test/resources/scenarios"))) {
            if (!root.isDirectory())
                continue;
            try (Stream<Path> paths = Files.walk(root.toPath())) {
                paths.filter(path -> path.toString().endsWith(".hjson") && !path.toString().contains(File.separator + "campaigns" + File.separator))
                        .sorted().forEach(files::add);
            }
        }
        return files;
    }

    @Test
    public void everyScenarioSurvivesBeingWrittenAndReadBack() throws Exception {
        List<Path> files = everyScenarioFile();
        assertFalse(files.isEmpty());
        for (Path file : files) {
            ScenarioDefinition first = ScenarioParser.parse(Files.readString(file, StandardCharsets.UTF_8));
            String written = ScenarioWriter.toHjson(first, ScenarioTesting.library());
            ScenarioDefinition second;
            try {
                second = ScenarioParser.parse(written);
            } catch (ScenarioFormatException exp) {
                fail(file.getFileName() + " was written as something that is not a scenario: " + exp.getMessage() + "\n" + written);
                return;
            }
            assertEquals(file.getFileName() + " is not the same scenario after being written and read back",
                    ScenarioWriter.toJson(first).toString(), ScenarioWriter.toJson(second).toString());
            //Written again, it is the same text: the writer's form is where a file settles
            assertEquals(written, ScenarioWriter.toHjson(second, ScenarioTesting.library()));
            //...and the tree, as JSON, is itself something the parser reads (it is what the authoring screen sends)
            assertEquals(ScenarioWriter.toJson(first).toString(),
                    ScenarioWriter.toJson(ScenarioParser.parse(ScenarioWriter.toJson(first).toString())).toString());

            //It plays the same, too
            ScenarioRunReport before = ScenarioRunner.run(first, ScenarioTesting.library(), ScenarioTesting.format(first.format));
            ScenarioRunReport after = ScenarioRunner.run(second, ScenarioTesting.library(), ScenarioTesting.format(second.format));
            assertEquals(file.getFileName().toString(), before.passed(), after.passed());
            assertEquals(file.getFileName().toString(), before.decisions.size(), after.decisions.size());
        }
    }

    @Test
    public void textThatWouldBeReadAsSomethingElseIsQuoted() throws Exception {
        String[] awkward = {
                "[[Gandalf]] is a companion.", "{twilight} is the pool.", "\"Spot X\" means X is on the table.", "'tis the season",
                "# not a comment", "// nor this", "/* nor this */", ": colon first", ", comma first",
                "42", "007", "2.50", "-3", "1e5", "true", "false", "null",
                "two\nlines", "a tab\there", "...uh-oh.", "ends with a comma,",
                "2 of your enemies have the **archer** keyword", "It costs (4): a hefty sum # really", "a } brace ] inside",
                //What HJSON takes for a number (or true, false, null) with something else after it
                "3, 2, 1: go", "3 # of them", "3, then", "7] done", "-2} left", "true, as far as it goes", "null // nothing",
                "1.5 /* or so */ wounds", "3 is the number", "10_121 is Frodo"
        };
        StringBuilder messages = new StringBuilder();
        for (String text : awkward)
            messages.append("\t\t\t\t").append(org.hjson.JsonValue.valueOf(text)).append("\n");
        ScenarioDefinition first = ScenarioTesting.parse("""
                	summary: "# not a comment either"
                	note: "{ a note }"
                	steps: [
                		{
                			note: 12
                			when: freeps, action
                			messages: [
                MESSAGES
                				{
                					note: "[why]"
                					header: "007"
                					text: "null"
                					anchor: card:aragorn
                				}
                			]
                			allow: {
                				answers: pass
                				otherwise: "[[Aragorn]] can wait."
                				hints: {
                					gandalf: "\\"Not yet,\\" he said."
                				}
                			}
                		}
                	]
                """.replace("MESSAGES\n", messages.toString()));
        assertEquals(awkward.length + 1, first.steps.getFirst().messages.size());
        for (int i = 0; i < awkward.length; i++)
            assertEquals(awkward[i], first.steps.getFirst().messages.get(i).text);

        String written = ScenarioWriter.toHjson(first, ScenarioTesting.library());
        ScenarioDefinition second = ScenarioParser.parse(written);
        for (int i = 0; i < awkward.length; i++)
            assertEquals(written, awkward[i], second.steps.getFirst().messages.get(i).text);
        assertEquals("null", second.steps.getFirst().messages.getLast().text);
        assertEquals("007", second.steps.getFirst().messages.getLast().header);
        assertEquals("[why]", second.steps.getFirst().messages.getLast().note);
        assertEquals("12", second.steps.getFirst().note);
        assertEquals("{ a note }", second.note);
        assertEquals("# not a comment either", second.summary);
        assertEquals("[[Aragorn]] can wait.", second.steps.getFirst().allow.otherwise);
        assertEquals("\"Not yet,\" he said.", second.steps.getFirst().allow.hints.get("gandalf"));
        assertEquals(ScenarioWriter.toJson(first).toString(), ScenarioWriter.toJson(second).toString());
    }

    @Test
    public void theWrittenFileSaysWhatItsCardsAreAndNumbersItsSteps() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, action
                			allow: pass
                		}
                		{
                			id: second
                			when: opponent, action
                			answer: pass
                		}
                	]
                """);
        String written = ScenarioWriter.toHjson(def, ScenarioTesting.library());
        assertTrue(written, written.contains("\t\t\t# Aragorn, Heir of Elendil\n\t\t\taragorn: 4_109\n"));
        assertTrue(written, written.contains("\t# Green Hill Country, Ettenmoors, Rivendell Terrace, Great Chasm\n\tsites: {"));
        assertTrue(written, written.contains("\t\t# ---- 1\n\t\t{\n"));
        assertTrue(written, written.contains("\t\t# ---- 2 (second)\n\t\t{\n\t\t\tid: second\n"));
        //Defaults are left out
        assertFalse(written, written.contains("options"));
        assertFalse(written, written.contains("highlightStyle"));
    }
}
