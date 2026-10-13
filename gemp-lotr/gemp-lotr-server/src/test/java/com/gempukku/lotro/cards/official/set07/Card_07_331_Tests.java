package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_331_Tests
{
	// The King path with Isengard Ruined as site 1, where every test starts.
	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the Gandalf to spot; his text is about his strength
					put("guard", "1_7");        // Dwarf Guard: here only as the card on top of each player's draw deck
				}},
				new HashMap<>()
				{{
					put("site1", "7_331");      // Isengard Ruined
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path, with no Gandalf in play.
	protected VirtualTableScenario GetNoGandalfScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion (an Ent) who is no Gandalf; stronger only with 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of another culture; a maneuver ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: cost 4, played from hand; his text is about his strength
				}},
				new HashMap<>()
				{{
					put("site1", "7_331");      // Isengard Ruined
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardRuinedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Isengard Ruined
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1K
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Spot Gandalf to add (2). Each player may draw a card.
		*/

		var scn = GetSiteScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Isengard Ruined", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void WithoutGandalfItIsNotOfferedUntilGandalfIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoGandalfScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();
		var site = scn.GetCurrentSite();

		// A [gandalf] companion who is no Gandalf, and Aragorn: nothing to spot.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(site));
	}

	@Test
	public void SpotsGandalfToAddTwoTwilightAndEachPlayerInTurnMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		var freepsGuard = scn.GetFreepsCard("guard");
		var shadowGuard = scn.GetShadowCard("guard");

		scn.StartGame();
		// Each player's own Dwarf Guard is the top card of his own draw deck.
		scn.MoveCardsToTopOfDeck(freepsGuard, shadowGuard);
		var site = scn.GetCurrentSite();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(site));
		scn.FreepsUseCardAction(site);

		// The Free Peoples player is asked first, and for himself only.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseYes();
		assertInZone(Zone.HAND, freepsGuard);
		assertInZone(Zone.DECK, shadowGuard);

		// Then the Shadow player, who draws from his own deck.
		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseYes();
		assertInZone(Zone.HAND, shadowGuard);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second use, and both say no: the twilight is added all the same and nobody draws.
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		scn.FreepsUseCardAction(site);
		scn.FreepsChooseNo();
		scn.ShadowChooseNo();

		assertEquals(4, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// An optional draw is still offered to a player whose draw deck is empty (Ketura on Q-0029).
	@Test
	public void EachPlayerIsStillAskedWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		// Each deck holds the scenario's two cards and nothing else: with the Free Peoples player's Gandalf in play,
		// the three left go to the discard piles.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("guard"), scn.GetShadowCard("gandalf"), scn.GetShadowCard("guard"));
		var site = scn.GetCurrentSite();

		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(site));
		scn.FreepsUseCardAction(site);

		// The site is usable and the twilight is added though nobody can draw.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		scn.FreepsChooseYes();

		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseYes();

		// Nothing to draw: both hands are as empty as they were.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
