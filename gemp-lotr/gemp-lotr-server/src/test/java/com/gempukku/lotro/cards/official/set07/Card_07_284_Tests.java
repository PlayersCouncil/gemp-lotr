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

public class Card_07_284_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "7_284");
					put("assassin2", "7_284");
					// Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; his text answers only a weapon played on him
					put("chieftain", "1_266");
					// Morgul Skulker: a [sauron] Orc, strength 7; his text answers his own play, and he is not played
					put("skulker", "1_258");
					// Cirith Ungol Sentry: a [sauron] minion who is no Orc (an Uruk-hai), strength 8, damage +1 and nothing else
					put("sentry", "10_84");
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [sauron] ([moria]), strength 13, no text
					// Goblin Runner: a [moria] Orc, strength 5; its text answers its own play only
					put("runner", "1_178");

					// Merry, From O'er the Brandywine: strength 3 (+2 only with a weapon), vitality 4
					put("merry", "1_303");
					// Pippin, Friend to Frodo: strength 3, vitality 4; his text is about [shire] tales
					put("pippin", "1_306");
					// Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; strength 3, vitality 4
					put("sam", "1_311");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man, strength 6, vitality 3, no text but Knight
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; his skirmish ability is never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion in no skirmish, to take threat wounds
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4; "Maneuver: Exert Thrarin to allow him to participate
					// in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOverwhelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "7_284");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, the companion overwhelmed
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion in no skirmish, to take the threat wound
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Response:</b> If a companion is overwhelmed during a skirmish involving a [sauron] Orc and threat wounds are about to be placed, remove a threat to assign this minion to the Ring-bearer (even if the Ring-bearer is already assigned).
		*/

		var scn = GetScenario();

		var assassin = scn.GetFreepsCard("assassin");

		assertEquals("Mordor Assassin", assassin.getBlueprint().getTitle());
		assertNull(assassin.getBlueprint().getSubtitle());
		assertFalse(assassin.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, assassin.getBlueprint().getSide());
		assertEquals(Culture.SAURON, assassin.getBlueprint().getCulture());
		assertEquals(CardType.MINION, assassin.getBlueprint().getCardType());
		assertEquals(Race.ORC, assassin.getBlueprint().getRace());
		assertEquals(3, assassin.getBlueprint().getTwilightCost());
		assertEquals(9, assassin.getBlueprint().getStrength());
		assertEquals(3, assassin.getBlueprint().getVitality());
		assertEquals(6, assassin.getBlueprint().getSiteNumber());
	}

	@Test
	public void ResponseRemovesAThreatToAssignTheAssassinToTheRingBearerBeforeThreatWoundsArePlaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOverwhelmScenario();

		var assassin = scn.GetShadowCard("assassin");
		var troop = scn.GetShadowCard("troop");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);

		scn.StartGame();
		// Three companions hold the 2 threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(assassin, troop);
		scn.SkipToAssignments();
		// The Free Peoples player may give Gimli one minion; the Shadow player gives him the one left over.
		scn.FreepsAssignToMinions(gimli, assassin);
		scn.ShadowAssignToMinions(gimli, troop);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.IsCharAssigned(frodo));
		scn.PassSkirmishActions();

		// 9 + 13 against 6: Gimli is overwhelmed and killed, and the 2 threats are about to become wounds.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));

		scn.ShadowChooseOptionalTrigger(assassin);
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		// One threat was the cost, paid before the threat wounds (CR 4.2 on Mordor Assassin), so one wound is
		// left to place and not two: the Free Peoples player puts it on either companion still alive.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// No second wound is asked for.  The next thing is the Ring-bearer's skirmish against the Assassin.
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ResponseAssignsTheAssassinFromOutOfAnySkirmishToTheRingBearerAndNotToAnotherRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var chieftain = scn.GetShadowCard("chieftain");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, sam, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, assassin2, chieftain);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		// Both copies are left out of every skirmish.
		scn.FreepsAssignToMinions(merry, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);

		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(assassin));
		assertFalse(scn.IsCharAssigned(assassin2));
		scn.PassSkirmishActions();

		// 7 against 3: Merry is overwhelmed in a skirmish involving a [sauron] Orc, with threats on the dead pile.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin2));
		scn.ShadowChooseOptionalTrigger(assassin);
		assertEquals(1, scn.GetThreats());
		// The copy whose response it was, and no other.
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		assertFalse(scn.IsCharAssigned(assassin2));
		assertFalse(scn.IsCharAssigned(sam));

		// Offered again for the same death while a threat is left to pay with (CR 4.2 "response"); declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowDeclineOptionalTrigger();

		// The threat left becomes one wound.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EvenWithTheRingBearerAlreadyAssignedTheAssassinIsAssignedToHimBesideHisMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var skulker = scn.GetShadowCard("skulker");
		var runner = scn.GetShadowCard("runner");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, chieftain, skulker, runner);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, chieftain}, new PhysicalCardImpl[]{frodo, runner});
		// The Skulker is the leftover minion the Shadow player gives the Knight; the Assassin stays out.
		scn.ShadowAssignToMinions(knight, skulker);
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertFalse(scn.IsCharAssigned(assassin));
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();

		// 7 + 7 against 6: the Knight, a Man, is overwhelmed.
		assertInZone(Zone.DEAD, knight);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowChooseOptionalTrigger(assassin);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));

		scn.ShadowDeclineOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	/**
	 * Q-0362: B (Ketura, 2026-10-28). CR 4.2's "both of them are unassigned" binds **Assignment:** special abilities;
	 * this is a response, so an Assassin waiting in another companion's skirmish is taken from it to the Ring-bearer.
	 */
	@Test
	public void AnAssassinAlreadyAssignedToAnotherCompanionIsTakenFromHimToTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, chieftain);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, chieftain}, new PhysicalCardImpl[]{pippin, assassin});
		assertTrue(scn.IsCharAssignedAgainst(pippin, assassin));
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();

		// Merry is overwhelmed by the Chieftain.  The Assassin, waiting to skirmish Pippin, leaves that skirmish for
		// the Ring-bearer's, and Pippin is left with no minion.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowChooseOptionalTrigger(assassin);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsCharAssignedAgainst(frodo, assassin));
		assertFalse(scn.IsCharAssignedAgainst(pippin, assassin));
		assertFalse(scn.IsCharAssigned(pippin));
		// Offered again while a threat is left and the Chieftain is in the skirmish.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
	}

	@Test
	public void IsNotOfferedWhenTheCompanionIsKilledByAWoundAndNotOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		// One wound short of death.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, chieftain);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// 7 against 6 is no overwhelm: Gimli dies of the wound for losing, and the threat becomes a wound.
		assertInZone(Zone.DEAD, gimli);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAnAllyIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, assassin);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();

		// 9 against 4: the ally is overwhelmed in a skirmish involving a [sauron] Orc, and the threat is to become a
		// wound; but he is no companion.
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOverwhelmingSkirmishHasASauronUrukHaiOrAMoriaOrcAndNoSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, sentry, troop);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, sentry}, new PhysicalCardImpl[]{pippin, troop});
		scn.ShadowDeclineAssignments();

		// 8 against 3: Merry is overwhelmed by a [sauron] minion who is no Orc.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// 13 against 3: Pippin is overwhelmed by an Orc who is not [sauron].
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheAssassinUnassignedAndBothThreatsBecomeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, chieftain);
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, chieftain);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowDeclineOptionalTrigger();

		// Both threats become wounds, and nobody is assigned to the Ring-bearer.
		assertInZone(Zone.DEAD, merry);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(assassin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoThreatOnTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var chieftain = scn.GetShadowCard("chieftain");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, chieftain);
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, merry);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
