package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_054_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer (a Hobbit, so nothing
	// lowers Smaug's cost) and no card in the Ring slot.  Smaug is played in the Shadow phase at Erebor 30_55 (site 7,
	// a mountain, where he stays in play), reached by SkipToSite(6) and the move; its Shadow ability is not used.
	// The hand, piles and pool are laid after the skipped turns, which draw.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gold", "32_54");
					// Smaug, The Golden: the one Smaug in print, a [smaug] Dragon minion of cost 16 (-1 for each Dwarf,
					// Man and Elf spotted), discarded if not at a mountain.  Two copies, for the two piles.
					put("smaug", "30_66");
					put("smaug2", "30_66");
					// Shadow conditions of two cultures, each cost 2 and with no requirement to play.
					put("jail", "32_39");        // Jail: [sauron]; a Regroup ability, not used
					put("river", "31_50");       // Enchanted River: [spider], unique; a trigger at site 5 and a Shadow ability, not used
					// A Free Peoples condition, which the Shadow player's deck holds too.
					put("lord", "1_21");         // Lord of Moria: [dwarven] support-area condition, each Dwarf damage +1
					// Orcs of three cultures, and a minion that is no Orc.
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("troop2", "1_177");
					put("sentinel", "33_49");    // Orc Sentinel: [sauron] Orc, cost 1; [sauron] Orcs' site number -1, a Shadow ability, not used
					put("underling", "33_50");   // Orc Underling: [sauron] Orc; the same -1 and a Response, not used
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					put("bats", "32_30");        // Dark Bats: [moria] Creature; a Shadow ability, not used
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TwistedGoldofDragonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Twisted Gold of Dragon
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Each time you play Smaug, you may play a Shadow condition from your draw deck.<br><b>Shadow:</b>
		 * Discard 3 Orcs from play to play Smaug from your draw deck or discard pile. Its twilight cost is -6.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gold");

		assertEquals("Twisted Gold of Dragon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingSmaugLetsYouPlayAShadowConditionFromYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var lord = scn.GetShadowCard("lord");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.SetShadowHand(smaug);
		scn.SetShadowDrawDeck(jail, river, lord, troop);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, gold);
		assertInHand(smaug);
		assertInZone(Zone.DECK, jail, river, lord, troop);
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(smaug);

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gold));
		scn.ShadowAcceptOptionalTrigger();
		scn.DismissRevealedCards();

		// Shadow conditions of two cultures; not a Shadow card that is no condition, nor a Free Peoples condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(jail, river));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, lord));
		scn.ShadowChooseCardBPFromSelection(jail);

		assertInZone(Zone.SUPPORT, jail);
		assertInZone(Zone.DECK, river, lord, troop);
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheConditionMayBeDeclinedAndNoDeckIsLookedThrough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.SetShadowHand(smaug);
		scn.SetShadowDrawDeck(jail);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, gold);
		assertInZone(Zone.DECK, jail);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(smaug);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gold));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DECK, jail);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAnotherMinionOffersNoCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var sentinel = scn.GetShadowCard("sentinel");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.SetShadowHand(sentinel);
		scn.SetShadowDrawDeck(jail);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, gold);
		assertInHand(sentinel);
		assertInZone(Zone.DECK, jail);

		scn.ShadowPlayCard(sentinel);

		assertInZone(Zone.SHADOW_CHARACTERS, sentinel);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gold));
		assertInZone(Zone.DECK, jail);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionDiscards3OrcsToPlaySmaugFromTheDrawDeckAtCostMinus6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var sentinel = scn.GetShadowCard("sentinel");
		var underling = scn.GetShadowCard("underling");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.MoveMinionsToTable(troop, sentinel, underling, aggressor, bats);
		scn.SetShadowHand();
		// A Smaug in each pile, and a minion of another title in the deck beside him.
		scn.SetShadowDrawDeck(smaug, troop2);
		scn.MoveCardsToDiscard(smaug2);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, gold);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, sentinel, underling, aggressor, bats);
		assertInZone(Zone.DECK, smaug, troop2);
		assertInZone(Zone.DISCARD, smaug2);
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(gold));

		scn.ShadowUseCardAction(gold);
		// Orcs of three cultures; not the Bats, a Creature.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, sentinel, underling, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(troop, sentinel, aggressor);
		assertInZone(Zone.DISCARD, troop, sentinel, aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, underling, bats);

		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.DismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(smaug));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop2));
		scn.ShadowChooseCardBPFromSelection(smaug);

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DISCARD, smaug2);
		assertInZone(Zone.DECK, troop2);
		// 12 - (16 - 6)
		assertEquals(2, scn.GetTwilight());
		// This card's own trigger would answer the play; with no Shadow condition left in the deck it is not offered.
		// A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gold));
		// One shuffle for the card leaving the deck that was looked through, one for the look itself.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeSmaugIsPlayedTheConditionIsOfferedAgainAndAPlayByTheShadowActionCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var jail = scn.GetShadowCard("jail");
		var river = scn.GetShadowCard("river");
		var troop = scn.GetShadowCard("troop");
		var sentinel = scn.GetShadowCard("sentinel");
		var underling = scn.GetShadowCard("underling");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.SetShadowHand(smaug);
		scn.SetShadowDrawDeck(jail, river);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, gold);
		assertInHand(smaug);

		// The first play, from hand.
		scn.ShadowPlayCard(smaug);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gold));
		scn.ShadowAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(jail);
		assertInZone(Zone.SUPPORT, jail);

		// Smaug is discarded with the other minions at the end of the turn.  Two turns on, the fellowship moves to
		// Smaug's Den 30_56 (site 8, a mountain), and Smaug is played again, from the discard pile.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, smaug);
		assertInZone(Zone.SUPPORT, gold, jail);
		scn.MoveMinionsToTable(troop, sentinel, underling);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(river, smaug2);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop, sentinel, underling);
		assertInZone(Zone.DECK, river, smaug2);
		assertTrue(scn.ShadowActionAvailable(gold));

		scn.ShadowUseCardAction(gold);
		// The three Orcs are discarded unasked; the other Smaug lies in the deck, and the discard pile is taken.
		assertInZone(Zone.DISCARD, troop, sentinel, underling);
		scn.ShadowChoose("discard pile");

		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		assertInZone(Zone.DECK, smaug2);
		// 12 - (16 - 6)
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gold));
		scn.ShadowAcceptOptionalTrigger();
		scn.DismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(river);

		assertInZone(Zone.SUPPORT, river);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSmaugOnlyInTheDiscardPileNoPileIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var sentinel = scn.GetShadowCard("sentinel");
		var underling = scn.GetShadowCard("underling");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.MoveMinionsToTable(troop, sentinel, underling);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(troop2);
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop, sentinel, underling);
		assertInZone(Zone.DISCARD, smaug);

		scn.ShadowUseCardAction(gold);

		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertInZone(Zone.SHADOW_CHARACTERS, smaug);
		// No Shadow condition in the deck, so the play of Smaug offers none.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gold));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithOnlyTwoOrcs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var smaug2 = scn.GetShadowCard("smaug2");
		var sentinel = scn.GetShadowCard("sentinel");
		var underling = scn.GetShadowCard("underling");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		// Two Orcs and a minion that is no Orc; a Smaug in each pile, and the pool enough for either.
		scn.MoveMinionsToTable(sentinel, underling, bats);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(smaug2);
		scn.MoveCardsToDiscard(smaug);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, gold);
		assertInZone(Zone.SHADOW_CHARACTERS, sentinel, underling, bats);
		assertInZone(Zone.DISCARD, smaug);
		assertInZone(Zone.DECK, smaug2);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gold));
	}

	@Test
	public void ShadowActionIsNotOfferedWithNoSmaugInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var sentinel = scn.GetShadowCard("sentinel");
		var underling = scn.GetShadowCard("underling");
		var freepsSmaug = scn.GetFreepsCard("smaug");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.MoveMinionsToTable(troop, sentinel, underling);
		// This player's Smaug in his hand, the other player's in that player's own discard pile.
		scn.SetShadowHand(smaug);
		scn.SetShadowDrawDeck(troop2);
		scn.MoveCardsToDiscard(freepsSmaug);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop, sentinel, underling);
		assertInHand(smaug);
		assertInZone(Zone.DECK, troop2);
		assertInZone(Zone.DISCARD, freepsSmaug);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gold));
	}

	@Test
	public void ShadowActionIsNotOfferedWithOneTwilightTooFewForSmaugInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gold = scn.GetShadowCard("gold");
		var smaug = scn.GetShadowCard("smaug");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var sentinel = scn.GetShadowCard("sentinel");
		var underling = scn.GetShadowCard("underling");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		scn.MoveCardsToSupportArea(gold);
		scn.MoveMinionsToTable(troop, sentinel, underling);
		scn.SetShadowHand();
		scn.SetShadowDrawDeck(troop2);
		scn.MoveCardsToDiscard(smaug);
		// 16 - 6 is 10, with only Bilbo to spot.
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop, sentinel, underling);
		assertInZone(Zone.DISCARD, smaug);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gold));

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gold));
	}
}
