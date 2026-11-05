package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_021_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver
	// phase of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer; no ally laid here
	// has his home there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herald", "32_21");
					// Radagast, The Brown: [gandalf] Wizard ally, Wise, home 5H, 8/4; a fellowship ability that wants a
					// Gandalf in the discard pile
					put("radagast", "31_17");
					// Elrond, Elven Lord: [elven] Elf ally, Wise, home 3H, 8/4; a start-of-turn heal of a wounded Wise
					// ally (none is wounded at the start of his tests) and a regroup ability, not used
					put("elrond", "30_21");
					// Thranduil, Elven King: [elven] Elf ally, not Wise, home 5H; a maneuver ability that wants Thorin or a
					// [dwarven] artifact, neither of which is laid
					put("thranduil", "31_12");
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion, Wise; a fellowship ability, a discard bar
					// that needs 2 [dwarven] followers, and a discount on Thorin
					put("gandalf", "30_26");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, 7/1; no other text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgûl, fierce; strength +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void FormerHeraldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Former Herald
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a Wise ally to make that ally strength +3 and participate in archery fire and skirmishes until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("herald");

		assertEquals("Former Herald", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAWiseAllyToMakeHimStrengthPlus3AndTakePartInArcheryFireAndSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herald = scn.GetFreepsCard("herald");
		var radagast = scn.GetFreepsCard("radagast");
		var elrond = scn.GetFreepsCard("elrond");
		var thranduil = scn.GetFreepsCard("thranduil");
		var gandalf = scn.GetFreepsCard("gandalf");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast, elrond, thranduil);
		scn.MoveCardsToHand(herald);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(herald);
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(thranduil));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(radagast));
		assertEquals(8, scn.GetStrength(elrond));
		assertFalse(scn.FreepsCanAssign(radagast));
		assertFalse(scn.FreepsCanAssign(elrond));
		assertTrue(scn.FreepsPlayAvailable(herald));

		scn.FreepsPlayCard(herald);
		// Wise allies of two cultures and races; not an ally who is not Wise, nor a Wise companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thranduil, gandalf, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(radagast);

		assertEquals(1, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(thranduil));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInDiscard(herald);
		// 8 + 3; Elrond, the Wise ally not exerted, is untouched.
		assertEquals(11, scn.GetStrength(radagast));
		assertEquals(8, scn.GetStrength(elrond));
		assertTrue(scn.FreepsCanAssign(radagast));
		assertFalse(scn.FreepsCanAssign(elrond));

		// The Marksman's archery wound may go to Radagast, and not to Elrond.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, bilbo, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, thranduil));
		scn.FreepsChooseCard(radagast);
		assertEquals(2, scn.GetWoundsOn(radagast));

		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(radagast));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(elrond));
		scn.FreepsAssignToMinions(radagast, marksman);
		assertTrue(scn.IsCharAssignedAgainst(radagast, marksman));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheBonusAndTheParticipationLastThroughTheFierceSkirmishAndEndAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herald = scn.GetFreepsCard("herald");
		var radagast = scn.GetFreepsCard("radagast");
		var balin = scn.GetFreepsCard("balin");
		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(herald);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(8, scn.GetStrength(radagast));
		scn.FreepsPlayCard(herald);
		assertEquals(11, scn.GetStrength(radagast));
		assertTrue(scn.FreepsCanAssign(radagast));

		// Balin takes the Troop; the Black Rider is left for the fierce round.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		assertTrue(scn.FreepsCanAssign(radagast));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(radagast, rider);
		assertTrue(scn.IsCharAssignedAgainst(radagast, rider));

		// In a skirmish after one already resolved, he is still +3.
		scn.FreepsResolveSkirmish(radagast);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(radagast));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(radagast));
		assertFalse(scn.FreepsCanAssign(radagast));
	}

	@Test
	public void CannotBePlayedWhileTheOnlyWiseAllyIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herald = scn.GetFreepsCard("herald");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(radagast, thranduil);
		scn.MoveCardsToHand(herald);
		// Vitality 4 with 3 wounds: Radagast cannot exert. Gandalf (a Wise companion) and Thranduil (an ally who is
		// not Wise) could.
		scn.AddWoundsToChar(radagast, 3);
		assertEquals(3, scn.GetWoundsOn(radagast));

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(herald);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(herald));
		assertEquals(3, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(thranduil));
		assertEquals(0, scn.GetWoundsOn(gandalf));
	}
}
