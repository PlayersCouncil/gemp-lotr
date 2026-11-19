package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_045_Tests
{
	// Isildur's Bane is worn on any wound, so an archery wound on Frodo is how he puts on The One Ring.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attention", "2_45");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, Damage +1 is its whole text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc; Shadow: exert it to add (1)
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, the archery wound on Frodo
					put("demands", "2_40");     // Demands of the Sackville-Bagginses: [isengard] condition; answers a [shire] ally exerting

					// The Free Peoples player's draw deck is laid from his own copies of these
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc; its trigger is on being played
					put("runner2", "1_178");
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
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If the Ring-bearer puts on The One Ring, spot an [isengard] minion to make the Free Peoples player discard the top 5 cards of his or her draw deck.
		*/

		var scn = GetScenario();

		var attention = scn.GetFreepsCard("attention");

		assertEquals("Too Much Attention", attention.getBlueprint().getTitle());
		assertNull(attention.getBlueprint().getSubtitle());
		assertFalse(attention.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, attention.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, attention.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, attention.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(attention, Timeword.RESPONSE));
		assertEquals(1, attention.getBlueprint().getTwilightCost());
	}

	@Test
	public void PuttingOnTheRingMakesTheFreePeoplesPlayerDiscardTheTopFiveCardsOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var savage = scn.GetShadowCard("savage");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(attention);
		assertInHand(attention);

		var deck1 = scn.GetFreepsCard("attention");
		var deck2 = scn.GetFreepsCard("savage");
		var deck3 = scn.GetFreepsCard("forger");
		var deck4 = scn.GetFreepsCard("runner1");
		var deck5 = scn.GetFreepsCard("runner2");
		var deck6 = scn.GetFreepsCard("demands");

		scn.StartGame();
		// Six cards, the first named on top: the sixth is the one a discard of five leaves.
		scn.SetFreepsDrawDeck(deck1, deck2, deck3, deck4, deck5, deck6);
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(deck1, scn.GetFreepsTopOfDeck());
		assertEquals(deck6, scn.GetFreepsBottomOfDeck());
		scn.MoveMinionsToTable(savage, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(6, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attention));

		scn.ShadowChooseOptionalTrigger(attention);

		// The top five go to the Free Peoples player's discard pile, and the sixth stays.
		assertInDiscard(deck1, deck2, deck3, deck4, deck5);
		assertInZone(Zone.DECK, deck6);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(freepsDiscard + 5, scn.GetFreepsDiscardCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// The Shadow player's own deck is untouched.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInDiscard(attention);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnIsengardOrcIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var forger = scn.GetShadowCard("forger");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(attention);
		assertInHand(attention);

		var deck1 = scn.GetFreepsCard("attention");
		var deck2 = scn.GetFreepsCard("savage");
		var deck3 = scn.GetFreepsCard("forger");
		var deck4 = scn.GetFreepsCard("runner1");
		var deck5 = scn.GetFreepsCard("runner2");
		var deck6 = scn.GetFreepsCard("demands");

		scn.StartGame();
		scn.SetFreepsDrawDeck(deck1, deck2, deck3, deck4, deck5, deck6);
		assertEquals(6, scn.GetFreepsDeckCount());
		// The Forger, an Orc, is the only [isengard] minion.
		scn.MoveMinionsToTable(forger, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(6, scn.GetFreepsDeckCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attention));

		scn.ShadowChooseOptionalTrigger(attention);

		assertInDiscard(deck1, deck2, deck3, deck4, deck5);
		assertInZone(Zone.DECK, deck6);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInDiscard(attention);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAnIsengardMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attention = scn.GetShadowCard("attention");
		var marksman = scn.GetShadowCard("marksman");
		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToHand(attention);
		assertInHand(attention);
		scn.MoveCardsToSupportArea(demands);
		assertInZone(Zone.SUPPORT, demands);

		scn.StartGame();
		// A [moria] minion and an [isengard] condition: neither is an [isengard] minion.
		scn.MoveMinionsToTable(marksman);
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attention));
		assertInHand(attention);
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
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
		assertInHand(attention);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, marksman);
		int freepsDeck = scn.GetFreepsDeckCount();

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attention));
		assertInHand(attention);
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
