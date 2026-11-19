package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("living", "15_143");
					// Daddy Twofoot, Next-door Neighbor and The Gaffer, Master Gardener: [shire] followers of two
					// titles; their texts answer a transfer (Aid), which no test makes.
					put("daddy", "13_144");
					put("gaffer", "13_151");
					// Rohan Worker: a [rohan] follower; its texts answer a transfer and a skirmish won.
					put("worker", "15_135");
					// Bounder: a [shire] Hobbit ally (home 2F); a skirmish ability used only on purpose.
					put("bounder", "1_286");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit (to play, add a burden: cheated
					// in); an assignment ability only.
					put("smeagol", "103_116");
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated
					// in); a skirmish ability used only on purpose.
					put("pippin", "103_31");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("living", "15_143");
					put("daddy", "13_144");
					put("gaffer", "13_151");
					put("worker", "15_135");
					put("bounder", "1_286");
					put("smeagol", "103_116");
					put("pippin", "103_31");
				}},
				VirtualTableScenario.FellowshipSites,
				// Gimli, Bearer of Grudges: a Dwarf Ring-bearer, so that no Hobbit is in play unless one is laid.
				// His text acts at the start of a skirmish only.
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CommunityLivingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Community Living
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot a Hobbit to play a [shire] follower from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("living");

		assertEquals("Community Living", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAShireFollowerOfEitherTitleFromTheDrawDeckAndNoOtherCardAndReshufflesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var living = scn.GetFreepsCard("living");
		var daddy = scn.GetFreepsCard("daddy");
		var gaffer = scn.GetFreepsCard("gaffer");
		var worker = scn.GetFreepsCard("worker");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(living);

		scn.StartGame();

		assertInZone(Zone.DECK, daddy, gaffer, worker, bounder, smeagol);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		// Frodo, the Ring-bearer, is the Hobbit spotted.
		assertTrue(scn.FreepsPlayAvailable(living));

		scn.FreepsPlayCard(living);
		scn.FreepsDismissRevealedCards();
		// Not a [rohan] follower, nor a [shire] ally (a Hobbit) or a [shire] companion who are no followers.
		assertTrue(scn.FreepsHasCardChoiceAvailable(daddy, gaffer));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(worker, bounder, smeagol));
		// A play from a draw deck is 0 to 1 (Q-0019).
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(gaffer);

		assertInZone(Zone.SUPPORT, gaffer);
		assertInZone(Zone.DECK, daddy, worker, bounder, smeagol);
		assertInDiscard(living);
		// As the follower left the deck looked through, and the definition's own shuffle after the play (F-0313).
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAHobbitUntilAHobbitAllyIsPlayedAndThenMayPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var living = scn.GetFreepsCard("living");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var daddy = scn.GetFreepsCard("daddy");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCardsToHand(living, bounder);
		scn.MoveCompanionsToTable(smeagol);
		// A [shire] follower in play: no race, so no Hobbit (CR 4.2 "follower").
		scn.MoveCardsToSupportArea(daddy);

		scn.StartGame();

		// Near misses: a [shire] companion who is no Hobbit, and a [shire] follower.
		assertFalse(scn.IsRace(smeagol, Race.HOBBIT));
		assertInZone(Zone.SUPPORT, daddy);
		assertTrue(scn.FreepsPlayAvailable(bounder));
		assertFalse(scn.FreepsPlayAvailable(living));

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		// A Hobbit who is an ally will do.
		assertTrue(scn.FreepsPlayAvailable(living));

		assertInZone(Zone.DECK, gaffer);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(living);
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaffer));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, gaffer);
		assertInDiscard(living);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitOfAnotherCultureWillDoAndWithNoShireFollowerInTheDeckTheEventIsStillPlayedAndTheDeckReshuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var living = scn.GetFreepsCard("living");
		var pippin = scn.GetFreepsCard("pippin");
		var daddy = scn.GetFreepsCard("daddy");
		var gaffer = scn.GetFreepsCard("gaffer");
		var worker = scn.GetFreepsCard("worker");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToHand(living);
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToDiscard(daddy, gaffer);

		scn.StartGame();

		assertEquals(Culture.GONDOR, pippin.getBlueprint().getCulture());
		assertInZone(Zone.DECK, worker, bounder);
		assertInDiscard(daddy, gaffer);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsPlayAvailable(living));

		scn.FreepsPlayCard(living);
		// The deck is still looked through, and nothing in it may be chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(bounder, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, worker, bounder);
		assertInDiscard(daddy, gaffer);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(living);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
