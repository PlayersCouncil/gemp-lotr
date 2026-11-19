package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_362_Tests
{
	// The Towers path, Orthanc Library its site 9.  Each test skips to site 8 and passes, so that the fellowship
	// moves to Orthanc Library; the Ring-bearer is the first companion of every fellowship here.
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
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");      // Ring of Isengard: a Shadow ability that plays a [dunland] minion (none here)
					put("site9", "4_362");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrthancLibraryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Orthanc Library
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: When the fellowship moves to Orthanc Library, each Shadow player may draw a card for each
		 * 	companion over 4.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Orthanc Library", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ShadowPlayerDrawsOneCardWhenAFellowshipOfFiveCompanionsAndAnAllyMovesToOrthancLibrary() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
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
		assertTrue(shadowDeck >= 3);
		assertTrue(freepsDeck >= 2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.FreepsPass();
		var library = scn.GetCurrentSite();
		assertEquals("Orthanc Library", library.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(library));
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
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(shadowDeck >= 3);

		scn.FreepsPass();
		var library = scn.GetCurrentSite();
		assertEquals("Orthanc Library", library.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(library));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerDrawsThreeCardsForAFellowshipOfSeven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
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
		var library = scn.GetCurrentSite();
		assertEquals("Orthanc Library", library.getBlueprint().getTitle());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(library));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand + 3, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 3, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NothingIsOfferedOrDrawnForAFellowshipOfFourCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		// Frodo and three Guards: no companion over 4, so there is nothing to draw and nothing to ask.
		// Grimir would be the fifth if allies counted.  One more Guard is the fellowship that draws a card.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"), scn.GetFreepsCard("guard3"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("grimir"));
		scn.MoveCardsToTopOfDeck(scn.GetShadowCard("guard1"), scn.GetShadowCard("guard2"), scn.GetShadowCard("guard3"));
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(shadowDeck >= 3);

		scn.FreepsPass();
		assertEquals("Orthanc Library", scn.GetCurrentSite().getBlueprint().getTitle());
		// The Shadow player is not asked about a draw of no cards: the game has gone straight on to his Shadow phase.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowPlayerIsStillAskedWithNoCardsInHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(8);
		// Frodo and four Guards: one companion over 4.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));
		// The turns played on the way here have drawn the Shadow player's whole deck into his hand.
		assertEquals(0, scn.GetShadowDeckCount());
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPass();
		var library = scn.GetCurrentSite();
		assertEquals("Orthanc Library", library.getBlueprint().getTitle());
		// He is asked all the same: it is his chance to notice that his deck has run out.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(library));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
