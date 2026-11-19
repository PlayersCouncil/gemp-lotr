package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("runner", "5_109");
					put("runner2", "5_109");     // a second copy: not unique, and "exert him"
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, site 6; his text answers a weapon played on him
					put("pillager", "7_275");     // Gorgoroth Pillager: [sauron] Orc, site 5; his text works only while he is stacked
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai), site 6; damage +1 only
					put("hound", "7_192");        // Morgul Hound: an Orc who is not [sauron] ([wraith]), site 4; his text needs 2 wounds on the Ring-bearer
					put("orctrooper", "3_99");    // Orc Trooper: [sauron] Orc; strength +1 for each card in the Shadow hand, no trigger
					put("blood", "2_31");         // Blood of Númenor: each [sauron] Orc comes into play exhausted (and skip the archery phase)
					// The Shadow hand to discard from: a minion, a possession, a condition of another culture and a Free Peoples card
					put("scimitar", "1_269");     // Orc Scimitar: [sauron] possession
					put("armory", "1_173");       // Goblin Armory: [moria] condition
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion
					put("deck1", "1_278");        // Strength Born of Fear: the card drawn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcRunnerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Orc Runner
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
		 * When you play this minion, you may exert him and discard 2 cards from hand to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("runner");

		assertEquals("Orc Runner", card.getBlueprint().getTitle());
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

		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager, sentry, hound);

		// Before the Runner is in play: printed site numbers.
		assertEquals(6, scn.GetMinionSiteNumber(runner));
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(pillager));
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));

		scn.MoveMinionsToTable(runner);

		assertEquals(5, scn.GetMinionSiteNumber(runner));
		assertEquals(5, scn.GetMinionSiteNumber(chieftain));
		assertEquals(4, scn.GetMinionSiteNumber(pillager));
		// The [sauron] Uruk-hai and the [wraith] Orc are no [sauron] Orcs.
		assertEquals(6, scn.GetMinionSiteNumber(sentry));
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite5ASauronOrcOfSiteNumber6RoamsNoLongerOnceTheRunnerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(chieftain, sentry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));

		scn.MoveMinionsToTable(runner);

		// 6 - 1 is 5, the fellowship's site: the Chieftain is not roaming; the [sauron] Uruk-hai still is.
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(runner, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(sentry, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenPlayedHeMayExertAndDiscardTwoCardsOfAnyKindFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var orctrooper = scn.GetShadowCard("orctrooper");
		var scimitar = scn.GetShadowCard("scimitar");
		var armory = scn.GetShadowCard("armory");
		var aragorn = scn.GetShadowCard("aragorn");
		var deck1 = scn.GetShadowCard("deck1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetShadowHand(runner2, orctrooper, scimitar, armory, aragorn);
		scn.SetShadowDrawDeck(deck1);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(runner2);
		// Only the copy just played answers its own play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(runner2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner));
		assertEquals(0, scn.GetWoundsOn(runner2));
		assertEquals(4, scn.GetShadowHandCount());

		scn.ShadowAcceptOptionalTrigger();
		// Any card of the hand may go: a minion, a possession, a condition of another culture, a Free Peoples card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(orctrooper, scimitar, armory, aragorn));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(armory, aragorn);

		assertEquals(1, scn.GetWoundsOn(runner2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.DISCARD, armory);
		assertInZone(Zone.DISCARD, aragorn);
		assertInHand(orctrooper);
		assertInHand(scimitar);
		// 4 - 2 discarded + 1 drawn.
		assertEquals(3, scn.GetShadowHandCount());
		assertInHand(deck1);
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndAnotherSauronOrcPlayedAfterHimIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var orctrooper = scn.GetShadowCard("orctrooper");
		var scimitar = scn.GetShadowCard("scimitar");
		var armory = scn.GetShadowCard("armory");

		scn.StartGame();
		scn.SetShadowHand(runner, orctrooper, scimitar, armory);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertEquals(4, scn.GetShadowHandCount());
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(runner);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(runner));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(orctrooper);
		assertInPlay(orctrooper);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(2, scn.GetShadowHandCount());
	}

	@Test
	public void WithOneCardLeftInHandAfterHisPlayHeMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SetShadowHand(runner, scimitar);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowPlayCard(runner);
		assertInPlay(runner);
		// One card in hand: the discard of 2 cannot be paid in full.
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInHand(scimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedExhaustedUnderBloodOfNumenorHeMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var armory = scn.GetShadowCard("armory");
		var blood = scn.GetFreepsCard("blood");
		scn.MoveCardsToSupportArea(blood);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, blood);
		scn.SetShadowHand(runner, scimitar, armory);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertEquals(3, scn.GetShadowHandCount());

		scn.ShadowPlayCard(runner);
		// Vitality 2 with 1 wound as he comes into play: exhausted, so he cannot exert, though 2 cards are in hand.
		assertEquals(1, scn.GetWoundsOn(runner));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(runner));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
