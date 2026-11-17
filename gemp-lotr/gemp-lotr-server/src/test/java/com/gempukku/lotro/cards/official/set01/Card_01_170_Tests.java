package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fool", "1_170");
					put("fool2", "1_170");

					put("merry", "1_303");          // Merry, From O'er the Brandywine: [shire] Hobbit companion; strength +2 while bearing a weapon
					put("trollslayer", "103_31");   // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");        // Bounder: [shire] Hobbit ally; a skirmish ability used only on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound by rule but not the Ring-bearer; a fellowship ability and a response used only on purpose
					put("smeagol", "103_116");      // Smeagol, Once Like Us: [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("ted", "18_75");            // Ted Sandyman: [men] minion who is a Hobbit; abilities used only on purpose

					put("runner", "1_178");         // Goblin Runner: [moria] Orc; its text answers only its own play
					put("runner2", "1_178");
					put("tentacle", "101_36");      // Vile Tentacle: [moria] minion who is no Orc (a Creature)
					put("armory", "1_173");         // Goblin Armory: [moria] condition, no minion
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, no text
					put("chieftain", "1_266");      // Orc Chieftain: [sauron] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoolofaTookStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Fool of a Took!
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot a Hobbit (except the Ring-bearer) to reveal cards from the top of your draw deck until you reveal one that is not a [moria] minion. Take the revealed cards into hand.
		*/

		var scn = GetScenario();

		var fool = scn.GetFreepsCard("fool");

		assertEquals("Fool of a Took!", fool.getBlueprint().getTitle());
		assertNull(fool.getBlueprint().getSubtitle());
		assertFalse(fool.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fool.getBlueprint().getSide());
		assertEquals(Culture.MORIA, fool.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fool.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fool, Timeword.SHADOW));
		assertEquals(0, fool.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAShireHobbitRevealsPastMoriaMinionsToAMoriaConditionAndTakesAllThreeIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetShadowCard("fool");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		var tentacle = scn.GetShadowCard("tentacle");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(fool);

		scn.StartGame();
		// A [moria] Orc and a [moria] Creature, then a [moria] card that is no minion, then a [moria] Orc below it.
		scn.SetShadowDrawDeck(runner, tentacle, armory, troop);
		scn.FreepsPass();

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(fool));
		scn.ShadowPlayCard(fool);

		// Shown to both players; the card below the Armory is not revealed.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(tentacle, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		assertInZone(Zone.HAND, runner, tentacle, armory);
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, troop);
		assertEquals(1, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, fool);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAGondorHobbitRevealsOnlyATopMinionOfAnotherCultureAndTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetShadowCard("fool");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(trollslayer);
		scn.MoveCardsToHand(fool);

		scn.StartGame();
		// A [sauron] Orc on top, a [moria] Orc below it.
		scn.SetShadowDrawDeck(chieftain, runner);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(fool));
		scn.ShadowPlayCard(fool);

		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, false));
		scn.DismissRevealedCards();

		assertInZone(Zone.HAND, chieftain);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, runner);
		assertEquals(1, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, fool);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTheRingBearerAndACompanionWhoIsNoHobbitButCanWithTheShadowPlayersOwnHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetShadowCard("fool");
		var smeagol = scn.GetFreepsCard("smeagol");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(fool);

		scn.StartGame();
		scn.FreepsPass();

		// The only Hobbit is the Ring-bearer; Smeagol is [shire] and no Hobbit.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fool));

		// A Hobbit of the Shadow player's own (a bare "Hobbit" names no side).
		scn.MoveMinionsToTable(ted);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fool));
	}

	@Test
	public void SpottingSamWhoIsRingBoundButIsNotTheRingBearerIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetShadowCard("fool");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(fool);

		scn.StartGame();
		scn.FreepsPass();

		// Sam is Ring-bound as Frodo is, but only Frodo is the Ring-bearer, and the exception is the Ring-bearer.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fool));
	}

	@Test
	public void SpottingAHobbitAllyWithOnlyMoriaMinionsInTheDeckTakesTheWholeDeckAndASecondCopyIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fool = scn.GetShadowCard("fool");
		var fool2 = scn.GetShadowCard("fool2");
		var bounder = scn.GetFreepsCard("bounder");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(fool, fool2);

		scn.StartGame();
		scn.SetShadowDrawDeck(runner, runner2);
		assertInZone(Zone.HAND, fool, fool2);
		scn.FreepsPass();

		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(fool));
		scn.ShadowPlayCard(fool);

		// No card that is not a [moria] minion is ever reached: the whole deck is revealed and taken.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		assertInZone(Zone.HAND, runner, runner2, fool2);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());

		// With the deck empty the event is still played (CR 4.2 "effect"), and takes nothing.
		assertTrue(scn.ShadowPlayAvailable(fool2));
		scn.ShadowPlayCard(fool2);

		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, runner, runner2);
		assertEquals(0, scn.GetShadowDeckCount());
		assertInZone(Zone.DISCARD, fool, fool2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
