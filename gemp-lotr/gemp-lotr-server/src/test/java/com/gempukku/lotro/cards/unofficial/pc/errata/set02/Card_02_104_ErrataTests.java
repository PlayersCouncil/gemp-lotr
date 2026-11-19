package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_104_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "52_104");
					put("rosie", "1_309");      // Rosie Cotton: Hobbit ally, home site 1
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, home site 3
					put("gaffer", "1_291");     // The Gaffer: Hobbit ally, home site 1; put in the discard pile
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a Hobbit companion, not an ally
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an ally who is an Elf, not a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Merry, Horticulturalist
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Fellowship:</b> Exert Merry to take a Hobbit ally into hand from your draw deck (limit once
		 * per turn).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Horticulturalist", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void ExertsMerryToTakeAHobbitAllyFromTheDrawDeckIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rosie = scn.GetFreepsCard("rosie");
		var bilbo = scn.GetFreepsCard("bilbo");
		var gaffer = scn.GetFreepsCard("gaffer");
		var pippin = scn.GetFreepsCard("pippin");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToDiscard(gaffer);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		scn.FreepsDismissRevealedCards();

		// The Hobbit allies in the draw deck are offered; the Hobbit companion and the Elf ally in the deck are not,
		// nor the Hobbit ally in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rosie, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, orophin, gaffer));
		assertInZone(Zone.DECK, pippin, orophin);
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.MarkGameLog();
		scn.FreepsChooseCardBPFromSelection(rosie);
		assertInHand(rosie);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, bilbo, pippin, orophin);
		assertInDiscard(gaffer);
		// The deck was looked through, so it is shuffled; the other player's is not.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		// Not revealed: the game log alone names the ally taken.
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(rosie));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedAgainInTheTurnItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rosie = scn.GetFreepsCard("rosie");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		scn.FreepsUseCardAction(merry);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(rosie);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInHand(rosie);

		// Limit once per turn: a limit on uses, so the ability is not offered again, though Merry could still
		// exert and the draw deck still holds a Hobbit ally (Ketura on Q-0093).
		assertInZone(Zone.DECK, bilbo);
		assertFalse(scn.FreepsActionAvailable(merry));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rosie = scn.GetFreepsCard("rosie");
		scn.MoveCompanionsToTable(merry);
		// Vitality 4 with 3 wounds.
		scn.AddWoundsToChar(merry, 3);

		scn.StartGame();

		assertInZone(Zone.DECK, rosie);
		assertFalse(scn.FreepsActionAvailable(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MerryMayTakeNothingAndTheDeckIsStillShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rosie = scn.GetFreepsCard("rosie");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsUseCardAction(merry);
		scn.FreepsDismissRevealedCards();
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, rosie, bilbo);
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHobbitAllyInTheDrawDeckTheAbilityIsStillOfferedAndMerryStillExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rosie = scn.GetFreepsCard("rosie");
		var bilbo = scn.GetFreepsCard("bilbo");
		var gaffer = scn.GetFreepsCard("gaffer");
		var pippin = scn.GetFreepsCard("pippin");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(merry);
		// The only Hobbit allies on the Free Peoples side are in the discard pile and in hand, not in the draw deck.
		scn.MoveCardsToDiscard(gaffer);
		scn.MoveCardsToHand(rosie, bilbo);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DECK, pippin, orophin);

		// The cost can be paid, so the ability is offered though the deck holds nothing for its effect to take.
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		// The deck is looked through, so it is shown and then shuffled; nothing in it can be taken, so nothing is asked.
		scn.FreepsDismissRevealedCards();
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, pippin, orophin);
		assertInDiscard(gaffer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
