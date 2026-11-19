package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_165_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("intruder", "7_165");
					put("intruder2", "7_165");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 or 5 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man of the Free Peoples player's
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a companion of cost 1; a bonus while bearing a weapon only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion of cost 2; a skirmish ability only
					put("bounder", "1_286");    // Bounder: an ally of cost 1; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion of cost 2; an archery ability only
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a companion of cost 1; protects [shire] tales only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronIntruderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Intruder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>.
		 * While you can spot 3 twilight tokens, all [raider] Men are strength +1.
		 * While you can spot 6 twilight tokens, all [raider] Men are strength +1.
		 * While you can spot 9 twilight tokens, all [raider] Men are strength +1.
		*/

		var scn = GetScenario();

		var intruder = scn.GetFreepsCard("intruder");

		assertEquals("Southron Intruder", intruder.getBlueprint().getTitle());
		assertNull(intruder.getBlueprint().getSubtitle());
		assertFalse(intruder.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, intruder.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, intruder.getBlueprint().getCulture());
		assertEquals(CardType.MINION, intruder.getBlueprint().getCardType());
		assertEquals(Race.MAN, intruder.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(intruder, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(intruder, Keyword.ARCHER));
		assertEquals(6, intruder.getBlueprint().getTwilightCost());
		assertEquals(11, intruder.getBlueprint().getStrength());
		assertEquals(3, intruder.getBlueprint().getVitality());
		assertEquals(4, intruder.getBlueprint().getSiteNumber());
	}

	@Test
	public void AllRaiderMenAreStrengthPlus1At3TwilightTokensPlus2At6AndPlus3At9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var intruder = scn.GetShadowCard("intruder");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Laid in the fellowship phase only to read the standing text (Q-0228).  The Intruder himself and two
		// [raider] Men of different kinds, a Southron and an Easterling; a [raider] minion who is no Man, a Man
		// who is not [raider], and the Free Peoples player's own Man.
		scn.MoveMinionsToTable(intruder, explorer, axeman, khamul, brigand);

		// Two twilight tokens: one short of the first sentence; every printed strength.
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(intruder));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));

		// Three: every [raider] Man +1, the Intruder himself among them ("all", not "other"), and nobody else.
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(intruder));
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(7, scn.GetStrength(axeman));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));

		// Five: one short of the second sentence, still +1.
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(12, scn.GetStrength(intruder));
		assertEquals(6, scn.GetStrength(explorer));
		assertEquals(7, scn.GetStrength(axeman));

		// Six: the first two sentences, +2.
		scn.SetTwilight(6);
		assertEquals(6, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(intruder));
		assertEquals(7, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(axeman));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));

		// Eight: one short of the third sentence, still +2.
		scn.SetTwilight(8);
		assertEquals(8, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(intruder));
		assertEquals(7, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(axeman));

		// Nine: all three sentences, +3, and still nobody else.
		scn.SetTwilight(9);
		assertEquals(9, scn.GetTwilight());
		assertEquals(14, scn.GetStrength(intruder));
		assertEquals(8, scn.GetStrength(explorer));
		assertEquals(9, scn.GetStrength(axeman));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void TwoCopiesEachStrengthenEveryRaiderManThemselvesIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intruder = scn.GetShadowCard("intruder");
		var intruder2 = scn.GetShadowCard("intruder2");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(intruder, intruder2, explorer);

		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(intruder));
		assertEquals(11, scn.GetStrength(intruder2));
		assertEquals(5, scn.GetStrength(explorer));

		// Three: +1 from each copy, to each of the three.
		scn.SetTwilight(3);
		assertEquals(13, scn.GetStrength(intruder));
		assertEquals(13, scn.GetStrength(intruder2));
		assertEquals(7, scn.GetStrength(explorer));

		// Nine: +3 from each copy.
		scn.SetTwilight(9);
		assertEquals(17, scn.GetStrength(intruder));
		assertEquals(17, scn.GetStrength(intruder2));
		assertEquals(11, scn.GetStrength(explorer));
	}

	@Test
	public void SwitchesOnAsFreePeoplesPlaysAddTheThirdTheSixthAndTheNinthToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var legolas = scn.GetFreepsCard("legolas");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(merry, gimli, bounder, legolas, pippin);
		assertInHand(merry, gimli, bounder, legolas, pippin);

		var intruder = scn.GetShadowCard("intruder");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(intruder, explorer);
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(intruder));
		assertEquals(5, scn.GetStrength(explorer));

		scn.FreepsPlayCard(merry);
		assertEquals(12, scn.GetStrength(intruder));
		assertEquals(6, scn.GetStrength(explorer));

		scn.FreepsPlayCard(gimli);
		assertEquals(12, scn.GetStrength(intruder));
		assertEquals(6, scn.GetStrength(explorer));

		scn.FreepsPlayCard(bounder);
		assertEquals(13, scn.GetStrength(intruder));
		assertEquals(7, scn.GetStrength(explorer));

		scn.FreepsPlayCard(legolas);
		assertEquals(13, scn.GetStrength(intruder));
		assertEquals(7, scn.GetStrength(explorer));

		scn.FreepsPlayCard(pippin);
		assertEquals(14, scn.GetStrength(intruder));
		assertEquals(8, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
