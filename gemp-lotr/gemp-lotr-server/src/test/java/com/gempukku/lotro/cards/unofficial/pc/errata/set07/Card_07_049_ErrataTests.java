package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_049_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("champion", "57_49");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("avenger", "6_27");     // Ent Avenger: [gandalf] companion who is not Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4
					put("horde", "6_28");       // Ent Horde: [gandalf] Ent companion, vitality 5
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an ally, so a character who is no companion

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SteadfastChampionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Steadfast Champion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Game Text: Bearer must be Gandalf.
		 * Each minion gains this ability: "Assignment: Assign this minion to Gandalf."
		 * At the start of the regroup phase, you may discard this condition to discard a minion and heal a
		 * companion 3 times.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("champion");

		assertEquals("Steadfast Champion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnGandalfAndOnNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(champion);
		scn.MoveCompanionsToTable(avenger, aragorn);

		scn.StartGame();

		// A [gandalf] companion of another title and a companion of another culture: neither may bear it.
		assertFalse(scn.FreepsPlayAvailable(champion));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(champion));

		scn.FreepsPlayCard(champion);

		// Gandalf is the only bearer, so no choice of bearer is asked.
		assertEquals(gandalf, champion.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionMayAssignItselfToGandalfInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		// Ent Avenger is a [gandalf] companion who is not Gandalf, Aragorn a companion of another culture.
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The maneuver phase: the ability is an assignment one.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop));
		assertFalse(scn.ShadowActionAvailable(savage));
		scn.ShadowPass();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// The minions are the Shadow player's: the Free Peoples player is offered nothing on them.
		assertFalse(scn.FreepsActionAvailable(troop));
		assertFalse(scn.FreepsActionAvailable(savage));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troop));
		assertTrue(scn.ShadowActionAvailable(savage));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertFalse(scn.IsCharAssigned(savage));

		scn.ShadowUseCardAction(savage);

		// Gandalf is the only one it can name, so no choice is asked.
		assertTrue(scn.IsCharAssignedAgainst(gandalf, savage));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(avenger));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceGandalfIsAssignedNoMinionIsOfferedTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(troop));
		scn.ShadowUseCardAction(savage);
		assertTrue(scn.IsCharAssignedAgainst(gandalf, savage));

		scn.FreepsPass();
		// Gandalf is assigned now, and an assignment action needs both cards unassigned (CR 4.2): the ability
		// has no cost and would do nothing, so it is not offered (Q-0034).
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troop));
	}

	@Test
	public void AtTheStartOfRegroupDiscardsItselfToDiscardAMinionAndHealOneCompanionThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horde = scn.GetFreepsCard("horde");
		var celeborn = scn.GetFreepsCard("celeborn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, aragorn, horde);
		scn.MoveAlliesToTable(celeborn);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.AddWoundsToChar(horde, 4);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(savage, 1);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(champion));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(champion);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, aragorn, horde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInDiscard(troop);
		assertInPlay(savage);

		assertTrue(scn.FreepsHasCardChoiceAvailable(horde, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.GetWoundsOn(horde));
		scn.FreepsChooseCard(horde);

		// Three heals, all on the one companion chosen.
		assertEquals(1, scn.GetWoundsOn(horde));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningAtTheStartOfRegroupLeavesEverythingAndNoRegroupActionFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.AddWoundsToChar(aragorn, 2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// The start of another phase does not set it off.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(champion));

		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(champion));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(gandalf, champion.getAttachedTo());
		assertInPlay(troop, savage);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// It is a trigger at the start of the phase, not a Regroup action.
		assertFalse(scn.FreepsActionAvailable(champion));
	}

	@Test
	public void WithNoMinionAndNoWoundItIsStillOfferedAndCostsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var champion = scn.GetFreepsCard("champion");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, champion);

		scn.StartGame();

		// No minion, so the game goes from the Shadow phase straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(champion));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(champion);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
