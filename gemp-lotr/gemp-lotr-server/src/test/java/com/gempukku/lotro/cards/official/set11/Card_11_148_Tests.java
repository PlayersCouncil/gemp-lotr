package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hrothlac", "11_148");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, resistance 6, valiant and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, resistance 6; he offers to heal
					                            //   another [rohan] Man who exerts, declined here
					put("soldier", "17_101");   // Soldier of Rohan: a [rohan] Man companion, resistance 5; strength +2 only while
					                            //   the Ring-bearer is assigned
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, resistance 6

					put("slayer", "3_93");      // Morgul Slayer: "Regroup: Exert this minion to wound a companion
					put("slayer2", "3_93");     //   (except the Ring-bearer)." Vitality 2: one use each.
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HrothlacStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Hrothlac, Man of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Response:</b> If a [rohan] companion who has resistance 4 or more is about to take a wound, exert Hrothlac to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hrothlac");

		assertEquals("Hrothlac", card.getBlueprint().getTitle());
		assertEquals("Man of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsToPreventAWoundToARohanCompanionOfResistanceFourWhateverHisRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrothlac = scn.GetFreepsCard("hrothlac");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(hrothlac, guard, merry);

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(slayer, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Resistance 6 less 2 burdens: exactly 4.
		assertEquals(4, scn.GetResistance(guard));
		assertEquals(4, scn.GetResistance(merry));
		assertEquals(0, scn.GetWoundsOn(hrothlac));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(merry));

		// A [rohan] Man.
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hrothlac));
		scn.FreepsChooseOptionalTrigger(hrothlac);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(hrothlac));
		// Merry answers Hrothlac's exertion with an offer to heal him.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A [rohan] Hobbit.
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hrothlac));
		scn.FreepsChooseOptionalTrigger(hrothlac);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(hrothlac));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExertsToPreventAWoundToHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrothlac = scn.GetFreepsCard("hrothlac");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(hrothlac, guard);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(hrothlac));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(hrothlac);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hrothlac));
		scn.FreepsChooseOptionalTrigger(hrothlac);
		// The exertion alone: the wound itself is prevented (2 if it were not).
		assertEquals(1, scn.GetWoundsOn(hrothlac));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForACompanionOfResistanceThreeOrOneOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrothlac = scn.GetFreepsCard("hrothlac");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(hrothlac, soldier, aragorn);

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(slayer, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Resistance 5 less 2 burdens is 3; Aragorn's 6 less 2 is 4.
		assertEquals(3, scn.GetResistance(soldier));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(0, scn.GetWoundsOn(hrothlac));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// A [rohan] companion one short of the resistance, with Hrothlac able to exert.
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(soldier);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hrothlac));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(hrothlac));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A companion of resistance 4 who is not [rohan].
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer2);
		scn.ShadowChooseCard(aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hrothlac));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(hrothlac));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileHrothlacIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrothlac = scn.GetFreepsCard("hrothlac");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(hrothlac, guard);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.AddWoundsToChar(hrothlac, 2);
		assertEquals(2, scn.GetWoundsOn(hrothlac));
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: he cannot exert.
		assertEquals(2, scn.GetWoundsOn(hrothlac));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(hrothlac));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(hrothlac));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
