package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aid", "6_9");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: a [dwarven] Dwarf companion who is no Gimli, strength 5, vitality 3; +2 only against an Orc
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6; +3 only against a Nazgul
					put("lorien", "1_53");      // Lorien Elf: Elf companion, strength 4, no text in play
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an Elf ally, strength 6; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf, strength 8; a maneuver ability used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LendUsYourAidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Lend Us Your Aid
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert Gimli twice to make him and each Elf companion strength +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var aid = scn.GetFreepsCard("aid");

		assertEquals("Lend Us Your Aid", aid.getBlueprint().getTitle());
		assertNull(aid.getBlueprint().getSubtitle());
		assertFalse(aid.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, aid.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, aid.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, aid.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(aid, Timeword.MANEUVER));
		assertEquals(0, aid.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGimliTwiceToMakeHimAndEachElfCompanionStrengthPlus3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aid = scn.GetFreepsCard("aid");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var arwen = scn.GetFreepsCard("arwen");
		var lorien = scn.GetFreepsCard("lorien");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(gimli, farin, arwen, lorien, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(aid);

		scn.StartGame();
		assertInHand(aid);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorien));
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(farin));
		assertTrue(scn.FreepsPlayAvailable(aid));
		scn.FreepsPlayCard(aid);

		// Gimli exerts twice; the other Dwarf companion does not.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertInDiscard(aid);
		// 6 + 3 for Gimli, and for each Elf companion of either title.
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(lorien));
		// Not an Elf who is no companion, a companion who is no Elf, or another Dwarf.
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(farin));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{arwen, troop2});

		// 13 against 8: Aragorn's skirmish is over first.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Still +3 in the skirmish after it, the last before the regroup phase.
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(9, scn.GetStrength(arwen));
		assertEquals(7, scn.GetStrength(lorien));

		// 13 against 9: Arwen loses too.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(arwen));

		// Gone at the first regroup decision.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(4, scn.GetStrength(lorien));
	}

	@Test
	public void CannotBePlayedWhenGimliCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aid = scn.GetFreepsCard("aid");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(gimli, farin, arwen);
		scn.MoveCardsToHand(aid);

		scn.StartGame();
		assertInHand(aid);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 3 with 1 wound: Gimli can exert once, not twice.  Farin could exert twice, and is no Gimli.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(aid));

		// With that wound gone, the same board offers it.
		scn.RemoveWoundsFromChar(gimli, 1);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(aid));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(6, scn.GetStrength(arwen));
		assertInHand(aid);
	}
}
