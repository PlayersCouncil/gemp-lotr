package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_348_Tests
{

	// The Fellowship path with the Pass as site 4.  Each test begins its turn at site 3 (Rivendell Valley), so that
	// the move to the Pass is the turn's first and the Shadow phase follows there, on the Shadow player's own copy.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Uruk Savage: an [isengard] Uruk-hai minion, vitality 3; damage +1 is its whole text
					put("savage", "1_151");
					// Crebain Flock: an [isengard] Crow minion; its Shadow ability needs 2 wounded companions or Saruman
					put("crebain", "101_25");
					// Goblin Patrol Troop: a [moria] Orc minion, no text
					put("troop", "1_177");
					// Urgency: an [isengard] weather condition, cost 0, plays to the support area; its move trigger
					// counts Ents (there are none) and its skirmish ability is used only on purpose
					put("urgency", "19_17");
					// Saruman's Frost: an [isengard] weather condition, cost 2, played on a site by exerting an
					// [isengard] minion
					put("frost", "1_135");
					// Just a Wisp of Cloud: an [isengard] weather EVENT of the Shadow phase: discard a Free Peoples card
					// from hand to play Saruman from the draw deck or discard pile
					put("wisp", "101_28");
					// Cruel Caradhras: an [isengard] weather event of the MANEUVER phase
					put("cruel", "1_124");
					// No Ordinary Storm: an [isengard] condition that is no weather; it answers a weather condition
					// played by exerting a [gandalf] character (there is none)
					put("storm", "1_130");
					// Saruman, Servant of the Eye: an [isengard] minion who is no weather, cost 4
					put("saruman", "3_69");
					// Hobbit Sword: a Free Peoples card for the Shadow player's hand, to pay Just a Wisp of Cloud
					put("hobbitsword", "1_299");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PassofCaradhrasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Pass of Caradhras
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. <b>Shadow:</b> Spot an [isengard] minion to play a weather card from your draw deck (limit 1 per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Pass of Caradhras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ShadowActionSpotsAnIsengardMinionToPlayAWeatherCardFromTheDrawDeckOnceInTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowSite(4);
		var savage = scn.GetShadowCard("savage");
		var urgency = scn.GetShadowCard("urgency");
		var frost = scn.GetShadowCard("frost");
		var wisp = scn.GetShadowCard("wisp");
		var cruel = scn.GetShadowCard("cruel");
		var storm = scn.GetShadowCard("storm");
		var saruman = scn.GetShadowCard("saruman");
		var hobbitsword = scn.GetShadowCard("hobbitsword");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(hobbitsword);
		scn.MoveCardsToTopOfDeck(urgency, frost, wisp, cruel, storm, saruman);
		assertInZone(Zone.DECK, urgency, frost, wisp, cruel, storm, saruman);
		scn.FreepsPass();

		assertSame(pass, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DECK, urgency, frost, wisp, cruel, storm, saruman);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.ShadowActionAvailable(pass));
		scn.ShadowUseCardAction(pass);

		// The whole deck is shown to the Shadow player alone.
		assertTrue(scn.ShadowHasCardChoiceAvailable(urgency, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(storm, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Weather cards of each type that can be played now: a condition for the support area, a condition for a
		// site, and a Shadow event (the Hobbit Sword in hand pays it, and Saruman in the deck is there to play).
		assertTrue(scn.ShadowHasCardChoiceAvailable(urgency));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frost));
		assertTrue(scn.ShadowHasCardChoiceAvailable(wisp));
		// A weather event of another phase (CR 4.2 "event"), an [isengard] condition that is no weather, and an
		// [isengard] minion the pool could pay for.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cruel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(storm));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		scn.ShadowChooseCardBPFromSelection(urgency);

		assertInZone(Zone.SUPPORT, urgency);
		assertInZone(Zone.DECK, frost, wisp, cruel, storm, saruman);
		// One shuffle as the card leaves the deck that was looked through, and the action's own after the play.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());

		// Limit 1 per turn: with the Savage still there, the Frost still in the deck and the pool to pay for it,
		// the action is not offered again.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(pass));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutAnIsengardMinionAndIsOnceACrebainFlockIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowSite(4);
		var crebain = scn.GetShadowCard("crebain");
		var troop = scn.GetShadowCard("troop");
		var storm = scn.GetShadowCard("storm");
		var urgency = scn.GetShadowCard("urgency");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand();
		scn.MoveCardsToTopOfDeck(urgency);
		assertInZone(Zone.DECK, urgency);
		scn.FreepsPass();

		assertSame(pass, scn.GetCurrentSite());
		// A minion of another culture, and an [isengard] card that is no minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(storm);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.DECK, urgency);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pass));

		// An [isengard] minion who is no Uruk-hai.
		scn.MoveMinionsToTable(crebain);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(pass));
	}

	@Test
	public void WithNoWeatherCardInTheDrawDeckTheActionIsStillUsedForALookThatSpendsTheTurnsUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowSite(4);
		var savage = scn.GetShadowCard("savage");
		var urgency = scn.GetShadowCard("urgency");
		var frost = scn.GetShadowCard("frost");
		var wisp = scn.GetShadowCard("wisp");
		var cruel = scn.GetShadowCard("cruel");
		var storm = scn.GetShadowCard("storm");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand();
		scn.MoveCardsToDiscard(urgency, frost, wisp, cruel);
		scn.MoveCardsToTopOfDeck(storm);
		assertInZone(Zone.DISCARD, urgency, frost, wisp, cruel);
		assertInZone(Zone.DECK, storm);
		scn.FreepsPass();

		assertSame(pass, scn.GetCurrentSite());
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, urgency, frost, wisp, cruel);
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// No penalty for failing to play from a draw deck (Q-0019): the action may be used for a look.
		assertTrue(scn.ShadowActionAvailable(pass));
		scn.ShadowUseCardAction(pass);
		assertTrue(scn.ShadowHasCardChoiceAvailable(storm, false));
		scn.ShadowDismissRevealedCards();

		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.DECK, storm);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());

		// The look was the turn's use: with a weather card back on top of the deck, the action is not offered.
		scn.MoveCardsToTopOfDeck(urgency);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DECK, urgency);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pass));
	}

	@Test
	public void OnTheNextTurnTheOtherShadowPlayerMayUseThePassAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowSite(4);
		// Each player's own cards, for the turn on which that player is the Shadow player
		var secondPlayersSavage = scn.GetShadowCard("savage");
		var secondPlayersUrgency = scn.GetShadowCard("urgency");
		var firstPlayersSavage = scn.GetFreepsCard("savage");
		var firstPlayersUrgency = scn.GetFreepsCard("urgency");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand();
		scn.MoveCardsToTopOfDeck(secondPlayersUrgency);
		assertInZone(Zone.DECK, secondPlayersUrgency);
		scn.FreepsPass();

		assertSame(pass, scn.GetCurrentSite());
		scn.MoveMinionsToTable(secondPlayersSavage);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(pass));
		scn.ShadowUseCardAction(pass);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(secondPlayersUrgency);
		assertInZone(Zone.SUPPORT, secondPlayersUrgency);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pass));

		// The second player's turn.  His fellowship moves from site 3 to the same Pass, and the first player has
		// the Shadow phase there.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SetFreepsHand();
		scn.MoveCardsToTopOfDeck(firstPlayersUrgency);
		assertInZone(Zone.DECK, firstPlayersUrgency);
		scn.ShadowPass();

		assertSame(pass, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		scn.MoveMinionsToTable(firstPlayersSavage);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, firstPlayersUrgency);

		assertTrue(scn.FreepsActionAvailable(pass));
		scn.FreepsUseCardAction(pass);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(firstPlayersUrgency);

		assertInZone(Zone.SUPPORT, firstPlayersUrgency);
		assertFalse(scn.FreepsActionAvailable(pass));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
