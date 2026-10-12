package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_197_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regiment", "7_197");
					put("regiment2", "7_197");
					// Úlairë Enquëa, Lieutenant of Morgul: a [wraith] Nazgûl, fierce; her ability needs 6 companions or
					// 5 burdens
					put("enquea", "1_231");
					// Úlairë Lemenya, Lieutenant of Morgul: a second Nazgûl, strength 9, cost 4, site 3, not fierce; her
					// text is about companions bearing [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], strength 12; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, strength 8, vitality 2; its text needs 3 burdens
					put("creeper", "8_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					// Bill Ferny, Swarthy Sneering Fellow: a [wraith] Man.  "Nazgûl are not roaming. The Free Peoples
					// player may not assign a character to skirmish Bill Ferny. Discard Bill Ferny if underground."
					put("ferny", "2_75");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					// Gimli, Son of Glóin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2"
					put("gimli", "1_13");
					// Bilbo, Retired Adventurer: a [shire] ally, vitality 3, home site 3; he can exert, and is no companion
					put("bilbo", "1_284");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulRegimentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Regiment
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: For each Nazgûl you can spot, the Free Peoples player must exert a companion to assign this minion to a skirmish.<br><b>Skirmish:</b> Exert this minion to make a Nazgûl or [wraith] Orc strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("regiment");

		assertEquals("Morgul Regiment", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForSevenAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		scn.MoveCardsToHand(regiment);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, regiment);
		assertTrue(scn.ShadowPlayAvailable(regiment));

		scn.ShadowPlayCard(regiment);

		// 7 to play + 2 roaming: its site number is 4 and the fellowship is at site 2
		assertEquals(twilight - 9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, regiment);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MustExertToAssignMorgulRegiment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Nazgûl arrives by play, beside a Regiment already there.
		assertTrue(scn.ShadowPlayAvailable(lemenya));
		scn.ShadowPlayCard(lemenya);
		scn.ShadowPass();

		scn.SkipToAssignments();

		// The price is offered as he starts assigning, ahead of it (Q-0283).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsChooseOptionalTrigger(regiment);

		// One Nazgûl, so one exertion, by the one companion there is, with nothing to choose.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment));

		scn.FreepsAssignToMinions(frodo, regiment);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(frodo, regiment));
		// The Nazgûl is left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// F-0425: "For each Nazgûl you can spot, the Free Peoples player must exert a companion": a cost that cannot be
	// paid in full is not begun (CR 4.2 "cost").
	@Test
	public void WithTwoNazgulAndOneExertionLeftMorgulRegimentIsNotTheFreePeoplesPlayersToAssignAndNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, enquea, lemenya);
		// The Ring-bearer (vitality 4) is the only companion: with 2 wounds he can exert once, and two Nazgûl
		// ask for two exertions
		scn.AddWoundsToChar(frodo, 2);
		scn.SkipToAssignments();

		assertEquals(2, scn.GetVitality(frodo));
		// Not offered: the first thing asked is his assigning
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(regiment));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(lemenya));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(regiment));
		assertFalse(scn.FreepsCanAssign(regiment));
		assertFalse(scn.FreepsCanAssignAgainst(frodo, regiment));

		scn.FreepsDeclineAssignments();

		// Nothing was paid, and the Regiment is the Shadow player's to assign, for nothing
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(regiment));
		scn.ShadowAssignToMinions(frodo, regiment);

		assertTrue(scn.IsCharAssignedAgainst(frodo, regiment));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithTwoNazgulAndOneExertionLeftOnEachOfTwoCompanionsMorgulRegimentIsAssignedAndBothExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, enquea, lemenya);
		// Each of the two companions (vitality 4) can exert once: two exertions between them, for two Nazgûl
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment));
		scn.FreepsChooseOptionalTrigger(regiment);

		// The first exertion is his to place on either; the second is then the other's, the one companion left
		// able to exert, and is not asked
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment));
		assertTrue(scn.FreepsCanAssign(regiment));
		scn.FreepsAssignToMinions(aragorn, regiment);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, regiment));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// Q-0283: each Regiment is offered by itself and judged afresh after each payment, so one that can no longer be
	// paid for in full is not offered, and nothing is paid once he has answered.
	@Test
	public void AfterOneRegimentIsPaidForTheOtherWhoseTwoExertionsCanNoLongerBothBePaidIsOfferedNoMoreAndIsNotHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment1 = scn.GetShadowCard("regiment");
		var regiment2 = scn.GetShadowCard("regiment2");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment1, regiment2, enquea, lemenya);
		// Three exertions are left among the companions, all Aragorn's; two Nazgûl ask two for each Regiment
		scn.AddWoundsToChar(frodo, 3);
		scn.SkipToAssignments();

		// Each Regiment can be paid for by itself, so both are offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment2));

		scn.FreepsChooseOptionalTrigger(regiment2);

		// Aragorn, the one companion able to, exerts twice, unasked.  One exertion is left for the other Regiment,
		// which asks two: it is offered no more, nobody exerts for it, and it is not his to assign.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(regiment1));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment2));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(regiment1));

		scn.FreepsAssignToMinions(aragorn, regiment2);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, regiment2));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(regiment1));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(regiment2));
	}

	// "For each Nazgûl": two of them, of two cultures, ask two exertions.  Each is a choice of its own, so one
	// companion may make both.
	@Test
	public void TwoNazgulOfTwoCulturesAskTwoExertionsAndOneCompanionMayMakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, lemenya, khamul, troop);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment));
		scn.FreepsChooseOptionalTrigger(regiment);

		// The first exertion: either companion, and one of them must; Bilbo, an ally who could exert, is not asked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The second: the companion who has just exerted may exert again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, regiment}, new PhysicalCardImpl[]{frodo, troop});

		// Two exertions were the whole price: the Troop, assigned in the same answer, cost nothing.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, regiment));
		assertTrue(scn.IsCharAssignedAgainst(frodo, troop));
		// The Nazgûl are left over for the Shadow player.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}

	// With no Nazgûl there is nothing to count, beside a [wraith] Orc who is none.
	@Test
	public void WithNoNazgulToSpotMorgulRegimentIsAssignedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, creeper);
		scn.SkipToAssignments();

		// Nothing to pay, so nothing is offered, and it is his to assign
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(regiment));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, regiment}, new PhysicalCardImpl[]{frodo, creeper});

		assertTrue(scn.IsCharAssignedAgainst(aragorn, regiment));
		assertTrue(scn.IsCharAssignedAgainst(frodo, creeper));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	// The price is each Regiment's own, and the Free Peoples player's alone: the one he assigns is paid for, and
	// the one he leaves to the Shadow player costs nothing.
	@Test
	public void EachMorgulRegimentTheFreePeoplesPlayerAssignsIsPaidForByItselfAndTheShadowPlayerAssignsOneForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment1 = scn.GetShadowCard("regiment");
		var regiment2 = scn.GetShadowCard("regiment2");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment1, regiment2, khamul);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment2));
		scn.FreepsChooseOptionalTrigger(regiment2);

		// One Nazgûl, Khamûl, a [raider] minion: one exertion for the one Regiment he takes.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, frodo));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The other is offered again, and he passes: it is not his to assign.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(regiment1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(regiment2));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(regiment2));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(regiment1));
		scn.FreepsAssignToMinions(aragorn, regiment2);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, regiment2));
		assertFalse(scn.IsCharAssigned(regiment1));

		// The other is the Shadow player's to assign, for nothing, though the price could have been paid.
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(regiment1));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(regiment2));
		scn.ShadowAssignToMinions(frodo, regiment1);

		assertTrue(scn.IsCharAssignedAgainst(frodo, regiment1));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void SkirmishAbilityExertsTheRegimentToMakeANazgulOrAWraithOrcStrengthPlusOneForThatSkirmishUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment, lemenya, khamul, creeper, troop, ferny);

		// "Skirmish:" is not the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(regiment));
		scn.ShadowPass();

		// The Creeper and the Nazgûl cost nothing to assign; the Regiment is not paid for, and is left out of the
		// skirmishes.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, creeper}, new PhysicalCardImpl[]{aragorn, lemenya});
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(regiment));
		assertEquals(0, scn.GetWoundsOn(regiment));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(15, scn.GetStrength(regiment));

		scn.ShadowUseCardAction(regiment);

		// A Nazgûl of either culture and a [wraith] Orc, the Regiment itself among them, in the skirmish or out of
		// it.  Not the [moria] Orc, not the [wraith] Man, and no companion.
		assertEquals(1, scn.GetWoundsOn(regiment));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul, creeper, regiment));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, ferny, gimli, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(creeper);
		assertEquals(9, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(15, scn.GetStrength(regiment));

		scn.FreepsPass();
		scn.ShadowUseCardAction(regiment);
		scn.ShadowChooseCard(lemenya);
		assertEquals(2, scn.GetWoundsOn(regiment));
		assertEquals(10, scn.GetStrength(lemenya));

		scn.FreepsPass();
		scn.ShadowUseCardAction(regiment);
		scn.ShadowChooseCard(khamul);
		assertEquals(3, scn.GetWoundsOn(regiment));
		assertEquals(13, scn.GetStrength(khamul));

		// Vitality 4 with 3 wounds: exhausted, so it can no longer exert.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(regiment));
		assertEquals(9, scn.GetStrength(creeper));

		// Gimli, 6, loses to the Creeper, 8 + 1.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The next skirmish: each of them is as it was.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
