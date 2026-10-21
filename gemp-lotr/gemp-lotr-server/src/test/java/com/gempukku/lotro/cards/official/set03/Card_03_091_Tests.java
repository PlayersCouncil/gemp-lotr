package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cruelty", "3_91");

					// The minions.
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, vitality 2; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text

					// Two cards of two titles for the top of a draw deck, neither of them played.
					put("guard", "1_7");        // Dwarf Guard
					put("boromir", "1_96");     // Boromir, Lord of Gondor
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HisCrueltyandMaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: His Cruelty and Malice
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Regroup:</b> Exert a [sauron] minion to make the Free Peoples player discard the top card from his or her draw deck.
		*/

		var scn = GetScenario();

		var cruelty = scn.GetFreepsCard("cruelty");

		assertEquals("His Cruelty and Malice", cruelty.getBlueprint().getTitle());
		assertNull(cruelty.getBlueprint().getSubtitle());
		assertFalse(cruelty.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, cruelty.getBlueprint().getSide());
		assertEquals(Culture.SAURON, cruelty.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, cruelty.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(cruelty, Keyword.SUPPORT_AREA));
		assertEquals(1, cruelty.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupAbilityExertsASauronMinionToDiscardTheTopCardOfTheFreePeoplesDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var lights = scn.GetShadowCard("lights");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var shadowGuard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, lights, troop);
		// Dwarf Guard on top of the Free Peoples deck, Boromir under it; the Shadow player's Dwarf Guard on his.
		scn.MoveCardsToTopOfDeck(boromir, guard, shadowGuard);
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertEquals(boromir, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(shadowGuard, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, guard, boromir);
		assertInZone(Zone.SUPPORT, cruelty);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(troop));
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(guard, scn.GetFreepsTopOfDeck());
		assertTrue(scn.ShadowActionAvailable(cruelty));

		scn.ShadowUseCardAction(cruelty);
		// The cost: a [sauron] Orc and a [sauron] Wraith, and not the [moria] Orc.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, lights));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(troop));
		// The top card goes, and the one under it is now on top.
		assertInDiscard(guard);
		assertEquals(boromir, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(discard + 1, scn.GetFreepsDiscardCount());
		// Not the Shadow player's deck.
		assertEquals(shadowGuard, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertInZone(Zone.SUPPORT, cruelty);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cruelty));

		scn.ShadowPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cruelty));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenTheOnlySauronMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		// The [moria] Orc beside him is fresh.
		scn.MoveMinionsToTable(soldier, troop);
		scn.AddWoundsToChar(soldier, 1);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(soldier));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cruelty));
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyItIsStillOfferedAndTheMinionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier = scn.GetShadowCard("soldier");
		var shadowGuard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(cruelty);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetDrawDeck(VirtualTableScenario.P1);
		scn.MoveCardsToTopOfDeck(shadowGuard);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDeckCount());
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(shadowGuard, scn.GetShadowTopOfDeck());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.ShadowActionAvailable(cruelty));

		// Orc Soldier is the one [sauron] minion: he exerts unasked.
		scn.ShadowUseCardAction(cruelty);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(shadowGuard, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
