package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("late", "1_87");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, cost 4; its text needs 3 Ents
					put("barliman", "1_70");    // Barliman Butterbur, Prancing Pony Proprietor: [gandalf] Man ally, cost 0
					put("ottar", "1_80");       // Ottar, Man of Laketown: [gandalf] Man ally, cost 1; to play, spot Gandalf
					put("treebeard", "4_103");  // Treebeard, Earthborn: [gandalf] Ent ally, cost 4
					put("pallando", "13_37");   // Pallando, Far-travelling One: [gandalf] follower, cost 2, and no character
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, cost 1
					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWizardIsNeverLateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: A Wizard Is Never Late
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Play a [gandalf] character from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("late");

		assertEquals("A Wizard Is Never Late", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAGandalfCompanionFromYourDrawDeckPayingItsCostAndReshufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		var treebeard = scn.GetFreepsCard("treebeard");
		var pallando = scn.GetFreepsCard("pallando");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCardsToHand(late);
		// A [gandalf] ally that is not in the draw deck.  The opponent's copy of Treebeard is in the opponent's
		// draw deck, so Treebeard stands for both "not your draw deck" cases.
		scn.MoveCardsToDiscard(treebeard);

		scn.StartGame();

		assertInZone(Zone.DECK, gandalf, avenger, barliman, ottar, pallando, thrarin);
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(late));

		scn.FreepsPlayCard(late);
		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();

		// A Wizard companion, a companion who is no Wizard, and an ally who is a Man.  Not a [gandalf] follower
		// (no character), not an ally of another culture, not Ottar while Gandalf cannot be spotted, and not the
		// [gandalf] ally in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pallando, thrarin, ottar, treebeard));
		// CR 4.2 "draw deck": the player may choose not to play a card found.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(gandalf);

		assertEquals(5, scn.GetTwilight()); // 1 for the event + 4 for Gandalf
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DECK, avenger, barliman, ottar, pallando, thrarin);
		assertInDiscard(late, treebeard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// Looked through, so reshuffled, and only that deck: once as Gandalf left it and once when the look was over.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysAGandalfAllyFromYourDrawDeckMeetingItsRequirementToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveCardsToHand(late);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		assertInZone(Zone.DECK, avenger, barliman, ottar);
		assertEquals(0, scn.GetTwilight());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(late);
		scn.FreepsDismissRevealedCards();

		// With Gandalf to spot, Ottar can be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ottar, barliman, avenger));
		scn.FreepsChooseCardBPFromSelection(ottar);

		assertEquals(2, scn.GetTwilight()); // 1 for the event + 1 for Ottar
		// An ally goes to the support area.
		assertInZone(Zone.SUPPORT, ottar);
		assertInZone(Zone.DECK, avenger, barliman);
		assertInDiscard(late);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThePlayerMayPlayNothingAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(late);

		scn.StartGame();

		assertInZone(Zone.DECK, gandalf, barliman);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(late);
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, barliman));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetTwilight()); // the event only
		assertInZone(Zone.DECK, gandalf, barliman);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(late);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithNoGandalfCharacterInTheDrawDeckToPlayAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		var treebeard = scn.GetFreepsCard("treebeard");
		var pallando = scn.GetFreepsCard("pallando");
		var thrarin = scn.GetFreepsCard("thrarin");
		// [gandalf] characters in hand and in the discard pile are not in the draw deck.
		scn.MoveCardsToHand(late, gandalf, barliman);
		scn.MoveCardsToDiscard(avenger, treebeard);

		scn.StartGame();

		// Left in the draw deck: Ottar (Gandalf cannot be spotted), the follower, the [dwarven] ally and the troop.
		assertInZone(Zone.DECK, ottar, pallando, thrarin);
		assertEquals(3, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertTrue(scn.FreepsPlayAvailable(late));

		scn.FreepsPlayCard(late);
		// The deck is still looked through, and there is nothing in it to choose.
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetTwilight()); // the event only
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, ottar, pallando, thrarin);
		assertInZone(Zone.HAND, gandalf, barliman);
		assertInDiscard(late, avenger, treebeard);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var barliman = scn.GetFreepsCard("barliman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(late);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DECK, barliman);
		assertTrue(scn.FreepsPlayAvailable(late));

		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, late);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(late));
	}
}
