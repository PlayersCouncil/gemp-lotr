package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fiend1", "10_91");
					put("fiend2", "10_91");

					put("brigand", "4_10");         // Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text: a minion of another culture and race
					put("fear", "1_278");           // Strength Born of Fear: a [sauron] support-area condition, a Shadow card that is no minion; its skirmish ability is never used
				}},
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
		 * Game Text: <b>Shadow:</b> If you cannot spot another minion and there are 3 or fewer twilight tokens, add (3) (or (5) if this minion is roaming).
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
	public void AtSite6TheFiendAloneAddsThreeAndAgainWhileThePoolHoldsThreeOrFewer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");
		var fear = scn.GetShadowCard("fear");
		// A Shadow card in play that is no minion.
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(fiend);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		scn.FreepsPass();

		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertEquals(6, scn.GetCurrentSiteNumber());
		// Site number 6 at site 6: not roaming.
		assertFalse(scn.HasKeyword(fiend, Keyword.ROAMING));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(fiend));

		scn.ShadowUseCardAction(fiend);
		assertEquals(3, scn.GetTwilight());

		// No limit is printed, and 3 is still "3 or fewer".
		assertTrue(scn.ShadowActionAvailable(fiend));
		scn.ShadowUseCardAction(fiend);
		assertEquals(6, scn.GetTwilight());

		assertFalse(scn.ShadowActionAvailable(fiend));
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite2ARoamingFiendAddsFiveAndNotThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");

		scn.MoveCardsToHand(fiend);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 set + 1 companion + 2 for the site
		assertEquals(5, scn.GetTwilight());

		// Played from hand at site 2, where it roams: 3 and 2 for roaming.
		scn.ShadowPlayCard(fiend);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		// Site number 6 at site 2: roaming.
		assertTrue(scn.HasKeyword(fiend, Keyword.ROAMING));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(fiend));

		scn.ShadowUseCardAction(fiend);
		// 0 + 5: not 0 + 3, nor 0 + 3 + 5.
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(fiend));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourTwilightTokensTheAbilityIsNotOfferedAndWithThreeItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");

		scn.StartGame();
		scn.MoveMinionsToTable(fiend);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 companion + 2 for the site
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(fiend));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(fiend));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondFiendInPlayStopsBothCopies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend1 = scn.GetShadowCard("fiend1");
		var fiend2 = scn.GetShadowCard("fiend2");

		scn.StartGame();
		scn.MoveMinionsToTable(fiend1, fiend2);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend1, fiend2);
		scn.FreepsPass();

		// 1 companion + 2 for the site: few enough; each copy is the other's "another minion".
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(fiend1));
		assertFalse(scn.ShadowActionAvailable(fiend2));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionOfAnotherCultureAndRaceStopsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fiend = scn.GetShadowCard("fiend1");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(fiend, brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, fiend, brigand);
		scn.FreepsPass();

		// 1 companion + 2 for the site: few enough; the [dunland] Man is what stops it.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(fiend));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
