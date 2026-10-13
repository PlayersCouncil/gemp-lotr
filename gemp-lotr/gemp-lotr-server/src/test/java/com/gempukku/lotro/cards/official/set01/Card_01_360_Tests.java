package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_360_Tests
{

	// Emyn Muil is site 9 of FellowshipSites.  The table is in a Players' Council format, in which the regroup
	// phase at site 9 is played (the game is won as it ends), so that the end of "until the regroup phase" can
	// be read there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, strength 5, vitality 3, damage +1 only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, beats the Savage; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"pc_fotr_block"
		);
	}

	@Test
	public void EmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Emyn Muil
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Maneuver:</b> Exert your minion to make that minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ManeuverActionExertsAMinionOfTheShadowPlayersToMakeItFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		var emyn = scn.GetCurrentSite();
		assertEquals("Emyn Muil", emyn.getBlueprint().getTitle());

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// "Your minion": the Free Peoples player, who has none, is not offered it with minions on the table.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(emyn));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(emyn));
		scn.ShadowUseCardAction(emyn);

		// Either of his minions, of two cultures and races, may exert; the Ring-bearer may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		// The minion exerted is fierce; the other is not.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Aragorn beats the Savage in the first round, and the Savage, wounded and alive, is still fierce after it.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		// The Savage is fierce and offered for the fierce assignment; the Troop is not.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// At the first regroup decision he is fierce no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
	}

	@Test
	public void WithHisOnlyMinionExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		var emyn = scn.GetCurrentSite();
		assertEquals("Emyn Muil", emyn.getBlueprint().getTitle());

		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(1, scn.GetVitality(savage));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(emyn));
	}
}
