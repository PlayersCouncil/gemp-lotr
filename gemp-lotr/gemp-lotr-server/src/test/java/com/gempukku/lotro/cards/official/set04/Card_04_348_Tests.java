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

public class Card_04_348_Tests
{

	// The Towers path, whose site 5 is Deeping Wall.  Each test skips to site 4, lays the piles there (the turns
	// skipped have drawn cards) and moves on, so that it acts in the Shadow phase at Deeping Wall.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Saruman, Black Traitor: [isengard] Wizard minion, cost 4, site 4; "When you play Saruman, you may
					// discard a condition" (none in play, so not offered)
					put("traitor", "4_173");
					// Saruman, Rabble-rouser: [dunland] Wizard minion, cost 4, site 4; his move trigger counts [dunland] Men
					put("rabble", "4_33");
					// Saruman, Servant of Sauron: [isengard] follower, cost 1; an Aid and a skirmish-win trigger only
					put("follower", "17_38");
					// Saruman, Keeper of Isengard: [isengard] Wizard minion; laid in the Shadow player's hand, or in play
					put("keeper", "3_68");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai of another title, cost 2; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6; no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeepingWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Deeping Wall
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 5
		 * Shadow Number: 7
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Shadow:</b> Play Saruman from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Deeping Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void ShadowActionPlaysASarumanMinionFromTheShadowPlayersDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traitor = scn.GetShadowCard("traitor");
		var rabble = scn.GetShadowCard("rabble");
		var follower = scn.GetShadowCard("follower");
		var keeper = scn.GetShadowCard("keeper");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToTopOfDeck(traitor, rabble, follower, savage);
		scn.SetShadowHand(keeper);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, traitor, rabble, follower, savage);
		assertInHand(keeper);
		scn.FreepsPass();

		var wall = scn.GetCurrentSite();
		assertEquals("Deeping Wall", wall.getBlueprint().getTitle());
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(wall));

		scn.ShadowUseCardAction(wall);
		// The deck is shown first; then the Sarumen of two cultures and two types can be taken, and not a minion of
		// another title.
		assertTrue(scn.ShadowHasCardChoiceAvailable(traitor, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, false));
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(traitor, rabble, follower));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		scn.ShadowChooseCardBPFromSelection(rabble);

		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertInZone(Zone.DECK, traitor, follower, savage);
		assertInHand(keeper);
		// One shuffle as the card leaves the deck that was looked through, and the card's own after the play;
		// the other player's deck is not touched.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPlaysTheSarumanFollowerToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traitor = scn.GetShadowCard("traitor");
		var follower = scn.GetShadowCard("follower");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToTopOfDeck(traitor, follower, savage);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, traitor, follower, savage);
		scn.FreepsPass();

		var wall = scn.GetCurrentSite();
		assertEquals("Deeping Wall", wall.getBlueprint().getTitle());
		scn.ShadowUseCardAction(wall);
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(traitor, follower));
		scn.ShadowChooseCardBPFromSelection(follower);

		assertInZone(Zone.SUPPORT, follower);
		assertInZone(Zone.DECK, traitor, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSarumanInTheDrawDeckTheActionIsStillOfferedAndOnlyLooksThroughTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traitor = scn.GetShadowCard("traitor");
		var rabble = scn.GetShadowCard("rabble");
		var follower = scn.GetShadowCard("follower");
		var keeper = scn.GetShadowCard("keeper");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToDiscard(traitor, rabble, follower);
		scn.MoveCardsToTopOfDeck(savage);
		scn.SetShadowHand(keeper);
		scn.SetTwilight(10);
		assertInDiscard(traitor, rabble, follower);
		assertInZone(Zone.DECK, savage);
		assertInHand(keeper);
		scn.FreepsPass();

		var wall = scn.GetCurrentSite();
		assertEquals("Deeping Wall", wall.getBlueprint().getTitle());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.ShadowActionAvailable(wall));

		scn.ShadowUseCardAction(wall);
		// The deck is shown with nothing in it that can be taken.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, false));
		scn.ShadowDismissRevealedCards();

		assertInDiscard(traitor, rabble, follower);
		assertInZone(Zone.DECK, savage);
		assertInHand(keeper);
		// The card's own shuffle, and no other.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsAShadowPhaseActionAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traitor = scn.GetShadowCard("traitor");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToTopOfDeck(traitor);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, traitor);
		scn.FreepsPass();

		var wall = scn.GetCurrentSite();
		assertEquals("Deeping Wall", wall.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(wall));
		// A minion in play, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wall));
		assertInZone(Zone.DECK, traitor);
	}

	@Test
	public void WhileASarumanIsInPlayTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var traitor = scn.GetShadowCard("traitor");
		var keeper = scn.GetShadowCard("keeper");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToTopOfDeck(traitor);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, traitor);
		scn.FreepsPass();

		var wall = scn.GetCurrentSite();
		assertEquals("Deeping Wall", wall.getBlueprint().getTitle());
		assertTrue(scn.ShadowActionAvailable(wall));

		// With a Saruman in play no other can be played (CR 4.2 "unique"); the definition then withholds the look too.
		scn.MoveMinionsToTable(keeper);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, keeper);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wall));

		// A minion of another title in his place: offered again.
		scn.MoveCardsToDiscard(keeper);
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(wall));
		assertInZone(Zone.DECK, traitor);
	}
}
