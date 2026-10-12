package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_045_ErrataTests
{
	// Isildur's Bane is worn on any wound, so an archery wound on Frodo is how he puts on The One Ring.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attention", "52_45");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, Damage +1 is its whole text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc; Shadow: exert it to add (1)
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, the archery wound on Frodo
					put("demands", "2_40");     // Demands of the Sackville-Bagginses: [isengard] condition; answers a [shire] ally exerting
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, Damage +1 is its whole text
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc; its trigger is on being played
					put("runner2", "1_178");
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition; the fellowship has no archery total to lower
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing
		);
	}

	@Test
	public void TooMuchAttentionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Too Much Attention
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If the Ring-bearer puts on The One Ring, spot an [isengard] minion to take a minion
		 * 	into hand from your draw deck.  The Free Peoples player may discard the top 5 cards of their draw deck to
		 * 	prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attention");

		assertEquals("Too Much Attention", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PuttingOnTheRingTakesAMinionFromTheDrawDeckWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		var forger = scn.GetShadowCard("forger");
		var warrior = scn.GetShadowCard("warrior");
		var runner1 = scn.GetShadowCard("runner1");
		var armory = scn.GetShadowCard("armory");
		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToHand(attention);
		scn.MoveCardsToDiscard(forger);
		// Exactly 5 cards left in the Free Peoples player's draw deck: enough to prevent.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("attention"), scn.GetFreepsCard("savage"),
				scn.GetFreepsCard("forger"), scn.GetFreepsCard("marksman"));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attention));
		scn.ShadowChooseOptionalTrigger(attention);
		scn.ShadowDismissRevealedCards();

		// Minions of two cultures and of two races are offered; conditions in the deck and a minion in the discard pile are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, runner1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertFalse(scn.ShadowHasCardChoiceAvailable(armory));
		assertFalse(scn.ShadowHasCardChoiceAvailable(demands));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(warrior);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.MarkGameLog();
		scn.FreepsChooseNo();

		assertInZone(Zone.HAND, warrior);
		assertInZone(Zone.DECK, runner1);
		assertInDiscard(attention);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		// The Free Peoples player's deck is as it was.
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsDiscardCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's deck was looked through, so it is reshuffled (Comprehensive Rules 4.2, "draw deck").
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		// The minion taken is named in the game log; one left in the deck is not.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(warrior));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner1));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnIsengardMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var marksman = scn.GetShadowCard("marksman");
		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToHand(attention);
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();
		// A [moria] minion and an [isengard] condition: neither is an [isengard] minion.
		scn.MoveMinionsToTable(marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attention));
		assertInZone(Zone.HAND, attention);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheRingBearerTakesTheWoundWithoutPuttingOnTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var attention = scn.GetShadowCard("attention");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(attention);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attention));
		assertInZone(Zone.HAND, attention);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnIsengardOrcIsEnoughToSpotAndAFreePeoplesDeckOfFourCannotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var forger = scn.GetShadowCard("forger");
		var marksman = scn.GetShadowCard("marksman");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToHand(attention);
		// One Goblin Runner in the deck, so the one picked by its blueprint is this one.
		scn.MoveCardsToDiscard(scn.GetShadowCard("runner2"));
		// 4 cards left in the Free Peoples player's draw deck: one short of the 5 that preventing costs.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("attention"), scn.GetFreepsCard("savage"),
				scn.GetFreepsCard("forger"), scn.GetFreepsCard("marksman"), scn.GetFreepsCard("demands"));

		scn.StartGame();
		scn.MoveMinionsToTable(forger, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attention));
		scn.ShadowChooseOptionalTrigger(attention);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(runner1);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.HAND, runner1);
		assertInDiscard(attention);
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDiscardsTheTopFiveCardsOfTheirDeckToPreventTheTake() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToHand(attention);
		// 6 cards left in the Free Peoples player's draw deck.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("attention"), scn.GetFreepsCard("savage"), scn.GetFreepsCard("forger"));
		var fpArmory = scn.GetFreepsCard("armory");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, marksman);
		scn.MoveCardsToBottomOfDeck(fpArmory);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();

		int twilight = scn.GetTwilight();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowChooseOptionalTrigger(attention);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(warrior);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// The top 5 are discarded and the bottom card stays.
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(8, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, fpArmory);
		assertInDiscard(scn.GetFreepsCard("marksman"), scn.GetFreepsCard("demands"), scn.GetFreepsCard("warrior"),
				scn.GetFreepsCard("runner1"), scn.GetFreepsCard("runner2"));
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// The minion is not taken, and the event and its twilight are spent all the same.
		assertInZone(Zone.DECK, warrior);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(attention);
		// The deck was looked through before the take was prevented.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayTakeNothingAndTheFreePeoplesPlayerIsThenNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(attention);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		scn.ShadowChooseOptionalTrigger(attention);
		scn.ShadowDismissRevealedCards();
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInDiscard(attention);
		// Looked through all the same.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
