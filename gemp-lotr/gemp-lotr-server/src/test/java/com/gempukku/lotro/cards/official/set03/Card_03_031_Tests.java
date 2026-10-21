package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depart", "3_31");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in);
					// his skirmish ability is used only on purpose.
					put("pippin", "103_31");
					// Bounder: [shire] Hobbit ally, home site 2 of the Fellowship path, where either player may assign
					// him; his skirmish ability is used only on purpose.
					put("bounder", "1_286");
					// Ted Sandyman, Chief's Men's Ally: the Shadow player's Hobbit, a minion; his abilities are used only
					// on purpose (the maneuver one needs a site he controls).
					put("ted", "18_75");
					put("runner", "1_178");     // Goblin Runner: a minion who is no Hobbit; its trigger is on being played
					put("runner2", "1_178");
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSnapshotScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depart", "3_31");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus only
					// Eomer, Eored Leader: "Maneuver: If you can spot more minions than companions, exert Eomer to play a
					// [rohan] companion from your draw deck."
					put("eomer", "12_112");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, played from the draw deck by Eomer; his texts
					// answer exertions of other [rohan] Men once he is in play, and a skirmish, used only on purpose.
					put("merry", "103_85");
					put("runner", "1_178");     // Goblin Runner: a minion that does nothing once in play
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Tower block: the same site numbers, but not Fellowship block sites.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("depart", "3_31");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus only
					// Pippin, Trollslayer: a [gondor] Hobbit companion (to play, spot a [gondor] companion: cheated in);
					// his skirmish ability is used only on purpose.
					put("pippin", "103_31");
					put("runner", "1_178");     // Goblin Runner: a minion who is no Hobbit; its trigger is on being played
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DepartSilentlyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Depart Silently
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Stealth</b>.<br><b>Maneuver:</b> At sites 1 to 5, spot Gandalf to prevent Hobbits from being assigned to skirmishes until the regroup phase. At any other site, spot Gandalf to make a Hobbit strength +3 until the regroup phase.
		*/

		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");

		assertEquals("Depart Silently", depart.getBlueprint().getTitle());
		assertNull(depart.getBlueprint().getSubtitle());
		assertFalse(depart.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, depart.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, depart.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, depart.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(depart, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(depart, Timeword.MANEUVER));
		assertEquals(1, depart.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite2NoHobbitOfAnyCultureOrSideCanBeAssignedAndNoHobbitIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(depart);
		assertInHand(depart);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		// Before the event, every one of them could be assigned, by either player.
		assertTrue(scn.FreepsCanAssign(frodo));
		assertTrue(scn.FreepsCanAssign(pippin));
		assertTrue(scn.FreepsCanAssign(bounder));
		assertTrue(scn.FreepsCanAssign(ted));
		assertTrue(scn.ShadowCanAssign(frodo));
		assertTrue(scn.ShadowCanAssign(bounder));
		int frodoStrength = scn.GetStrength(frodo);
		int pippinStrength = scn.GetStrength(pippin);
		int bounderStrength = scn.GetStrength(bounder);
		int tedStrength = scn.GetStrength(ted);
		assertTrue(scn.FreepsPlayAvailable(depart));

		scn.FreepsPlayCard(depart);
		assertInDiscard(depart);

		// Hobbits of either player, companions and the ally at his home site alike, cannot be assigned.
		assertFalse(scn.FreepsCanAssign(frodo));
		assertFalse(scn.FreepsCanAssign(pippin));
		assertFalse(scn.FreepsCanAssign(bounder));
		assertFalse(scn.FreepsCanAssign(ted));
		assertFalse(scn.ShadowCanAssign(frodo));
		assertFalse(scn.ShadowCanAssign(pippin));
		assertFalse(scn.ShadowCanAssign(bounder));
		assertFalse(scn.ShadowCanAssign(ted));
		// Those who are no Hobbits still can.
		assertTrue(scn.FreepsCanAssign(aragorn));
		assertTrue(scn.FreepsCanAssign(gandalf));
		assertTrue(scn.FreepsCanAssign(runner));
		assertTrue(scn.ShadowCanAssign(aragorn));
		// At site 2 no Hobbit is made stronger.
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(pippinStrength, scn.GetStrength(pippin));
		assertEquals(bounderStrength, scn.GetStrength(bounder));
		assertEquals(tedStrength, scn.GetStrength(ted));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(pippin));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(bounder));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(ted));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void AtSite5HobbitsCannotBeAssignedThroughTheFierceAssignmentPhaseAndCanAgainFromTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetFreepsHand(depart);
		scn.MoveMinionsToTable(pursuer, runner);
		assertInHand(depart);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(frodo));
		int frodoStrength = scn.GetStrength(frodo);

		scn.FreepsPlayCard(depart);
		assertFalse(scn.FreepsCanAssign(frodo));
		assertFalse(scn.ShadowCanAssign(frodo));
		assertTrue(scn.FreepsCanAssign(aragorn));
		// The last site of "1 to 5": no Hobbit made stronger.
		assertEquals(frodoStrength, scn.GetStrength(frodo));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(frodo));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer}, new PhysicalCardImpl[]{gandalf, runner});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassCurrentPhaseActions();
		// The Uruk Pursuer lived through Aragorn (8 against 5) and is fierce: the last assignment before the regroup
		// phase.  Frodo is still not to be assigned.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(frodo));
		assertFalse(scn.ShadowCanAssign(frodo));

		scn.PassFierceAssignmentActions();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(frodo));
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// From the regroup phase the Hobbit may be assigned again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsCanAssign(frodo));
		assertTrue(scn.ShadowCanAssign(frodo));
	}

	@Test
	public void WithNoGandalfToSpotItIsNotPlayableAtSite5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var avenger = scn.GetFreepsCard("avenger");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(avenger);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetFreepsHand(depart);
		scn.MoveMinionsToTable(runner);
		assertInHand(depart);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Ent Avenger is a [gandalf] companion, and no Gandalf.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(depart));
		assertInHand(depart);
	}

	@Test
	public void WithNoGandalfToSpotItIsNotPlayableAtSite6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var avenger = scn.GetFreepsCard("avenger");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(avenger);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(depart);
		scn.MoveMinionsToTable(runner);
		assertInHand(depart);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(depart));
		assertInHand(depart);
	}

	@Test
	public void AtSite6ItMakesAHobbitOfAnyCultureOrSideStrengthPlus3UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var depart = scn.GetFreepsCard("depart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(gandalf, aragorn, pippin);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(depart);
		scn.MoveMinionsToTable(ted, runner, runner2);
		assertInHand(depart);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(6, scn.GetCurrentSiteNumber());
		int frodoStrength = scn.GetStrength(frodo);
		int pippinStrength = scn.GetStrength(pippin);
		int bounderStrength = scn.GetStrength(bounder);
		int tedStrength = scn.GetStrength(ted);
		int aragornStrength = scn.GetStrength(aragorn);

		scn.FreepsPlayCard(depart);
		// Any one Hobbit, of either culture or side, companion, ally or minion; no one who is not a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gandalf, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(pippinStrength + 3, scn.GetStrength(pippin));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(bounderStrength, scn.GetStrength(bounder));
		assertEquals(tedStrength, scn.GetStrength(ted));
		assertEquals(aragornStrength, scn.GetStrength(aragorn));
		// At any other site, nobody is kept from being assigned.
		assertTrue(scn.FreepsCanAssign(frodo));
		assertTrue(scn.FreepsCanAssign(pippin));
		assertTrue(scn.FreepsCanAssign(ted));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{pippin, runner2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Pippin's skirmish comes after Aragorn's has resolved: the bonus is still there.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(pippinStrength + 3, scn.GetStrength(pippin));
		scn.PassCurrentPhaseActions();

		// Gone as the regroup phase begins.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(pippinStrength, scn.GetStrength(pippin));
	}

	@Test
	public void AtASiteNumberedInRangeOfTheTowerBlockHobbitsCanStillBeAssignedAndAHobbitIsMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var depart = scn.GetFreepsCard("depart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var pippin = scn.GetFreepsCard("pippin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, pippin);
		scn.MoveCardsToHand(depart);
		assertInHand(depart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2 of the Tower block is no Fellowship block site, so it is an "other site" (CR 4.2 "block symbol").
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(frodo));
		assertTrue(scn.FreepsCanAssign(pippin));
		assertTrue(scn.ShadowCanAssign(frodo));
		int frodoStrength = scn.GetStrength(frodo);
		int pippinStrength = scn.GetStrength(pippin);
		assertTrue(scn.FreepsPlayAvailable(depart));

		scn.FreepsPlayCard(depart);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, runner));
		scn.FreepsChooseCard(pippin);

		assertEquals(pippinStrength + 3, scn.GetStrength(pippin));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		// Nobody is kept from being assigned.
		assertTrue(scn.FreepsCanAssign(frodo));
		assertTrue(scn.FreepsCanAssign(pippin));
		assertTrue(scn.ShadowCanAssign(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AHobbitPutIntoPlayAfterTheEventCanStillBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSnapshotScenario();

		var depart = scn.GetFreepsCard("depart");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var runner4 = scn.GetShadowCard("runner4");
		scn.MoveCompanionsToTable(gandalf, eomer);
		scn.MoveCardsToHand(depart);
		assertInHand(depart);
		assertInZone(Zone.DECK, merry);

		scn.StartGame();
		// Four minions against three companions, for Eomer's ability.
		scn.MoveMinionsToTable(runner, runner2, runner3, runner4);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(depart);
		assertFalse(scn.FreepsCanAssign(frodo));
		scn.ShadowPass();

		// The event affects the Hobbits in play when it was played (CR 4.2 "event"); Merry comes later.
		assertTrue(scn.FreepsActionAvailable(eomer));
		scn.FreepsUseCardAction(eomer);
		// The deck is shown first, then the [rohan] companion is chosen from it.
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(eomer));

		assertTrue(scn.FreepsCanAssign(merry));
		assertTrue(scn.ShadowCanAssign(merry));
		assertFalse(scn.FreepsCanAssign(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
