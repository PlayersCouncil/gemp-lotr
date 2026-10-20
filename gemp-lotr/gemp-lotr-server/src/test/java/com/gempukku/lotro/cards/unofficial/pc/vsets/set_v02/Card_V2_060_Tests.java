package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MinionSiteNumberModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spotter1", "102_60");
					put("spotter2", "102_60");

					// Grishnakh, Servant of Lugburz: "The site number of each [sauron] orc is -3."  His skirmish
					// ability is never used.
					put("grishnakh", "102_59");
					// Orc Chaser: a [sauron] Orc, cost 2, site 6.  "The site number of each [sauron] Orc is -1."
					// Its other sentence is about its own strength.
					put("chaser1", "7_298");
					put("chaser2", "7_298");
					put("chaser3", "7_298");
					put("chaser4", "7_298");
					// Orc Soldier: another [sauron] Orc, strength 7, site 6.  A skirmish ability that is never used.
					put("soldier", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LugburzSpotterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Lugburz Spotter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: Tracker.
		 * If this minion's site number is 1 or less, it is strength +5.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spotter1");

		assertEquals("Lugburz Spotter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysForTwoAndTheRoamingPenaltyAndIsStrengthSixAtSiteNumberSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spotter = scn.GetShadowCard("spotter1");
		scn.MoveCardsToHand(spotter);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 1 set + 1 companion + 2 for the site
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(spotter));

		scn.ShadowPlayCard(spotter);
		// 2 to play + 2 roaming (site number 6 at site 2)
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, spotter);
		assertEquals(6, scn.GetMinionSiteNumber(spotter));
		assertEquals(6, scn.GetStrength(spotter));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyIsStrengthPlusFiveOnceItsOwnSiteNumberIsOneOrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spotter1 = scn.GetShadowCard("spotter1");
		var spotter2 = scn.GetShadowCard("spotter2");
		var grishnakh = scn.GetShadowCard("grishnakh");
		var chaser1 = scn.GetShadowCard("chaser1");
		var chaser2 = scn.GetShadowCard("chaser2");
		var chaser3 = scn.GetShadowCard("chaser3");
		var chaser4 = scn.GetShadowCard("chaser4");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(chaser2, chaser3);

		scn.StartGame();
		scn.MoveMinionsToTable(spotter1, spotter2, grishnakh, chaser1, soldier);
		// The second copy is kept one step above the first, so that the two can be told apart.
		scn.ApplyAdHocModifier(new MinionSiteNumberModifier(null, spotter2, null, 1));
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 6 - 3 for Grishnakh - 1 for the Chaser; the second copy 1 more.
		assertEquals(2, scn.GetMinionSiteNumber(spotter1));
		assertEquals(3, scn.GetMinionSiteNumber(spotter2));
		assertEquals(2, scn.GetMinionSiteNumber(soldier));
		assertEquals(6, scn.GetStrength(spotter1));
		assertEquals(6, scn.GetStrength(spotter2));
		assertEquals(7, scn.GetStrength(soldier));

		// A second Chaser is played: the first copy is at exactly 1, the second at 2.
		scn.ShadowPlayCard(chaser2);
		assertEquals(1, scn.GetMinionSiteNumber(spotter1));
		assertEquals(2, scn.GetMinionSiteNumber(spotter2));
		assertEquals(1, scn.GetMinionSiteNumber(soldier));
		// 6 + 5
		assertEquals(11, scn.GetStrength(spotter1));
		assertEquals(6, scn.GetStrength(spotter2));
		// Another [sauron] Orc at site number 1 is no stronger for it.
		assertEquals(7, scn.GetStrength(soldier));

		// A third: the first copy is at 0 and the second at exactly 1.  Each is +5 for itself, and once.
		scn.ShadowPlayCard(chaser3);
		assertEquals(0, scn.GetMinionSiteNumber(spotter1));
		assertEquals(1, scn.GetMinionSiteNumber(spotter2));
		assertEquals(11, scn.GetStrength(spotter1));
		assertEquals(11, scn.GetStrength(spotter2));
		assertEquals(7, scn.GetStrength(soldier));

		// A fourth takes the first copy below zero, which is zero.
		scn.MoveMinionsToTable(chaser4);
		assertEquals(0, scn.GetMinionSiteNumber(spotter1));
		assertEquals(0, scn.GetMinionSiteNumber(spotter2));
		assertEquals(11, scn.GetStrength(spotter1));
		assertEquals(11, scn.GetStrength(spotter2));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
