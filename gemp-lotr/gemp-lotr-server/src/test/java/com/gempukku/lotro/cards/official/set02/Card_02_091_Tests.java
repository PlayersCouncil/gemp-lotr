package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_091_Tests
{
	// Isildur's Bane is worn on any wound, so an archery wound on Frodo, the only companion, is how he puts on The One Ring.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spies", "2_91");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("sentinel", "10_83");   // Cirith Ungol Sentinel: [sauron] Uruk-hai, no Orc; a cost discount only
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, the archery wound on Frodo
					put("fear", "1_278");       // Strength Born of Fear: [sauron] support-area condition; a skirmish ability for (3)

					// The Free Peoples hand is laid from his own copies of these
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc; its trigger is on being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.IsildursBaneRing
		);
	}

	@Test
	public void SouthernSpiesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Southern Spies
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If the Ring-bearer puts on The One Ring, spot a [sauron] minion to make the Free Peoples player discard his or her hand.
		*/

		var scn = GetScenario();

		var spies = scn.GetFreepsCard("spies");

		assertEquals("Southern Spies", spies.getBlueprint().getTitle());
		assertNull(spies.getBlueprint().getSubtitle());
		assertFalse(spies.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spies.getBlueprint().getSide());
		assertEquals(Culture.SAURON, spies.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, spies.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(spies, Timeword.RESPONSE));
		assertEquals(0, spies.getBlueprint().getTwilightCost());
	}

	@Test
	public void PuttingOnTheRingWithASauronOrcToSpotMakesTheFreePeoplesPlayerDiscardHisWholeHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");
		var shadowFear = scn.GetShadowCard("fear");
		var hand1 = scn.GetFreepsCard("runner1");
		var hand2 = scn.GetFreepsCard("runner2");
		var hand3 = scn.GetFreepsCard("fear");

		scn.StartGame();
		scn.SetFreepsHand(hand1, hand2, hand3);
		scn.SetShadowHand(spies, shadowFear);
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(hand1, hand2, hand3, spies, shadowFear);
		// The Orc Soldier is the only [sauron] minion.
		scn.MoveMinionsToTable(soldier, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		int freepsDiscard = scn.GetFreepsDiscardCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));

		scn.ShadowChooseOptionalTrigger(spies);

		// Every card of the Free Peoples hand is discarded.
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(hand1, hand2, hand3);
		assertEquals(freepsDiscard + 3, scn.GetFreepsDiscardCount());
		// The Shadow player's own hand keeps its other card.
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(shadowFear);
		assertInDiscard(spies);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ASauronMinionWhoIsNoOrcIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var sentinel = scn.GetShadowCard("sentinel");
		var marksman = scn.GetShadowCard("marksman");
		var hand1 = scn.GetFreepsCard("runner1");
		var hand2 = scn.GetFreepsCard("soldier");

		scn.StartGame();
		scn.SetFreepsHand(hand1, hand2);
		scn.SetShadowHand(spies);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(hand1, hand2, spies);
		// The Cirith Ungol Sentinel, an Uruk-hai, is the only [sauron] minion.
		scn.MoveMinionsToTable(sentinel, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));

		scn.ShadowChooseOptionalTrigger(spies);

		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(hand1, hand2);
		assertInDiscard(spies);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyAMoriaMinionAndASauronConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var marksman = scn.GetShadowCard("marksman");
		var fear = scn.GetShadowCard("fear");
		var hand1 = scn.GetFreepsCard("runner1");
		var hand2 = scn.GetFreepsCard("runner2");
		scn.MoveCardsToSupportArea(fear);
		assertInZone(Zone.SUPPORT, fear);

		scn.StartGame();
		scn.SetFreepsHand(hand1, hand2);
		scn.SetShadowHand(spies);
		assertInHand(hand1, hand2, spies);
		// A [moria] minion and a [sauron] condition: neither is a [sauron] minion.
		scn.MoveMinionsToTable(marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spies));
		assertInHand(spies);
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(hand1, hand2);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheRingBearerTakesTheWoundWithoutPuttingOnTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var spies = scn.GetShadowCard("spies");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");
		var hand1 = scn.GetFreepsCard("runner1");
		var hand2 = scn.GetFreepsCard("runner2");

		scn.StartGame();
		scn.SetFreepsHand(hand1, hand2);
		scn.SetShadowHand(spies);
		assertInHand(hand1, hand2, spies);
		scn.MoveMinionsToTable(soldier, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spies));
		assertInHand(spies);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAnEmptyFreePeoplesHandItIsStillPlayedAndDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var soldier = scn.GetShadowCard("soldier");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SetFreepsHand();
		scn.SetShadowHand(spies);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInHand(spies);
		scn.MoveMinionsToTable(soldier, marksman);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.RBWearingOneRing());

		int freepsDiscard = scn.GetFreepsDiscardCount();
		// A discard of a hand with no card in it is a discard of none (Q-0255), and an event is offered whatever
		// its effect will do.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowChooseOptionalTrigger(spies);

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());
		assertInDiscard(spies);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
