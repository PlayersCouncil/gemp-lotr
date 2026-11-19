package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pursuer", "5_108");
					put("pursuer2", "5_108");     // a second copy: not unique, and "exert him"
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, site 6; his text answers a weapon played on him
					put("pillager", "7_275");     // Gorgoroth Pillager: [sauron] Orc, site 5; his text works only while he is stacked
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai), site 6; damage +1 only
					put("hound", "7_192");        // Morgul Hound: an Orc who is not [sauron] ([wraith]), site 4; his text needs 2 wounds on the Ring-bearer
					put("orctrooper", "3_99");    // Orc Trooper: [sauron] Orc; strength +1 for each card in the Shadow hand, no trigger
					put("blood", "2_31");         // Blood of Númenor: each [sauron] Orc comes into play exhausted (and skip the archery phase)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcPursuerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Pursuer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.
		 * The site number of each [sauron] Orc is -1.
		 * When you play this minion, you may exert him and remove (2) to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pursuer");

		assertEquals("Orc Pursuer", card.getBlueprint().getTitle());
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
	public void TheSiteNumberOfEachSauronOrcIsMinusOneItsOwnIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);

		// Before the Pursuer is in play: printed site numbers.
		assertEquals(6, scn.GetMinionSiteNumber(pursuer));
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(pillager));
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));

		scn.MoveMinionsToTable(pursuer);

		assertEquals(5, scn.GetMinionSiteNumber(pursuer));
		assertEquals(5, scn.GetMinionSiteNumber(chieftain));
		assertEquals(4, scn.GetMinionSiteNumber(pillager));
		// The [sauron] Uruk-hai and the [wraith] Orc are no [sauron] Orcs.
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite5ASauronOrcOfSiteNumber6RoamsNoLongerOnceThePursuerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(chieftain, sentry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));

		scn.MoveMinionsToTable(pursuer);

		// 6 - 1 is 5, the fellowship's site: the Chieftain is not roaming; the [sauron] Uruk-hai still is.
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(pursuer, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenPlayedHeMayExertAndRemoveTwoToDrawACardAndACopyInPlayMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCardsToHand(pursuer2);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(pursuer2);
		// Only the copy just played answers its own play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pursuer2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer2));
		assertEquals(0, scn.GetShadowHandCount());
		int pool = scn.GetTwilight();

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(pursuer2));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(pool - 2, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndAnotherSauronOrcPlayedAfterHimIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var orctrooper = scn.GetShadowCard("orctrooper");
		scn.MoveCardsToHand(pursuer, orctrooper);

		scn.StartGame();
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertEquals(2, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(pursuer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		int pool = scn.GetTwilight();
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(orctrooper);
		assertInPlay(orctrooper);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetShadowHandCount());
	}

	@Test
	public void PlayedExhaustedUnderBloodOfNumenorHeMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		var blood = scn.GetFreepsCard("blood");
		scn.MoveCardsToHand(pursuer);
		scn.MoveCardsToSupportArea(blood);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, blood);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(pursuer);
		// Vitality 2 with 1 wound as he comes into play: exhausted, so he cannot exert.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneTwilightLeftAfterHisPlayHeMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToHand(pursuer);

		scn.StartGame();
		// 2 set + 1 companion + 2 for site 2 = 5; he costs 2 and 2 more for roaming at site 2 (his own -1 makes
		// him site 5, still above 2), so 1 is left for a removal of (2).
		scn.SetTwilight(2);

		scn.FreepsPass();
		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(pursuer);
		assertInPlay(pursuer);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
