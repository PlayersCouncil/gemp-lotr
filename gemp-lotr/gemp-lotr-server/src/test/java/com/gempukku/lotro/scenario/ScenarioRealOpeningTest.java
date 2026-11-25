package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.scenario.DecisionKinds.Kind;
import com.gempukku.lotro.scenario.ScenarioSession.Ruling;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * setup.opening: game.  The game's own opening (the bid, the choice of going first, the starting fellowships, the
 * opening draw, the mulligans) is played as decisions the script answers, in place of the board being laid out.  The
 * fixture real-opening is played through by ScenarioCanonTest; here the parts of it are looked at.
 */
public class ScenarioRealOpeningTest {
    private static String fixtureText() throws Exception {
        return Files.readString(new File("src/test/resources/scenarios/real-opening.hjson").toPath(), StandardCharsets.UTF_8);
    }

    private static ScenarioRunReport run(ScenarioDefinition def) throws Exception {
        return ScenarioRunner.run(def, ScenarioTesting.library(), ScenarioTesting.format(def.format));
    }

    @Test
    public void eachOfTheOpeningsDecisionsIsOneAWhenCanName() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(fixtureText());
        assertTrue(def.setup.realOpening);
        //(to the first decision of the first turn)
        Set<Kind> met = ScenarioDecisionKindsTest.meet(def, (table, ruling) -> table.gameState().getTurnNumber() >= 1);
        assertTrue(met.toString(), met.containsAll(EnumSet.of(Kind.BID, Kind.GO_FIRST, Kind.STARTING_FELLOWSHIP, Kind.MULLIGAN)));
    }

    @Test
    public void theOpeningIsTheScriptsToAnswerAndItsDrawIsKnown() throws Exception {
        //The deck is shuffled before the opening draw: as the scenario says
        ScenarioDefinition def = ScenarioParser.parse(fixtureText().replace("\tsteps: [", "\trng: shuffle(freeps.deck: swordarm, servant1)\n\tsteps: ["));
        ScenarioSession session = ScenarioOpponentRulesTest.session(def);
        ScenarioTable table = session.Table();
        //Nothing is answered by the table: the first decision is the person's bid, before any card is dealt
        Ruling ruling = session.Ruling(table.HumanPlayer());
        assertNotNull(ruling);
        assertEquals(Kind.BID, DecisionKinds.of(ruling.decision()));
        assertEquals("bid", ruling.step().id);
        assertFalse(table.CardsDealt());
        try {
            table.Card("aragorn");
            fail("No card has its name before the cards are dealt");
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("dealt once the seats have been chosen"));
        }

        //From the first decision after the seats are chosen, the cards have their names
        ruling = ScenarioOpponentRulesTest.playTo(session, one -> one.step() != null && "fellowship".equals(one.step().id));
        assertTrue(table.CardsDealt());
        assertNull(table.SeatingProblem());
        assertEquals(table.Player(Seat.FREEPS), table.gameState().getFirstPlayerId());
        assertEquals(Zone.DECK, table.Card("boromir").getZone());

        ruling = ScenarioOpponentRulesTest.playTo(session, one -> one.step() != null && "mulligan".equals(one.step().id));
        //Aragorn went into the starting fellowship; the rest of the deck was the hand, in the order the shuffle said
        assertEquals(Zone.FREE_CHARACTERS, table.Card("aragorn").getZone());
        assertEquals(List.of("swordarm", "servant1", "boromir", "gandalf"),
                table.gameState().getHand(table.HumanPlayer()).stream().map(table::AliasOf).toList());
        //The burdens bid are the game's: the person bid one
        assertEquals(1, table.gameState().getPlayerBurdens(table.HumanPlayer()));
        assertEquals(0, table.gameState().getPlayerBurdens(table.OpponentPlayer()));

        ScenarioRunReport report = run(def);
        assertTrue(report.toString(), report.passed());
    }

    @Test
    public void theRunnerProvesTheFreePeoplesSeatGoesFirst() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(fixtureText().replace("allow: option(Go first)", "allow: option(Go second)"));
        ScenarioRunReport report = run(def);
        assertFalse(report.passed());
        assertTrue(report.toString(), report.failures.getFirst().contains("did not seat the players as the scenario declares them"));
        assertEquals("the opening seated the players the other way round", report.stopped);

        //The other seat outbidding the person, and choosing to go second, is as good
        ScenarioDefinition outbid = ScenarioParser.parse(fixtureText()
                .replace("allow: number(1)", "allow: number(0)")
                .replace("answer: number(0)", "answer: number(2)")
                .replace("when: human, goFirst", "when: opponent, goFirst")
                .replace("allow: option(Go first)", "answer: option(Go second)"));
        report = run(outbid);
        assertTrue(report.toString(), report.passed());

        //A tied bid is the Free Peoples seat's to choose from, whoever bid first
        ScenarioDefinition tied = ScenarioParser.parse(fixtureText().replace("allow: number(1)", "allow: number(0)"));
        report = run(tied);
        assertTrue(report.toString(), report.passed());
    }

    @Test
    public void aCardNamedBeforeTheCardsAreDealtIsAMistakeSaidAsOne() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(fixtureText().replace(
                "Each player bids burdens for the right to choose who goes first.  Bid one.",
                "Aragorn is {strength:aragorn}.  Bid one."));
        ScenarioRunReport report = run(def);
        assertFalse(report.passed());
        assertTrue(report.toString(), report.failures.getFirst().contains("the cards are dealt once the seats have been chosen"));
    }

    @Test
    public void nothingOfTheBoardButTheDecksOrderCanBeSaid() throws Exception {
        String text = fixtureText();
        assertEquals(List.of(), ScenarioValidator.validate(ScenarioParser.parse(text), ScenarioTesting.library()));
        for (String[] wrong : new String[][] {
                {"\t\tfreeps: {\n\t\t\tdeck: boromir\n\t\t}", "\t\tfreeps: {\n\t\t\tcharacters: aragorn\n\t\t}", "setup.freeps.characters"},
                {"\t\topening: game\n", "\t\topening: game\n\t\tburdens: 2\n", "setup.burdens"},
                {"\t\topening: game\n", "\t\topening: game\n\t\tstartAt: {\n\t\t\tsite: 2\n\t\t}\n", "setup.startAt"},
                {"\t\topening: game\n", "\t\topening: game\n\t\twounds: {\n\t\t\tfrodo: 1\n\t\t}\n", "setup.wounds"},
                {"\t\t{\n\t\t\tid: bid\n", "\t\t{\n\t\t\twhen: opening\n\t\t\tmessages: [\n\t\t\t\tBefore.\n\t\t\t]\n\t\t}\n\t\t{\n\t\t\tid: bid\n", "'when: opening'"}}) {
            assertTrue(wrong[0], text.contains(wrong[0]));
            List<String> problems = ScenarioValidator.validate(ScenarioParser.parse(text.replace(wrong[0], wrong[1])), ScenarioTesting.library());
            assertEquals(wrong[2] + ": " + problems, 1, problems.size());
            assertTrue(problems.getFirst(), problems.getFirst().contains(wrong[2]) && problems.getFirst().contains("setup.opening: game"));
        }
    }

    @Test
    public void theOpeningIsReadAndWrittenBack() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(fixtureText());
        String written = ScenarioWriter.toHjson(def, ScenarioTesting.library());
        assertTrue(written, written.contains("\tsetup: {\n\t\topening: game\n"));
        assertEquals(ScenarioWriter.toJson(def).toString(), ScenarioWriter.toJson(ScenarioParser.parse(written)).toString());
        //board is the default, and is not written
        ScenarioDefinition board = ScenarioParser.parse(fixtureText().replace("opening: game", "opening: board"));
        assertFalse(board.setup.realOpening);
        assertFalse(ScenarioWriter.toHjson(board, ScenarioTesting.library()).contains("opening:"));
        try {
            ScenarioParser.parse(fixtureText().replace("opening: game", "opening: yes"));
            fail("opening is board or game");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("setup.opening: has to be board"));
        }
    }

    @Test
    public void aMistakenOpeningIsLeftOutWhenTheFileIsOpenedWithItsMistakes() throws Exception {
        //A mistaken word for how the game opens, and a starting fellowship placed where it is chosen: each is left out
        String text = fixtureText().replace("opening: game", "opening: yes");
        ScenarioSalvage.Result read = ScenarioSalvage.read(text, def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(read.errors().toString(), read.def());
        assertFalse(read.def().setup.realOpening);
        assertEquals("setup.opening", read.leftOut().getFirst().where());

        text = fixtureText().replace("\t\tfreeps: {\n\t\t\tdeck: boromir\n\t\t}", "\t\tfreeps: {\n\t\t\tdeck: boromir\n\t\t\tcharacters: aragorn\n\t\t}");
        read = ScenarioSalvage.read(text, def -> ScenarioValidator.validate(def, ScenarioTesting.library()));
        assertNotNull(read.errors().toString(), read.def());
        assertTrue(read.def().setup.realOpening);
        assertEquals(List.of(), ScenarioValidator.validate(read.def(), ScenarioTesting.library()));
        assertTrue(read.leftOut().toString(), read.leftOut().getFirst().where().startsWith("setup.freeps"));
    }
}
