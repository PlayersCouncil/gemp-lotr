package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("maggot", "11_163");
					put("merry", "1_302");      // Merry, Friend to Sam: another Hobbit companion; his skirmish ability is
					                            //   used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Site 1 is Bag End, which has no terrain.
					put("site2", "4_330");      // Derndingle: a forest; "While you can spot an unbound Hobbit at Derndingle,
					                            //   the move limit is +1 for this turn"
					put("site3", "1_342");      // Rivendell Waterfall: a forest and a sanctuary; a move-limit text for a ranger
					remove("site8");
					remove("site9");
					// Shadows-block sites, laid on the path by MoveCardToAdventurePath as the sites after site 3.
					put("bree", "18_140");      // Streets of Bree: a dwelling; a bar on transferring followers
					put("valley", "11_261");    // Valley of the Silverlode: a dwelling and a river; "When your fellowship
					                            //   moves to this site, you may heal each Hobbit companion"
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarmerMaggotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Farmer Maggot, Hobbit of the Marish
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 8
		 * Game Text: While Farmer Maggot is at a dwelling or forest site, he is strength +4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("maggot");

		assertEquals("Farmer Maggot", card.getBlueprint().getTitle());
		assertEquals("Hobbit of the Marish", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void HeIsStrengthPlus4AtEachForestAndEachDwellingSiteAndNotElsewhereAndNoOtherHobbitIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var maggot = scn.GetFreepsCard("maggot");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(maggot, merry);

		scn.StartGame();
		// Bag End: no terrain.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(merry));

		// Derndingle, a forest.
		scn.SkipToSite(2);
		assertSame(scn.GetFreepsSite("site2").getBlueprint(), scn.GetCurrentSite().getBlueprint());
		assertEquals(7, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(merry));

		// Rivendell Waterfall, a forest that is also a sanctuary.
		scn.SkipToSite(3);
		assertSame(scn.GetFreepsSite("site3").getBlueprint(), scn.GetCurrentSite().getBlueprint());
		assertEquals(7, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(merry));

		// Streets of Bree, a dwelling.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("bree"));
		scn.FreepsPass();
		assertSame(scn.GetShadowSite("bree"), scn.GetCurrentSite());
		assertEquals(7, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(merry));

		// Valley of the Silverlode, a dwelling that is also a river, by a second move.
		scn.MoveCardToAdventurePath(scn.GetShadowSite("valley"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertSame(scn.GetShadowSite("valley"), scn.GetCurrentSite());
		assertEquals(7, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Galadriel's Glade, on the next turn: a sanctuary that is neither a forest nor a dwelling.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
