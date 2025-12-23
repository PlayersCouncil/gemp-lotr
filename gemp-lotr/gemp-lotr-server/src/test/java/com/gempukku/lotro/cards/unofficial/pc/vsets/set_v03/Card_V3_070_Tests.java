package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mouth", "103_70");
					put("savage", "1_151");  // 5/3, Damage +1

					put("legolas", "1_50");  // Archer, exert to wound
					put("gandalf", "1_364"); // Gandalf, The Grey Wizard
					put("yceh", "103_9");    // You Cannot Enter Here
					put("mysterious", "1_78"); // Mysterious Wizard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheMouthofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Mouth of Sauron, Bearer of Dread News
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Lurker.
		* 	Other minions cannot take more than 1 wound per phase.
		* 	Response: If a Free Peoples event is played, exert this minion to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mouth");

		assertEquals("The Mouth of Sauron", card.getBlueprint().getTitle());
		assertEquals("Bearer of Dread News", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}


// ======== WOUND PROTECTION TESTS ========

	@Test
	public void MouthOfSauronProtectsOtherMinionsFromMultipleWoundsPerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveMinionsToTable(mouth, savage);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(mouth));

		// First archery wound - both minions are valid targets
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoicesAvailable(savage, mouth));
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));

		// Second archery wound - Savage already took 1 wound this phase
		// Mouth's protection makes Savage invalid target, auto-selects Mouth
		scn.ShadowPass();
		scn.FreepsUseCardAction(legolas);

		assertEquals(1, scn.GetWoundsOn(savage));  // Still 1
		assertEquals(1, scn.GetWoundsOn(mouth));   // Now wounded

		scn.SkipToAssignments();

		// Assign Legolas vs Savage (6 str vs 5 str - Legolas wins)
		scn.FreepsAssignAndResolve(legolas, savage);
		scn.BothPass();

		// Skirmish phase is new - protection resets, Savage can take another wound
		assertEquals(2, scn.GetWoundsOn(savage));
	}

	@Test
	public void MouthOfSauronDoesNotProtectItself() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var legolas = scn.GetFreepsCard("legolas");

		scn.MoveMinionsToTable(mouth);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		// First wound to Mouth
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(mouth));

		// Second wound to Mouth - protection says "other minions", not self
		scn.ShadowPass();
		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(mouth));
	}

// ======== EVENT CANCELLATION TESTS ========

	@Test
	public void MouthOfSauronCanCancelFPEventButCostIsStillPaid() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var yceh = scn.GetFreepsCard("yceh");

		scn.MoveMinionsToTable(mouth, savage);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(yceh);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(mouth));
		assertFalse(scn.IsHindered(savage));

		// Play You Cannot Enter Here
		scn.FreepsPlayCard(yceh);

		// Mouth can respond to cancel
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Gandalf still exerted (cost was paid before cancel)
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Mouth exerted to cancel
		assertEquals(1, scn.GetWoundsOn(mouth));
		// Savage NOT hindered (effect cancelled)
		assertFalse(scn.IsHindered(savage));
		// Gandalf NOT exhausted (exhaust was part of effect, not cost)
		assertFalse(scn.IsExhausted(gandalf));
	}

	@Test
	public void MouthOfSauronCanCancelSkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var mysterious = scn.GetFreepsCard("mysterious");

		scn.MoveMinionsToTable(mouth, savage);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(mysterious);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(gandalf, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gandalf);

		int gandalfStrength = scn.GetStrength(gandalf);

		// Play Mysterious Wizard (should give +4 with ≤4 burdens)
		scn.FreepsPlayCard(mysterious);

		// Mouth can respond to cancel
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Gandalf's strength unchanged (event cancelled)
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetWoundsOn(mouth));
	}

	@Test
	public void MouthOfSauronCannotCancelIfExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var mouth = scn.GetShadowCard("mouth");
		var savage = scn.GetShadowCard("savage");
		var gandalf = scn.GetFreepsCard("gandalf");
		var yceh = scn.GetFreepsCard("yceh");

		scn.MoveMinionsToTable(mouth, savage);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(yceh);
		scn.AddWoundsToChar(mouth, 2);  // Vitality 3, 2 wounds = exhausted

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.IsExhausted(mouth));

		// Play You Cannot Enter Here
		scn.FreepsPlayCard(yceh);
		scn.FreepsChooseCard(savage);

		// Mouth cannot respond - exhausted, can't exert
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Event resolves - Savage is hindered
		assertTrue(scn.IsHindered(savage));
	}
}
