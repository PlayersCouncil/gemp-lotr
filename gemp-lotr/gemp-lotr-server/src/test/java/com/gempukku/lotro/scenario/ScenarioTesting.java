package com.gempukku.lotro.scenario;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.LotroFormat;

/**
 * What the scenario tests have in common: the one card library the whole suite shares, and a small scenario to
 * start from.
 */
public final class ScenarioTesting {
    private ScenarioTesting() { }

    public static LotroCardBlueprintLibrary library() {
        return RigLibrary.get();
    }

    public static LotroFormat format(String code) throws CardNotFoundException {
        return VirtualTableScenario.FindFormat(code);
    }

    /** The test rig keeps its card library to itself and its subclasses. */
    private static class RigLibrary extends VirtualTableScenario {
        private RigLibrary() throws Exception {
            super(null);
        }

        static LotroCardBlueprintLibrary get() {
            return _cardLibrary;
        }
    }

    /**
     * A scenario with Frodo and a handful of plain Fellowship-block cards on each side, and nothing else: no setup,
     * no rules, no steps.
     * @param rest Whatever else the scenario should say, as top-level fields.
     */
    public static String scenario(String rest) {
        return """
                {
                	id: test
                	title: Test
                	format: scenario
                	cards: {
                		freeps: {
                			frodo: 10_121
                			aragorn: 4_109
                			gandalf: 0_64
                			servant1: 1_83
                			servant2: 1_83
                			swordarm: 1_116
                			boromir: 0_65
                		}
                		shadow: {
                			oppFrodo: 10_121
                			runner1: 1_178
                			runner2: 1_178
                			bowman: 2_60
                			marksman: 1_176
                		}
                	}
                	ringBearer: {
                		freeps: frodo
                		shadow: oppFrodo
                	}
                	sites: {
                		freeps: 1_323, 1_331, 1_340, 2_118
                		shadow: 1_323, 1_331, 1_340, 2_118
                	}
                """ + rest + "\n}";
    }

    public static ScenarioDefinition parse(String rest) throws ScenarioFormatException {
        return ScenarioParser.parse(scenario(rest));
    }

    public static ScenarioTable table(String rest) throws Exception {
        ScenarioDefinition def = parse(rest);
        ScenarioTable table = new ScenarioTable(def, library(), format(def.format), ScenarioRunner.HUMAN, ScenarioRunner.OPPONENT);
        table.Start();
        return table;
    }

    public static ScenarioRunReport run(String rest) throws Exception {
        ScenarioDefinition def = parse(rest);
        return ScenarioRunner.run(def, library(), format(def.format));
    }
}
