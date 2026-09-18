package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alatar", "13_78");
					put("saruman1", "17_48");   // Saruman, Coldly Still: assignable Wizard minion, no relevant triggers
					put("saruman2", "17_48");
					put("troop", "1_177");      // non-Wizard minion
					put("aragorn", "1_89");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlatarDeceivedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Alatar Deceived
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a Wizard wins a skirmish, this condition becomes a <b>fierce</b>, <b>damage +1</b> Wizard minion until the start of the regroup phase that has 11 strength and 1 vitality, and cannot take wounds or bear other cards. This card is still a condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alatar");

		assertEquals("Alatar Deceived", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WizardWinningSkirmishTurnsAlatarIntoMinionUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var alatar = scn.GetShadowCard("alatar");
		var saruman1 = scn.GetShadowCard("saruman1");
		scn.MoveCardsToSupportArea(alatar);
		scn.MoveMinionsToTable(saruman1);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman1);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn 8 vs Saruman 8: skirmish is a loss for Freeps, so the Wizard wins
		scn.PassSkirmishActions();

		// Skirmish damage and Alatar's trigger are both required responses; the Free Peoples player orders them
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsChooseAction("Alatar Deceived");
		if (scn.FreepsDecisionAvailable("Required responses"))
			scn.FreepsChooseAction("Resolve skirmish damage");

		assertEquals(Zone.SHADOW_CHARACTERS, alatar.getZone());
		assertTrue(scn.IsType(alatar, CardType.MINION));
		assertTrue(scn.IsType(alatar, CardType.CONDITION));
		assertTrue(scn.IsRace(alatar, Race.WIZARD));
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetVitality(alatar));
		assertTrue(scn.HasKeyword(alatar, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(Zone.SUPPORT, alatar.getZone());
		assertFalse(scn.IsType(alatar, CardType.MINION));
	}

	@Test
	public void NonWizardWinningSkirmishDoesNotTriggerAlatar() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var alatar = scn.GetShadowCard("alatar");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(alatar);
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(Zone.SUPPORT, alatar.getZone());
		assertFalse(scn.IsType(alatar, CardType.MINION));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoWizardsWinningTheSameSkirmishOnlyApplyTheTransformationOnce() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1062: each winning Wizard emits its own WinsSkirmish result, so the trigger is collected
		// twice before the first copy resolves; the second copy must not stack another +11/+1 on top.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var alatar = scn.GetShadowCard("alatar");
		var saruman1 = scn.GetShadowCard("saruman1");
		var saruman2 = scn.GetShadowCard("saruman2");
		scn.MoveCardsToSupportArea(alatar);
		scn.MoveMinionsToTable(saruman1, saruman2);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, saruman1);
		// Shadow assigns the leftover Wizard onto the same skirmish
		scn.ShadowAssignToMinions(aragorn, saruman2);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Both copies of the trigger were collected; resolve them back to back before the damage
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsChooseAction("Alatar Deceived");
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		scn.FreepsChooseAction("Alatar Deceived");
		if (scn.FreepsDecisionAvailable("Required responses"))
			scn.FreepsChooseAction("Resolve skirmish damage");

		assertEquals(Zone.SHADOW_CHARACTERS, alatar.getZone());
		assertEquals(11, scn.GetStrength(alatar));
		assertEquals(1, scn.GetVitality(alatar));
		assertEquals(1, scn.GetKeywordCount(alatar, Keyword.DAMAGE));
	}
}
