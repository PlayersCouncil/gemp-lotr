package com.gempukku.lotro.scenario;

import com.gempukku.lotro.game.LotroFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Plays every scenario that ships with the game (and every fixture kept with the tests) through from start to finish,
 * headless.  A scenario that has drifted out of step with the cards or the rules fails here rather than in front of a
 * new player.
 */
@RunWith(Parameterized.class)
public class ScenarioCanonTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> scenarios() throws IOException {
        List<Object[]> result = new ArrayList<>();
        //The ones kept with the tests as fixtures.  (Not the scenarios that ship with the game: they are changing too
        // much to hold the build to yet.  The server plays each through as it reads it, and says what fails.)
        for (File root : List.of(new File("src/test/resources/scenarios"))) {
            if (!root.isDirectory())
                continue;
            try (Stream<Path> paths = Files.walk(root.toPath())) {
                paths.filter(path -> path.toString().endsWith(".hjson") && !path.toString().contains(File.separator + "campaigns" + File.separator))
                        .sorted()
                        .forEach(path -> result.add(new Object[] { root.toPath().relativize(path).toString(), path }));
            }
        }
        return result;
    }

    private final Path _path;

    public ScenarioCanonTest(String name, Path path) {
        _path = path;
    }

    @Test
    public void playsThroughToItsEnd() throws Exception {
        ScenarioDefinition def;
        try {
            def = ScenarioParser.parse(Files.readString(_path, StandardCharsets.UTF_8));
        } catch (ScenarioFormatException exp) {
            fail(_path.getFileName() + " is not a well-formed scenario: " + exp.getMessage());
            return;
        }

        LotroFormat format = ScenarioTesting.format(def.format);
        assertTrue(_path.getFileName() + ": there is no format called '" + def.format + "'", format != null);

        //-Dscenario.explore=<part of a file name> prints what the game asks for beyond that scenario's script
        String explore = System.getProperty("scenario.explore");
        //-Dscenario.choose=<name,name> says what to choose at each choice the exploring comes to (the first thing
        // on offer otherwise)
        String choose = System.getProperty("scenario.choose", "");
        if (explore != null && _path.toString().contains(explore))
            System.out.println("EXPLORING " + _path.getFileName() + "\n" + ScenarioRunner.explore(def, ScenarioTesting.library(), format,
                    choose.isEmpty() ? List.of() : List.of(choose.split(","))));

        ScenarioRunReport report = ScenarioRunner.run(def, ScenarioTesting.library(), format);
        assertTrue(report.toString(), report.passed());
    }
}
