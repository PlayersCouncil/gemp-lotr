package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_310_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "51_310");

					put("sword", "1_299");      // Hobbit Sword: [shire] possession, hand weapon, no other text
					put("sword2", "1_299");
					put("mithril", "2_105");    // Mithril-coat: [shire] artifact (armor), bearer must be Frodo
					put("appetite", "1_294");   // Hobbit Appetite: [shire] event, not an item
					put("bow", "1_41");         // Elven Bow: [elven] possession, ranged weapon
					put("guard", "1_7");        // Dwarf Guard: companion, strength 4, vitality 2, no text in play

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: strength 3
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "51_310");
					put("frodo", "1_290");      // Frodo, Son of Drogo, here only a companion: strength 3
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sam, Faithful Companion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 5
		 * Signet: Frodo
		 * Game Text: Fellowship: Exert Sam to take a [shire] item into hand from your draw deck (limit once per turn).
		 * Response: If Frodo dies, make Sam the Ring-bearer (resistance 5).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sam");

		assertEquals("Sam", card.getBlueprint().getTitle());
		assertEquals("Faithful Companion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		// Every Sam is Ring-bound (CR 4.2 "Ring-bound").
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityExertsSamToTakeAShireItemFromTheDrawDeckIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var mithril = scn.GetFreepsCard("mithril");
		var appetite = scn.GetFreepsCard("appetite");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(sword2);

		scn.StartGame();

		assertInZone(Zone.DECK, sword, mithril, appetite, bow);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.FreepsDismissRevealedCards();

		// A [shire] possession and a [shire] artifact; not a [shire] event, nor an [elven] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, mithril));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(appetite, bow));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(mithril);

		assertInZone(Zone.HAND, mithril);
		assertInZone(Zone.DECK, sword, appetite, bow);
		assertInZone(Zone.DISCARD, sword2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// The deck has been looked through, so it is shuffled; the other player's is not.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// Taken by its type and not revealed: the game log names it, and not a card left in the deck.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(mithril));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(appetite));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TakingNothingStillExertsSamAndShufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.DECK, sword);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedAgainInTheTurnItWasUsedButIsInSamsNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		scn.MoveCompanionsToTable(sam);
		// One Hobbit Sword in the deck: a choice from the deck goes by blueprint, so with both copies there the
		// game may hand over the other one.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("sword2"));

		scn.StartGame();

		scn.FreepsUseCardAction(sam);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(sword);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.HAND, sword);
		assertEquals(1, scn.GetFreepsHandCount());
		// Limit once per turn: a limit on uses, so the ability is not offered again, though Sam could still
		// exert and the draw deck still holds a [shire] item (Ketura on Q-0093).
		assertInZone(Zone.DECK, mithril);
		assertFalse(scn.FreepsActionAvailable(sam));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWhileSamIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(sam);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(sam, 3);

		scn.StartGame();

		assertInZone(Zone.DECK, sword);
		assertFalse(scn.FreepsActionAvailable(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenFrodoIsKilledSamCanBecomeTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertAttachedTo(ring, frodo);
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(5, scn.GetResistance(sam));

		// 3 + 1 for the Ring against 13: Frodo is overwhelmed, which kills him.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsAcceptOptionalTrigger();

		assertSame(sam, scn.GetRingBearer());
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.GameIsFinished());
		assertAttachedTo(ring, sam);
		// 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(0, scn.GetBurdens());
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheResponseWhenFrodoIsKilledLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);
		assertFalse(scn.GameIsFinished());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		assertFalse(scn.GameIsFinished());
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.DEAD, frodo);
		assertNotSame(sam, scn.GetRingBearer());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ResponseIsNotOfferedWhenAnotherCompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, guard);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, troop);

		// 4 against 13: the Dwarf Guard is overwhelmed and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(frodo, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenARingBearerWhoIsNotFrodoIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(galadriel, troop);
		assertFalse(scn.GameIsFinished());

		// 3 + 1 for the Ring against 13: the Ring-bearer is overwhelmed and killed, but she is not Frodo.
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DEAD, galadriel);
		assertNotSame(sam, scn.GetRingBearer());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ResponseIsNotOfferedWhenAFrodoWhoIsNotTheRingBearerIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetFreepsCard("frodo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam, frodo);

		scn.StartGame();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, troop);

		// 3 against 13: this Frodo is overwhelmed and killed, but he was not the Ring-bearer.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertSame(galadriel, scn.GetRingBearer());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
