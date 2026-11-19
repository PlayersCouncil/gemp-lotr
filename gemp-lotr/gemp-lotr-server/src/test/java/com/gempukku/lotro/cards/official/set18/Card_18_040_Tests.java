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
					put("boromir", "18_40");
					put("brand", "2_32");       // Flaming Brand: [gondor] possession (hand weapon), cost 0, "Bearer must be a Man"; acts only against a Nazgûl
					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor), cost 1, "Bearer must be a Man"; nobody here skirmishes
					put("citadel", "11_55");    // Armor of the Citadel: [gondor] possession (armor), cost 2, for a [gondor] Man
					put("hobbitsword", "1_299");// Hobbit Sword: [shire] possession, cost 1, "Bearer must be a Hobbit", no other text
					put("northranger", "18_55");// Ranger of the North: [gondor] CONDITION, cost 0, for a [gondor] Man; abilities used only on purpose
					put("ranger", "7_116");     // Ranger of Osgiliath: another [gondor] Man companion, cost 2; a regroup ability used only on purpose
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

		var card = scn.GetFreepsCard("boromir");

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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyAPossessionOfCostOneOrLessIsOfferedItGoesOnHimAndTheDeckIsShuffledTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var brand = scn.GetFreepsCard("brand");
		var chain = scn.GetFreepsCard("chain");
		var citadel = scn.GetFreepsCard("citadel");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var northranger = scn.GetFreepsCard("northranger");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(boromir);

		scn.StartGame();

		assertInZone(Zone.DECK, brand, chain, citadel, hobbitsword, northranger);
		assertEquals(0, scn.GetAttachedCards(ranger).size());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.FreepsPlayCard(boromir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		// Costs 0 and 1, [gondor] and [rohan]; not the Armor of the Citadel (cost 2), the Hobbit Sword (cost 1, but
		// not for him) or Ranger of the North (cost 0, for him, but a condition).
		assertTrue(scn.FreepsHasCardChoiceAvailable(brand, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(citadel, hobbitsword, northranger));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(brand);

		// On him, and not on the other Man who could bear it.
		assertAttachedTo(brand, boromir);
		assertEquals(0, scn.GetAttachedCards(ranger).size());
		assertInZone(Zone.DECK, chain, citadel, hobbitsword, northranger);
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the Brand left the deck looked through, and the reshuffle
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedNothingIsPlayedAndTheDeckIsNotShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var brand = scn.GetFreepsCard("brand");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCardsToHand(boromir);

		scn.StartGame();

		assertInZone(Zone.DECK, brand, chain);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(boromir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.GetAttachedCards(boromir).size());
		assertInZone(Zone.DECK, brand, chain);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoPossessionHeCanTakeInTheDeckTheOfferIsTakenForNothingAndTheDeckIsShuffledOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var brand = scn.GetFreepsCard("brand");
		var chain = scn.GetFreepsCard("chain");
		var citadel = scn.GetFreepsCard("citadel");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var northranger = scn.GetFreepsCard("northranger");
		scn.MoveCardsToHand(boromir);
		scn.MoveCardsToDiscard(brand, chain);

		scn.StartGame();

		assertInDiscard(brand, chain);
		assertInZone(Zone.DECK, citadel, hobbitsword, northranger);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsPlayCard(boromir);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));
		scn.FreepsAcceptOptionalTrigger();
		// The deck is shown with nothing in it that may be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(citadel, false));
		scn.FreepsDismissRevealedCards();

		assertEquals(0, scn.GetAttachedCards(boromir).size());
		assertInZone(Zone.DECK, citadel, hobbitsword, northranger);
		assertInDiscard(brand, chain);
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenHeIsPlayedInTheStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var brand = scn.GetFreepsCard("brand");
		var chain = scn.GetFreepsCard("chain");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertInZone(Zone.DECK, boromir, brand, chain);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir));
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.FreepsChooseCard(boromir);

		// His 3 leaves 1 of the 4 allowed, and no companion here costs 1 or less: the choice is over. Nothing is
		// offered him, and the other player is choosing now.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(0, scn.GetAttachedCards(boromir).size());
		assertInZone(Zone.DECK, brand, chain);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void AnotherCompanionPlayedWhileBoromirIsInPlayBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var ranger = scn.GetFreepsCard("ranger");
		var brand = scn.GetFreepsCard("brand");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(ranger);

		scn.StartGame();

		assertInZone(Zone.DECK, brand);
		assertTrue(scn.FreepsPlayAvailable(ranger));
		scn.FreepsPlayCard(ranger);

		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, brand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
