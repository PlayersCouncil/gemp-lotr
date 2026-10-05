package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_052_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "101_52");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4

					// Shelob, Last Child of Ungoliant: [gollum] Spider, strength 8, vitality 8, enduring and fierce
					put("shelob", "8_26");
					// Uruk Runner: [isengard] Uruk-hai, strength 8, vitality 2, fierce; strength +2 in a fierce skirmish
					put("runner", "4_193");
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					// Orc Slaughterer: [sauron] Orc, vitality 3, enduring.  The minion of the Rules Team's ruling.
					put("slaughterer", "10_95");
					// Saruman, Servant of the Eye: "Assignment: Exert Saruman to assign an [isengard] minion to a
					// companion (except the Ring-bearer). That companion may exert to prevent this."
					put("saruman", "3_69");
					put("party", "1_158");          // Uruk-hai Raiding Party: [isengard], strength 9, damage +1
					put("savage", "1_151");         // Uruk Savage: [isengard], strength 5, damage +1
					// Pinned Down: a [moria] condition in the support area that does nothing without a [moria] archer
					put("pinned", "1_192");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Merry, Of Buckland
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Assignment: Exert Merry twice to prevent a minion from being assigned to a skirmish until the
		 * 	regroup phase.  The Shadow player may exhaust that minion to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Of Buckland", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void PlaysFromHandAsACompanionForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(merry);

		scn.StartGame();

		assertInHand(merry);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AssignmentAbilityExertsMerryTwiceAndNeitherPlayerCanAssignTheChosenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shelob = scn.GetShadowCard("shelob");
		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToSupportArea(pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(shelob));
		assertTrue(scn.ShadowCanAssign(shelob));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		// A [gollum] Spider and a [moria] Orc; not a companion, and not a Shadow card that is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shelob, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, pinned));

		scn.FreepsChooseCard(shelob);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("exhaust"));

		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(shelob));

		// The Free Peoples player assigns nobody, which leaves both minions for the Shadow player to assign.
		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(troop));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(shelob));

		scn.ShadowAssignToMinions(aragorn, troop);
		assertTrue(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(shelob));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayerMayExhaustTheChosenMinionAndItCanThenBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shelob = scn.GetShadowCard("shelob");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(merry);
		scn.FreepsChooseCard(shelob);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowDecisionAvailable("exhaust"));

		// The Shadow player is not asked which minion: it is the one Merry chose.
		scn.ShadowChooseYes();
		// Vitality 8, so exhausting her is 7 exertions.
		assertEquals(7, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(troop));
		// What Merry paid stays paid.
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsCanAssign(shelob));
		assertTrue(scn.ShadowCanAssign(shelob));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(shelob));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(shelob));

		scn.ShadowAssignToMinions(aragorn, shelob);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, shelob));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void IsNotOfferedWhenMerryCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, aragorn);
		// Vitality 4 with 2 wounds: one exertion left in him, and the ability asks for two.
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void MinionCannotBeAssignedInTheFierceAssignmentPhaseEitherAndCanBeAgainFromTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(merry);
		scn.FreepsChooseCard(shelob);
		scn.ShadowChooseNo();
		scn.ShadowPass();
		scn.FreepsPass();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(shelob));

		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		// 8 against 8: the Shadow side wins a tie.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Both minions are fierce, so a second assignment phase follows: the last one before the regroup phase.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsCanAssign(shelob));
		assertFalse(scn.ShadowCanAssign(shelob));

		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(shelob));

		scn.FreepsDeclineAssignments();
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(runner));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(shelob));

		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsCanAssign(shelob));
		assertTrue(scn.ShadowCanAssign(shelob));

		// The fellowship moves again, and Shelob is there to be assigned like the Runner.
		scn.PassRegroupActions();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(shelob));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void AnAlreadyExhaustedOrcSlaughtererCannotBeExhaustedToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var slaughterer = scn.GetShadowCard("slaughterer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(slaughterer, troop);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(slaughterer, 2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetWoundsOn(slaughterer));
		assertTrue(scn.FreepsCanAssign(slaughterer));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(slaughterer, troop));
		scn.FreepsChooseCard(slaughterer);
		assertEquals(2, scn.GetWoundsOn(merry));

		// The Shadow player is asked nothing about exhausting it, and has the next assignment action.
		assertFalse(scn.ShadowDecisionAvailable("exhaust"));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(2, scn.GetWoundsOn(slaughterer));
		assertFalse(scn.FreepsCanAssign(slaughterer));
		assertFalse(scn.ShadowCanAssign(slaughterer));

		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(troop));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(slaughterer));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void AMinionKeptFromBeingAssignedIsNotTheOneAnotherCardsAssignmentAbilityAssigns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saruman = scn.GetShadowCard("saruman");
		var party = scn.GetShadowCard("party");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, party, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(merry);
		// Saruman may not be assigned to a skirmish as it is, and is a minion to be chosen all the same.
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, party, savage));
		scn.FreepsChooseCard(party);
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));

		// Saruman would assign either of the two other [isengard] minions.  With the Raiding Party kept from
		// being assigned, the Savage is the only one he can, and it is taken without a choice of minion.
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(aragorn);
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// Aragorn does not exert to prevent it.
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertFalse(scn.IsCharAssigned(party));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
