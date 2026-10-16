package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_11_147_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gamling", "11_147");
					put("scout", "5_90");         // Rohirrim Scout: [rohan] Man companion; a regroup ability used only on purpose

					put("brand", "2_32");         // Flaming Brand: [gondor] hand weapon, cost 0, "Bearer must be a Man"
					put("chain", "4_278");        // Heavy Chain: [rohan] armor, cost 1, "Bearer must be a Man"
					put("bow", "1_41");           // Elven Bow: a possession whose bearer must be an Elf
					put("brought", "19_24");      // Brought Down from Inside: a [rohan] condition that goes on a [rohan] Man

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GamlingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gamling, Defender of the Hornburg
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: At the start of the fellowship phase, you may add (1) to play a possession on Gamling from
		 * 	your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gamling");

		assertEquals("Gamling", card.getBlueprint().getTitle());
		assertEquals("Defender of the Hornburg", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAddsOneToPlayAPossessionOnGamlingFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var scout = scn.GetFreepsCard("scout");
		var brand = scn.GetFreepsCard("brand");
		var chain = scn.GetFreepsCard("chain");
		var bow = scn.GetFreepsCard("bow");
		var brought = scn.GetFreepsCard("brought");
		scn.MoveCompanionsToTable(gamling, scout);

		// The offer is pending as the first fellowship phase starts.
		scn.StartGame();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gamling));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, brand, chain, bow, brought);
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		// The deck is shown, and then the possessions Gamling can bear are offered: not a possession only an Elf
		// may bear, and not a condition that would go on him.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(brand, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bow, brought));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(brand);
		// The (1) Gamling adds; the Flaming Brand costs 0.
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.ATTACHED, brand);
		assertEquals(gamling, brand.getAttachedTo());
		assertEquals(0, scn.GetAttachedCards(scout).size());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, chain, bow, brought);
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedNothingIsAddedPlayedOrShuffledAndNoOfferComesAtTheStartOfTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var brand = scn.GetFreepsCard("brand");
		var chain = scn.GetFreepsCard("chain");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gamling);

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gamling));
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, brand, chain);
		assertEquals(0, scn.GetAttachedCards(gamling).size());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship moves, a minion is laid in the Shadow phase, and the maneuver phase starts: Gamling's
		// text answers the start of the fellowship phase only.
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.ShadowPass();
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, brand, chain);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoPossessionHeCanBearInTheDeckTheOfferIsStillMadeAndTheDeckIsShuffledOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var bow = scn.GetFreepsCard("bow");
		var brought = scn.GetFreepsCard("brought");
		scn.MoveCompanionsToTable(gamling);
		// The Elven Bow and Brought Down from Inside stay in the deck.
		scn.MoveCardsToFreepsDiscard("brand", "chain");

		scn.StartGame();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gamling));
		assertEquals(0, scn.GetTwilight());
		int deck = scn.GetFreepsDeckCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();
		// Shown the deck, with nothing in it to play on Gamling.
		scn.FreepsDismissRevealedCards();
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetAttachedCards(gamling).size());
		assertInZone(Zone.DECK, bow, brought);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
