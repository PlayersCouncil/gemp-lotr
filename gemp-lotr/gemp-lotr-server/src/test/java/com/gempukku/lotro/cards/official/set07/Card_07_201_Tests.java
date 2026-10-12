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

public class Card_07_201_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spearman", "7_201");
					put("spearman2", "7_201");
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
	public void MorgulSpearmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Spearman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, the Free Peoples player must exert a companion to assign this minion to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spearman");

		assertEquals("Morgul Spearman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForTwoAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		scn.MoveCardsToHand(spearman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, spearman);
		assertTrue(scn.ShadowPlayAvailable(spearman));

		scn.ShadowPlayCard(spearman);

		// 2 to play + 2 roaming: its site number is 4 and the fellowship is at site 2
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, spearman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}


	@Test
	public void MustExertToAssignMorgulSpearman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Nazgûl arrives by play, beside a Spearman already there.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);
		scn.ShadowPass();

		scn.SkipToAssignments();

		// The price is paid as the Spearman is assigned, and nothing is asked ahead of it.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman));

		scn.FreepsAssignToMinions(frodo, spearman);

		// The one companion there is exerts, with nothing to choose, and the Spearman is assigned.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, spearman));
		// The Nazgûl is left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// The price is the Free Peoples player's, and is paid only for an assignment he makes.  There is no question
	// to decline: he declines by leaving the Spearman unassigned.
	@Test
	public void LeavingMorgulSpearmanUnassignedCostsNothingAndTheShadowPlayerAssignsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya);
		scn.SkipToAssignments();

		// Nothing is asked ahead; his assignment lists both companions, the Nazgûl and the Spearman.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman));
		scn.FreepsDeclineAssignments();

		// He assigned nothing, so nobody exerts.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The Shadow player's assignment lists the Spearman, and he assigns it with nobody exerting.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spearman));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(lemenya));
		scn.ShadowAssignToMinions(aragorn, spearman);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	// With a second companion beside the Ring-bearer, which of them exerts is the Free Peoples player's to choose,
	// asked once he has assigned the Spearman and before anything is assigned.
	@Test
	public void ExertingACompanionOfHisChoiceLetsTheFreePeoplesPlayerAssignMorgulSpearman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya, troop);
		scn.SkipToAssignments();

		// His assignment lists the Spearman beside the Nazgûl and the Troop, and nobody has exerted.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman}, new PhysicalCardImpl[]{frodo, troop});

		// "Exert a companion": either of the two, and one of them must; Bilbo, an ally who could exert, is not
		// asked.  Nothing is assigned until it is paid.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsCharAssigned(spearman));
		assertFalse(scn.IsCharAssigned(troop));
		scn.FreepsChooseCard(aragorn);

		// One exertion was the whole price: the Troop, assigned in the same answer, cost nothing.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
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

		var spearman = scn.GetShadowCard("spearman");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, khamul);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman));
		scn.FreepsAssignToMinions(aragorn, spearman);

		// The companion who exerts need not be the one the Spearman is assigned to.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(frodo);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// "While you can spot a Nazgûl" is no "for each Nazgûl": two of them, of two cultures, ask one exertion and no more.
	@Test
	public void WithTwoNazgulSpottedMorgulSpearmanStillCostsOneExertionAndNotTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, lemenya, khamul);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman));
		scn.FreepsAssignToMinions(aragorn, spearman);

		// The one exertion, by either companion; a second would be asked before anything is assigned.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertFalse(scn.IsCharAssigned(spearman));
		scn.FreepsChooseCard(frodo);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The Nazgûl are left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// "While you can spot a Nazgûl": with none, beside [wraith] Orcs who are none, the Spearman is assigned like any
	// other minion.
	@Test
	public void WithNoNazgulToSpotMorgulSpearmanIsAssignedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, creeper);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman}, new PhysicalCardImpl[]{frodo, creeper});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
		assertTrue(scn.IsCharAssignedAgainst(frodo, creeper));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	// The price is each minion's own: a Morgul Spearman the Free Peoples player assigns is paid for by itself, and
	// neither a second Spearman nor a Morgul Regiment that he leaves alone costs him anything.
	@Test
	public void EachSpearmanTheFreePeoplesPlayerAssignsIsPaidForByItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var spearman1 = scn.GetShadowCard("spearman");
		var spearman2 = scn.GetShadowCard("spearman2");
		var regiment = scn.GetShadowCard("regiment");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman1, spearman2, regiment, lemenya);
		scn.SkipToAssignments();

		// Nothing is asked ahead, and all of them are his to assign beside the Nazgûl.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman1));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(spearman2));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));

		// He assigns one Spearman: one exertion, of his choice, and no more.
		scn.FreepsAssignToMinions(aragorn, spearman2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman2));
		assertFalse(scn.IsCharAssigned(spearman1));
		assertFalse(scn.IsCharAssigned(regiment));

		// The Spearman and the Regiment he left alone are the Shadow player's to assign, for nothing.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spearman1));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(regiment));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(spearman2));
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{frodo, spearman1}, new PhysicalCardImpl[]{gimli, regiment});
		assertTrue(scn.IsCharAssignedAgainst(frodo, spearman1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, regiment));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AssigningBothSpearmenCostsAnExertionForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var spearman1 = scn.GetShadowCard("spearman");
		var spearman2 = scn.GetShadowCard("spearman2");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman1, spearman2, lemenya);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, spearman1}, new PhysicalCardImpl[]{gimli, spearman2});

		// One exertion for each, asked for one after the other; neither is assigned until both are settled.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertFalse(scn.IsCharAssigned(spearman1));
		assertFalse(scn.IsCharAssigned(spearman2));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertFalse(scn.IsCharAssigned(spearman1));
		assertFalse(scn.IsCharAssigned(spearman2));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman1));
		assertTrue(scn.IsCharAssignedAgainst(gimli, spearman2));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	@Test
	public void WithEveryCompanionExhaustedMorgulSpearmanIsNotTheFreePeoplesPlayersToAssignOnceANazgulIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spearman = scn.GetShadowCard("spearman");
		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(spearman, troop);
		// Both companions (vitality 4) are exhausted.  Bilbo, an ally, could exert, and is no companion.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// With no Nazgûl to spot there is no price, and the Spearman is the Free Peoples player's to assign.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.FreepsCanAssign(spearman));

		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.FreepsCanAssign(spearman));
		scn.ShadowPass();

		scn.SkipToAssignments();

		// His assignment lists the Troop and the Nazgûl, and not the Spearman he cannot pay for.
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(spearman));
		assertFalse(scn.FreepsCanAssignAgainst(aragorn, spearman));
		scn.FreepsDeclineAssignments();

		// Nobody exerted, and the Spearman is the Shadow player's to assign, for nothing.
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(spearman));
		scn.ShadowAssignToMinions(aragorn, spearman);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, spearman));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
