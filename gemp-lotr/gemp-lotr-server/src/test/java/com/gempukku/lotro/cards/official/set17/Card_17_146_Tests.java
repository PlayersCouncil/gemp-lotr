package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Cards for the dead piles, laid there by the cheat: a companion and an ally of the player
					// whose fellowship moves, and a card of another title for the other player's pile (a choice
					// from a pile is read by blueprint).
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] Dwarf ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
				}},
				new HashMap<>()
				{{
					// The Fellowship-block path, with the Falls in place of site 9, which no test reaches: it is put
					// on the path as the next site (MoveCardToAdventurePath) and takes that site's number.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("falls", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FallsofRaurosStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Falls of Rauros
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>River</b>. When the Fellowship moves to this site in region 2, the Free Peoples player places a card from the dead pile out of play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("falls");

		assertEquals("Falls of Rauros", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void MovingToItAtSite4TheFreePeoplesPlayerPlacesACardFromHisDeadPileOutOfPlayAndMovingOnDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToDeadPile(legolas, grimir, shadowAragorn);
		assertInZone(Zone.DEAD, legolas, grimir, shadowAragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var falls = scn.GetShadowSite("falls");
		scn.MoveCardToAdventurePath(falls);
		scn.FreepsPass();

		// Site 4 is in region 2.  The Free Peoples player chooses one card of his own dead pile: the companion or
		// the ally, not the other player's card.
		assertSame(falls, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shadowAragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(legolas);
		assertInZone(Zone.REMOVED, legolas);
		assertInZone(Zone.DEAD, grimir, shadowAragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The move from the Falls on to site 5, still in region 2, places nothing more out of play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DEAD, grimir, shadowAragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToItAtSite3InRegion1PlacesNothingOutOfPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToDeadPile(legolas, grimir);
		assertInZone(Zone.DEAD, legolas, grimir);

		scn.StartGame();
		scn.SkipToSite(2);
		var falls = scn.GetShadowSite("falls");
		scn.MoveCardToAdventurePath(falls);
		scn.FreepsPass();

		assertSame(falls, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DEAD, legolas, grimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToItAtSite7InRegion3PlacesNothingOutOfPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToDeadPile(legolas, grimir);
		assertInZone(Zone.DEAD, legolas, grimir);

		scn.StartGame();
		scn.SkipToSite(6);
		var falls = scn.GetShadowSite("falls");
		scn.MoveCardToAdventurePath(falls);
		scn.FreepsPass();

		assertSame(falls, scn.GetCurrentSite());
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DEAD, legolas, grimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnHeIsTheFreePeoplesPlayerAndTheCardComesFromHisOwnDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();
		scn.SkipToSite(3);
		var falls = scn.GetShadowSite("falls");
		scn.MoveCardToAdventurePath(falls);
		scn.FreepsPass();

		// Player 1's own move to the Falls, with nothing in his dead pile: nothing is asked.
		assertSame(falls, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Player 2's companion and ally in his dead pile; player 1's Aragorn in his.
		var legolas = scn.GetShadowCard("legolas");
		var grimir = scn.GetShadowCard("grimir");
		var freepsAragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToDeadPile(legolas, grimir, freepsAragorn);
		assertInZone(Zone.DEAD, legolas, grimir, freepsAragorn);

		// Player 2's marker is on site 3 too: his move goes on to the Falls at site 4.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		assertSame(falls, scn.GetCurrentSite());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowHasCardChoiceAvailable(legolas, grimir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsAragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(grimir);
		assertInZone(Zone.REMOVED, grimir);
		assertInZone(Zone.DEAD, legolas, freepsAragorn);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
