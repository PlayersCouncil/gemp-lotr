package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_005_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");      // Gimli, Son of Gloin: [dwarven] companion, vitality 3
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("legolas", "1_50");    // Legolas, Greenleaf: [elven] companion, vitality 3
					put("bounder", "1_286");   // Bounder: [shire] Hobbit ally, vitality 2; able to exert, and no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_5", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");      // Gimli, Son of Gloin: [dwarven] companion, vitality 3
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] companion, vitality 4
					put("legolas", "1_50");    // Legolas, Greenleaf: [elven] companion, vitality 3
					put("bounder", "1_286");   // Bounder: [shire] Hobbit ally, vitality 2; able to exert, and no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_5"
		);
	}

	@Test
	public void ExertToRemoveTwilightStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_5
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Fellowship: Exert X companions to remove (X).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_5", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void ExertOneCompanionToRemoveOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: Exert 1 companion to remove (1).

		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gimli);
		// An ally can exert as well as anyone, and is no companion
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SetTwilight(5);

		int twilightBefore = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));
		// The Ring-bearer and Gimli are the companions; X is at least 1 and at most the two of them
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Choose Gimli to exert
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(twilightBefore - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertTwoCompanionsToRemoveTwoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: Exert 2 companions to remove (2).

		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, aragorn, legolas);
		// Legolas has vitality 3: with 2 wounds he is exhausted and cannot exert
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();
		scn.SetTwilight(5);

		int twilightBefore = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsUseCardAction(scn.GetFreepsCard("mod"));
		// Three companions can exert, and X may be any number of them
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas));
		assertEquals(3, scn.FreepsGetChoiceMax());
		// Choose both to exert
		scn.FreepsChooseCards(gimli, aragorn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(twilightBefore - 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void XIsNotHeldToTheTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		// X is what is paid, whatever it buys: two companions may exert to empty a pool of 1, and the action
		// is still there to be paid for when the pool is empty.

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		scn.SetTwilight(1);

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, aragorn);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotUseWithNoCompanionsThatCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: If no companions can exert, ability should not be available.
		// Frodo alone with 3 wounds (exhausted) = can't exert.

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		// Exhaust Frodo (vitality 4, so 3 wounds = exhausted)
		scn.AddWoundsToChar(scn.GetRingBearer(), 3);
		// An ally who could exert pays nothing here
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();
		scn.SetTwilight(3);

		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanUseWithOpponentsMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_5: The text has no "your": the modifier the other player holds is the Free Peoples player's to use.

		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.SetTwilight(3);

		assertTrue(scn.FreepsActionAvailable(shadowMod));

		// Frodo is the only companion, so he is the one who exerts
		scn.FreepsUseCardAction(shadowMod);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
