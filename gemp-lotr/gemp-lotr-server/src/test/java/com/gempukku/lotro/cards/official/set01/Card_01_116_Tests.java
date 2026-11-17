package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swordarm", "1_116");
					put("swordarm2", "1_116");
					put("valor", "1_103");      // Elendil's Valor: [gondor] Maneuver event, (0). Make a [gondor] companion defender +1 until the regroup phase
					put("valor2", "1_103");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7; a skirmish ability used only on purpose
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: [gondor] Man, strength 8; defender +1 is his whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit, strength 3 (enduring: kept unwounded); a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, a Man; a maneuver ability used only on purpose

					put("savage1", "1_151");    // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordarmoftheWhiteTowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Swordarm of the White Tower
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] companion strength +2 (or +4 if he or she is <b>defender +1</b>).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordarm");

		assertEquals("Swordarm of the White Tower", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenGondorCompanionWhoIsNoDefenderStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordarm = scn.GetFreepsCard("swordarm");
		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(swordarm);
		assertInHand(swordarm);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(swordarm));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsPlayAvailable(swordarm));

		scn.FreepsPlayCard(swordarm);
		// The [gondor] companions, Men and a Hobbit, in this skirmish, assigned to another or in none; not the
		// [gondor] ally, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, guard, frodo, savage1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		// Boromir is no defender: +2, though Aragorn beside him is defender +1.
		assertEquals(9, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(guard));
		assertInDiscard(swordarm);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// In the next skirmish the bonus is gone (CR 4.2: each skirmish is a phase of its own).
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void MakesADefenderGondorCompanionStrengthPlus4AndOneInNoSkirmishPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordarm = scn.GetFreepsCard("swordarm");
		var swordarm2 = scn.GetFreepsCard("swordarm2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToHand(swordarm, swordarm2);
		assertInHand(swordarm, swordarm2);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.IsCharAssigned(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));

		scn.FreepsPlayCard(swordarm);
		scn.FreepsChooseCard(aragorn);
		// Printed defender +1: +4.
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(swordarm2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		scn.FreepsChooseCard(pippin);
		// Pippin, in no skirmish and no defender: +2.
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(12, scn.GetStrength(aragorn));
		assertInDiscard(swordarm, swordarm2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordarm = scn.GetFreepsCard("swordarm");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(swordarm);
		assertInHand(swordarm);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage1);
		scn.FreepsResolveSkirmish(frodo);

		// The Ring-bearer and a [rohan] Man are the only companions: nobody for the event to strengthen.
		var frodoStrength = scn.GetStrength(frodo);
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(swordarm));

		scn.FreepsPlayCard(swordarm);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(guard));
		assertInDiscard(swordarm);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DefenderGainedByAnEventAndDefenderPlus2EachMakeItPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordarm = scn.GetFreepsCard("swordarm");
		var swordarm2 = scn.GetFreepsCard("swordarm2");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToHand(swordarm, swordarm2, valor, valor2);
		assertInHand(swordarm, swordarm2, valor, valor2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Elendil's Valor makes Boromir defender +1 and Aragorn, printed defender +1, defender +2.
		scn.FreepsPlayCard(valor);
		scn.FreepsChooseCard(boromir);
		scn.ShadowPass();
		scn.FreepsPlayCard(valor2);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsPlayCard(swordarm);
		scn.FreepsChooseCard(boromir);
		assertEquals(11, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(swordarm2);
		scn.FreepsChooseCard(aragorn);
		// Defender +2 meets "defender +1" (CR 4.2 "defender +X").
		assertEquals(12, scn.GetStrength(aragorn));
		assertInDiscard(swordarm, swordarm2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
