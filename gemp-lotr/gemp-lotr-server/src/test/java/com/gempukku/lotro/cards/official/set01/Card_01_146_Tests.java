package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fighter", "1_146");
					put("fighter2", "1_146");   // a second copy: not unique, and "this minion" is the one used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf; stronger only against a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					// Bounder: [shire] Hobbit ally, a character who is no companion; a skirmish ability used only on purpose
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukFighterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Fighter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Spot 5 companions to make this minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var fighter = scn.GetFreepsCard("fighter");

		assertEquals("Uruk Fighter", fighter.getBlueprint().getTitle());
		assertNull(fighter.getBlueprint().getSubtitle());
		assertFalse(fighter.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fighter.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, fighter.getBlueprint().getCulture());
		assertEquals(CardType.MINION, fighter.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, fighter.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(fighter, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(fighter, Keyword.DAMAGE));
		assertEquals(3, fighter.getBlueprint().getTwilightCost());
		assertEquals(8, fighter.getBlueprint().getStrength());
		assertEquals(2, fighter.getBlueprint().getVitality());
		assertEquals(5, fighter.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityMakesThisMinionFierceUntilTheRegroupPhaseWhileFiveCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// With Frodo, five companions: a [shire] Hobbit who is the Ring-bearer, two [gondor] Men, a Dwarf and an Elf.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("arwen"), scn.GetFreepsCard("boromir"));

		var fighter = scn.GetShadowCard("fighter");
		var fighter2 = scn.GetShadowCard("fighter2");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter, fighter2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(fighter2, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(fighter));

		scn.ShadowUseCardAction(fighter);
		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));
		// The other copy is not made fierce.
		assertFalse(scn.HasKeyword(fighter2, Keyword.FIERCE));

		// Fierce already, he may be made fierce again: the offer stands (Q-0245).
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fighter));
		scn.ShadowPass();

		// Nobody is assigned, so the fierce assignments follow at once, the last before the regroup phase: this
		// Fighter is still fierce and is the one minion to be assigned.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(fighter));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(fighter2));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithFourCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// With Frodo, four companions, and an ally beside them.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("arwen"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("bounder"));

		var fighter = scn.GetShadowCard("fighter");

		scn.StartGame();
		scn.MoveMinionsToTable(fighter);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fighter));

		// A fifth companion, and it is.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("boromir"));
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fighter));
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
	}
}
