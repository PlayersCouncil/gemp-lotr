package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					put("scout", "10_78");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					// Úlairë Nelya, Lieutenant of Morgul: fierce, vitality 3.  "Shadow: Exert Úlairë Nelya and
					// spot an opponent's site to replace it with your site of the same number."
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExertScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "11_157");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, for a second Weary
					// Weary: [isengard] condition, cost 1.  "Plays on a companion or ally.  Limit 1 per character."
					// Its trigger needs an [isengard] tracker.
					put("weary1", "4_212");
					put("weary2", "4_212");
					// Orthanc Warrior: vitality 2, so one exertion exhausts it; Damage +1 is its whole text
					put("warrior", "4_165");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RushofSteedsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rush of Steeds
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [rohan] Man.
		 * Each time a Shadow condition is played, you may exert a minion.
		 * <b>Response:</b> If a minion exerts as a cost of its special ability, discard this condition to prevent that and return that minion to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rush");

		assertEquals("Rush of Steeds", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RushofSteedsDoesntCrashWhenUsedAgainstAdvanceScout() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.MoveCardsToSupportArea(rush);

		var scout = scn.GetShadowCard("scout");
		scn.MoveMinionsToTable(scout);

		scn.StartGame();

		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);
		scn.ShadowChooseOption("exert");

		assertEquals(Zone.SUPPORT, rush.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, scout.getZone());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(Zone.DISCARD, rush.getZone());
		assertEquals(Zone.HAND, scout.getZone());
	}

	// Ketura on Q-0038: a cost that is prevented is "no longer considered paid", and what it was to buy does not
	// happen (CR 4.2 "cost": "If all the costs cannot be paid, that card has no effect").
	@Test
	public void TheAbilityWhoseExertionWasPreventedDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.MoveCardsToSupportArea(rush);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		var freepsSite1 = scn.GetFreepsSite(1);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		// The fellowship is at site 2, which the Shadow player laid.  Site 1 behind it is the Free Peoples
		// player's own: the one site of an opponent's that Nelya can spot.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(freepsSite1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, rush);
		assertInZone(Zone.HAND, nelya);
		// The exertion was the cost of Nelya's ability: site 1 is the card it was.
		assertSame(freepsSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// The control of the test above: the same board with the Response declined.  The site is replaced by the
	// Shadow player's copy of the same site, as the game is built (Q-0226).
	@Test
	public void WithTheResponseDeclinedTheMinionExertsAndItsAbilityIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var rush = scn.GetFreepsCard("rush");
		scn.MoveCardsToSupportArea(rush);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		assertSame(freepsSite1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Ketura on Q-0034: an optional trigger with no cost whose whole effect would do nothing is not offered.
	// "Each time a Shadow condition is played, you may exert a minion": with no minion in play who can exert,
	// a yes would do nothing.  The game used to offer it all the same (F-0373).
	@Test
	public void WithNoMinionWhoCanExertTheExertionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertScenario();

		var frodo = scn.GetRingBearer();
		var rush = scn.GetFreepsCard("rush");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(rush);

		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(weary1, weary2);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// A Shadow condition is played while the Warrior can exert: the offer is made, and taken.
		assertEquals(0, scn.GetWoundsOn(warrior));
		scn.ShadowPlayCard(weary1);
		scn.ShadowChooseCard(frodo);
		assertAttachedTo(weary1, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(rush));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.IsExhausted(warrior));

		// A second is played, on the one companion who bears none: the only minion is exhausted, and nothing
		// is asked of the Free Peoples player.
		scn.ShadowPlayCard(weary2);
		assertAttachedTo(weary2, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(rush));
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
