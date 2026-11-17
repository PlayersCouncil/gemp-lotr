package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strategist", "12_97");
					put("horror", "14_13");      // Horror of Harad: unique [orc] Half-troll, lurker among plain keywords, no other text
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc and no lurker; stronger underground only
					put("column", "11_72");      // Column of Easterlings: [men] Man, lurker; a skirmish ability used only on purpose
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, no lurker, strength 5, vitality 1; its text asks for an underground site
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "11_8");        // Gimli, Lively Combatant: strength 6, vitality 3; a draw only when he wins
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
					put("strategist", "12_97");
					put("horror", "14_13");      // Horror of Harad: unique [orc] Half-troll, lurker among plain keywords, no other text
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc and no lurker; stronger underground only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_11"    // Race Text 94_11, the Shadow player's: "At the start of the assignment phase, the Shadow player may make a minion gain lurker until the regroup phase."
		);
	}

	@Test
	public void OrcStrategistStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Orc Strategist
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Maneuver:</b> Make an [orc] lurker minion lose <b>lurker</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var strategist = scn.GetFreepsCard("strategist");

		assertEquals("Orc Strategist", strategist.getBlueprint().getTitle());
		assertNull(strategist.getBlueprint().getSubtitle());
		assertFalse(strategist.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, strategist.getBlueprint().getSide());
		assertEquals(Culture.ORC, strategist.getBlueprint().getCulture());
		assertEquals(CardType.MINION, strategist.getBlueprint().getCardType());
		assertEquals(Race.ORC, strategist.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(strategist, Keyword.LURKER));
		assertEquals(4, strategist.getBlueprint().getTwilightCost());
		assertEquals(11, strategist.getBlueprint().getStrength());
		assertEquals(3, strategist.getBlueprint().getVitality());
		assertEquals(4, strategist.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityMakesAnOrcLurkerMinionLoseLurker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strategist = scn.GetShadowCard("strategist");
		var horror = scn.GetShadowCard("horror");
		var denizen = scn.GetShadowCard("denizen");
		var column = scn.GetShadowCard("column");

		scn.StartGame();
		scn.MoveMinionsToTable(strategist, horror, denizen, column);
		scn.FreepsPass();

		// Not in the Shadow phase...
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(strategist));

		// ...but in the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.HasKeyword(horror, Keyword.LURKER));
		assertTrue(scn.ShadowActionAvailable(strategist));
		scn.ShadowUseCardAction(strategist);
		// [orc] lurker minions of two races; not an [orc] minion who is no lurker, nor a lurker of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(strategist, horror));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(denizen, column));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(horror);

		assertFalse(scn.HasKeyword(horror, Keyword.LURKER));
		assertTrue(scn.HasKeyword(strategist, Keyword.LURKER));
		assertTrue(scn.HasKeyword(column, Keyword.LURKER));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void LurkerIsLostThroughTheSkirmishesAndBackInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var strategist = scn.GetShadowCard("strategist");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(strategist, goblin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.HasKeyword(strategist, Keyword.LURKER));
		// The only [orc] lurker minion is the Strategist himself.
		scn.ShadowUseCardAction(strategist);
		assertFalse(scn.HasKeyword(strategist, Keyword.LURKER));

		// With no [orc] lurker minion left, the ability would do nothing and is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(strategist));

		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, strategist}, new PhysicalCardImpl[]{aragorn, goblin});
		// No longer a lurker, his skirmish may be resolved first.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		// Gimli (6) loses to the Strategist (11).
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.HasKeyword(strategist, Keyword.LURKER));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(strategist, Keyword.LURKER));
	}

	@Test
	public void ALurkerLostIsNotGainedAgainByAnotherCardBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRaceTextScenario();

		var mod = scn.GetShadowCard("mod");
		var strategist = scn.GetShadowCard("strategist");
		var horror = scn.GetShadowCard("horror");

		scn.StartGame();
		scn.MoveMinionsToTable(strategist, horror);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(strategist);
		scn.ShadowChooseCard(horror);
		assertFalse(scn.HasKeyword(horror, Keyword.LURKER));

		// "At the start of the assignment phase, the Shadow player may make a minion gain lurker until the regroup phase."
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(horror);
		// Lose beats gain (Q-0011): still no lurker.
		assertFalse(scn.HasKeyword(horror, Keyword.LURKER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithoutTheStrategistsAbilityTheRaceTextGivesLurker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRaceTextScenario();

		var mod = scn.GetShadowCard("mod");
		var strategist = scn.GetShadowCard("strategist");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(strategist, denizen);
		assertFalse(scn.HasKeyword(denizen, Keyword.LURKER));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(denizen);
		assertTrue(scn.HasKeyword(denizen, Keyword.LURKER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
