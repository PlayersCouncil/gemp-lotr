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

public class Card_04_332_Tests
{

	// The Towers path, Fangorn Forest its site 2.  SkipToSite(2) rests in the fellowship phase of turn 2 there; the
	// skipped turn draws cards, so the deck is laid after it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Treebeard, Earthborn: [gandalf] Ent ally, home 2T/8T; a response and a fellowship ability used only on purpose
					put("earthborn", "4_103");
					// Treebeard, Guardian of the Forest: [gandalf] Ent companion; assignment and skirmish abilities used only on purpose
					put("guardian", "6_37");
					// Ent Avenger: a [gandalf] Ent companion who is no Treebeard; +2 strength only while 3 Ents can be spotted
					put("avenger", "6_27");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				new HashMap<>() {{
					put("site1", "4_325");
					put("site2", "4_332");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FangornForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Fangorn Forest
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Forest</b>. <b>Fellowship:</b> Play Treebeard from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Fangorn Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void FellowshipActionPlaysATreebeardCompanionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var earthborn = scn.GetFreepsCard("earthborn");
		var guardian = scn.GetFreepsCard("guardian");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(earthborn, guardian, avenger);

		scn.FreepsRefreshDecision();
		var forest = scn.GetCurrentSite();
		assertEquals("Fangorn Forest", forest.getBlueprint().getTitle());
		assertInZone(Zone.DECK, earthborn, guardian, avenger);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(forest));

		scn.FreepsUseCardAction(forest);
		// The deck is shown first, and then a Treebeard of either type can be taken, and the Ent of another title not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(earthborn, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, false));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(earthborn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger));
		scn.FreepsChooseCardBPFromSelection(guardian);

		assertInZone(Zone.FREE_CHARACTERS, guardian);
		assertInZone(Zone.DECK, earthborn, avenger);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play.
		assertEquals(freepsShuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionPlaysTheTreebeardAllyToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var earthborn = scn.GetFreepsCard("earthborn");
		var guardian = scn.GetFreepsCard("guardian");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(earthborn, guardian);

		scn.FreepsRefreshDecision();
		var forest = scn.GetCurrentSite();
		assertInZone(Zone.DECK, earthborn, guardian);
		assertTrue(scn.FreepsActionAvailable(forest));

		scn.FreepsUseCardAction(forest);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(earthborn);

		assertInZone(Zone.SUPPORT, earthborn);
		assertInZone(Zone.DECK, guardian);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTreebeardInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var earthborn = scn.GetFreepsCard("earthborn");
		var guardian = scn.GetFreepsCard("guardian");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToDiscard(earthborn, guardian);
		scn.MoveCardsToTopOfDeck(avenger);

		scn.FreepsRefreshDecision();
		var forest = scn.GetCurrentSite();
		assertInZone(Zone.DISCARD, earthborn, guardian);
		assertInZone(Zone.DECK, avenger);
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.FreepsActionAvailable(forest));

		scn.FreepsUseCardAction(forest);
		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(avenger, false));
		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DISCARD, earthborn, guardian);
		assertInZone(Zone.DECK, avenger);
		// The card's own shuffle, and no other.
		assertEquals(freepsShuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsAFellowshipActionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(guardian);
		assertInZone(Zone.DECK, guardian);
		// A minion in play, so that there is a maneuver phase at site 2 on the first turn.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		var forest = scn.GetCurrentSite();
		assertEquals("Fangorn Forest", forest.getBlueprint().getTitle());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forest));
		assertInZone(Zone.DECK, guardian);
	}

	@Test
	public void WhileATreebeardIsInPlayTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var earthborn = scn.GetFreepsCard("earthborn");
		var guardian = scn.GetFreepsCard("guardian");
		var avenger = scn.GetFreepsCard("avenger");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(guardian);

		scn.FreepsRefreshDecision();
		var forest = scn.GetCurrentSite();
		assertInZone(Zone.DECK, guardian);
		assertTrue(scn.FreepsActionAvailable(forest));

		// With a Treebeard in play no other can be played (CR 4.2 "unique"); the definition then withholds the look
		// too, as Ketura ruled on Q-0317.
		scn.MoveAlliesToTable(earthborn);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, earthborn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(forest));

		// An Ent of another title in his place: offered again.
		scn.MoveCardsToDiscard(earthborn);
		scn.MoveCompanionsToTable(avenger);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertTrue(scn.FreepsActionAvailable(forest));
		assertInZone(Zone.DECK, guardian);
	}
}
