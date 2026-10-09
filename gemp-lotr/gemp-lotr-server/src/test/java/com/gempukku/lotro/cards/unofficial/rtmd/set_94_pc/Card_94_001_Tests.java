package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_94_001_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] Elf ally, home site 6, strength 6, vitality 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] Elf ally, home site 6, archer, strength 3, vitality 3
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] Dwarf ally, home site 3, vitality 3
					put("erland", "2_21");      // Erland, Advisor to Brand: a [gandalf] Man ally, home site 3, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, strength 8, vitality 4
					put("backstabber", "1_174"); // Goblin Backstabber: a minion, strength 5, vitality 1
					put("marksman", "1_176");   // Goblin Marksman: a minion, strength 7, vitality 1; Archer is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_1", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an [elven] Elf ally, home site 6, strength 6, vitality 3
					put("backstabber", "1_174"); // Goblin Backstabber: a minion, strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_1"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_1
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Maneuver: Exert your ally to allow them to participate in archery fire and
		 * skirmishes. At the start of the regroup phase, kill that ally.
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_1", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void ExertAllyEnablesParticipationThenKillsItAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var celeborn = scn.GetFreepsCard("celeborn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveAlliesToTable(celeborn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// Pre-check: ally can't normally join skirmishes/archery, no cost paid yet
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.CanBeAssignedViaAction(celeborn));

		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		// Celeborn is the only ally in play, so he is the one who exerts and nobody is asked.

		// Cost paid (exert = 1 wound) and effect applied (can now be assigned)
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.CanBeAssignedViaAction(celeborn));

		// Control alternates to Shadow (who has nothing to do) and back to Freeps
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// At the start of regroup, the ally is killed: with 1 wound of his 3 vitality, another wound would not do it
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.DEAD, celeborn);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutUsingTheActionAllyIsNeverAssignableAndSurvives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var celeborn = scn.GetFreepsCard("celeborn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveAlliesToTable(celeborn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);

		scn.SkipToPhase(Phase.REGROUP);

		assertFalse(scn.CanBeAssignedViaAction(celeborn));
		assertInPlay(celeborn);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyOfTheOwnersAlliesMayExertAndEachOneExertedIsKilledWhileAnAllyLeftAloneIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var celeborn = scn.GetFreepsCard("celeborn");
		var grimir = scn.GetFreepsCard("grimir");
		var erland = scn.GetFreepsCard("erland");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		scn.MoveAlliesToTable(celeborn, grimir, erland);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// An Elf, a Dwarf and a Man, of three cultures, are the allies; Aragorn, Frodo and the Troop
		// could each exert and are no allies of the owner's.
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, grimir, erland));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(celeborn));
		scn.FreepsChooseCard(celeborn);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(erland));
		assertTrue(scn.FreepsCanAssign(celeborn));
		assertFalse(scn.FreepsCanAssign(grimir));
		assertFalse(scn.FreepsCanAssign(erland));

		// A second use, on another ally
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(erland));
		assertTrue(scn.FreepsCanAssign(celeborn));
		assertTrue(scn.FreepsCanAssign(grimir));
		assertFalse(scn.FreepsCanAssign(erland));

		// Both allies who exerted are killed, each with 2 vitality left; Erland and Aragorn are as they were.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.DEAD, celeborn, grimir);
		assertEquals(2, scn.GetFreepsDeadCount());
		assertInZone(Zone.SUPPORT, erland);
		assertEquals(0, scn.GetWoundsOn(erland));
		assertInPlay(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheAllyCountsInArcheryFireMayTakeAnArcheryWoundAndFightsASkirmishBeforeTheRegroupPhaseKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var orophin = scn.GetFreepsCard("orophin");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveAlliesToTable(orophin, celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// Both allies live at site 6 and the fellowship is at site 2: Orophin's bow counts for nothing.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertFalse(scn.FreepsCanAssign(orophin));

		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(orophin);
		assertEquals(1, scn.GetWoundsOn(orophin));

		// In the archery phase he is an archer of the fellowship, and one who may take the Marksman's arrow.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(orophin, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(orophin));

		// His own arrow kills the Marksman (vitality 1).
		scn.ShadowChooseCard(marksman);
		assertInZone(Zone.DISCARD, marksman);

		scn.SkipToAssignments();
		assertTrue(scn.FreepsCanAssign(orophin));
		assertFalse(scn.FreepsCanAssign(celeborn));
		scn.FreepsAssignToMinions(orophin, backstabber);
		scn.FreepsResolveSkirmish(orophin);
		assertTrue(scn.IsCharSkirmishingAgainst(orophin, backstabber));
		assertInZone(Zone.SUPPORT, orophin);
		assertEquals(1, scn.GetWoundsOn(orophin));

		// Strength 3 against 5: he loses and takes a second wound of his 3 vitality, which he survives.
		// The skirmish phase ends, the regroup phase begins, and he is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, orophin);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInZone(Zone.SUPPORT, celeborn);
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertInPlay(backstabber);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedOnceTheOnlyAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveAlliesToTable(celeborn);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(celeborn, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 3 with 1 wound: one exertion left.
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		assertEquals(2, scn.GetWoundsOn(celeborn));

		// Exhausted, with Aragorn and the Troop unwounded beside him.
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOwnerAsPlayerTwoUsesItOnHisOwnTurnAndHisAllyIsKilledAtRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var celeborn = scn.GetShadowCard("celeborn");
		var backstabber = scn.GetFreepsCard("backstabber");

		scn.StartGame();
		// Player 2's turn: he is the Free Peoples player, and the modifier is his.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.MoveAlliesToTable(celeborn);
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhaseInverted(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.ShadowCanAssign(celeborn));
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);
		// Celeborn is the only ally in play, so he is the one who exerts and nobody is asked.
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertTrue(scn.ShadowCanAssign(celeborn));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhaseInverted(Phase.REGROUP);
		assertInZone(Zone.DEAD, celeborn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void OwnerGatingFreepsCantUseWhenShadowOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 94_1's ability says "your ally" - when Shadow owns the mod, the ability must be absent
		// for the non-owner (Freeps), even though Freeps has an ally that would otherwise qualify.
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var celeborn = scn.GetFreepsCard("celeborn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveAlliesToTable(celeborn);
		scn.StartGame();
		scn.MoveMinionsToTable(backstabber);
		scn.SkipToPhase(Phase.MANEUVER);

		// Celeborn, unwounded with 3 vitality, could exert
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertFalse(scn.FreepsActionAvailable(mod));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
