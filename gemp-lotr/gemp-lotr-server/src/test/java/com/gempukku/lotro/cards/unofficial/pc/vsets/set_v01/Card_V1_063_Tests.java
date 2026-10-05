package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_063_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who is the first companion of every fellowship here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarf Guard: a companion who is not unique and has no text in play.  Each player has six.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] Dwarf like the Guards, but an ally
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");      // Anduin Confluence: discards every ally when the fellowship moves there
					put("site8", "101_63");
					put("site9", "1_360");      // Emyn Muil: a maneuver ability for the Shadow player
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NenHithoelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Nen Hithoel
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 8
		 * Shadow Number: 7
		 * Direction: Right
		 * Game Text: River. When the fellowship moves to Nen Hithoel, each Shadow player may draw a card for each
		 * 	companion over 4.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Nen Hithoel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void ShadowPlayerDrawsOneCardWhenAFellowshipOfFiveCompanionsAndAnAllyMovesToNenHithoel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// Frodo and four Guards: one companion over 4.  Grimir is no companion.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("grimir"));
		// Both players have cards to draw.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("guard5"), scn.GetFreepsCard("guard6"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsHand = scn.GetFreepsHandCount();
		int freepsDeck = scn.GetFreepsDeckCount();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsPass();
		var nen = scn.GetCurrentSite();
		assertEquals("Nen Hithoel", nen.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(shadowHand, scn.GetShadowHandCount());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerMayDeclineTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPass();
		var nen = scn.GetCurrentSite();
		assertEquals("Nen Hithoel", nen.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nen));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerDrawsThreeCardsForAFellowshipOfSeven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// Frodo and six Guards: three companions over 4.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"),
				scn.GetFreepsCard("guard4"), scn.GetFreepsCard("guard5"), scn.GetFreepsCard("guard6"));
		// Five cards in the Shadow player's deck, so that three is not simply all there is.
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"),
				scn.GetShadowCard("guard4"), scn.GetShadowCard("guard5"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(shadowDeck >= 5);

		scn.FreepsPass();
		var nen = scn.GetCurrentSite();
		assertEquals("Nen Hithoel", nen.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nen));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand + 3, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsDrawnForAFellowshipOfFourCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// Frodo and three Guards.  Grimir would be the fifth if allies counted.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("grimir"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsPass();
		var nen = scn.GetCurrentSite();
		assertEquals("Nen Hithoel", nen.getBlueprint().getTitle());
		// The game still asks, with nothing to draw; saying yes draws nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nen));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerIsStillAskedWithNoCardsInHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// Frodo and four Guards: one companion over 4.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		// The turns played on the way here have drawn the Shadow player's whole deck into his hand.
		assertEquals(0, scn.GetShadowDeckCount());
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPass();
		var nen = scn.GetCurrentSite();
		assertEquals("Nen Hithoel", nen.getBlueprint().getTitle());
		// He is asked all the same: it is his chance to notice that his deck has run out.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nen));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenAFellowshipOfFiveMovesOnFromNenHithoel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// Frodo arrives at Nen Hithoel alone and the Shadow player says no to a draw of nothing.
		scn.FreepsPass();
		assertEquals("Nen Hithoel", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.ShadowDeclineOptionalTrigger();

		// With no minion to fight, the turn comes to the choice of a second move.  Four Guards join Frodo first.
		scn.SkipToMovementDecision();
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnTheFirstPlayerIsTheShadowPlayerAndDraws() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// The first player's Frodo goes on to Nen Hithoel alone, and his turn ends there.
		scn.FreepsPass();
		var nen = scn.GetCurrentSite();
		assertEquals("Nen Hithoel", nen.getBlueprint().getTitle());
		scn.ShadowDeclineOptionalTrigger();
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship is still at site 7: his Frodo and four of his Guards.
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCompanionsToTable(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"),
				scn.GetShadowCard("guard3"), scn.GetShadowCard("guard4"));
		scn.MoveCardsToTopOfDeck(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard5"), scn.GetShadowCard("guard6"));
		int firstHand = scn.GetFreepsHandCount();
		int firstDeck = scn.GetFreepsDeckCount();
		int secondHand = scn.GetShadowHandCount();
		int secondDeck = scn.GetShadowDeckCount();

		scn.ShadowPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(nen));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(firstHand + 1, scn.GetFreepsHandCount());
		assertEquals(firstDeck - 1, scn.GetFreepsDeckCount());
		assertEquals(secondHand, scn.GetShadowHandCount());
		assertEquals(secondDeck, scn.GetShadowDeckCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
