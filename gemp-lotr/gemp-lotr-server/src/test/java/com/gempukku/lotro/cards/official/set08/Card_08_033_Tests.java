package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("edict", "8_33");
					put("oathbreaker", "8_41");  // Oathbreaker: [gondor] Wraith, strength 6, vitality 3, enduring
					put("sleepless", "8_45");    // Sleepless Dead: [gondor] Wraith, vitality 3; an optional trigger on his own win
					put("deadman", "10_27");     // Dead Man of Dunharrow: [gondor] Wraith, vitality 3; exerts twice when played (cheated in)
					put("king", "8_38");         // King of the Dead, Oathbreaker: [gondor] Wraith, vitality 4; a response for Aragorn only
					put("boromir", "1_97");      // Boromir, Son of Denethor: [gondor] Man, vitality 3
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man, vitality 4
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, no text; overwhelms a companion of 6
					put("marksman1", "1_176");   // Goblin Marksman: archer, and nothing else
					put("marksman2", "1_176");
					put("deadones", "6_100");    // Dead Ones: [sauron] Wraith minion, strength 5, vitality 1 (to play, a burden or a twilight minion: cheated in)
					put("lights", "6_99");       // Corpse Lights: [sauron] Wraith minion, vitality 2; a skirmish ability
					put("wisp", "6_108");        // Wisp of Pale Sheen: [sauron] Wraith minion, vitality 2; a regroup ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [gondor] Wraiths, neither exhausted, and a minion that overwhelms one of them.
	protected VirtualTableScenario GetTwoWraithsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("edict", "8_33");
					put("oathbreaker", "8_41");
					put("sleepless", "8_45");
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElessarsEdictStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Elessar's Edict
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a [gondor] Wraith is about to be killed, discard him and either exhaust another [gondor] Wraith or spot another exhausted [gondor] Wraith to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("edict");

		assertEquals("Elessar's Edict", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "either exhaust another [gondor] Wraith or spot another exhausted [gondor] Wraith": the definition let any
	// [gondor] Wraith be spotted, so the other Wraith need never be exhausted.
	@Test
	public void AnotherWraithWhoIsNotExhaustedMustBeExhaustedAndCannotBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWraithsScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		scn.MoveCompanionsToTable(oathbreaker, sleepless);
		scn.MoveCardsToHand(edict);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.FreepsResolveSkirmish(oathbreaker);

		// 6 against 13: Oathbreaker is overwhelmed and about to be killed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsPlayAvailable(edict));
		scn.FreepsPlayCard(edict);

		// He is discarded and not killed.  Sleepless Dead is not exhausted, so there is no "exhausted [gondor]
		// Wraith" to spot: he is exhausted, and with one thing to do nothing is asked.
		assertInZone(Zone.DISCARD, oathbreaker);
		assertEquals(2, scn.GetWoundsOn(sleepless));
		assertTrue(scn.IsExhausted(sleepless));
		assertInZone(Zone.DISCARD, edict);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherExhaustedWraithIsSpottedAndNotWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(oathbreaker, deadman);
		// Dead Man of Dunharrow, vitality 3, is exhausted, as his own play leaves him.
		scn.AddWoundsToChar(deadman, 2);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);
		assertEquals(2, scn.GetWoundsOn(deadman));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.FreepsResolveSkirmish(oathbreaker);

		// 6 against 13: Oathbreaker is overwhelmed and about to be killed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsPlayAvailable(edict));
		scn.FreepsPlayCard(edict);

		// No other Wraith can be exhausted, so the spot is the one way to pay, taken unasked.
		assertInZone(Zone.DISCARD, oathbreaker);
		assertNotInZone(Zone.DEAD, oathbreaker);
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertInZone(Zone.DISCARD, edict);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAWraithToExhaustAndAnExhaustedOneToSpotThePlayerChoosesAndTheSpotExhaustsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(oathbreaker, sleepless, deadman);
		scn.AddWoundsToChar(deadman, 2);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);
		assertEquals(2, scn.GetWoundsOn(deadman));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.FreepsResolveSkirmish(oathbreaker);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(sleepless));
		assertTrue(scn.FreepsPlayAvailable(edict));
		scn.FreepsPlayCard(edict);

		// Both can be paid: he is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exhaust"));
		assertTrue(scn.FreepsChoiceAvailable("Spot"));
		scn.FreepsChoose("Spot");

		assertInZone(Zone.DISCARD, oathbreaker);
		assertEquals(0, scn.GetWoundsOn(sleepless));
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertInZone(Zone.DISCARD, edict);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyOtherGondorWraithMayBeExhaustedAndAGondorManOrASauronWraithMayNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		var king = scn.GetFreepsCard("king");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(oathbreaker, sleepless, king, boromir);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);

		var troop = scn.GetShadowCard("troop");
		var wisp = scn.GetShadowCard("wisp");

		scn.StartGame();
		// Wisp of Pale Sheen is a [sauron] Wraith who could be exhausted, and is no [gondor] Wraith.
		scn.MoveMinionsToTable(troop, wisp);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(oathbreaker);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(wisp));
		assertTrue(scn.FreepsPlayAvailable(edict));
		scn.FreepsPlayCard(edict);

		// No exhausted Wraith to spot: the exhaust is the one way to pay, and either other Wraith may pay it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleepless, king));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, wisp));
		scn.FreepsChooseCard(king);

		// Vitality 4: exhausted is three wounds.
		assertEquals(3, scn.GetWoundsOn(king));
		assertEquals(0, scn.GetWoundsOn(sleepless));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(wisp));
		assertInZone(Zone.DISCARD, oathbreaker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedGondorManAndAnExhaustedSauronWraithAreNoExhaustedGondorWraithToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(oathbreaker, sleepless, aragorn);
		// Aragorn, vitality 4, is an exhausted [gondor] Man.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);

		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		// Corpse Lights, a [sauron] Wraith of vitality 2, is exhausted.
		scn.MoveMinionsToTable(troop, lights);
		scn.AddWoundsToChar(lights, 1);
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsExhausted(lights));
		assertTrue(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(sleepless));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(oathbreaker);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsPlayAvailable(edict));
		scn.FreepsPlayCard(edict);

		// No exhausted [gondor] Wraith can be spotted, so he is not asked which: Sleepless Dead is exhausted.
		assertInZone(Zone.DISCARD, oathbreaker);
		assertEquals(2, scn.GetWoundsOn(sleepless));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(lights));
		assertInZone(Zone.DISCARD, edict);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoOtherGondorWraithItIsNotOfferedAndHeIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(oathbreaker, boromir, aragorn);
		// Aragorn, vitality 4, is an exhausted [gondor] Man; Boromir a [gondor] Man who could be exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		var troop = scn.GetShadowCard("troop");
		var lights = scn.GetShadowCard("lights");
		var wisp = scn.GetShadowCard("wisp");

		scn.StartGame();
		// Two Wraiths of another culture: Corpse Lights exhausted, Wisp of Pale Sheen not.
		scn.MoveMinionsToTable(troop, lights, wisp);
		scn.AddWoundsToChar(lights, 1);
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(wisp));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(oathbreaker);
		scn.PassSkirmishActions();

		// The kill goes ahead: no response window is put to him.
		assertInZone(Zone.DEAD, oathbreaker);
		assertInHand(edict);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(wisp));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWraithWoundedAndNotKilledAndAGondorManKilledAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(oathbreaker, sleepless, boromir);
		// Boromir, vitality 3, is exhausted: an archery wound kills him.
		scn.AddWoundsToChar(boromir, 2);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);
		assertEquals(2, scn.GetWoundsOn(boromir));

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(oathbreaker));
		scn.FreepsChooseCard(oathbreaker);
		// A wound that does not kill him: the next archery wound is asked for at once.
		assertEquals(1, scn.GetWoundsOn(oathbreaker));
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, sleepless));
		scn.FreepsChooseCard(boromir);

		assertInZone(Zone.DEAD, boromir);
		assertInHand(edict);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AWraithMinionKilledIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edict = scn.GetFreepsCard("edict");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(oathbreaker, deadman);
		scn.AddWoundsToChar(deadman, 2);
		scn.MoveCardsToHand(edict);
		assertInHand(edict);
		assertEquals(2, scn.GetWoundsOn(deadman));

		var deadones = scn.GetShadowCard("deadones");

		scn.StartGame();
		scn.MoveMinionsToTable(deadones);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(oathbreaker, deadones);
		scn.FreepsResolveSkirmish(oathbreaker);

		// Oathbreaker (6) beats Dead Ones (5), a [sauron] Wraith of vitality 1, who is killed by the wound.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, deadones);
		assertInHand(edict);
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
