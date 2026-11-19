package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_200_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spawn", "7_200");
					put("spawn2", "7_200");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, cost 4, site 3, not fierce; her text
					// is about companions bearing [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider]; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc who is no Nazgûl; its text needs 3 burdens
					put("creeper", "8_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					// Morgul Regiment: "For each Nazgûl you can spot, the Free Peoples player must exert a companion
					// to assign this minion to a skirmish."  Another minion with a price of its own.
					put("regiment", "7_197");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf, vitality 3
					// Bilbo, Retired Adventurer: a [shire] ally, vitality 3, home site 3; he can exert, and is no companion
					put("bilbo", "1_284");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSpawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Spawn
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, the Free Peoples player must exert a companion to assign this minion to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spawn");

		assertEquals("Morgul Spawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}


	@Test
	public void MustExertToAssignMorgulSpawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Nazgûl arrives by play, beside a Spawn already there.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);
		scn.ShadowPass();

		scn.SkipToAssignments();

		// The price is offered as he starts assigning, ahead of it (Q-0283).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseOptionalTrigger(spawn);

		// The one companion there is exerts, with nothing to choose, and the Spawn is then his to assign.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsAssignToMinions(frodo, spawn);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn));
		// The Nazgûl is left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// The price is the Free Peoples player's, and is paid only for an assignment he makes.  There is no question
	// to decline: he declines by leaving the Spawn unassigned.
	@Test
	public void LeavingMorgulSpawnUnassignedCostsNothingAndTheShadowPlayerAssignsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.SkipToAssignments();

		// He is offered to pay for the Spawn, and passes: his assignment lists both companions and the Nazgûl, and
		// not the Spawn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsDeclineAssignments();

		// He paid for nothing, so nobody exerts.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The Shadow player's assignment lists the Spawn, and he assigns it with nobody exerting.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		scn.ShadowAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	// With a second companion beside the Ring-bearer, which of them exerts is the Free Peoples player's to choose,
	// asked once he has assigned the Spawn and before anything is assigned.
	@Test
	public void ExertingACompanionOfHisChoiceLetsTheFreePeoplesPlayerAssignMorgulSpawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya, troop);
		scn.SkipToAssignments();

		// The Spawn is offered to be paid for, the Nazgûl and the Troop are not, and nobody has exerted.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lemenya));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(troop));
		scn.FreepsChooseOptionalTrigger(spawn);

		// "Exert a companion": either of the two, and one of them must; Bilbo, an ally who could exert, is not
		// asked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// His assignment then lists the Spawn beside the Nazgûl and the Troop.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spawn}, new PhysicalCardImpl[]{frodo, troop});

		// One exertion was the whole price: the Troop, assigned in the same answer, cost nothing.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, troop));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		// The Nazgûl is left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// "A Nazgûl" is a race: Khamûl is a [raider] minion, and the price is owed beside him alone.
	@Test
	public void ANazgulOfAnotherCultureIsSpottedAndEitherCompanionMayExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, khamul);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		scn.FreepsChooseOptionalTrigger(spawn);

		// The companion who exerts need not be the one the Spawn is assigned to.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(frodo);
		scn.FreepsAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// "While you can spot a Nazgûl" is no "for each Nazgûl": two of them, of two cultures, ask one exertion and no more.
	@Test
	public void WithTwoNazgulSpottedMorgulSpawnStillCostsOneExertionAndNotTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya, khamul);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn));
		scn.FreepsChooseOptionalTrigger(spawn);

		// The one exertion, by either companion; a second would be asked before his assigning.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The Nazgûl are left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// "While you can spot a Nazgûl": with none, beside [wraith] Orcs who are none, the Spawn is assigned like any
	// other minion.
	@Test
	public void WithNoNazgulToSpotMorgulSpawnIsAssignedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, creeper);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spawn}, new PhysicalCardImpl[]{frodo, creeper});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, creeper));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	// The price is each minion's own: a Morgul Spawn the Free Peoples player assigns is paid for by itself, and
	// neither a second Spawn nor a Morgul Regiment that he leaves alone costs him anything.
	@Test
	public void EachMorgulSpawnTheFreePeoplesPlayerAssignsIsPaidForByItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var spawn1 = scn.GetShadowCard("spawn");
		var spawn2 = scn.GetShadowCard("spawn2");
		var regiment = scn.GetShadowCard("regiment");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn1, spawn2, regiment, lemenya);
		scn.SkipToAssignments();

		// Each of the three that charge is offered to be paid for; the Nazgûl is not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(lemenya));

		// He pays for one Spawn: one exertion, of his choice, and no more.  The other two are offered again, and he
		// passes.
		scn.FreepsChooseOptionalTrigger(spawn2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(spawn2));
		scn.FreepsDeclineOptionalTrigger();

		// The Spawn paid for and the Nazgûl are his to assign; the two not paid for are not.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spawn2));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spawn1));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(regiment));
		scn.FreepsAssignToMinions(aragorn, spawn2);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn2));
		assertFalse(scn.IsCharAssigned(spawn1));
		assertFalse(scn.IsCharAssigned(regiment));

		// The Spawn and the Regiment he left alone are the Shadow player's to assign, for nothing.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn1));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(regiment));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(spawn2));
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{frodo, spawn1}, new PhysicalCardImpl[]{gimli, regiment});
		assertTrue(scn.IsCharAssignedAgainst(frodo, spawn1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, regiment));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssigningBothMorgulSpawnCostsAnExertionForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var spawn1 = scn.GetShadowCard("spawn");
		var spawn2 = scn.GetShadowCard("spawn2");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn1, spawn2, lemenya);
		scn.SkipToAssignments();

		// One exertion for each, paid for one after the other, and the same companion may make both.
		scn.FreepsChooseOptionalTrigger(spawn1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(spawn2));
		scn.FreepsChooseOptionalTrigger(spawn2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spawn1}, new PhysicalCardImpl[]{gimli, spawn2});

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, spawn2));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void WithEveryCompanionExhaustedMorgulSpawnIsNotTheFreePeoplesPlayersToAssignOnceANazgulIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spawn = scn.GetShadowCard("spawn");
		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, troop);
		// Both companions (vitality 4) are exhausted.  Bilbo, an ally, could exert, and is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// With no Nazgûl to spot there is no price, and the Spawn is the Free Peoples player's to assign.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.FreepsCanAssign(spawn));

		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.FreepsCanAssign(spawn));
		scn.ShadowPass();

		scn.SkipToAssignments();

		// His assignment lists the Troop and the Nazgûl, and not the Spawn he cannot pay for.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spawn));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, spawn));
		scn.FreepsDeclineAssignments();

		// Nobody exerted, and the Spawn is the Shadow player's to assign, for nothing.
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spawn));
		scn.ShadowAssignToMinions(aragorn, spawn);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spawn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
