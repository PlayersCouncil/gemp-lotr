package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "13_5");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: Man companion, strength 8, vitality 4; a maneuver ability used only on purpose

					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9, no lurker; stronger underground only
					put("pursuer", "4_190");     // Uruk Pursuer: [isengard] Uruk-hai, strength 5, fierce; its texts are about unbound Hobbits
					put("column", "11_72");      // Column of Easterlings: [men] Man, strength 8, vitality 2, lurker; a skirmish ability used only on purpose
					put("strategist", "12_97");  // Orc Strategist: [orc] Orc, strength 11, lurker; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRaceTextScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "13_5");
					put("horror", "14_13");      // Horror of Harad: unique [orc] Half-troll, fierce and lurker among plain keywords, no other text
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc and no lurker; stronger underground only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_11"    // Race Text 94_11, the Shadow player's: "At the start of the assignment phase, the Shadow player may make a minion gain lurker until the regroup phase."
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Gimli, Lord of the Glittering Caves
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.<br>Gimli is strength +1 for each minion assigned to a skirmish.<br><b>Assignment:</b> Exert Gimli and spot a lurker minion to make that minion lose <b>lurker</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", gimli.getBlueprint().getTitle());
		assertEquals("Lord of the Glittering Caves", gimli.getBlueprint().getSubtitle());
		assertTrue(gimli.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gimli.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, gimli.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, gimli.getBlueprint().getCardType());
		assertEquals(Race.DWARF, gimli.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(2, gimli.getBlueprint().getTwilightCost());
		assertEquals(6, gimli.getBlueprint().getStrength());
		assertEquals(3, gimli.getBlueprint().getVitality());
		assertEquals(6, gimli.getBlueprint().getResistance());
	}

	@Test
	public void GimliIsStrengthPlusOneForEachMinionAssignedToASkirmishWhoeverItIsAssignedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var troop = scn.GetShadowCard("troop");
		var denizen = scn.GetShadowCard("denizen");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, denizen, pursuer);
		scn.SkipToAssignments();

		// Three minions in play, none of them assigned yet.
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));

		// A [moria] Orc on Gimli and an [orc] Orc on Aragorn; the [isengard] Uruk-hai is left over for now.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{aragorn, denizen});
		// 6 + 2 assigned
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.ShadowAssignToMinions(aragorn, pursuer);
		// 6 + 3 assigned
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignmentAbilityExertsGimliToMakeALurkerMinionLoseLurkerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var column = scn.GetShadowCard("column");
		var strategist = scn.GetShadowCard("strategist");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(column, strategist, denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
		assertTrue(scn.FreepsActionAvailable(gimli));
		scn.FreepsUseCardAction(gimli);
		// Lurker minions of two cultures and races; not a minion who is no lurker.
		assertTrue(scn.FreepsHasCardChoiceAvailable(column, strategist));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denizen));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(column);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.HasKeyword(column, Keyword.LURKER));
		assertTrue(scn.HasKeyword(strategist, Keyword.LURKER));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, column}, new PhysicalCardImpl[]{aragorn, strategist});
		scn.ShadowDeclineAssignments();

		// No longer a lurker, the Column's skirmish is resolved first.
		scn.FreepsResolveSkirmish(gimli);
		assertFalse(scn.HasKeyword(column, Keyword.LURKER));
		scn.PassSkirmishActions();
		// Gimli, 6 + 2 assigned, ties the Column, 8, and loses.
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.HasKeyword(column, Keyword.LURKER));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWithNoLurkerMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhileGimliIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.AddWoundsToChar(gimli, 2);

		var column = scn.GetShadowCard("column");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(column);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
	}

	@Test
	public void ALurkerLostIsNotGainedAgainByAnotherCardBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRaceTextScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var mod = scn.GetShadowCard("mod");
		var horror = scn.GetShadowCard("horror");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(horror, denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The control: with nothing in the way, the Race Text gives a minion lurker.
		assertFalse(scn.HasKeyword(denizen, Keyword.LURKER));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(denizen);
		assertTrue(scn.HasKeyword(denizen, Keyword.LURKER));

		assertTrue(scn.HasKeyword(horror, Keyword.LURKER));
		scn.FreepsUseCardAction(gimli);
		scn.FreepsChooseCard(horror);
		assertFalse(scn.HasKeyword(horror, Keyword.LURKER));

		// Nobody is assigned; the fierce Horror makes a fierce assignment phase, and the Race Text answers its start.
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(horror);

		// Lose beats gain (Q-0011): still no lurker.
		assertFalse(scn.HasKeyword(horror, Keyword.LURKER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
