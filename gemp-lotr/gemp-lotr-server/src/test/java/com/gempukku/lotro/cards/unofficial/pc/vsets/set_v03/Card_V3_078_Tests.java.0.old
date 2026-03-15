package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "103_78");
					put("shotgun", "1_231");
					put("armory", "1_173");   // Moria condition
					put("bladetip", "1_209");   // Ringwraith condition

					put("sam", "1_311");
					put("aragorn", "1_89");
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
		* 	Each time a Nazgul wins a skirmish, you may exert this minion to play a Shadow condition from your discard pile. Add a threat if it is a [ringwraith] condtion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
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


// ======== BASIC TRIGGER TESTS ========

	@Test
	public void OtseaCanPlayConditionFromDiscardWhenNazgulWinsNormally() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var armory = scn.GetShadowCard("armory");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(armory);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Witch-king wins, Otsëa trigger available
		int threatsBefore = scn.GetThreats();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Otsëa exerts
		assertEquals(1, scn.GetWoundsOn(otsea));

		// armory auto-selected, played to support area
		assertInZone(Zone.SUPPORT, armory);

		// No threat added (Isengard, not Ringwraith)
		assertEquals(threatsBefore, scn.GetThreats());
	}

	@Test
	public void OtseaCanPlayConditionFromDiscardWhenNazgulWinsViaOverwhelm() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var armory = scn.GetShadowCard("armory");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(armory);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		// Witch-king wins, Otsëa trigger available
		int threatsBefore = scn.GetThreats();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Otsëa exerts
		assertEquals(1, scn.GetWoundsOn(otsea));

		// armory auto-selected, played to support area
		assertInZone(Zone.SUPPORT, armory);

		// No threat added (Isengard, not Ringwraith)
		assertEquals(threatsBefore, scn.GetThreats());
	}

	@Test
	public void OtseaAddsThreatWhenPlayingRingwraithCondition() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var bladetip = scn.GetShadowCard("bladetip");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(bladetip);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		int threatsBefore = scn.GetThreats();

		scn.ShadowAcceptOptionalTrigger();

		// Blade Tip played
		assertInZone(Zone.SUPPORT, bladetip);

		// Threat added (Ringwraith condition)
		assertEquals(threatsBefore + 1, scn.GetThreats());
	}

	@Test
	public void OtseaOffersChoiceWhenMultipleConditionsInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var armory = scn.GetShadowCard("armory");
		var bladetip = scn.GetShadowCard("bladetip");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(armory, bladetip);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		int threatsBefore = scn.GetThreats();

		scn.ShadowAcceptOptionalTrigger();

		// Should have choice between conditions
		assertTrue(scn.ShadowHasCardChoicesAvailable(armory, bladetip));

		// Choose Ringwraith condition
		scn.ShadowChooseCard(bladetip);

		assertInZone(Zone.SUPPORT, bladetip);
		assertInDiscard(armory);
		assertEquals(threatsBefore + 1, scn.GetThreats());
	}

	@Test
	public void OtseaCanTriggerOffOwnSkirmishVictory() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var bladetip = scn.GetShadowCard("bladetip");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(bladetip);

		scn.StartGame();
		scn.SkipToAssignments();

		// Otsëa fights and wins
		scn.FreepsAssignToMinions(sam, otsea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		// Otsëa won, can trigger off own victory
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertInZone(Zone.SUPPORT, bladetip);
	}

// ======== TRIGGER AVAILABILITY ========

	@Test
	public void OtseaTriggerNotAvailableWithNoConditionsInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(sam);
		// No conditions in discard

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OtseaTriggerNotAvailableWhenExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var bladetip = scn.GetShadowCard("bladetip");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(bladetip);
		scn.AddWoundsToChar(otsea, 2);  // Vitality 3, exhausted

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void OtseaTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var shotgun = scn.GetShadowCard("shotgun");
		var bladetip = scn.GetShadowCard("bladetip");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(otsea, shotgun);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToDiscard(bladetip);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, shotgun);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInDiscard(bladetip);
	}
}
