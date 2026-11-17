package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dunlending", "13_96");
					put("dunlending2", "13_96");

					// Footman of Dunland: a [men] Man, strength 7, vitality 3.  Its text makes the current site a plains,
					// which nothing here reads.
					put("footman", "11_82");
					// Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3, vitality 4.  A maneuver and a
					// regroup ability, never used.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");
					// Goaded to War: a [men] condition, no minion.  Its trigger answers a move to a battleground or
					// plains, and nobody moves after it is laid.
					put("goaded", "12_67");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3.  A skirmish ability, never used.
					put("gimli", "1_13");
					// Boromir, Son of Denethor: a [gondor] Man, strength 7, vitality 3.  A skirmish ability, never used.
					put("boromir", "1_97");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2.  His maneuver ability lets him take
					// part in skirmishes until the regroup phase.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MercilessDunlendingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Merciless Dunlending
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot another [men] minion, this minion is strength +2 for each companion assigned to a skirmish.
		*/

		var scn = GetScenario();

		var dunlending = scn.GetFreepsCard("dunlending");

		assertEquals("Merciless Dunlending", dunlending.getBlueprint().getTitle());
		assertNull(dunlending.getBlueprint().getSubtitle());
		assertFalse(dunlending.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, dunlending.getBlueprint().getSide());
		assertEquals(Culture.MEN, dunlending.getBlueprint().getCulture());
		assertEquals(CardType.MINION, dunlending.getBlueprint().getCardType());
		assertEquals(Race.MAN, dunlending.getBlueprint().getRace());
		assertEquals(3, dunlending.getBlueprint().getTwilightCost());
		assertEquals(8, dunlending.getBlueprint().getStrength());
		assertEquals(2, dunlending.getBlueprint().getVitality());
		assertEquals(4, dunlending.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAMenManBesideItEachCompanionAssignedIsPlusTwoAndNoAllyOrUnassignedCompanionCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(thrarin);

		var dunlending = scn.GetShadowCard("dunlending");
		var footman = scn.GetShadowCard("footman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending, footman, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		// Nobody is assigned yet: the printed 8.
		assertEquals(8, scn.GetStrength(dunlending));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, dunlending},
				new PhysicalCardImpl[]{gimli, footman},
				new PhysicalCardImpl[]{thrarin, brigand});

		// Aragorn and Gimli: 8 + 2 + 2.  Thrarin is an ally, and Boromir is not assigned.  The Footman keeps his 7.
		assertEquals(12, scn.GetStrength(dunlending));
		assertEquals(7, scn.GetStrength(footman));

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(12, scn.GetStrength(dunlending));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli's skirmish is over and he is assigned no longer: Aragorn alone, 8 + 2.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(10, scn.GetStrength(dunlending));
	}

	@Test
	public void AMenHobbitBesideItAlsoSwitchesItOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var dunlending = scn.GetShadowCard("dunlending");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending, ted);
		scn.SkipToAssignments();
		assertEquals(8, scn.GetStrength(dunlending));
		scn.FreepsAssignToMinions(aragorn, dunlending);
		// Ted is left over, and stays so.
		scn.ShadowDeclineAssignments();

		// Aragorn alone is assigned: 8 + 2.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(10, scn.GetStrength(dunlending));
		assertEquals(3, scn.GetStrength(ted));
	}

	@Test
	public void TwoCopiesSpotEachOtherAndEachIsStrongerOnlyByItsOwnBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var dunlending = scn.GetShadowCard("dunlending");
		var dunlending2 = scn.GetShadowCard("dunlending2");

		scn.StartGame();
		scn.MoveMinionsToTable(dunlending, dunlending2);
		scn.SkipToAssignments();
		assertEquals(8, scn.GetStrength(dunlending));
		assertEquals(8, scn.GetStrength(dunlending2));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, dunlending},
				new PhysicalCardImpl[]{gimli, dunlending2});

		// Each is the other's [men] minion.  Two companions assigned: 8 + 2 + 2 each, not the other's bonus as well.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(12, scn.GetStrength(dunlending));
		assertEquals(12, scn.GetStrength(dunlending2));
	}

	@Test
	public void WithNoOtherMenMinionToSpotItKeepsItsPrintedStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var dunlending = scn.GetShadowCard("dunlending");
		var brigand = scn.GetShadowCard("brigand");
		var goaded = scn.GetShadowCard("goaded");

		scn.StartGame();
		// Beside it: a [dunland] Man and a [men] card that is no minion.
		scn.MoveCardsToSupportArea(goaded);
		scn.MoveMinionsToTable(dunlending, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, dunlending},
				new PhysicalCardImpl[]{gimli, brigand});

		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(8, scn.GetStrength(dunlending));
	}
}
