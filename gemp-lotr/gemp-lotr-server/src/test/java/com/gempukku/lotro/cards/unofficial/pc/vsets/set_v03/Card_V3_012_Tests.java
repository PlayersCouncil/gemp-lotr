package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pass", "103_12");
					put("shelob", "8_26");
					put("runner", "1_178");
					put("runner2", "1_178");
					put("gollum", "9_28");

					put("aragorn", "1_89"); // Gondor Man for Citadel
					put("glorfindel", "9_16"); // Strength 9, can beat Shelob
					put("citadel", "5_32");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PassoftheSpiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pass of the Spider
		 * Unique: true
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Each time a special ability on a Free Peoples condition is used, you may discard a minion stacked here to hinder that condition and heal Shelob.
		* 	Skirmish: If Shelob is skirmishing, stack another minion from play here to make a character skirmishing her strength -2 (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pass");

		assertEquals("Pass of the Spider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PassOfTheSpiderSkirmishAbilityStacksMinionAndReducesStrength() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(shelob, runner);

		scn.StartGame();
		scn.SkipToAssignments();

		int glorfindelBaseStrength = scn.GetStrength(glorfindel);

		// Assign Glorfindel to Shelob
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();

		assertEquals(glorfindelBaseStrength, scn.GetStrength(glorfindel));
		assertEquals(0, scn.GetStackedCards(pass).size());

		// Use Pass of the Spider ability to stack runner
		assertTrue(scn.ShadowActionAvailable(pass));
		scn.ShadowUseCardAction(pass);

		// Runner is now stacked on Pass
		assertEquals(1, scn.GetStackedCards(pass).size());

		// Glorfindel should be -2 strength
		assertEquals(glorfindelBaseStrength - 2, scn.GetStrength(glorfindel));
	}

	@Test
	public void PassOfTheSpiderSkirmishAbilityNotAvailableWithoutOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(shelob);
		// No other minions besides Shelob

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign Glorfindel to Shelob
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(glorfindel);

		// Ability should not be available - no other minions to stack
		assertFalse(scn.ShadowActionAvailable(pass));
	}

	@Test
	public void PassOfTheSpiderSkirmishAbilityLimitedOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(pass);
		scn.MoveMinionsToTable(shelob, runner, runner2);

		scn.StartGame();
		scn.SkipToAssignments();

		int glorfindelBaseStrength = scn.GetStrength(glorfindel);

		// Assign Glorfindel to Shelob
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();

		// First use - stack runner
		assertTrue(scn.ShadowActionAvailable(pass));
		scn.ShadowUseCardAction(pass);
		scn.ShadowChooseCard(runner);

		assertEquals(1, scn.GetStackedCards(pass).size());
		assertEquals(glorfindelBaseStrength - 2, scn.GetStrength(glorfindel));

		// Limit once per turn
		assertFalse(scn.ShadowActionAvailable(pass));
	}

	@Test
	public void PassOfTheSpiderTriggerNotOfferedWithoutStackedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var citadel = scn.GetFreepsCard("citadel");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(pass, citadel);
		scn.MoveMinionsToTable(shelob);
		// No minions stacked on Pass

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign Aragorn to Shelob
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Freeps activates Citadel
		assertTrue(scn.FreepsActionAvailable(citadel));
		scn.FreepsUseCardAction(citadel);

		// Shadow should NOT have optional trigger - no minions stacked
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void PassOfTheSpiderHindersConditionAndHealsShelobWhenFPConditionUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var citadel = scn.GetFreepsCard("citadel");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(pass, citadel);
		scn.MoveMinionsToTable(shelob);
		scn.StackCardsOn(pass, runner);

		scn.StartGame();

		// Wound Shelob twice
		scn.AddWoundsToChar(shelob, 2);
		assertEquals(2, scn.GetWoundsOn(shelob));

		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(citadel));

		// Assign Aragorn (Gondor Man) to Shelob
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Freeps activates Citadel (exert Aragorn to transfer to Shelob)
		assertTrue(scn.FreepsActionAvailable(citadel));
		scn.FreepsUseCardAction(citadel);

		// Shadow should have optional trigger from Pass of the Spider
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Runner auto-selected as only stacked minion
		assertEquals(0, scn.GetStackedCards(pass).size());
		assertInZone(Zone.DISCARD, runner);

		// Citadel should be hindered
		assertTrue(scn.IsHindered(citadel));

		// Shelob should be healed (2 wounds -> 1 wound)
		assertEquals(1, scn.GetWoundsOn(shelob));
	}
}
