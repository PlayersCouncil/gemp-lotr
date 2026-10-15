package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_341_Tests
{

	// The Towers path with the Throne Room as site 3.  Site 2 is Eastfold (a Shadow ability that needs 3 [isengard]
	// trackers) and site 4 White Rocks (Shadow number +1 for each mounted companion).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Théoden, Son of Thengel: [rohan] Man companion, cost 2; a regroup ability that needs a [rohan] ally
					put("thengel", "4_292");
					// Théoden, Lord of the Mark: [rohan] Man companion, cost 2; a fellowship ability that plays a [rohan]
					// possession from hand (none is in hand)
					put("lord", "4_365");
					// Théoden, King of the Golden Hall: [rohan] Man companion, cost 2; a maneuver ability never used here
					put("king", "5_93");
					// Éomer, Third Marshal of Riddermark: a [rohan] Man companion of another title; a cost discount and a
					// skirmish strength bonus only
					put("eomer", "4_267");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6; no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site3", "4_341");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThroneRoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Throne Room
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3T
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Play Théoden from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Throne Room", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void FellowshipActionPlaysATheodenFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thengel = scn.GetFreepsCard("thengel");
		var lord = scn.GetFreepsCard("lord");
		var king = scn.GetFreepsCard("king");
		var eomer = scn.GetFreepsCard("eomer");

		scn.StartGame();
		scn.SkipToSite(3);
		// The turns played on the way drew the deck into hand, so the cards wanted are laid after them.
		scn.MoveCardsToTopOfDeck(thengel, lord, eomer);
		scn.MoveCardsToHand(king);
		assertInZone(Zone.DECK, thengel, lord, eomer);
		assertInHand(king);

		var throne = scn.GetCurrentSite();
		assertEquals("Throne Room", throne.getBlueprint().getTitle());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(throne));

		scn.FreepsUseCardAction(throne);
		// The deck is shown first; then the Théodens of two subtitles can be taken, and not a [rohan] companion of
		// another title.  The choice is 0 to 1 (Q-0019).
		assertTrue(scn.FreepsHasCardChoiceAvailable(thengel, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(thengel, lord));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eomer));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(lord);

		assertInZone(Zone.FREE_CHARACTERS, lord);
		assertInZone(Zone.DECK, thengel, eomer);
		// The Théoden in hand is not "from your draw deck".
		assertInHand(king);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play; the
		// other player's deck is not touched.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTheodenInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thengel = scn.GetFreepsCard("thengel");
		var lord = scn.GetFreepsCard("lord");
		var king = scn.GetFreepsCard("king");
		var eomer = scn.GetFreepsCard("eomer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToDiscard(thengel, lord);
		scn.MoveCardsToTopOfDeck(eomer);
		scn.MoveCardsToHand(king);
		assertInDiscard(thengel, lord);
		assertInZone(Zone.DECK, eomer);
		assertInHand(king);

		var throne = scn.GetCurrentSite();
		assertEquals("Throne Room", throne.getBlueprint().getTitle());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(throne));

		scn.FreepsUseCardAction(throne);
		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, false));
		scn.FreepsDismissRevealedCards();

		assertInDiscard(thengel, lord);
		assertInZone(Zone.DECK, eomer);
		assertInHand(king);
		// The card's own shuffle, and no other.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsAFellowshipActionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(lord);
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.DECK, lord);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);

		// The fellowship moves to the Throne Room.
		scn.FreepsPass();
		var throne = scn.GetCurrentSite();
		assertEquals("Throne Room", throne.getBlueprint().getTitle());
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(throne));
		assertInZone(Zone.DECK, lord);
	}

	@Test
	public void WhileATheodenIsInPlayTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lord = scn.GetFreepsCard("lord");
		var king = scn.GetFreepsCard("king");
		var eomer = scn.GetFreepsCard("eomer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(lord);
		assertInZone(Zone.DECK, lord);

		var throne = scn.GetCurrentSite();
		assertEquals("Throne Room", throne.getBlueprint().getTitle());
		assertTrue(scn.FreepsActionAvailable(throne));

		// With a Théoden in play no other can be played (CR 4.2 "unique"); the definition then withholds the look too.
		scn.MoveCompanionsToTable(king);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, king);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(throne));

		// A [rohan] companion of another title in his place: offered again.
		scn.MoveCardsToDiscard(king);
		scn.MoveCompanionsToTable(eomer);
		scn.FreepsRefreshDecision();
		assertInDiscard(king);
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.FreepsActionAvailable(throne));
		assertInZone(Zone.DECK, lord);
	}
}
