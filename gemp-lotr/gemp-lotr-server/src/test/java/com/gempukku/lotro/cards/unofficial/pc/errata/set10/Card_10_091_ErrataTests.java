package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_091_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fiend1", "60_91");
					put("fiend2", "60_91");

					put("brigand", "4_10");         // Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text: a minion of another culture and of no Orc race
					put("fear", "1_278");           // Strength Born of Fear: a [sauron] card that is no minion (a support-area condition; its ability costs (3) and is not used)
				}},
				// Shadow numbers: site 2 Ettenmoors 2, site 6 Galadriel's Glade 3.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorFiendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Mordor Fiend
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: When you play this minion, if you cannot spot another minion and there are 3 or fewer
		 * twilight tokens, add (3) (or (5) if this minion is roaming).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fiend1");

		assertEquals("Mordor Fiend", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtSite2ForThreeAndTwoMoreForRoamingAndWithFourTokensLeftAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");
		scn.MoveCardsToHand(fiend);

		scn.StartGame();
		scn.SetTwilight(6);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 6 set + 1 companion + 2 for the site
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fiend));

		scn.ShadowPlayCard(fiend);
		// 3 to play + 2 roaming: its site number is 6 and this is site 2.  4 tokens are left, one too many.
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARoamingFiendWithThreeTokensLeftAddsFiveAndASecondFiendPlayedBesideItAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend1 = scn.GetShadowCard("fiend1");
		var fiend2 = scn.GetShadowCard("fiend2");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(fiend1, fiend2);
		// A [sauron] card in play that is no minion.
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 1 companion + 2 for the site
		assertEquals(8, scn.GetTwilight());

		scn.ShadowPlayCard(fiend1);
		// 3 to play + 2 roaming leaves exactly 3, and there is no other minion: 5 are added, not 3.
		assertInZone(Zone.SHADOW_CHARACTERS, fiend1);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(fiend2);
		// Again 3 are left, but now the first Fiend is another minion.
		assertInZone(Zone.SHADOW_CHARACTERS, fiend2);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMinionOfAnotherCultureAndRaceInPlayTheFiendAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(fiend);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand);
		scn.ShadowRefreshDecision();

		// 5 set + 1 companion + 2 for the site
		assertEquals(8, scn.GetTwilight());

		scn.ShadowPlayCard(fiend);
		// 3 to play + 2 roaming leaves 3, which would do; the Dunlending Brigand, a Man of another culture, is what stops it.
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6TheFiendIsNotRoamingAndAddsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fiend);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		// 2 set + 1 companion + 3 for the site
		assertEquals(6, scn.GetTwilight());

		scn.ShadowPlayCard(fiend);
		// 3 to play and no roaming at its own site leaves exactly 3: 3 are added, not 5.
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6ItIsPlayedForThreeAndWithFourTokensLeftAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToHand(fiend);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(6, scn.GetCurrentSiteNumber());
		// 3 set + 1 companion + 3 for the site
		assertEquals(7, scn.GetTwilight());

		scn.ShadowPlayCard(fiend);
		// 3 to play, nothing for roaming: 4 are left, one too many.
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
