package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foul", "1_44");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3
					put("lorien", "1_53");      // Lorien Elf: Elf companion, cost 1, vitality 2; to play, spot an Elf
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc minion
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] condition
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion
					put("feral", "11_183");     // Feral Uruk: an [uruk-hai] Uruk-hai minion
					// No Business of Ours: "The Free Peoples player may not look at or reveal cards in any Shadow
					// player's hand."
					put("business", "2_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foul", "1_44");
					put("legolas", "1_50");     // Legolas, Greenleaf: the Elf, vitality 3

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc minion
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] condition
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoulCreationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Foul Creation
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert an Elf to reveal an opponent's hand. You may discard an [isengard] minion revealed to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foul");

		assertEquals("Foul Creation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MayDiscardAnIsengardMinionRevealedToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var foul = scn.GetFreepsCard("foul");
		var legolas = scn.GetFreepsCard("legolas");
		// The Shadow player's hand: two [isengard] minions, an [isengard] card that is no minion, and a minion
		// of another culture.  The Free Peoples player's own copies of the four are his draw deck.
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var ambition = scn.GetShadowCard("ambition");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(foul);
		scn.MoveCardsToHand(savage, worker, ambition, runner);

		scn.StartGame();

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertInZone(Zone.HAND, savage, worker, ambition, runner);
		assertTrue(scn.FreepsPlayAvailable(foul));

		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// "You may" is asked as a yes or no; then one of the two [isengard] minions and nothing else.
		assertTrue(scn.FreepsDecisionAvailable("to draw 2 cards"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ambition, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(worker);
		assertInZone(Zone.DISCARD, worker);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, savage, ambition, runner);
		// The event has left the hand and two cards have come into it.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsAnElfToRevealTheWholeOfTheOpponentsHandAndWithNoIsengardMinionInItNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		// An [isengard] minion in my own hand is not in "an opponent's hand".
		var ownSavage = scn.GetFreepsCard("savage");
		// The Shadow player's hand of five: an [isengard] card that is no minion, two [moria] Orcs, and an Uruk-hai
		// minion that is not [isengard], and a Free Peoples card.
		var ambition = scn.GetShadowCard("ambition");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var feral = scn.GetShadowCard("feral");
		var shadowArwen = scn.GetShadowCard("arwen");
		// His [isengard] minions stay in his draw deck.
		var worker = scn.GetShadowCard("worker");
		// An Elf companion, an Elf ally, and a companion who could exert and is no Elf.
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(foul, ownSavage);
		scn.MoveCardsToHand(ambition, runner, troop, feral, shadowArwen);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, ownSavage);
		assertTrue(scn.FreepsPlayAvailable(foul));

		scn.MarkGameLog();
		scn.FreepsPlayCard(foul);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The whole hand is shown to the player of the event; the owner of the hand is told by the game log.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(ambition, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(feral, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowArwen, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(worker, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(ownSavage, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(ambition, runner, troop, feral, shadowArwen));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(worker));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(ownSavage));

		// No [isengard] minion among the five: there is nothing to discard, and nothing is asked.
		scn.FreepsDismissRevealedCards();
		assertFalse(scn.FreepsDecisionAvailable("draw 2 cards"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.DISCARD, foul);
		assertInZone(Zone.HAND, ownSavage);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen, boromir);
		// Arwen, vitality 3, is exhausted; a healthy Man stands beside her.
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveCardsToHand(foul, lorien);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsPlayAvailable(foul));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(lorien);
		assertTrue(scn.FreepsPlayAvailable(foul));

		// The Lorien Elf is the only one who can exert.
		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		// A hand of one card
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));

		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDiscardMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		// An Uruk-hai is the only [isengard] minion in his hand.
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(foul);
		scn.MoveCardsToHand(savage, troop);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, savage, troop);

		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("draw 2 cards"));

		scn.FreepsChooseNo();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, savage, troop);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnIsengardOrcAloneAmongTheCardsRevealedBringsTheOfferAsAnUrukHaiDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		// An Orc is the only [isengard] minion in his hand, beside a [moria] Orc.
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(foul);
		scn.MoveCardsToHand(worker, troop);

		scn.StartGame();

		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, worker, troop);

		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("draw 2 cards"));

		// The Worker is the one card that can be discarded, so he goes unasked.
		scn.FreepsChooseYes();
		assertInZone(Zone.DISCARD, worker);
		assertInZone(Zone.HAND, troop);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AgainstAnEmptyHandItMayStillBePlayedAndOnlyItsCostIsPaid()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(foul);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(foul));

		scn.MarkGameLog();
		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(arwen));
		// An empty hand is shown to nobody: there is nothing to dismiss, and the game log says so.
		assertFalse(scn.FreepsDecisionAvailable("Hand of"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));

		assertFalse(scn.FreepsDecisionAvailable("draw 2 cards"));
		assertInZone(Zone.DISCARD, foul);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnIsengardMinionInAHandThatMayNotBeRevealedIsNotOfferedForTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		var business = scn.GetShadowCard("business");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(foul);
		scn.MoveCardsToSupportArea(business);
		scn.MoveCardsToHand(savage, worker);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.HAND, savage, worker);
		assertTrue(scn.FreepsPlayAvailable(foul));

		scn.FreepsPlayCard(foul);
		// The Elf is the cost and is paid; the reveal is barred.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		// Two [isengard] minions are in that hand, and neither was revealed.
		assertFalse(scn.FreepsDecisionAvailable("draw 2 cards"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, savage, worker);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, foul);
	}
}
