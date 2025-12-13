package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "103_13");
					put("gollum", "9_28");
					put("runner", "1_178");

					put("aragorn", "1_89");
					put("glorfindel", "9_16");
					put("gauntlets", "4_112"); // Boromir's Gauntlets - cancels skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shelob, Terror of Cirith Ungol
		 * Unique: true
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: Fierce.  Enduring.
		* 	Each time a skirmish involving Shelob ends, you may hinder each character she was skirmishing.
		* 	Skirmish: If Gollum is skirmishing, exert Gollum and Shelob to hinder a character he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shelob");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Terror of Cirith Ungol", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShelobHindersCharactersWhenShelobWinsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();
		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(aragorn));

		// Assign Aragorn (strength 8) to Shelob (strength 8) - tie goes to Shadow
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);

		// Pass skirmish actions - Shelob wins
		scn.PassCurrentPhaseActions();

		// Shadow should have optional trigger to hinder
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Aragorn should be hindered
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void ShelobHindersCharactersWhenShelobLosesSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();
		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(glorfindel));

		// Assign Glorfindel (strength 9) to Shelob (strength 8) - Shelob loses
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.FreepsResolveSkirmish(glorfindel);

		// Pass skirmish actions - Shelob loses
		scn.PassCurrentPhaseActions();

		// Shadow should have optional trigger to hinder
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Glorfindel should be hindered
		assertTrue(scn.IsHindered(glorfindel));
	}

	@Test
	public void ShelobHindersCharactersWhenSkirmishIsCanceled() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gauntlets = scn.GetFreepsCard("gauntlets");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gauntlets);
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();
		scn.SkipToAssignments();

		assertFalse(scn.IsHindered(aragorn));

		// Assign Aragorn to Shelob
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);

		// Use Boromir's Gauntlets to cancel the skirmish
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);
		scn.ShadowChooseNo();

		// Shadow should have optional trigger to hinder (skirmish ended via cancel)
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Aragorn should be hindered
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void ShelobCanDeclineHinderTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(shelob);

		scn.StartGame();
		scn.SkipToAssignments();

		// Assign Aragorn to Shelob
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);

		// Pass skirmish actions - skirmish ends
		scn.PassCurrentPhaseActions();

		// Decline trigger
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Aragorn should not be hindered
		assertFalse(scn.IsHindered(aragorn));
	}

	@Test
	public void ShelobSkirmishAbilityExertsGollumAndShelobToHinderCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveMinionsToTable(shelob, gollum, runner);

		scn.StartGame();
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertFalse(scn.IsHindered(aragorn));

		// Assign Aragorn to Gollum, Glorfindel to Shelob
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);
		scn.ShadowDeclineAssignments();

		// Resolve Gollum's skirmish first
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Use Shelob's ability to hinder Aragorn
		assertTrue(scn.ShadowActionAvailable(shelob));
		scn.ShadowUseCardAction(shelob);

		// Both Gollum and Shelob should be exerted
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(shelob));

		// Aragorn (character skirmishing Gollum) should be hindered
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void ShelobSkirmishAbilityNotAvailableWithoutGollumInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(shelob, gollum);

		scn.StartGame();
		scn.SkipToAssignments();

		// Only assign Aragorn to Shelob, leave Gollum unassigned
		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Shelob's ability should not be available - Gollum not in skirmish
		assertFalse(scn.ShadowActionAvailable(shelob));
	}

	@Test
	public void ShelobSkirmishAbilityNotAvailableIfGollumExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveMinionsToTable(shelob, gollum, runner);

		scn.StartGame();

		// Exhaust Gollum (vitality 4, so 3 wounds)
		scn.AddWoundsToChar(gollum, 3);
		assertEquals(1, scn.GetVitality(gollum));

		scn.SkipToAssignments();

		// Assign Aragorn to Gollum, Glorfindel to Shelob
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Shelob's ability should not be available - Gollum exhausted, cannot exert
		assertFalse(scn.ShadowActionAvailable(shelob));
	}

	@Test
	public void ShelobSkirmishAbilityNotAvailableIfShelobExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveMinionsToTable(shelob, gollum, runner);

		scn.StartGame();

		// Exhaust Shelob (vitality 8, so 7 wounds)
		scn.AddWoundsToChar(shelob, 7);
		assertEquals(1, scn.GetVitality(shelob));

		scn.SkipToAssignments();

		// Assign Aragorn to Gollum, Glorfindel to Shelob
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Shelob's ability should not be available - Shelob exhausted, cannot exert
		assertFalse(scn.ShadowActionAvailable(shelob));
	}
}
