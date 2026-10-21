package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spies", "2_92");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, no tracker; a skirmish ability only
					put("band", "1_270");       // Orc Scouting Band: [sauron] Orc tracker; a roaming discount and a skirmish ability
					put("assassin", "1_262");   // Orc Assassin: [sauron] Orc tracker; its assignment ability wants 2 Hobbit companions
					put("sentinel", "10_83");   // Cirith Ungol Sentinel: [sauron] Uruk-hai, no Orc; a cost discount only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its trigger is on being played
					put("scout", "2_47");       // Uruk Scout: [isengard] Uruk-hai tracker; a roaming discount and a response to ranger events

					// Cards for the Shadow player's draw deck
					put("deck1", "1_278");      // Strength Born of Fear
					put("deck2", "1_278");
					put("deck3", "1_278");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rivendell Waterfall as site 3, with Aragorn, a ranger: the move limit is 3 there, so a turn has two regroup moves.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spies", "2_92");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger; a maneuver ability only

					put("deck1", "1_278");      // Strength Born of Fear, for the Shadow player's draw deck
					put("deck2", "1_278");
					put("deck3", "1_278");
					put("deck4", "1_278");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiesofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Spies of Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, spot a [sauron] Orc. Plays to your support area.<br>Each time the fellowship moves during the regroup phase, you may draw a card (or 2 cards if you can spot a [sauron] tracker).
		*/

		var scn = GetScenario();

		var spies = scn.GetFreepsCard("spies");

		assertEquals("Spies of Mordor", spies.getBlueprint().getTitle());
		assertNull(spies.getBlueprint().getSubtitle());
		assertFalse(spies.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spies.getBlueprint().getSide());
		assertEquals(Culture.SAURON, spies.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, spies.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(spies, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(spies, Keyword.SUPPORT_AREA));
		assertEquals(1, spies.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASauronOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var soldier = scn.GetShadowCard("soldier");
		var sentinel = scn.GetShadowCard("sentinel");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(spies, soldier);
		assertInHand(spies, soldier);

		scn.StartGame();
		// A [sauron] minion who is no Orc and an Orc who is not [sauron].
		scn.MoveMinionsToTable(sentinel, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(spies));

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.ShadowPlayAvailable(spies));

		scn.ShadowPlayCard(spies);
		assertInZone(Zone.SUPPORT, spies);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedSpottingAnOrcAssassinItDrawsTwoCardsAtTheRegroupMoveForThatTracker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var assassin = scn.GetShadowCard("assassin");
		var deck1 = scn.GetShadowCard("deck1");
		var deck2 = scn.GetShadowCard("deck2");
		var deck3 = scn.GetShadowCard("deck3");
		scn.MoveCardsToHand(spies);
		assertInHand(spies);

		scn.StartGame();
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());
		scn.MoveMinionsToTable(assassin);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(spies));
		scn.ShadowPlayCard(spies);
		assertInZone(Zone.SUPPORT, spies);

		scn.SkipToMovementDecision();
		assertInPlay(assassin);
		// Three known cards, the first named on top.
		scn.SetShadowDrawDeck(deck1, deck2, deck3);
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(deck1, scn.GetShadowTopOfDeck());
		int hand = scn.GetShadowHandCount();

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertInHand(deck1, deck2);
		assertInZone(Zone.DECK, deck3);
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARegroupMoveOffersToDrawACardAndTheFellowshipPhaseMoveDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var soldier = scn.GetShadowCard("soldier");
		var scout = scn.GetShadowCard("scout");
		var deck1 = scn.GetShadowCard("deck1");
		var deck2 = scn.GetShadowCard("deck2");
		var deck3 = scn.GetShadowCard("deck3");

		scn.StartGame();
		scn.MoveCardsToSupportArea(spies);
		assertInZone(Zone.SUPPORT, spies);
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());

		// The fellowship's move at the end of the fellowship phase is no regroup move.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(spies));

		// No minion: the Shadow phase is followed by the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		// A [sauron] Orc who is no tracker and a tracker who is not [sauron]: one card, not two.
		scn.MoveMinionsToTable(soldier, scout);
		scn.SkipToMovementDecision();
		assertInPlay(soldier, scout);
		scn.SetShadowDrawDeck(deck1, deck2, deck3);
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(deck1, scn.GetShadowTopOfDeck());
		int hand = scn.GetShadowHandCount();

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertInHand(deck1);
		assertInZone(Zone.DECK, deck2, deck3);
		assertEquals(2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithASauronTrackerToSpotItDrawsTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var band = scn.GetShadowCard("band");
		var deck1 = scn.GetShadowCard("deck1");
		var deck2 = scn.GetShadowCard("deck2");
		var deck3 = scn.GetShadowCard("deck3");

		scn.StartGame();
		scn.MoveCardsToSupportArea(spies);
		assertInZone(Zone.SUPPORT, spies);
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(band);
		scn.SkipToMovementDecision();
		assertInPlay(band);
		scn.SetShadowDrawDeck(deck1, deck2, deck3);
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(deck1, scn.GetShadowTopOfDeck());
		int hand = scn.GetShadowHandCount();

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowAcceptOptionalTrigger();

		// Two cards, and only two: the parenthesis is taken when it can be.
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertInHand(deck1, deck2);
		assertInZone(Zone.DECK, deck3);
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheDrawLeavesHandAndDeckAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var band = scn.GetShadowCard("band");
		var deck1 = scn.GetShadowCard("deck1");
		var deck2 = scn.GetShadowCard("deck2");

		scn.StartGame();
		scn.MoveCardsToSupportArea(spies);
		assertInZone(Zone.SUPPORT, spies);
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());

		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(band);
		scn.SkipToMovementDecision();
		scn.SetShadowDrawDeck(deck1, deck2);
		assertEquals(2, scn.GetShadowDeckCount());
		int hand = scn.GetShadowHandCount();

		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(hand, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, deck1, deck2);
		assertEquals(2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachOfTwoRegroupMovesInOneTurnOffersTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var spies = scn.GetShadowCard("spies");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deck1 = scn.GetShadowCard("deck1");
		var deck2 = scn.GetShadowCard("deck2");
		var deck3 = scn.GetShadowCard("deck3");
		var deck4 = scn.GetShadowCard("deck4");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(spies);
		assertInZone(Zone.SUPPORT, spies);
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetShadowDrawDeck(deck1, deck2);
		assertEquals(deck1, scn.GetShadowTopOfDeck());
		int hand = scn.GetShadowHandCount();

		// The first regroup move, to Rivendell Waterfall.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertInHand(deck1);
		assertInZone(Zone.DECK, deck2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.SetShadowDrawDeck(deck3, deck4);
		assertEquals(deck3, scn.GetShadowTopOfDeck());
		hand = scn.GetShadowHandCount();

		// The second regroup move of the same turn.
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(spies));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetShadowHandCount());
		assertInHand(deck3);
		assertInZone(Zone.DECK, deck4);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveCount());
	}
}
