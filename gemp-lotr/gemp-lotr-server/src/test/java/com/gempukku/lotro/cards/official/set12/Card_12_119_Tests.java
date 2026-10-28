package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bilbo", "12_119");
					put("boromir", "1_97");      // Boromir, Son of Denethor: strength 7
					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1
					put("pursuer", "4_190");     // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("deftness", "1_293");    // Halfling Deftness: [shire] Skirmish event
					put("sturdy", "1_315");      // Stout and Sturdy: [shire] Maneuver or Skirmish event
					put("appetite", "1_294");    // Hobbit Appetite: [shire] Fellowship event
					put("resilience", "1_287");  // Extraordinary Resilience: [shire] Response event
					put("axestrike", "1_3");     // Axe Strike: [dwarven] Skirmish event
					put("stealth", "1_298");     // Hobbit Stealth: [shire] Skirmish event, the Shadow player's in his own discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Bilbo, Melancholy Hobbit
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ring-bound</b>.<br>At the start of each skirmish involving Bilbo, you may exert him to take a [shire] skirmish event into hand from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bilbo");

		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Melancholy Hobbit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfHisSkirmishMayExertToTakeAShireSkirmishEventFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var deftness = scn.GetFreepsCard("deftness");
		var sturdy = scn.GetFreepsCard("sturdy");
		var appetite = scn.GetFreepsCard("appetite");
		var resilience = scn.GetFreepsCard("resilience");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToFreepsDiscard("deftness", "sturdy", "appetite", "resilience", "axestrike");

		var pursuer = scn.GetShadowCard("pursuer");
		var stealth = scn.GetShadowCard("stealth");
		scn.MoveCardsToShadowDiscard("stealth");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		assertInZone(Zone.DISCARD, deftness, sturdy, appetite, resilience, axestrike, stealth);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(bilbo));

		assertTrue(scn.FreepsHasCardChoiceAvailable(deftness, sturdy));
		// Another phase's [shire] event, a [shire] Response event, another culture's Skirmish event, and the
		// Shadow player's [shire] Skirmish event in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(appetite, resilience, axestrike, stealth));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sturdy);

		assertInHand(sturdy);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, deftness, appetite, resilience, axestrike, stealth);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotAskedAtTheStartOfAnotherSkirmishDeclinedInHisAndAskedAgainInHisFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var boromir = scn.GetFreepsCard("boromir");
		var deftness = scn.GetFreepsCard("deftness");
		var sturdy = scn.GetFreepsCard("sturdy");
		scn.MoveCompanionsToTable(bilbo, boromir);
		scn.MoveCardsToFreepsDiscard("deftness", "sturdy");

		var runner = scn.GetShadowCard("runner");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, pursuer);
		assertInZone(Zone.DISCARD, deftness, sturdy);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, pursuer}, new PhysicalCardImpl[]{boromir, runner});

		// Boromir's skirmish starts first, Bilbo waiting for his own: nothing is asked.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, deftness, sturdy);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();
		// 3 against the Pursuer's 5: one wound, not overwhelmed.
		assertEquals(1, scn.GetWoundsOn(bilbo));

		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(bilbo));
		scn.FreepsChooseCard(deftness);
		assertInHand(deftness);
		assertInZone(Zone.DISCARD, sturdy);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotAskedWhileExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var deftness = scn.GetFreepsCard("deftness");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToFreepsDiscard("deftness");

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Vitality 4 with 3 wounds.
		scn.AddWoundsToChar(bilbo, 3);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.DISCARD, deftness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertEquals(3, scn.GetWoundsOn(bilbo));
	}

	@Test
	public void WithNoShireSkirmishEventInTheDiscardPileHeExertsAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var appetite = scn.GetFreepsCard("appetite");
		var axestrike = scn.GetFreepsCard("axestrike");
		scn.MoveCompanionsToTable(bilbo);
		scn.MoveCardsToFreepsDiscard("appetite", "axestrike");

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		assertInZone(Zone.DISCARD, appetite, axestrike);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, pursuer);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, appetite, axestrike);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
