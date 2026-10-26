package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_14_007_Tests
{

	// Site 1 has no terrain, 2 is a forest, 3 a river and a sanctuary, 4 underground, 6 a forest and a sanctuary, 7 a river.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("duilin", "14_7");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, strength 8; a maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");  // Bag End: no terrain
					put("site2", "1_334");  // Trollshaw Forest: forest alone; draws on a possession played on a companion
					put("site3", "1_338");  // Ford of Bruinen: river, sanctuary; a discount for a Nazgul
					put("site4", "1_343");  // Balin's Tomb: underground
					put("site5", "1_349");
					put("site6", "1_352");  // Lothlorien Woods: forest, sanctuary; strengthens allies whose home is site 6
					put("site7", "1_353");  // Anduin Confluence: river alone; discards every ally as the fellowship moves here
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DuilinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Duilin, Ranger From Blackroot Vale
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: <b>Ranger</b>.<br>While the fellowship is at a forest or river site, Duilin is strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("duilin");

		assertEquals("Duilin", card.getBlueprint().getTitle());
		assertEquals("Ranger From Blackroot Vale", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void IsStrengthPlus3AtAForestAndAtARiverAndNotAtASiteThatIsNeither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duilin = scn.GetFreepsCard("duilin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(duilin, aragorn);

		scn.StartGame();

		// Bag End: no terrain.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(duilin));
		assertEquals(8, scn.GetStrength(aragorn));

		// The fellowship's own move to Trollshaw Forest: a forest with no sanctuary.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(duilin));
		// Another [gondor] Man ranger beside him is not raised.
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Ford of Bruinen: a river, and a sanctuary.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(duilin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Balin's Tomb: underground, neither a forest nor a river.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(duilin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Lothlorien Woods: a forest, and a sanctuary.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(duilin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Anduin Confluence: a river with no sanctuary.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(duilin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
