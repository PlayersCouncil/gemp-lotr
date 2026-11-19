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

public class Card_12_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "12_144");

					// Intimidating Uruk: [uruk-hai] Uruk-hai, strength 10, vitality 3, damage +1; its text answers only its
					// play at a battleground, and it is cheated in.
					put("intimidating", "11_189");
					// Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, damage +1, toil 1 (a play cost only).
					put("tyrannical", "11_204");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 and no other text.
					put("guard", "10_81");
					// Dark Alliance: an [uruk-hai] condition in the support area; a Response to a Free Peoples special
					// ability, and none is used.
					put("alliance", "12_141");
					// Goblin Patrol Troop: strength 13, no text; overwhelms anyone of strength 6 or less.
					put("troop1", "1_177");
					put("troop2", "1_177");
					// Goblin Runner: strength 5, vitality 1; a play trigger, and it is cheated in.
					put("runner", "1_178");
					// Saruman's Staff, Wizard's Device: plays on Saruman; fierce, damage +1, and a Maneuver ability that
					// makes the first sentence of Saruman's game text not apply.
					put("staff", "4_174");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: a [dwarven] Dwarf, strength 6, vitality 3; a skirmish ability, never used.
					put("gimli", "1_13");
					// Arwen, Daughter of Elrond: an [elven] Elf, strength 6 (+3 only against a Nazgûl).
					put("arwen", "1_30");
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2.  "Maneuver: Exert Thrarin to allow him
					// to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "12_144");
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, no text but its requirement to play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("despair", "7_148");    // Fierce in Despair: a Shadow Regroup event, twilight 7: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)."
					put("axeman1", "4_224");    // Easterling Axeman: a [raider] Man minion; his text needs 2 burdens
					put("axeman2", "4_224");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Saruman, Agent of the Dark Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Skirmish:</b> Exert Saruman to make an [uruk-hai] minion strength +2.<br>Each time a companion is killed, you may heal Saruman.
		*/

		var scn = GetScenario();

		var saruman = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", saruman.getBlueprint().getTitle());
		assertEquals("Agent of the Dark Lord", saruman.getBlueprint().getSubtitle());
		assertTrue(saruman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, saruman.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, saruman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, saruman.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, saruman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(saruman, Keyword.LURKER));
		assertEquals(4, saruman.getBlueprint().getTwilightCost());
		assertEquals(8, saruman.getBlueprint().getStrength());
		assertEquals(4, saruman.getBlueprint().getVitality());
		assertEquals(4, saruman.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsHimToMakeAnUrukHaiMinionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var saruman = scn.GetShadowCard("saruman");
		var intimidating = scn.GetShadowCard("intimidating");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var guard = scn.GetShadowCard("guard");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, intimidating, tyrannical, guard);
		assertInZone(Zone.SUPPORT, alliance);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, intimidating}, new PhysicalCardImpl[]{gimli, tyrannical});
		// Saruman and the Guard are left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(14, scn.GetStrength(tyrannical));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		// Any [uruk-hai] minion: Uruk-hai in this skirmish or another, and Saruman himself, a Wizard.  Not the [sauron]
		// Uruk-hai, nor the [uruk-hai] condition, which is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(intimidating, tyrannical, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, alliance));
		scn.ShadowChooseCard(tyrannical);

		// Tyrannical Uruk, whose own skirmish is still to come.
		assertEquals(16, scn.GetStrength(tyrannical));
		assertEquals(10, scn.GetStrength(intimidating));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The bonus lasts the skirmish it was used in; each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(14, scn.GetStrength(tyrannical));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var intimidating = scn.GetShadowCard("intimidating");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, intimidating);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intimidating);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
		assertEquals(10, scn.GetStrength(intimidating));
	}

	@Test
	public void EachTimeACompanionIsKilledHeMayHealHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(gimli, arwen);

		var saruman = scn.GetShadowCard("saruman");
		var intimidating = scn.GetShadowCard("intimidating");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, intimidating, troop1, troop2);
		scn.AddWoundsToChar(saruman, 2);
		// Another wounded minion, which the heal does not reach.
		scn.AddWoundsToChar(intimidating, 1);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(intimidating));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop1}, new PhysicalCardImpl[]{arwen, troop2});
		scn.ShadowDeclineAssignments();

		// A Dwarf overwhelmed: 13 is more than twice 6.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(intimidating));

		// An Elf overwhelmed the same way: offered again.
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, arwen);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(intimidating));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesHisWoundsOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1);
		scn.AddWoundsToChar(saruman, 2);
		assertEquals(2, scn.GetWoundsOn(saruman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(saruman));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenACompanionIsKilledAndHeHasNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1);
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// The heal would do nothing: not offered.
		assertInZone(Zone.DEAD, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyKilledAndAMinionKilledDoNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		var saruman = scn.GetShadowCard("saruman");
		var troop1 = scn.GetShadowCard("troop1");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, troop1, runner);
		scn.AddWoundsToChar(saruman, 2);
		assertEquals(2, scn.GetWoundsOn(saruman));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, troop1}, new PhysicalCardImpl[]{aragorn, runner});
		scn.ShadowDeclineAssignments();

		// An ally overwhelmed: 13 is more than twice 4.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(2, scn.GetWoundsOn(saruman));

		// A minion killed: the Goblin Runner (5, vitality 1) loses to Aragorn (8) and dies of the wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionDiscardedFromPlayIsNotKilledAndDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var despair = scn.GetShadowCard("despair");
		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman1, axeman2);
		scn.MoveCardsToHand(despair);
		assertInHand(despair);
		scn.SkipToPhase(Phase.REGROUP);
		// Laid after the skirmishes, so that nothing on the way answers them.
		scn.MoveMinionsToTable(saruman);
		scn.AddWoundsToChar(saruman, 2);
		assertEquals(2, scn.GetWoundsOn(saruman));
		// Fierce in Despair costs 7.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsDeadCount());
		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, aragorn));
		scn.ShadowChooseCard(guard);

		// Discarded from play is not killed (CR 4.2 "killed"): the dead pile stays empty and nothing is offered.
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(saruman));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SarumansStaffHasNoEffectOnHimAndItsManeuverAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.AttachCardsTo(saruman, staff);
		assertEquals(saruman, staff.getAttachedTo());
		assertInZone(Zone.ATTACHED, staff);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Legacy Ruling 6: the Staff's ability "does not have any effect when used on Saruman, Agent of the Dark Lord".
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(staff));
		assertTrue(scn.HasKeyword(saruman, Keyword.LURKER));
	}
}
