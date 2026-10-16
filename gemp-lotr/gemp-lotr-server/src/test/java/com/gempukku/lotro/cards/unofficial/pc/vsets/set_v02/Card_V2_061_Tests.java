package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wight1", "102_61");
					put("wight2", "102_61");
					put("wight3", "102_61");

					// Ulaire Nelya, Ringwraith in Twilight: a twilight Nazgul of [ringwraith], cost 5, site 2.
					// The rest of his text needs 2 burdens or 2 wounds on the Ring-bearer.
					put("nelya", "2_84");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul who is not twilight.  His text is about
					// companions bearing [ringwraith] conditions, and there are none.
					put("lemenya", "1_232");
					// Corpse Lights: a twilight [sauron] Wraith that is not a Marsh Wight, cost 3, site 4.
					// A skirmish ability that is never used.
					put("lights1", "6_99");
					put("lights2", "6_99");
					// Orc Taskmaster: a [sauron] Orc who is not twilight.  "Shadow: Exert this minion to add (1)."
					put("taskmaster", "2_90");
					// Cover of Darkness, Omen of Fear: a condition that has the keyword twilight and is no minion.
					// The rest of its text is about being played and an assignment action that needs 3
					// [ringwraith] minions; it is put in the support area and no test reaches an assignment phase.
					put("cover", "103_64");

					put("sam", "1_311");       // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
					put("light", "4_309");      // Light Shining Faintly: "Fellowship: Add a burden to heal a Ring-bound companion twice."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MarshWightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Marsh Wight
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Wraith
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Twilight.
		 * To play, spot a twilight minion.
		 * This minion is twilight cost -1 for each burden you can spot.
		 * While you can spot 4 burdens, this minion is damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wight1");

		assertEquals("Marsh Wight", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilATwilightMinionIsSpottedAndThenCostsFiveAndTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wight1 = scn.GetShadowCard("wight1");
		var wight2 = scn.GetShadowCard("wight2");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var cover = scn.GetShadowCard("cover");
		scn.MoveCardsToHand(wight1, wight2, nelya);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, taskmaster);
		scn.MoveCardsToSupportArea(cover);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());
		// 8 set + 1 companion + 2 for the site: more than a Marsh Wight costs.
		assertEquals(11, scn.GetTwilight());
		// A Nazgul and a [sauron] minion are in play, and neither is twilight.  A twilight condition is in
		// play, and is no minion.  The Marsh Wights in hand are twilight minions, and are not in play to be
		// spotted.
		assertInZone(Zone.SUPPORT, cover);
		assertTrue(scn.HasKeyword(cover, Keyword.TWILIGHT));
		assertFalse(scn.IsHindered(cover));
		assertFalse(scn.ShadowPlayAvailable(wight1));
		assertFalse(scn.ShadowPlayAvailable(wight2));
		assertTrue(scn.ShadowPlayAvailable(nelya));

		scn.ShadowPlayCard(nelya);
		// 5 to play, and no roaming at site 2 for a minion of site number 2
		assertEquals(6, scn.GetTwilight());
		// Now there is a twilight minion, and the pool is one short: 5 + 2 roaming (site number 4 at site 2).
		assertFalse(scn.ShadowPlayAvailable(wight1));

		scn.ShadowUseCardAction(taskmaster);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(wight1));
		assertTrue(scn.ShadowPlayAvailable(wight2));

		scn.ShadowPlayCard(wight1);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, wight1);
		assertInZone(Zone.HAND, wight2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsOneLessForEachBurdenUntilOnlyTheRoamingPenaltyIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wight1 = scn.GetShadowCard("wight1");
		var wight2 = scn.GetShadowCard("wight2");
		var wight3 = scn.GetShadowCard("wight3");
		var lights1 = scn.GetShadowCard("lights1");
		var lights2 = scn.GetShadowCard("lights2");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToHand(wight1, wight2, wight3, lights2);

		scn.StartGame();
		scn.AddBurdens(1);
		// Corpse Lights is the twilight minion that is spotted.
		scn.MoveMinionsToTable(lights1, taskmaster);
		scn.SetTwilight(15);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetBurdens());
		// 15 set + 1 companion + 2 for the site
		assertEquals(18, scn.GetTwilight());

		scn.ShadowPlayCard(wight1);
		// 5 - 1 burden + 2 roaming
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, wight1);

		// Another twilight [sauron] Wraith costs what it prints, burden or no: 3 + 2 roaming.
		scn.ShadowPlayCard(lights2);
		assertEquals(7, scn.GetTwilight());

		scn.AddBurdens(2);
		scn.ShadowPlayCard(wight2);
		// 5 - 3 burdens + 2 roaming
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, wight2);
		// The third copy costs 4 as well, one more than is left: the two copies in play take nothing more
		// from it.
		assertFalse(scn.ShadowPlayAvailable(wight3));

		scn.ShadowUseCardAction(taskmaster);
		// A pool of 4 is short of the printed 5, and enough.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(wight3));

		scn.AddBurdens(3);
		assertEquals(6, scn.GetBurdens());
		scn.ShadowPlayCard(wight3);
		// 5 - 6 burdens is no cost at all, and the 2 for roaming are still paid.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, wight3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyIsDamagePlusOneWhileFourBurdensCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wight1 = scn.GetShadowCard("wight1");
		var wight2 = scn.GetShadowCard("wight2");
		var lights = scn.GetShadowCard("lights1");
		var sam = scn.GetFreepsCard("sam");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(wight1, wight2, lights);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetKeywordCount(wight1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(wight2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lights, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(light));

		// The fourth burden is the cost of Light Shining Faintly; nobody is wounded for it to heal.
		scn.FreepsPlayCard(light);
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(wight1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(wight2, Keyword.DAMAGE));
		// Another twilight [sauron] Wraith beside them has none.
		assertEquals(0, scn.GetKeywordCount(lights, Keyword.DAMAGE));

		// A fifth burden adds nothing more.
		scn.AddBurdens(1);
		assertEquals(1, scn.GetKeywordCount(wight1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(wight2, Keyword.DAMAGE));

		scn.FreepsUseCardAction(sam);
		assertEquals(4, scn.GetBurdens());
		assertEquals(1, scn.GetKeywordCount(wight1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(wight2, Keyword.DAMAGE));

		scn.FreepsUseCardAction(sam);
		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetKeywordCount(wight1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(wight2, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
