package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_051_Tests
{

// ----------------------------------------
// DUTY NO LESS THAN YOURS TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("duty1", "103_51");       // Duty No Less Than Yours
					put("duty2", "103_51");       // Second copy
					put("southron1", "4_250");    // Desert Explorer - [Raider] minion
					put("southron2", "4_250");
					put("orc", "1_271");          // Orc Soldier - [Sauron], not Raider

					put("aragorn", "1_89");       // Unbound companion
					put("sam", "1_311");          // Ring-bound companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DutyNoLessThanYoursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Duty No Less Than Yours
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [raider] minion dies, you may exert a [raider] minion to exert an unbound companion. If you can spot another Duty No Less Than Yours, you may discard it to also hinder that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("duty1");

		assertEquals("Duty No Less Than Yours", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



	@Test
	public void DutyTriggersWhenRaiderMinionDiesToExertUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(duty);
		scn.MoveMinionsToTable(southron1, southron2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(southron1, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		int aragornWoundsBefore = scn.GetWoundsOn(aragorn);
		int southron2WoundsBefore = scn.GetWoundsOn(southron2);

		// Aragorn wins, southron1 dies
		scn.BothPass();

		// Duty triggers
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Exert an unbound companion (aragorn auto-selected as only unbound)
		assertEquals(southron2WoundsBefore + 1, scn.GetWoundsOn(southron2));
		assertEquals(aragornWoundsBefore + 1, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void DutyDoesNotTriggerWhenNonRaiderMinionDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(duty);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orc);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn wins, orc dies
		scn.PassCurrentPhaseActions();

		// Duty should NOT trigger - orc is Sauron, not Raider
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}


	@Test
	public void DutyCanDiscardSecondCopyToHinderExertedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty1 = scn.GetShadowCard("duty1");
		var duty2 = scn.GetShadowCard("duty2");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(duty1, duty2);
		scn.MoveMinionsToTable(southron1, southron2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(southron1, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsHindered(aragorn));

		scn.PassCurrentPhaseActions();
		assertInDiscard(southron1);
		assertEquals(0, scn.GetWoundsOn(southron2));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Duty No Less Than Yours"));
		scn.ShadowChooseOptionalTrigger(duty1);
		assertEquals(1, scn.GetWoundsOn(southron2));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Offered to discard second copy to hinder
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();
		// duty2 auto-selected as only other copy

		assertInDiscard(duty2);
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void DutyCanDeclineToHinderEvenWithSecondCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty1 = scn.GetShadowCard("duty1");
		var duty2 = scn.GetShadowCard("duty2");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(duty1, duty2);
		scn.MoveMinionsToTable(southron1, southron2);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(southron1, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, southron1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.PassCurrentPhaseActions();

		scn.ShadowAcceptOptionalTrigger();

		// Decline to discard for hinder
		scn.ShadowChooseNo();

		assertInZone(Zone.SUPPORT, duty2); // Still in play
		assertFalse(scn.IsHindered(aragorn)); // Not hindered
		assertEquals(1, scn.GetWoundsOn(aragorn)); // But still exerted
	}
}
