package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_006_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (resistance 8) as the Ring-bearer. Turn 1
	// begins at Bag End (site 1), whose Fellowship ability plays a follower from hand only; the maneuver phase is at
	// The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("king", "32_6");

					// [dwarven] followers of two races
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] Dwarf follower; a skirmish-win trigger
					put("roac", "32_8");        // Roäc, Son of Carc: [dwarven] Bird follower; a maneuver ability while attached
					// Cards that may not be played by it
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession
					put("thrush", "31_42");     // Old Thrush, Messenger Bird: a [shire] follower

					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop", "1_177");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void KingUndertheMountainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: King Under the Mountain
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Add a doubt to play a [dwarven] follower from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("king");

		assertEquals("King Under the Mountain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsADoubtToPlayADwarvenFollowerOfEitherRaceFromTheDrawDeckAndReshufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var grimir = scn.GetFreepsCard("grimir");
		var axe = scn.GetFreepsCard("axe");
		var thrush = scn.GetFreepsCard("thrush");
		scn.MoveCardsToHand(king);

		scn.StartGame();

		assertInZone(Zone.DECK, bifur, roac, grimir, axe, thrush);
		assertEquals(0, scn.GetBurdens());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(king));

		scn.FreepsPlayCard(king);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsDismissRevealedCards();
		// Not a [dwarven] ally or possession, nor a follower of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, axe, thrush));
		// A play from a draw deck is 0 to 1 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(roac);

		assertInZone(Zone.SUPPORT, roac);
		assertInZone(Zone.DECK, bifur, grimir, axe, thrush);
		assertInDiscard(king);
		assertEquals(1, scn.GetBurdens());
		// As the follower left the deck looked through, and the shuffle after the play.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenFollowerInTheDeckTheEventIsNotPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var grimir = scn.GetFreepsCard("grimir");
		var thrush = scn.GetFreepsCard("thrush");
		scn.MoveCardsToHand(king);
		scn.MoveCardsToDiscard(bifur, roac);

		scn.StartGame();

		assertInDiscard(bifur, roac);
		assertInZone(Zone.DECK, grimir, thrush);
		assertEquals(0, scn.GetBurdens());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// The [dwarven] followers in the discard pile; in the deck, a [dwarven] card that is no follower and a
		// follower of another culture.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418; F-0802):
		// no doubt is paid for a look that finds nothing.
		assertFalse(scn.FreepsPlayAvailable(king));
		assertInZone(Zone.HAND, king);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var bifur = scn.GetFreepsCard("bifur");
		scn.MoveCardsToHand(king);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertInZone(Zone.DECK, bifur);
		assertInZone(Zone.HAND, king);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(king));
		assertInZone(Zone.HAND, king);
		assertEquals(0, scn.GetBurdens());
	}
}
