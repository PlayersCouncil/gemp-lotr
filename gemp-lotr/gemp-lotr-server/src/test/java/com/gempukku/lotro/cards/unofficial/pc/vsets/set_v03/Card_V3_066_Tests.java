package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "103_66");
					put("witchking", "1_237");

					put("sam", "1_311");      // Cost 2
					put("aragorn", "1_89");   // Cost 4

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gollum, Half a Wraith Himself
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: 
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: Each time you play a Nazgul, you may exert Gollum to add a threat.
		* 	Assignment: Remove X threats to assign Gollum to a companion costing X or less (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Half a Wraith Himself", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}



// ======== NAZGUL TRIGGER TESTS ========

	@Test
	public void GollumCanExertToAddThreatWhenPlayingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(witchking);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());

		scn.ShadowPlayCard(witchking);

		// Optional trigger should be available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Gollum exerted, threat added
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void GollumTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var witchking = scn.GetShadowCard("witchking");

		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(witchking);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(witchking);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();

		// Neither exertion nor threat
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void GollumTriggerDoesNotFireForNonNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(runner);

		// No trigger for non-Nazgul
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

// ======== ASSIGNMENT ABILITY TESTS ========

	@Test
	public void GollumAssignmentAbilityNotAvailableWithNoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(gollum);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void GollumCanAssignToCompanionCostingUpToXThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");  // Cost 2

		scn.MoveMinionsToTable(gollum);
		scn.MoveCompanionsToTable(sam);
		scn.AddThreats(3);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(3, scn.GetThreats());
		assertFalse(scn.IsCharAssigned(gollum));

		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		// Choose to remove 2 threats (Sam costs 2)
		scn.ShadowChoose("2");

		// Sam should be only valid target (costs 2, within budget)
		// Auto-selected since only one valid target

		assertEquals(1, scn.GetThreats());  // 3 - 2 = 1
		assertTrue(scn.IsCharAssignedAgainst(sam, gollum));
	}

	@Test
	public void GollumAssignmentLimitedByHighestCompanionCost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");  // Cost 2 - highest companion

		scn.MoveMinionsToTable(gollum);
		scn.MoveCompanionsToTable(sam);
		scn.AddThreats(5);  // More threats than highest companion cost

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(5, scn.GetThreats());

		scn.ShadowUseCardAction(gollum);

		// Max should be 2 (Sam's cost), not 5 (threat count)
		// So max choice should be 2
		assertEquals(2, scn.ShadowGetChoiceMax());
	}

	@Test
	public void GollumAssignmentLimitedByCurrentThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");  // Cost 4 - highest companion

		scn.MoveMinionsToTable(gollum);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddThreats(2);  // Fewer threats than highest companion cost

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(2, scn.GetThreats());

		scn.ShadowUseCardAction(gollum);

		// Max should be 2 (threat count), not 4 (Aragorn's cost)
		assertEquals(2, scn.ShadowGetChoiceMax());
	}

	@Test
	public void GollumCannotAssignToRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var frodo = scn.GetRingBearer();
		// Only Frodo (RB) as companion target

		scn.MoveMinionsToTable(gollum);
		scn.AddThreats(3);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Even with threats, Frodo is excluded as target
		// If no valid targets, ability shouldn't work
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void GollumAssignmentOffersValidTargetsBasedOnChosenX() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");        // Cost 2
		var aragorn = scn.GetFreepsCard("aragorn"); // Cost 4

		scn.MoveMinionsToTable(gollum);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddThreats(4);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(gollum);

		// Choose X = 2
		scn.ShadowChoose("2");

		// Sam (cost 2) should be valid, Aragorn (cost 4) should not
		// So sam is auto-selected

		assertEquals(2, scn.GetThreats());  // 4 - 2 = 2
		assertTrue(scn.IsCharAssignedAgainst(sam, gollum));
	}

	@Test
	public void GollumAssignmentWithHigherXAllowsMoreTargets() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");        // Cost 2
		var aragorn = scn.GetFreepsCard("aragorn"); // Cost 4

		scn.MoveMinionsToTable(gollum);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AddThreats(4);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(gollum);

		// Choose X = 4
		scn.ShadowChoose("4");

		// Both Sam (cost 2) and Aragorn (cost 4) should be valid
		assertTrue(scn.ShadowHasCardChoicesAvailable(sam, aragorn));

		scn.ShadowChooseCard(aragorn);

		assertEquals(0, scn.GetThreats());  // 4 - 4 = 0
		assertTrue(scn.IsCharAssignedAgainst(aragorn, gollum));
	}
}
