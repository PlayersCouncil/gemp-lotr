package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worker", "11_133");
					put("worker2", "11_133");
					put("worker3", "11_133");

					// Half-troll of Far Harad: an [orc] minion who is no Orc.  Its start-of-assignment trigger is
					// never reached.
					put("halftroll", "14_12");
					// Scurrying Goblin: an [orc] Orc, cost 1.  When played at an underground site beside another [orc]
					// minion, may add (2); played here away from one.
					put("goblin", "11_137");
					// The Beckoning Shadow: an [orc] condition, no minion.  Cheated into play (to play, exert an [orc]
					// minion); its other text needs 5 burdens.
					put("beckoning", "12_83");
					// Morgul Slayer: an Orc of the [sauron] culture; a regroup ability only
					put("slayer", "3_93");

					// Cards to lie on top of the Free Peoples player's draw deck.
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion, twilight cost 6, no text
					put("ax", "18_84");         // Orkish Ax: a Shadow possession, twilight cost 1
					put("hate", "3_53");        // Hate and Anger: a Shadow event, twilight cost 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion, twilight cost 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishWorkerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Orkish Worker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, you may spot another [orc] minion to reveal the top card of the Free Peoples player's deck. If it is a Shadow card, add (X), where X is its twilight cost.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("worker");

		assertEquals("Orkish Worker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RevealsTheFreePeoplesTopCardAndAddsTheTwilightCostOfEachKindOfShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetShadowCard("worker");
		var worker2 = scn.GetShadowCard("worker2");
		var worker3 = scn.GetShadowCard("worker3");
		var halftroll = scn.GetShadowCard("halftroll");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var troop = scn.GetFreepsCard("troop");
		var ax = scn.GetFreepsCard("ax");
		var hate = scn.GetFreepsCard("hate");
		scn.MoveCardsToHand(worker, worker2, worker3);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		// The Free Peoples player's deck: a Shadow minion costing 6 on top.  The Shadow player's own deck has a
		// card of another blueprint on top.
		scn.MoveCardsToTopOfDeck(troop);
		scn.MoveCardsToTopOfDeck(shadowAragorn);
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertSame(shadowAragorn, scn.GetShadowTopOfDeck());
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The only other [orc] minion is the Half-troll, who is no Orc.
		scn.ShadowPlayCard(worker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worker));
		int pool = scn.GetTwilight();
		scn.ShadowChooseOptionalTrigger(worker);

		// Both players are shown the Free Peoples player's top card, and no other card.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// A minion: + 6
		assertEquals(pool + 6, scn.GetTwilight());
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A possession of cost 1 put on top, and a second Worker played: + 1
		scn.MoveCardsToTopOfDeck(ax);
		assertSame(ax, scn.GetFreepsTopOfDeck());
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(worker2);
		pool = scn.GetTwilight();
		scn.ShadowChooseOptionalTrigger(worker2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(ax, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		assertEquals(pool + 1, scn.GetTwilight());
		assertSame(ax, scn.GetFreepsTopOfDeck());

		// An event of cost 2, and a third Worker: + 2
		scn.MoveCardsToTopOfDeck(hate);
		assertSame(hate, scn.GetFreepsTopOfDeck());
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(worker3);
		pool = scn.GetTwilight();
		scn.ShadowChooseOptionalTrigger(worker3);
		assertTrue(scn.ShadowHasCardChoiceAvailable(hate, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		assertEquals(pool + 2, scn.GetTwilight());
		assertSame(hate, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesCardRevealedAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetShadowCard("worker");
		var halftroll = scn.GetShadowCard("halftroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		// A Free Peoples card of twilight cost 4 on top of the Free Peoples player's deck.
		scn.MoveCardsToTopOfDeck(aragorn);
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(worker);
		int pool = scn.GetTwilight();
		scn.ShadowChooseOptionalTrigger(worker);

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		assertEquals(pool, scn.GetTwilight());
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedUntilAnotherOrcMinionIsSpottedAndItsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetShadowCard("worker");
		var worker2 = scn.GetShadowCard("worker2");
		var goblin = scn.GetShadowCard("goblin");
		var beckoning = scn.GetShadowCard("beckoning");
		var slayer = scn.GetShadowCard("slayer");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(worker, worker2, goblin);

		scn.StartGame();
		// An [orc] card that is no minion, and an Orc minion of another culture.
		scn.MoveCardsToSupportArea(beckoning);
		scn.MoveMinionsToTable(slayer);
		assertInZone(Zone.SUPPORT, beckoning);
		assertInZone(Zone.SHADOW_CHARACTERS, slayer);
		scn.MoveCardsToTopOfDeck(troop);
		assertSame(troop, scn.GetFreepsTopOfDeck());
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The Worker is an [orc] minion, but not "another" one.
		scn.ShadowPlayCard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [orc] minion played after the Worker does not set it off.
		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second Worker is offered the reveal, and declines: nothing is shown and nothing added.
		scn.ShadowPlayCard(worker2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worker2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(worker));
		int pool = scn.GetTwilight();
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(pool, scn.GetTwilight());
		assertSame(troop, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker = scn.GetShadowCard("worker");
		var worker2 = scn.GetShadowCard("worker2");
		var halftroll = scn.GetShadowCard("halftroll");
		var shadowTroop = scn.GetShadowCard("troop");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(worker, worker2);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		// The Shadow player's own deck is not the one the reveal names.
		scn.MoveCardsToTopOfDeck(shadowTroop);
		assertSame(shadowTroop, scn.GetShadowTopOfDeck());
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The spot pays nothing, and the Free Peoples deck has nothing to reveal: not offered (Ketura's ruling,
		// Q-0365: A).
		scn.ShadowPlayCard(worker);
		assertInZone(Zone.SHADOW_CHARACTERS, worker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// One card put in the Free Peoples deck: a second Worker is offered the reveal.
		scn.SetFreepsDrawDeck(troop);
		assertEquals(1, scn.GetFreepsDeckCount());
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(worker2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(worker2));
	}
}
