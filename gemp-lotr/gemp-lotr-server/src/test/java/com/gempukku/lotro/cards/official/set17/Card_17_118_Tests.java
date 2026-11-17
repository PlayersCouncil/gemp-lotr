package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "17_118");
					// [uruk-hai] hunters of two titles, site 5, no text but the keyword:
					put("tracking", "15_171");  // Tracking Uruk: cost 5, strength 14, vitality 3, Hunter 1
					put("butcher", "17_122");   // White Hand Butcher: cost 4, strength 5, vitality 2, Hunter 6
					put("butcher2", "17_122");
					// Uruk Decimator: an [uruk-hai] Uruk-hai with no keyword while it bears nothing (strength +2 at a
					// battleground, which no site of this path is)
					put("decimator", "12_150");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no hunter; companions resistance
					// -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");
					// Hunting Herdsman: a [men] Man minion, Hunter 1; strength +3 for each follower (none here)
					put("herdsman", "15_83");
					// White Hand Warrior: an [uruk-hai] hunter of a third title, for the Free Peoples discard pile
					put("warrior", "17_136");
					// Faramir, Captain of Ithilien: a [gondor] companion, Hunter 2, strength 7; a Maneuver heal used
					// only on purpose
					put("faramir", "18_48");
					// Blade of the White Hand: an [uruk-hai] possession, for the hand
					put("blade", "17_112");
					put("blade2", "17_112");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit", "17_118");
					// Four [uruk-hai] hunters of four blueprints, site 5, none with a text on being played.
					// For the hand, the only two cards there, which pay the discard of 2 without a choice:
					put("tracking", "15_171"); // Tracking Uruk: Hunter 1 and nothing else
					put("guard", "17_126");    // White Hand Guard: a skirmish ability
					// For the discard pile:
					put("butcher", "17_122");  // White Hand Butcher, cost 4: Hunter 6 and nothing else
					put("seeking", "15_169");  // Seeking Uruk: a text in fierce skirmishes only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VilePitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Vile Pit
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot an [uruk-hai] hunter.<br>At the start of each Shadow phase, you may discard 2 [uruk-hai] cards from hand to play an [uruk-hai] hunter from your discard pile.<br><b>Regroup:</b> Discard this possession to return an [uruk-hai] hunter from play to your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pit");

		assertEquals("Vile Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheHunterPlayedIsNotOneOfTheCardsDiscardedToPayForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var pit = scn.GetShadowCard("pit");
		var tracking = scn.GetShadowCard("tracking");
		var guard = scn.GetShadowCard("guard");
		var butcher = scn.GetShadowCard("butcher");
		var seeking = scn.GetShadowCard("seeking");

		scn.MoveCardsToSupportArea(pit);
		scn.MoveCardsToHand(tracking, guard);
		scn.MoveCardsToDiscard(butcher, seeking);

		scn.StartGame();
		assertInHand(tracking, guard);
		assertInDiscard(butcher, seeking);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(tracking, guard);
		assertInDiscard(butcher, seeking);
		// 20 set + 1 companion + 2 for Ettenmoors
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(tracking, guard);

		// The two hunters that lay in the discard pile before the cost was paid, and neither of the two that lie
		// there now because they paid it (CR 4.2 "effect", the They Are Coming example; Q-0320).
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher, seeking));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tracking, guard));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(butcher);

		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		// 4, and 2 for roaming (a site 5 minion at site 2)
		assertEquals(17, scn.GetTwilight());
		assertInDiscard(tracking, guard, seeking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiHunterIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pit = scn.GetShadowCard("pit");
		var butcher = scn.GetShadowCard("butcher");
		var decimator = scn.GetShadowCard("decimator");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(pit);
		assertInHand(pit);

		scn.StartGame();
		scn.MoveMinionsToTable(decimator, herdsman);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// An [uruk-hai] Uruk-hai who is no hunter, a [men] hunter and a [gondor] hunter.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(pit);
		assertFalse(scn.ShadowPlayAvailable(pit));

		scn.MoveMinionsToTable(butcher);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(pit));
		scn.ShadowPlayCard(pit);
		assertInZone(Zone.SUPPORT, pit);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TrackingUrukIsAnUrukHaiHunterToSpotToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToHand(pit);
		assertInHand(pit);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(pit));
		scn.ShadowPlayCard(pit);
		assertInZone(Zone.SUPPORT, pit);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfTheShadowPhaseDiscardsTwoUrukHaiCardsFromHandToPlayAnUrukHaiHunterFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var guard = scn.GetShadowCard("guard");
		var tracking = scn.GetShadowCard("tracking");
		var butcher = scn.GetShadowCard("butcher");
		var saruman = scn.GetShadowCard("saruman");
		var herdsman = scn.GetShadowCard("herdsman");
		// An [uruk-hai] hunter in the OTHER player's discard pile, of a title the Shadow player's piles do not hold.
		var freepsWarrior = scn.GetFreepsCard("warrior");
		scn.MoveCardsToSupportArea(pit);
		scn.MoveCardsToHand(decimator, blade, blade2, guard);
		scn.MoveCardsToDiscard(tracking, butcher, saruman, herdsman, freepsWarrior);
		assertInZone(Zone.SUPPORT, pit);
		assertInHand(decimator, blade, blade2, guard);
		assertInDiscard(tracking, butcher, saruman, herdsman, freepsWarrior);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowAcceptOptionalTrigger();
		// Two [uruk-hai] cards, a minion and a possession among them; not the [sauron] one.
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(decimator, blade, blade2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));
		scn.ShadowChooseCards(decimator, blade);
		assertInDiscard(decimator, blade);
		assertInHand(blade2, guard);
		assertEquals(2, scn.GetShadowHandCount());

		// The two [uruk-hai] hunters; not the [uruk-hai] Wizard, the [men] hunter or the other player's hunter.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, herdsman, freepsWarrior));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(butcher);
		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		assertInDiscard(tracking, saruman, herdsman, freepsWarrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesHandAndDiscardPileAsTheyWereAndTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var blade = scn.GetShadowCard("blade");
		var tracking = scn.GetShadowCard("tracking");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(pit);
		scn.MoveCardsToHand(decimator, blade);
		scn.MoveCardsToDiscard(tracking);
		assertInZone(Zone.SUPPORT, pit);
		assertInHand(decimator, blade);
		assertInDiscard(tracking);

		scn.StartGame();
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(guard);
		scn.SetTwilight(10);
		scn.FreepsRefreshDecision();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowDeclineOptionalTrigger();
		assertInHand(decimator, blade);
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(tracking);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The start of the maneuver phase is no start of a Shadow phase.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInHand(decimator, blade);
		assertInDiscard(tracking);
	}

	@Test
	public void WithOnlyOneUrukHaiCardInHandTheTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var guard = scn.GetShadowCard("guard");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(pit);
		// Two cards in hand, one of them [sauron].
		scn.MoveCardsToHand(decimator, guard);
		scn.MoveCardsToDiscard(tracking);
		assertInZone(Zone.SUPPORT, pit);
		assertInHand(decimator, guard);
		assertInDiscard(tracking);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pit));
		assertInHand(decimator, guard);
		assertInDiscard(tracking);
	}

	@Test
	public void WithNoUrukHaiHunterInTheDiscardPileTheTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var blade = scn.GetShadowCard("blade");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		var herdsman = scn.GetShadowCard("herdsman");
		var freepsWarrior = scn.GetFreepsCard("warrior");
		scn.MoveCardsToSupportArea(pit);
		// A hunter in hand that the cost could discard, and in the discard pile only an [uruk-hai] Wizard, a [men]
		// hunter and, in the other player's pile, his [uruk-hai] hunter.
		scn.MoveCardsToHand(decimator, blade, tracking);
		scn.MoveCardsToDiscard(saruman, herdsman, freepsWarrior);
		assertInZone(Zone.SUPPORT, pit);
		assertInHand(decimator, blade, tracking);
		assertInDiscard(saruman, herdsman, freepsWarrior);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The play from the discard pile is a cost (Q-0062): with nothing there to play, no offer.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pit));
		assertInHand(decimator, blade, tracking);
		assertInDiscard(saruman, herdsman, freepsWarrior);
	}

	@Test
	public void WithTooLittleTwilightForTheHunterTheTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var blade = scn.GetShadowCard("blade");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCardsToSupportArea(pit);
		scn.MoveCardsToHand(decimator, blade);
		scn.MoveCardsToDiscard(butcher);
		assertInZone(Zone.SUPPORT, pit);
		assertInHand(decimator, blade);
		assertInDiscard(butcher);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 companion + 2 for Ettenmoors; the Butcher costs 4 and 2 for roaming (site 5 at site 2).
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pit));
		assertInHand(decimator, blade);
		assertInDiscard(butcher);
	}

	@Test
	public void TheOfferIsMadeAgainAtTheSecondShadowPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var saruman = scn.GetShadowCard("saruman");
		var butcher = scn.GetShadowCard("butcher");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToSupportArea(pit);
		scn.MoveCardsToHand(decimator, blade, blade2, saruman);
		scn.MoveCardsToDiscard(butcher, tracking);
		assertInZone(Zone.SUPPORT, pit);
		assertInHand(decimator, blade, blade2, saruman);
		assertInDiscard(butcher, tracking);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(decimator, blade);
		scn.ShadowChooseCardBPFromSelection(butcher);
		assertInZone(Zone.SHADOW_CHARACTERS, butcher);
		assertInDiscard(tracking);
		assertEquals(2, scn.GetShadowHandCount());

		// Faramir is a hunter, so the Butcher's hunter 6 does not count against him: 7 beats 5, and the Butcher
		// lives with a wound.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, butcher);
		scn.FreepsResolveSkirmish(faramir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(0, scn.GetWoundsOn(faramir));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// The second Shadow phase of the turn, at site 3.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCards(blade2, saruman);
		assertInDiscard(blade2, saruman);
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsThePitToReturnAnUrukHaiHunterFromPlayToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pit = scn.GetShadowCard("pit");
		var tracking = scn.GetShadowCard("tracking");
		var butcher = scn.GetShadowCard("butcher");
		var decimator = scn.GetShadowCard("decimator");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToSupportArea(pit);
		assertInZone(Zone.SUPPORT, pit);

		scn.StartGame();
		// With no minion in play the game goes straight to the regroup phase; minions that lived through the
		// skirmishes stand here.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(tracking, butcher, decimator, herdsman);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetShadowHandCount());
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(pit));
		scn.ShadowUseCardAction(pit);
		assertInDiscard(pit);
		assertEquals(1, scn.ShadowGetChoiceMin());
		// The two [uruk-hai] hunters; not the [uruk-hai] Uruk-hai who is no hunter, the [men] hunter or the
		// [gondor] hunter.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(decimator, herdsman, faramir));
		scn.ShadowChooseCard(butcher);
		assertInHand(butcher);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRegroupActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(pit);
		assertInZone(Zone.SUPPORT, pit);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pit));
		assertInZone(Zone.SUPPORT, pit);
	}

	@Test
	public void WithNoUrukHaiHunterInPlayTheRegroupActionDiscardsThePitForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var pit = scn.GetShadowCard("pit");
		var decimator = scn.GetShadowCard("decimator");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToSupportArea(pit);
		assertInZone(Zone.SUPPORT, pit);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(decimator, herdsman);
		scn.FreepsRefreshDecision();
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(pit));
		scn.ShadowUseCardAction(pit);
		assertInDiscard(pit);
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, decimator);
		assertInZone(Zone.SHADOW_CHARACTERS, herdsman);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
