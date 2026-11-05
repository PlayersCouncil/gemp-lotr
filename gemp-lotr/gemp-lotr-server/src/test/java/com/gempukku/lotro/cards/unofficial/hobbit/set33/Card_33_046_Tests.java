package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_046_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The forests are The
	// Troll Hoard 30_50 (site 2, also underground) and Mirkwood 30_53 (site 5); Goblin Town 30_52 (site 4) is
	// underground and no forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hidden", "33_46");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, site 4; archer only at site 9, a Regroup ability not used
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, twilight 3, site 4; texts on a win and in regroup only
					// Watchful Orc: [gundabad] Orc, twilight 4, site 3; its "when you play" offer is declined
					put("watchful", "30_40");
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, twilight 5, site 5
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, twilight 1; its play trigger never fires (cheated in)
					put("jail", "32_39");       // Jail: [sauron] condition, support area; a Regroup ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, twilight 1, site 4; a Shadow ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HiddenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Hidden
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Spot a [sauron] card to play an Orc from your discard pile. Its twilight cost is -1 (or -3 at a forest).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hidden");

		assertEquals("Hidden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAForestSpottingASauronMinionPlaysAnOrcFromTheDiscardPileAtTwilightCostMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var veteran = scn.GetShadowCard("veteran");
		var watchful = scn.GetShadowCard("watchful");
		var pursuer = scn.GetShadowCard("pursuer");
		var sauron = scn.GetShadowCard("sauron");
		var freepsRunner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(hidden);

		scn.StartGame();
		scn.MoveCardsToDiscard(watchful, pursuer, sauron, freepsRunner);
		assertInHand(hidden);
		assertInDiscard(watchful, pursuer, sauron, freepsRunner);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("The Troll Hoard", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(hidden));
		scn.ShadowPlayCard(hidden);
		// Orcs of two cultures; not a [sauron] minion who is no Orc (payable at 5 + 2 roaming - 3), not the Free
		// Peoples player's Orc in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(watchful, pursuer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, freepsRunner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(watchful);
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, watchful);
		assertInDiscard(hidden, pursuer, sauron, freepsRunner);
		// 9 - (4 + 2 roaming - 3)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtMirkwoodSpottingASauronConditionTheOrcIsTwilightCostMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var jail = scn.GetShadowCard("jail");
		var runner = scn.GetShadowCard("runner");
		var watchful = scn.GetShadowCard("watchful");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(hidden);
		scn.MoveCardsToSupportArea(jail);
		scn.MoveCardsToDiscard(watchful);
		assertInHand(hidden);
		assertInZone(Zone.SUPPORT, jail);
		assertInDiscard(watchful);
		scn.FreepsPass();
		// The one [sauron] card in play is Jail; the minion is a [moria] Orc.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Mirkwood", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(hidden));
		scn.ShadowPlayCard(hidden);
		// Watchful Orc is the one Orc there, and is taken unasked.
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, watchful);
		assertInDiscard(hidden);
		// 3 - (4 - 3); Watchful Orc (site 3) is not roaming at site 5.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AwayFromAForestTheOrcIsTwilightCostMinus1AndThatDiscountCountsTowardPayingForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var veteran = scn.GetShadowCard("veteran");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(hidden);
		scn.MoveCardsToDiscard(pursuer);
		assertInHand(hidden);
		assertInDiscard(pursuer);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Goblin Town", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();
		// Orkish Pursuer costs 3, and is playable with 2 only by the -1.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(hidden));
		scn.ShadowPlayCard(hidden);

		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertInDiscard(hidden);
		// 2 - (3 - 1); Orkish Pursuer (site 4) is not roaming at site 4.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotPlayableWithoutASauronCardInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var runner = scn.GetShadowCard("runner");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToHand(hidden);

		scn.StartGame();
		scn.MoveCardsToDiscard(pursuer);
		assertInHand(hidden);
		assertInDiscard(pursuer);
		scn.FreepsPass();
		// A [moria] Orc in play; the [sauron] Orc is in the discard pile and Hidden itself in hand.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hidden));
	}

	@Test
	public void NotPlayableWithNoOrcInTheDiscardPileThatCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hidden = scn.GetShadowCard("hidden");
		var veteran = scn.GetShadowCard("veteran");
		var pursuer = scn.GetShadowCard("pursuer");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(hidden);
		scn.MoveCardsToDiscard(pursuer, bats);
		assertInHand(hidden);
		assertInDiscard(pursuer, bats);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals("Goblin Town", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsPass();
		// Orkish Pursuer would cost 3 - 1 = 2; Dark Bats (1) could be paid for, but is no Orc.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hidden));
	}
}
