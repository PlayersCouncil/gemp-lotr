package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_362_Tests
{

	// Summit of Amon Hen is site 9; the rest of the path is that of FellowshipSites.  The turns played to reach
	// site 8 draw the decks down, so each test lays the draw decks it reads.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Dwarf Guard: a companion with no text in play, used only to fill the draw decks.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_362");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SummitofAmonHenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Summit of Amon Hen
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 9
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: When the fellowship moves to Summit of Amon Hen, each Shadow player may draw a card for each
		 * burden.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Summit of Amon Hen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowPlayerMayDrawOneCardForOneBurdenWhenTheFellowshipMovesToSummitOfAmonHen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowDrawDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"));
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		int shadowHand = scn.GetShadowHandCount();
		int freepsHand = scn.GetFreepsHandCount();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsPass();
		var summit = scn.GetCurrentSite();
		assertEquals("Summit of Amon Hen", summit.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(summit));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(shadowHand, scn.GetShadowHandCount());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
		// The Free Peoples player, with cards to draw, draws none.
		assertEquals(freepsHand, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerMayDeclineTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowDrawDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		scn.AddBurdens(2);
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPass();
		var summit = scn.GetCurrentSite();
		assertEquals("Summit of Amon Hen", summit.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(summit));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerDrawsThreeCardsForThreeBurdensAndNoneForTheBurdensOnHisOwnRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		// The second player's turn: two burdens on his own Ring-bearer.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(2);
		// The first player's turn again, at site 8: three burdens on his.
		scn.SkipToOtherPlayersTurn();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.AddBurdens(3);
		assertEquals(3, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());
		// Six cards in the Shadow player's deck, so that neither three nor five is all there is.
		scn.SetShadowDrawDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"),
				scn.GetShadowCard("guard4"), scn.GetShadowCard("guard5"), scn.GetShadowCard("guard6"));
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPass();
		var summit = scn.GetCurrentSite();
		assertEquals("Summit of Amon Hen", summit.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(summit));

		scn.ShadowAcceptOptionalTrigger();
		// One for each of the moving fellowship's three burdens: two more would count his own as well.
		assertEquals(shadowHand + 3, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedWithNoBurdenThoughTheShadowPlayersOwnRingBearerHasSome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(7);
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(2);
		scn.SkipToOtherPlayersTurn();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());
		scn.SetShadowDrawDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPass();
		assertEquals("Summit of Amon Hen", scn.GetCurrentSite().getBlueprint().getTitle());
		// Nothing to draw for, so nothing is asked: the game has gone straight on to his Shadow phase.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerIsStillAskedWithNoCardsInHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		scn.SetShadowDrawDeck();
		scn.AddBurdens(1);
		int shadowHand = scn.GetShadowHandCount();
		assertEquals(0, scn.GetShadowDeckCount());

		scn.FreepsPass();
		var summit = scn.GetCurrentSite();
		assertEquals("Summit of Amon Hen", summit.getBlueprint().getTitle());
		// He is asked all the same: it is his chance to notice that his deck has run out.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(summit));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnTheFirstPlayerIsTheShadowPlayerAndDraws() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		// The second player moves twice on his sixth turn, so that he reaches site 9 before the first player.
		scn.SkipToSite(6);
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn(); // the first player's seventh turn: 7 -> 8
		scn.SkipToOtherPlayersTurn(); // the second player's seventh turn, at site 8

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.AddBurdens(2);
		assertEquals(2, scn.ShadowGetBurdens());
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));
		scn.SetShadowDrawDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int firstHand = scn.GetFreepsHandCount();
		int secondHand = scn.GetShadowHandCount();

		scn.ShadowPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		var summit = scn.GetCurrentSite();
		assertEquals("Summit of Amon Hen", summit.getBlueprint().getTitle());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(summit));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(firstHand + 2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(secondHand, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
