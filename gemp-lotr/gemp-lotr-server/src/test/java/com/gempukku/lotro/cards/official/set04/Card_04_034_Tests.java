package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("folk", "4_34");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, str 8, no game text
					put("troop", "1_177");      // Goblin Patrol Troop: non-Dunland minion, str 13, no game text
					put("aragorn", "1_89");
					put("goldberry", "9_51");   // Ally, home site 2 on the Fellowship path
					put("toby1", "1_305");
					put("toby2", "1_305");
					put("toby3", "1_305");
					put("lordOfMoria", "1_21"); // Free Peoples condition: must not be a valid discard target
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SecretFolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Secret Folk
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each time a companion or ally loses a skirmish involving a [dunland] Man, you may place a [dunland] token on this card.<br><b>Maneuver:</b> Discard a Free Peoples possession for each [dunland] token here (limit 3). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("folk");

		assertEquals("Secret Folk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CompanionLosingToDunlandManLetsShadowAddAToken() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);
		scn.MoveMinionsToTable(brigand);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn 8 vs Brigand 8: ties go to the Shadow player
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(folk));
	}

	@Test
	public void CompanionLosingToNonDunlandMinionDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var folk = scn.GetShadowCard("folk");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(folk);
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AllyLosingToDunlandManLetsShadowAddAToken() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1088: the trigger only matched companions, not allies.
		var scn = GetScenario();

		var goldberry = scn.GetFreepsCard("goldberry");
		scn.MoveCardsToSupportArea(goldberry);

		var folk = scn.GetShadowCard("folk");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(folk);
		scn.MoveMinionsToTable(brigand);

		scn.StartGame();
		// Site 2 is Goldberry's home, so the Free Peoples player may assign the Brigand to her
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(goldberry, brigand);
		scn.FreepsResolveSkirmish(goldberry);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(folk));
	}

	@Test
	public void ManeuverDiscardsOneFreePeoplesPossessionPerTokenThenSelf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var toby1 = scn.GetFreepsCard("toby1");
		var toby2 = scn.GetFreepsCard("toby2");
		var toby3 = scn.GetFreepsCard("toby3");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(toby1, toby2, toby3, lordOfMoria);

		var folk = scn.GetShadowCard("folk");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(folk);
		scn.MoveMinionsToTable(troop);

		scn.StartGame();
		scn.AddTokensToCard(folk, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetCultureTokensOn(folk));
		assertTrue(scn.ShadowActionAvailable(folk));
		scn.ShadowUseCardAction(folk);

		// 2 tokens: choose 2 of the 3 possessions; the condition is not a legal choice
		assertTrue(scn.ShadowHasCardChoiceAvailable(toby1, toby2, toby3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lordOfMoria));
		scn.ShadowChooseCards(toby1, toby2);

		assertEquals(Zone.DISCARD, toby1.getZone());
		assertEquals(Zone.DISCARD, toby2.getZone());
		assertEquals(Zone.SUPPORT, toby3.getZone());
		assertEquals(Zone.SUPPORT, lordOfMoria.getZone());
		assertEquals(Zone.DISCARD, folk.getZone());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
