package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_027_Tests
{

	// Site 1 has no terrain, 2 is a forest, 3 a sanctuary and no forest, 6 a forest and a sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentinel", "11_27");
					put("sentinel2", "11_27");
					put("legolas", "1_50");     // Legolas, Greenleaf: another Elf companion, strength 6; an archery ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "1_334");  // Trollshaw Forest: a forest alone; draws on a possession played on a companion
					put("site6", "1_352");  // Lothlorien Woods: a forest and a sanctuary; strengthens allies whose home is site 6
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoodlandSentinelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Woodland Sentinel
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While this companion is at a forest site, he is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentinel");

		assertEquals("Woodland Sentinel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void EachCopyIsStrengthPlus2AtAForestAndNotAtASiteThatIsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentinel = scn.GetFreepsCard("sentinel");
		var sentinel2 = scn.GetFreepsCard("sentinel2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(sentinel, sentinel2, legolas);

		scn.StartGame();

		// Bag End: no terrain.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(sentinel));
		assertEquals(5, scn.GetStrength(sentinel2));
		assertEquals(6, scn.GetStrength(legolas));

		// The fellowship's own move to Trollshaw Forest, a forest with no sanctuary.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 5 + 2 each: the second copy does not add to the first.
		assertEquals(7, scn.GetStrength(sentinel));
		assertEquals(7, scn.GetStrength(sentinel2));
		// Another Elf companion at the same forest is not raised.
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Rivendell Valley: a sanctuary and no forest.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(sentinel));
		assertEquals(5, scn.GetStrength(sentinel2));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Lothlorien Woods: a forest, and a sanctuary.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(sentinel));
		assertEquals(7, scn.GetStrength(sentinel2));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
