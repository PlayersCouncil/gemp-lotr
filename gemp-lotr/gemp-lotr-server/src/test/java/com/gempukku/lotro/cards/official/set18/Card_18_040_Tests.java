package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "18_40");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// What Boromir's own play may find in the draw deck: both players' decks hold every card named here
	protected VirtualTableScenario GetDrawDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "18_40");
					// Great Shield: [gondor] possession (shield), cost 1.  "Bearer must be a Man."  The minion
					// archery total is -1, and no test here has an archer.
					put("shield", "1_107");
					// Heavy Chain: [rohan] possession (armor), cost 1.  "Bearer must be a Man."  Bearer may not be
					// overwhelmed unless his strength is tripled, and nobody here skirmishes.
					put("chain", "4_278");
					// Rider's Spear: [rohan] possession (hand weapon), cost 1.  "Bearer must be a [rohan] Man."
					put("spear", "4_288");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Boromir, Proud and Noble Man
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: <b>Ranger</b>.<br>When you play Boromir (except in your starting fellowship), you may play a possession with a twilight cost of 1 or less on him from your draw deck. Reshuffle your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Proud and Noble Man", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void WhenPlayedAPossessionOfAnotherCultureThatHeMayBearIsPlayedOnHimFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawDeckScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var shield = scn.GetFreepsCard("shield");
		var chain = scn.GetFreepsCard("chain");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCardsToHand(boromir);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, shield, chain, spear);
		scn.FreepsPlayCard(boromir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsAcceptOptionalTrigger();

		// The whole draw deck is shown, and then the choice is put.
		scn.FreepsDismissRevealedCards();
		// "A possession with a twilight cost of 1 or less" names no culture: the Shield is a [gondor] one and the
		// Chain a [rohan] one, each of cost 1 and each for a Man.  The Spear costs 1 as well, and is for a [rohan]
		// Man, which Boromir is not: it could not be played "on him".
		assertTrue(scn.FreepsHasCardChoiceAvailable(shield, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(spear));
		scn.FreepsChooseCardBPFromSelection(chain);

		assertAttachedTo(chain, boromir);
		assertInZone(Zone.DECK, shield, spear);
		// 3 for Boromir + 1 for Heavy Chain
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
