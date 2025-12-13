package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static org.junit.Assert.*;

public class Card_V3_058_Tests
{

// ----------------------------------------
// SHIFTING BATTLE-LINES TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battlelines", "103_58"); // Shifting Battle-lines
					put("initiate", "103_47");    // Desert Wind Initiate - Ambush (3)
					put("southron1", "4_250");    // Southron Explorer - Southron Man
					put("southron2", "4_250");    // Another Southron for discard
					put("charger", "103_41");     // Bladetusk Charger - [Raider] item
					put("orc", "1_271");          // Orc Soldier - not Southron, no ambush

					put("aragorn", "1_89");
					put("moria", "1_21");         // Lord of Moria - FP condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShiftingBattlelinesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shifting Battle-lines
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: At the start of each skirmish involving a minion with ambush, you may remove (2) to hinder a
		 * 		Free Peoples condition.
		 * 		Each time your Southron Man dies, you may remove (1) to stack a Southron from your discard pile on a
		 * 		[raider] item that already has a Southron stacked on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battlelines");

		assertEquals("Shifting Battle-lines", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShiftingBattleLinesHindersFPConditionAtStartOfAmbushSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlelines = scn.GetShadowCard("battlelines");
		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCardsToSupportArea(battlelines, moria);
		scn.MoveMinionsToTable(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, initiate);

		// Initiate has Ambush - accept trigger
		scn.ShadowAcceptOptionalTrigger();

		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsHindered(moria));
		int twilightBefore = scn.GetTwilight();

		// At start of skirmish, trigger available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// moria auto-selected as only FP condition
		assertTrue(scn.IsHindered(moria));
		assertEquals(twilightBefore - 2, scn.GetTwilight());
	}

	@Test
	public void ShiftingBattleLinesDoesNotTriggerForMinionWithoutAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlelines = scn.GetShadowCard("battlelines");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCardsToSupportArea(battlelines, moria);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);

		// No ambush minion - no trigger
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void ShiftingBattleLinesStacksSouthronFromDiscardWhenSouthronManDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlelines = scn.GetShadowCard("battlelines");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var charger = scn.GetShadowCard("charger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(battlelines, charger);
		scn.StackCardsOn(charger, southron2); // Charger already has a Southron stacked
		scn.MoveMinionsToTable(southron1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.AddWoundsToChar(southron1, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(1, scn.GetStackedCards(charger).size());

		// Aragorn wins, southron1 dies
		scn.PassCurrentPhaseActions();

		// Southron Man died - trigger available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// southron1 now in discard, can stack it
		// charger auto-selected as only valid target (has Southron stacked)
		assertEquals(2, scn.GetStackedCards(charger).size());
	}

	@Test
	public void ShiftingBattleLinesRequiresItemWithSouthronAlreadyStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlelines = scn.GetShadowCard("battlelines");
		var southron1 = scn.GetShadowCard("southron1");
		var charger = scn.GetShadowCard("charger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(battlelines, charger);
		// Charger has NO Southrons stacked
		scn.MoveMinionsToTable(southron1);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.AddWoundsToChar(southron1, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron1);
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();

		// Southron died, but no valid target (Charger has no Southron stacked)
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(southron1);
	}

	@Test
	public void ShiftingBattleLinesDoesNotTriggerForNonSouthronManDeath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlelines = scn.GetShadowCard("battlelines");
		var orc = scn.GetShadowCard("orc");
		var southron1 = scn.GetShadowCard("southron1");
		var charger = scn.GetShadowCard("charger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(battlelines, charger);
		scn.StackCardsOn(charger, southron1);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.AddWoundsToChar(orc, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();

		// Orc died, not a Southron Man - no trigger
		assertInDiscard(orc);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
