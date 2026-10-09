package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_087_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wizard", "51_87");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4
					put("barliman", "1_70");    // Barliman Butterbur, Prancing Pony Proprietor: [gandalf] Man ally, cost 0
					put("ottar", "51_80");      // Ottar, Man of Laketown: [gandalf] Man ally, cost 1; to play, spot Gandalf
					put("treebeard", "4_103");  // Treebeard, Earthborn: [gandalf] Ent ally, cost 4
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, not a Wizard and not an ally
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally
					put("saruman", "3_68");     // Saruman, Keeper of Isengard: [isengard] Wizard minion
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
		 * Game Text: Play a Wizard or [gandalf] ally from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wizard");

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
	public void PlaysAWizardFromYourDrawDeckPayingItsCostAndReshufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		var treebeard = scn.GetFreepsCard("treebeard");
		var avenger = scn.GetFreepsCard("avenger");
		var thrarin = scn.GetFreepsCard("thrarin");
		var saruman = scn.GetFreepsCard("saruman");
		scn.MoveCardsToHand(wizard);
		// A [gandalf] ally that is not in the draw deck.  The opponent's copy of Treebeard is in the opponent's
		// draw deck, so Treebeard stands for both "not your draw deck" cases.
		scn.MoveCardsToDiscard(treebeard);

		scn.StartGame();

		assertInZone(Zone.DECK, gandalf, barliman, ottar, avenger, thrarin, saruman);
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(wizard));

		scn.FreepsPlayCard(wizard);
		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();

		// A Wizard and a [gandalf] ally.  Not a [gandalf] companion who is not a Wizard, not an ally of another
		// culture, not a Wizard the Free Peoples player cannot play (a minion), not Ottar while Gandalf cannot be
		// spotted, and not the [gandalf] ally in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, thrarin, saruman, ottar, treebeard));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(gandalf);

		assertEquals(5, scn.GetTwilight()); // 1 for the event + 4 for Gandalf
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.DECK, barliman, ottar);
		assertInDiscard(wizard);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		// Looked through, so reshuffled: once, and only that deck.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlaysAGandalfAllyFromYourDrawDeckMeetingItsRequirementToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveCardsToHand(wizard);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		assertInZone(Zone.DECK, barliman, ottar);
		assertEquals(0, scn.GetTwilight());
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsPlayCard(wizard);
		scn.FreepsDismissRevealedCards();

		// With Gandalf to spot, Ottar can be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ottar, barliman));
		scn.FreepsChooseCardBPFromSelection(ottar);

		assertEquals(2, scn.GetTwilight()); // 1 for the event + 1 for Ottar
		assertInZone(Zone.SUPPORT, ottar);
		assertInZone(Zone.DECK, barliman);
		assertInDiscard(wizard);
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ThePlayerMayPlayNothingAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(wizard);

		scn.StartGame();

		assertInZone(Zone.DECK, barliman);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsPlayCard(wizard);
		scn.FreepsDismissRevealedCards();

		assertTrue(scn.FreepsHasCardChoiceAvailable(barliman));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetTwilight()); // the event only
		assertInZone(Zone.DECK, barliman);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(wizard);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithNothingInTheDrawDeckToPlayAndTheDeckIsStillReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar = scn.GetFreepsCard("ottar");
		var treebeard = scn.GetFreepsCard("treebeard");
		var avenger = scn.GetFreepsCard("avenger");
		var saruman = scn.GetFreepsCard("saruman");
		scn.MoveCardsToHand(wizard);
		// Left in the draw deck: Ottar (Gandalf cannot be spotted), Ent Avenger, Thrarin, Saruman and the troop.
		scn.MoveCardsToDiscard(gandalf, barliman, treebeard);

		scn.StartGame();

		assertInZone(Zone.DECK, ottar, avenger, saruman);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// "There is no penalty if you don't find (or choose not to play) a card you are looking for in your draw deck."
		assertTrue(scn.FreepsPlayAvailable(wizard));

		scn.FreepsPlayCard(wizard);
		// The deck is still looked through, and there is nothing in it to choose.
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetTwilight()); // the event only
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(wizard);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wizard = scn.GetFreepsCard("wizard");
		var barliman = scn.GetFreepsCard("barliman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(wizard);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		assertInZone(Zone.DECK, barliman);
		assertTrue(scn.FreepsPlayAvailable(wizard));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wizard));
	}
}
