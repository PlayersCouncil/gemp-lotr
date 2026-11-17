package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lurtz", "1_127");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("tyrant", "11_204");    // Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14; damage +1 and toil 1
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc; a regroup ability only
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard, no Uruk-hai.  Each companion is
					// resistance -1; a skirmish ability used only on purpose.
					put("saruman", "17_116");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Lurtz, Servant of Isengard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Archer. Damage +1.
		 * Maneuver: Spot another Uruk-hai to make Lurtz fierce until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lurtz");

		assertEquals("Lurtz", card.getBlueprint().getTitle());
		assertEquals("Servant of Isengard", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilitySpotsAnotherUrukHaiToMakeLurtzFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(lurtz));
		scn.ShadowUseCardAction(lurtz);

		// Lurtz is fierce; the Uruk-hai he spotted is not.
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Lurtz is an archer: the minion archery wound goes on Aragorn.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Nobody is assigned in the first round, so the fierce round comes straight after it.
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));

		// Lurtz is fierce and offered for the fierce assignment; the Savage is not.
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lurtz));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(savage));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// At the first regroup decision he is fierce no longer.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
	}

	@Test
	public void AnUrukHaiOfAnotherCultureIsAnotherUrukHaiToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var tyrant = scn.GetShadowCard("tyrant");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, tyrant);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(lurtz));
		scn.ShadowUseCardAction(lurtz);

		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(tyrant, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedWithoutAnotherUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		// An [isengard] minion who is an Orc and an [uruk-hai] minion who is a Wizard.  Lurtz himself is the
		// only Uruk-hai.
		scn.MoveMinionsToTable(lurtz, worker, saruman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lurtz));
		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
	}

	@Test
	public void AbilityIsOfferedAgainWhileLurtzIsFierceAlready() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(lurtz, Keyword.FIERCE));
		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Fierce already, he is still offered the ability: the keyword given again is not nothing.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lurtz));
	}
}
