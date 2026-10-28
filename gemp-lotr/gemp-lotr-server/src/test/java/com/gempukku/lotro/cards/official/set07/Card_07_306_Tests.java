package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_306_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeker", "7_306");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, site 6, strength 7; his text answers a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc, site 5; his text works only while he is stacked
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai), site 6; damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), site 4; his text needs 2 wounds on the Ring-bearer
					put("hand1", "1_7");        // Dwarf Guard: cards for the Free Peoples hand, which decides initiative
					put("hand2", "1_7");
					put("hand3", "1_7");
					put("hand4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSeekerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Seeker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.
		 * The site number of each [sauron] Orc is -1.
		 * If you have initiative when you play this minion, you may spot another [sauron] Orc to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seeker");

		assertEquals("Orc Seeker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheSiteNumberOfEachSauronOrcIsMinusOneItsOwnIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);

		// Before the Seeker is in play: printed site numbers.
		assertEquals(6, scn.GetMinionSiteNumber(seeker));
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(pillager));
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));

		scn.MoveMinionsToTable(seeker);

		assertEquals(5, scn.GetMinionSiteNumber(seeker));
		assertEquals(5, scn.GetMinionSiteNumber(chieftain));
		assertEquals(4, scn.GetMinionSiteNumber(pillager));
		// The [sauron] Uruk-hai and the [wraith] Orc are no [sauron] Orcs.
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite5ASauronOrcOfSiteNumber6RoamsNoLongerOnceTheSeekerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(chieftain, sentry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));

		scn.MoveMinionsToTable(seeker);

		// 6 - 1 is 5, the fellowship's site: the Chieftain is not roaming; the [sauron] Uruk-hai still is.
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(seeker, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASauronOrcPlayedAtSite5BesideTheSeekerPaysNoRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(seeker);
		scn.MoveCardsToHand(chieftain);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		// The Chieftain's site number is 5 at site 5: his cost of 2 and no roaming penalty of 2.
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		scn.ShadowPlayCard(chieftain);

		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWithInitiativeAndAnotherSauronOrcHeMayDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(seeker);
		assertInHand(seeker);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// The Free Peoples hand is empty: the Shadow has initiative.
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(seeker);
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(seeker));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsDrawnAndAnotherOrcPlayedAfterHimSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var pillager = scn.GetShadowCard("pillager");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(seeker, chieftain);
		assertInHand(seeker, chieftain);

		scn.StartGame();
		// Another [sauron] Orc of another title than the first test's.
		scn.MoveMinionsToTable(pillager);
		scn.FreepsPass();
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowHasInitiative());
		assertEquals(2, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(seeker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(seeker));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [sauron] Orc played while the Seeker is in play: his text answers his own play alone.
		scn.ShadowPlayCard(chieftain);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesPlayerHoldingInitiativeNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(seeker);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		assertInHand(seeker);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(seeker);
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherSauronOrcBesideAnUrukHaiAndAWraithOrcNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeker = scn.GetShadowCard("seeker");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(seeker);
		assertInHand(seeker);

		scn.StartGame();
		// A [sauron] minion who is no Orc and an Orc who is not [sauron]; the Seeker himself is not "another".
		scn.MoveMinionsToTable(sentry, hound);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowHasInitiative());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(seeker);
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
