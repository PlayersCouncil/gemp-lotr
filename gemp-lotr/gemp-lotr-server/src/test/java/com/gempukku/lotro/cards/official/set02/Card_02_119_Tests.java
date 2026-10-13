package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_119_Tests
{

	// The Fellowship path with Hollin as site 4.  Site 3 is Rivendell Valley (Shadow number 0) and site 5 The
	// Bridge of Khazad-dûm (Shadow number 6; its Shadow action plays The Balrog only).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site number 5; Damage +1 is its whole text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, site number 6; Damage +1 is its whole text
					put("guard2", "10_81");
					// Orc Chieftain: a [sauron] Orc, cost 2, site number 6; its text answers a weapon played on it
					put("chieftain", "1_266");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "2_119");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HollinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Hollin
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: Plains. Uruk-hai are not roaming.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Hollin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void AtHollinUrukHaiOfEitherCultureAreNotRoamingAndAnOrcStillIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollin = scn.GetShadowSite(4);
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(savage, guard, chieftain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(hollin, scn.GetCurrentSite());
		// 10 set + 1 companion + 3 for Hollin
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [isengard] Uruk-hai of site number 5: 2 and no roaming penalty.
		scn.ShadowPlayCard(savage);
		assertEquals(12, scn.GetTwilight());
		assertFalse(scn.HasKeyword(savage, Keyword.ROAMING));

		// A [sauron] Uruk-hai of site number 6: 3 and no roaming penalty.
		scn.ShadowPlayCard(guard);
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.HasKeyword(guard, Keyword.ROAMING));

		// A [sauron] Orc of site number 6 is no Uruk-hai: 2 + 2 roaming.
		scn.ShadowPlayCard(chieftain);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithHollinStillAheadAnUrukHaiIsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(guard);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());

		// Hollin is on the path but the fellowship is not there: 3 + 2 roaming.
		scn.ShadowPlayCard(guard);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftHollinUrukHaiAreRoamingAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hollin = scn.GetShadowSite(4);
		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");

		scn.StartGame();
		scn.SkipToSite(4);
		assertSame(hollin, scn.GetCurrentSite());
		// Laid in the fellowship phase, to stand in play through the move that leaves Hollin.
		scn.MoveMinionsToTable(guard);
		scn.SetShadowHand(guard2);
		assertFalse(scn.HasKeyword(guard, Keyword.ROAMING));

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		// 10 set + 1 companion + 6 for The Bridge of Khazad-dûm
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard, Keyword.ROAMING));

		// 3 + 2 roaming.
		scn.ShadowPlayCard(guard2);
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard2, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
