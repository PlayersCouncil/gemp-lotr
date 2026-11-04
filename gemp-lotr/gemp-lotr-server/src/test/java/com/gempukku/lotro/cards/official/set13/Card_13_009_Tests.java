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

public class Card_13_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "13_9");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6; damage +1 and a skirmish ability used only on purpose
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion; +2 only at sites 6 to 8
					put("lorienelf", "1_53");   // Lórien Elf: Elf companion, strength 4, vitality 2, no text but its play requirement
					// Thrarin, Dwarven Smith: Dwarf ally; "Maneuver: Exert Thrarin to allow him to participate in archery
					// fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					put("savage2", "1_151");
					put("savage3", "1_151");
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its trigger is on being played
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: Eastemnet Gullies at site 1 is no forest, Derndingle at site 2 is a forest that is no sanctuary.
	protected VirtualTableScenario GetDerndingleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "13_9");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: [elven] Elf companion, resistance 6
					put("lorienelf", "1_53");   // Lórien Elf: [elven] Elf companion, resistance 6, no text but its play requirement
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally (resistance 0); a fellowship ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_330");      // Derndingle: Forest. Move limit +1 while an unbound Hobbit is there; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Rivendell Waterfall, a forest that is also a sanctuary, as site 3; site 4 (Balin's
	// Tomb) is underground.
	protected VirtualTableScenario GetWaterfallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "13_9");
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: [elven] Elf companion, resistance 6
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// Rivendell Waterfall: Forest, Sanctuary; move limit +1 for the turn while a ranger is spotted there (none is)
					put("site3", "1_342");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Arwen, Reflection of Lúthien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While the fellowship is at a forest site, each of your other [elven] companions is resistance +1.<br><b>Skirmish:</b> Exert Arwen to make a minion she is skirmishing strength -1 for each other companion assigned to a skirmish.
		*/

		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", arwen.getBlueprint().getTitle());
		assertEquals("Reflection of Lúthien", arwen.getBlueprint().getSubtitle());
		assertTrue(arwen.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, arwen.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, arwen.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, arwen.getBlueprint().getCardType());
		assertEquals(Race.ELF, arwen.getBlueprint().getRace());
		assertEquals(2, arwen.getBlueprint().getTwilightCost());
		assertEquals(6, arwen.getBlueprint().getStrength());
		assertEquals(3, arwen.getBlueprint().getVitality());
		assertEquals(7, arwen.getBlueprint().getResistance());
	}

	@Test
	public void AtDerndingleEachOtherElvenCompanionIsResistancePlus1AndNotAtASiteThatIsNoForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDerndingleScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf, aragorn);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();

		// Eastemnet Gullies is no forest: every resistance is as printed.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetResistance(arwen));
		assertEquals(6, scn.GetResistance(haldir));
		assertEquals(6, scn.GetResistance(lorienelf));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(celeborn));

		scn.FreepsPass();

		// At Derndingle the two other [elven] companions are +1; not Arwen herself, not the [gondor] Aragorn or
		// the [shire] Frodo, and not the [elven] ally.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetResistance(arwen));
		assertEquals(7, scn.GetResistance(haldir));
		assertEquals(7, scn.GetResistance(lorienelf));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(celeborn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtRivendellWaterfallAnotherElvenCompanionIsResistancePlus1UntilTheFellowshipMovesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWaterfallScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(arwen, haldir);

		scn.StartGame();
		assertEquals(6, scn.GetResistance(haldir));

		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetResistance(haldir));
		assertEquals(7, scn.GetResistance(arwen));

		// On to Balin's Tomb, which is no forest.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetResistance(haldir));
		assertEquals(7, scn.GetResistance(arwen));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsHerToMakeAMinionSheIsSkirmishingWeakerForEachOtherCompanionStillAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(arwen, aragorn, gimli, haldir, lorienelf);
		scn.MoveAlliesToTable(thrarin);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, savage3, runner1, runner2, runner3);

		// Thrarin, an ally, is made to take part in the skirmishes by his own ability.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();

		// Arwen fights an Uruk-hai and an Orc; Aragorn, the Lórien Elf, Gimli and Thrarin one minion each; Haldir
		// is not assigned.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage1},
				new PhysicalCardImpl[]{aragorn, savage2},
				new PhysicalCardImpl[]{lorienelf, runner2},
				new PhysicalCardImpl[]{gimli, savage3},
				new PhysicalCardImpl[]{thrarin, runner3});
		scn.ShadowAssignToMinions(arwen, runner1);

		// Gimli's skirmish is fought first: once it is over he is assigned to a skirmish no longer.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(savage3));
		assertEquals(5, scn.GetStrength(runner3));
		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsUseCardAction(arwen);

		// The two minions she is skirmishing; none of those assigned to the others.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, runner1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2, runner2, savage3, runner3));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage1);

		// Aragorn and the Lórien Elf are still assigned: 5 - 2.  Not Arwen herself, not Gimli (his skirmish is
		// over), not Haldir (unassigned), not Thrarin (an ally).
		assertEquals(3, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(runner1));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(runner2));
		assertEquals(5, scn.GetStrength(runner3));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileArwenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AddWoundsToChar(arwen, 2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.MoveMinionsToTable(savage1, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(arwen);

		// Vitality 3 with 2 wounds: she cannot exert.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage1));
	}

	@Test
	public void WithNoOtherCompanionAssignedSheMayStillExertAndTheMinionIsNotMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);

		// Aragorn is in play and not assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage1);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage1));
		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsUseCardAction(arwen);

		// The exertion is paid; there is no other companion to count.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetStrength(savage1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThePenaltyLastsForHerSkirmishOnlyAndUsedInAnotherSkirmishItReachesNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, savage1},
				new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(5, scn.GetStrength(savage1));
		scn.FreepsUseCardAction(arwen);
		// Aragorn is still assigned: 5 - 1.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(4, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Arwen (6) beats the Uruk Savage (4) without doubling it: it survives the wound.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		// The next skirmish is a phase of its own: the penalty is gone.
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(arwen));
		scn.FreepsUseCardAction(arwen);

		// Used in Aragorn's skirmish she pays, and neither his minion nor hers is one she is skirmishing.
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(savage1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
