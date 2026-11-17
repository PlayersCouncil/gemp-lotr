package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ship", "18_27");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf to spot; a strength bonus of his own
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a strength bonus at 3 Ents
					put("leaf", "17_106");      // Halfling Leaf: a [shire] pipeweed condition, cost 1; to play, spot a Hobbit; its skirmish ability is never used
					put("toby", "1_305");       // Old Toby: a [shire] pipeweed possession, cost 1; "When you play this possession, you may draw a card"
					// Gandalf's Pipe: a [gandalf] pipe possession, cost 0, on Gandalf; no pipeweed.  Its fellowship
					// ability is never used.  In the draw deck, a Free Peoples possession.
					put("pipe", "1_74");

					// For the top of the draw deck, by their kinds alone.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Free Peoples companion
					put("defendit", "4_115");   // Defend It and Hope: a Free Peoples event
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion
					put("wreathed", "1_238");   // Wreathed in Shadow: a Shadow condition
					put("bred", "1_121");       // Bred for Battle: a Shadow event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSmallDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ship", "18_27");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf to spot; a strength bonus of his own
					put("leaf", "17_106");      // Halfling Leaf: a [shire] pipeweed condition, cost 1; to play, spot a Hobbit; its skirmish ability is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShipofSmokeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ship of Smoke
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Pipeweed</b>.<br>Each time you play a pipeweed card, you may spot Gandalf to reveal the top 3 cards of your draw deck. Take all Free Peoples cards revealed into hand and discard the remaining cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ship");

		assertEquals("Ship of Smoke", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachPipeweedPlayedBesideGandalfRevealsTheTopThreeAndTakesTheFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ship = scn.GetFreepsCard("ship");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leaf = scn.GetFreepsCard("leaf");
		var toby = scn.GetFreepsCard("toby");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		var troop = scn.GetFreepsCard("troop");
		var pipe = scn.GetFreepsCard("pipe");
		var wreathed = scn.GetFreepsCard("wreathed");
		var bred = scn.GetFreepsCard("bred");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(ship);
		scn.MoveCardsToHand(leaf, toby);
		assertInZone(Zone.SUPPORT, ship);

		scn.StartGame();
		// The last named is the top card.
		scn.MoveCardsToTopOfDeck(avenger, troop, defendit, aragorn);
		assertInZone(Zone.DECK, avenger, troop, defendit, aragorn);
		scn.FreepsRefreshDecision();
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertInHand(leaf, toby);

		// A [shire] pipeweed condition.
		scn.FreepsPlayCard(leaf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ship));
		scn.FreepsAcceptOptionalTrigger();

		// The three are shown to both players, the fourth is not.
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(defendit, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(avenger, false));
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(avenger, false));
		scn.DismissRevealedCards();

		// The companion and the event into hand; the minion discarded; the card below them left.
		assertInHand(aragorn, defendit);
		assertInDiscard(troop);
		assertSame(avenger, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second pipeweed, a possession, the same phase: asked again.
		scn.MoveCardsToTopOfDeck(bred, wreathed, pipe);
		assertInZone(Zone.DECK, bred, wreathed, pipe);
		scn.FreepsRefreshDecision();
		assertSame(pipe, scn.GetFreepsTopOfDeck());
		scn.FreepsPlayCard(toby);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ship));
		scn.FreepsChooseOptionalTrigger(ship);
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();
		// Old Toby's own draw is offered after it, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsDeclineOptionalTrigger();

		// The possession into hand; the Shadow condition and the Shadow event discarded.
		assertInHand(pipe);
		assertInDiscard(wreathed, bred);
		assertSame(avenger, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItsOwnPlayIsAPipeweedPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ship = scn.GetFreepsCard("ship");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defendit = scn.GetFreepsCard("defendit");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(ship);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(troop, defendit, aragorn);
		assertInZone(Zone.DECK, troop, defendit, aragorn);
		scn.FreepsRefreshDecision();
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertInHand(ship);

		// A [gandalf] pipeweed condition, cost 0, answering its own play.
		scn.FreepsPlayCard(ship);
		assertInZone(Zone.SUPPORT, ship);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ship));
		scn.FreepsAcceptOptionalTrigger();
		scn.DismissRevealedCards();

		assertInHand(aragorn, defendit);
		assertInDiscard(troop);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APipeThatIsNoPipeweedSetsNothingOffAndDecliningLeavesTheDeckAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ship = scn.GetFreepsCard("ship");
		var gandalf = scn.GetFreepsCard("gandalf");
		var pipe = scn.GetFreepsCard("pipe");
		var leaf = scn.GetFreepsCard("leaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(ship);
		scn.MoveCardsToHand(pipe, leaf);
		assertInZone(Zone.SUPPORT, ship);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(aragorn);
		assertInZone(Zone.DECK, aragorn);
		scn.FreepsRefreshDecision();
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();

		// Gandalf's Pipe goes on Gandalf: a pipe, no pipeweed.
		scn.FreepsPlayCard(pipe);
		assertInZone(Zone.ATTACHED, pipe);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ship));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A pipeweed: asked, and declined.
		scn.FreepsPlayCard(leaf);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ship));
		scn.FreepsDeclineOptionalTrigger();
		assertSame(aragorn, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ship = scn.GetFreepsCard("ship");
		var avenger = scn.GetFreepsCard("avenger");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToSupportArea(ship);
		scn.MoveCardsToHand(leaf);
		assertInZone(Zone.SUPPORT, ship);

		scn.StartGame();
		assertInHand(leaf);

		// Frodo and the Ent Avenger, a [gandalf] companion who is no Gandalf.
		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ship));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheDrawDeckEmptyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmallDeckScenario();

		var ship = scn.GetFreepsCard("ship");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(ship);
		scn.MoveCardsToHand(leaf);
		assertInZone(Zone.SUPPORT, ship);

		scn.StartGame();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInHand(leaf);

		// The spot pays nothing, and there is nothing to reveal: not offered (Ketura's ruling, Q-0365: A).
		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ship));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
