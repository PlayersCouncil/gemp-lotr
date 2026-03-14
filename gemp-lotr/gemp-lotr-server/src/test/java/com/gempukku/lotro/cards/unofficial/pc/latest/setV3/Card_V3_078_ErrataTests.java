package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_078_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "103_78");
					put("nazgul2", "1_234"); // Ulaire Nertea
					put("condition", "1_206"); // Bent on Discovery (Wraith Condition)
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Otsea, Consecrated by Pestilence
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		* 	Each time a Nazgul wins a skirmish, you may exert this minion and remove (1) to play a Shadow condition
		*   from your discard pile. Add a threat if it is [ringwraith].
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Ulaire Otsea", card.getBlueprint().getTitle());
		assertEquals("Consecrated by Pestilence", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OtseaTriggerRequiresRemovingOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var condition = scn.GetShadowCard("condition");
		scn.MoveMinionsToTable(otsea, nazgul2);
		scn.MoveCardsToDiscard(condition);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		scn.SkipToAssignments();
		// Assign guard to nazgul2. Nazgul2 (Nertea) has STR 9, guard has STR 6.
		// Nazgul should win.
		scn.FreepsAssignToMinions(guard, nazgul2);
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();

		// After nazgul wins, Otsea's optional trigger fires.
		// The errata added RemoveTwilight(1) to cost.
		// With 0 twilight, the trigger should not be available.
		scn.SetTwilight(0);

		// No twilight means the cost cannot be paid
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OtseaTriggerWorksWithSufficientTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var nazgul2 = scn.GetShadowCard("nazgul2");
		var condition = scn.GetShadowCard("condition");
		scn.MoveMinionsToTable(otsea, nazgul2);
		scn.MoveCardsToDiscard(condition);

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SetTwilight(5);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, nazgul2);
		scn.FreepsResolveSkirmish(guard);
		scn.PassCurrentPhaseActions();

		// With twilight available, the trigger should be offered
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		int twilightBefore = scn.GetTwilight();
		scn.ShadowAcceptOptionalTrigger();

		// Otsea should be exerted and 1 twilight removed
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(twilightBefore - 1, scn.GetTwilight());
	}
}
