package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_156_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior", "1_156");
					put("warrior2", "1_156");   // a second copy: not unique, and "this minion" is the one used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf; stronger only against a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man; a skirmish ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; his text only changes his own strength
					// Bounder: [shire] Hobbit ally, a character who is no companion; a skirmish ability used only on purpose
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Maneuver:</b> Spot 6 companions to make this minion <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var warrior = scn.GetFreepsCard("warrior");

		assertEquals("Uruk Warrior", warrior.getBlueprint().getTitle());
		assertNull(warrior.getBlueprint().getSubtitle());
		assertFalse(warrior.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, warrior.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, warrior.getBlueprint().getCulture());
		assertEquals(CardType.MINION, warrior.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, warrior.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(warrior, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(3, warrior.getBlueprint().getTwilightCost());
		assertEquals(9, warrior.getBlueprint().getStrength());
		assertEquals(2, warrior.getBlueprint().getVitality());
		assertEquals(5, warrior.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityMakesThisMinionFierceUntilTheRegroupPhaseWhileSixCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// With Frodo, six companions: a [shire] Hobbit who is the Ring-bearer, two [gondor] Men, a Dwarf, an Elf and a Wizard.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("arwen"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gandalf"));

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(warrior2, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(warrior));

		scn.ShadowUseCardAction(warrior);
		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));
		// The other copy is not made fierce.
		assertFalse(scn.HasKeyword(warrior2, Keyword.FIERCE));

		// Fierce already, he may be made fierce again: the offer stands (Q-0245).
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warrior));
		scn.ShadowPass();

		// Nobody is assigned, so the fierce assignments follow at once, the last before the regroup phase: this
		// Warrior is still fierce and is the one minion to be assigned.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(warrior, Keyword.FIERCE));
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(warrior));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(warrior2));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// With Frodo, five companions, and an ally beside them.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("arwen"), scn.GetFreepsCard("gandalf"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("bounder"));

		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(warrior));

		// A sixth companion, and it is.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("boromir"));
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(warrior));
		assertFalse(scn.HasKeyword(warrior, Keyword.FIERCE));
	}
}
