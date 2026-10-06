package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_067_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, alone.  With no minion played, a turn goes from the Shadow phase
	// straight to the regroup phase, where the Shadow player reconciles to 8 cards before the Free Peoples
	// player decides whether to move again; each test then lays out both hands as it needs them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Twelve cards for each player to hold, discard and draw.  The first three are of both sides and
					// three types, so that a hand of them shows that any card will do for the discard.
					// Aragorn, Ranger of the North: a Free Peoples companion.  Never played here.
					put("card1", "1_89");
					// Dwarven Armor: a Free Peoples possession whose bearer must be a Dwarf, so it cannot be played here.
					put("card2", "1_8");
					// Goblin Patrol Troop: a Shadow minion with no text, as are the nine after it.
					put("card3", "1_177");
					put("card4", "1_177");
					put("card5", "1_177");
					put("card6", "1_177");
					put("card7", "1_177");
					put("card8", "1_177");
					put("card9", "1_177");
					put("card10", "1_177");
					put("card11", "1_177");
					put("card12", "1_177");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");      // Eastemnet Gullies: a fellowship ability that needs 2 unbound companions
					put("site2", "102_67");
					put("site3", "4_337");      // Barrows of Edoras: a sanctuary with a fellowship ability
					// Sites 4 to 9 only fill out the path: no test goes beyond site 3.
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
	public void EastemnetHillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Eastemnet Hills
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: Mountain. When the fellowship moves from here during the regroup phase, each player may discard
		 * 	2 cards to draw 2 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Eastemnet Hills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void MovingOnInTheRegroupPhaseLetsTheFreePeoplesPlayerDiscard2CardsToDraw2WhileTheShadowPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("card1");
		var gone1 = scn.GetFreepsCard("card2");
		var gone2 = scn.GetFreepsCard("card3");
		var drawn1 = scn.GetFreepsCard("card4");
		var drawn2 = scn.GetFreepsCard("card5");

		scn.StartGame();
		scn.FreepsPass();
		assertEquals("Eastemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToMovementDecision();
		// Three cards in hand, so that which two go is a choice: a companion, a possession and a minion.
		scn.MoveCardsToHand(kept, gone1, gone2);
		scn.MoveCardsToTopOfDeck(drawn1, drawn2);
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();
		// The Shadow player has reconciled to 8 cards and has more in his deck.
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertTrue(shadowDeck >= 2);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(kept, gone1, gone2));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gone1, gone2);

		assertInDiscard(gone1, gone2);
		assertInHand(kept, drawn1, drawn2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 2, scn.GetFreepsDeckCount());
		assertEquals(freepsDiscard + 2, scn.GetFreepsDiscardCount());

		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		scn.ShadowChooseNo();
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscard2CardsToDraw2WhileTheFreePeoplesPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetShadowCard("card1");
		var gone1 = scn.GetShadowCard("card2");
		var gone2 = scn.GetShadowCard("card3");
		var drawn1 = scn.GetShadowCard("card4");
		var drawn2 = scn.GetShadowCard("card5");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));
		// The Shadow player's hand is cut down from the 8 he reconciled to: three cards, the rest back on his deck.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("card6"), scn.GetShadowCard("card7"), scn.GetShadowCard("card8"),
				scn.GetShadowCard("card9"), scn.GetShadowCard("card10"), scn.GetShadowCard("card11"), scn.GetShadowCard("card12"));
		scn.MoveCardsToTopOfDeck(drawn1, drawn2);
		// His three are a companion, a possession and a minion: the cards he may discard are any cards.
		scn.MoveCardsToHand(kept, gone1, gone2);
		int freepsDeck = scn.GetFreepsDeckCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(9, scn.GetShadowDeckCount());

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		scn.FreepsChooseNo();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());

		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(kept, gone1, gone2));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(gone1, gone2);

		assertInDiscard(gone1, gone2);
		assertInHand(kept, drawn1, drawn2);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard + 2, scn.GetShadowDiscardCount());
		// The Free Peoples player's cards are no part of the Shadow player's exchange.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnBothPlayersAreAskedAsHisFellowshipMovesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// On this turn the second player moves the fellowship and the first player is the Shadow player.
		var moverGone1 = scn.GetShadowCard("card1");
		var moverGone2 = scn.GetShadowCard("card2");
		var moverKept = scn.GetShadowCard("card3");
		var otherGone1 = scn.GetFreepsCard("card1");
		var otherGone2 = scn.GetFreepsCard("card2");
		var otherKept = scn.GetFreepsCard("card3");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.ShadowPass();
		assertEquals("Eastemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		// Three cards in each hand, the rest of each player's cards on his deck.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("card4"), scn.GetShadowCard("card5"), scn.GetShadowCard("card6"),
				scn.GetShadowCard("card7"), scn.GetShadowCard("card8"), scn.GetShadowCard("card9"),
				scn.GetShadowCard("card10"), scn.GetShadowCard("card11"), scn.GetShadowCard("card12"));
		scn.MoveCardsToHand(moverGone1, moverGone2, moverKept);
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"),
				scn.GetFreepsCard("card7"), scn.GetFreepsCard("card8"), scn.GetFreepsCard("card9"),
				scn.GetFreepsCard("card10"), scn.GetFreepsCard("card11"), scn.GetFreepsCard("card12"));
		scn.MoveCardsToHand(otherGone1, otherGone2, otherKept);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(9, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(9, scn.GetFreepsDeckCount());

		scn.ShadowChooseToMove();
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		scn.ShadowChooseYes();
		scn.ShadowChooseCards(moverGone1, moverGone2);
		assertInDiscard(moverGone1, moverGone2);
		assertInHand(moverKept);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(7, scn.GetShadowDeckCount());

		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCards(otherGone1, otherGone2);
		assertInDiscard(otherGone1, otherGone2);
		assertInHand(otherKept);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(7, scn.GetFreepsDeckCount());

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void AShadowPlayerWithOneCardInHandIsNotAskedWhileAFreePeoplesPlayerWithTwoIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var only = scn.GetShadowCard("card1");
		var gone1 = scn.GetFreepsCard("card1");
		var gone2 = scn.GetFreepsCard("card2");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		// Exactly two cards for the Free Peoples player.
		scn.MoveCardsToHand(gone1, gone2);
		// One card for the Shadow player, and eleven in his deck that he could draw.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("card2"), scn.GetShadowCard("card3"), scn.GetShadowCard("card4"),
				scn.GetShadowCard("card5"), scn.GetShadowCard("card6"), scn.GetShadowCard("card7"), scn.GetShadowCard("card8"),
				scn.GetShadowCard("card9"), scn.GetShadowCard("card10"), scn.GetShadowCard("card11"), scn.GetShadowCard("card12"));
		scn.MoveCardsToHand(only);
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		scn.FreepsChooseYes();
		// His two cards are all he has, and go without a choice.
		assertInDiscard(gone1, gone2);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 2, scn.GetFreepsDeckCount());

		// The Shadow player cannot discard 2 cards and is asked nothing: the fellowship has moved on.
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		assertInHand(only);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesPlayerWithOneCardInHandIsNotAskedWhileAShadowPlayerWithTwoIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var only = scn.GetFreepsCard("card1");
		var gone1 = scn.GetShadowCard("card1");
		var gone2 = scn.GetShadowCard("card2");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		// One card for the Free Peoples player, and eleven in his deck that he could draw.
		scn.MoveCardsToHand(only);
		// Exactly two cards for the Shadow player.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("card3"), scn.GetShadowCard("card4"),
				scn.GetShadowCard("card5"), scn.GetShadowCard("card6"), scn.GetShadowCard("card7"), scn.GetShadowCard("card8"),
				scn.GetShadowCard("card9"), scn.GetShadowCard("card10"), scn.GetShadowCard("card11"), scn.GetShadowCard("card12"));
		scn.MoveCardsToHand(gone1, gone2);
		int freepsDiscard = scn.GetFreepsDiscardCount();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(10, scn.GetShadowDeckCount());

		scn.FreepsChooseToMove();
		// The Free Peoples player cannot discard 2 cards and is asked nothing.
		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		scn.ShadowChooseYes();
		assertInDiscard(gone1, gone2);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());

		assertInHand(only);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APlayerWithNoCardsInHisDrawDeckIsStillAskedAndDiscards2ToDrawNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var kept = scn.GetFreepsCard("card1");
		var gone1 = scn.GetFreepsCard("card2");
		var gone2 = scn.GetFreepsCard("card3");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(kept, gone1, gone2);
		// The rest of the Free Peoples player's deck is in his discard pile.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"),
				scn.GetFreepsCard("card7"), scn.GetFreepsCard("card8"), scn.GetFreepsCard("card9"),
				scn.GetFreepsCard("card10"), scn.GetFreepsCard("card11"), scn.GetFreepsCard("card12"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(9, scn.GetFreepsDiscardCount());

		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		scn.FreepsChooseYes();
		scn.FreepsChooseCards(gone1, gone2);
		assertInDiscard(gone1, gone2);
		assertInHand(kept);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(11, scn.GetFreepsDiscardCount());

		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		scn.ShadowChooseNo();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NobodyIsAskedWhenTheFellowshipStaysAtTheEndOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"));
		int freepsDiscard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.FreepsChooseToStay();
		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		// What he is asked next is his own reconciling, with his hand as it was.
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(freepsDiscard, scn.GetFreepsDiscardCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetCurrentSiteNumber());
	}

	@Test
	public void NobodyIsAskedWhenTheFellowshipMovesFromHereInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		// Both players have stopped at site 2, and the first player's second turn begins there.
		scn.SkipToSite(2);
		assertEquals("Eastemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		// The turns played on the way have filled both hands, and each player has cards left to draw.
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(freepsHand >= 2);
		assertTrue(freepsDeck >= 2);
		assertTrue(shadowHand >= 2);
		assertTrue(shadowDeck >= 2);

		scn.FreepsPass();
		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards to draw 2"));
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards to draw 2"));
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
