package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_044_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foul", "51_44");

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
		 * Game Text: Fellowship: Exert an Elf to reveal an opponent's hand. If an [isengard] minion was
		 * revealed, you may draw 2 cards.
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
	public void ExertsAnElfToRevealTheWholeOfTheOpponentsHandAndWithNoIsengardMinionInItNoDrawIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		// An [isengard] minion in my own hand is not in "an opponent's hand".
		var ownSavage = scn.GetFreepsCard("savage");
		// The Shadow player's hand of five: an [isengard] card that is no minion, two minions of another
		// culture, and two Free Peoples cards.
		var ambition = scn.GetShadowCard("ambition");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var shadowArwen = scn.GetShadowCard("arwen");
		var shadowLorien = scn.GetShadowCard("lorien");
		// His [isengard] minions stay in his draw deck.
		var worker = scn.GetShadowCard("worker");
		scn.MoveCompanionsToTable(arwen, aragorn, boromir);
		scn.MoveAlliesToTable(silinde);
		// An Elf of the [gondor] culture, and a Man of the [elven] culture who is still no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.ELF));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, boromir, null, Culture.ELVEN));
		scn.MoveCardsToHand(foul, ownSavage);
		scn.MoveCardsToHand(ambition, runner, troop, shadowArwen, shadowLorien);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(foul));

		scn.MarkGameLog();
		scn.FreepsPlayCard(foul);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, silinde, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(silinde);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());

		// The whole hand is shown to the player of the event; the owner of the hand is told by the game log.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(ambition, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowArwen, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowLorien, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(worker, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(ownSavage, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(ambition, runner, troop, shadowArwen, shadowLorien));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(worker));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(ownSavage));

		// No [isengard] minion among the five: nothing is offered.
		scn.FreepsDismissRevealedCards();
		assertFalse(scn.FreepsDecisionAvailable("draw 2 cards"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.DISCARD, foul);
		assertInZone(Zone.HAND, ownSavage);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetShadowHandCount());
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
		// Arwen, vitality 3, is exhausted; a healthy Man of the [elven] culture stands beside her.
		scn.AddWoundsToChar(arwen, 2);
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, boromir, null, Culture.ELVEN));
		scn.MoveCardsToHand(foul, lorien);
		scn.MoveCardsToHand(runner);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(foul));

		scn.FreepsPlayCard(lorien);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(foul));

		// The Lorien Elf is the only one who can exert.
		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		// 1 for the Lorien Elf + 1 for the event
		assertEquals(2, scn.GetTwilight());
		// A hand of one card
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));

		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnIsengardMinionRevealedTwoCardsMayBeDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foul = scn.GetFreepsCard("foul");
		var arwen = scn.GetFreepsCard("arwen");
		// An [isengard] minion that is no Uruk-hai, between two cards that are not [isengard] minions.
		var worker = scn.GetShadowCard("worker");
		var runner = scn.GetShadowCard("runner");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(foul);
		scn.MoveCardsToHand(runner, worker, ambition);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("draw 2 cards"));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.FreepsChooseYes();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		// The minion revealed stays where it was.
		assertInZone(Zone.HAND, worker);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDrawMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.FreepsPlayCard(foul);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetTwilight());

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsDecisionAvailable("draw 2 cards"));

		scn.FreepsChooseNo();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, savage);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, foul);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AgainstAnEmptyHandItMayStillBePlayedAndOnlyItsCostIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertEquals(1, scn.GetTwilight());
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
}
