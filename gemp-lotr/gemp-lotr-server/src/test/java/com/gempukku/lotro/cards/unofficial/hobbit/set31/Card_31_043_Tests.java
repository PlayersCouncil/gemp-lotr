package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_043_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The maneuver
	// phase of turn 1 is at The Troll Hoard (site 2), whose only text is a start-of-turn offer; no ally laid here
	// has his home there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("negotiator", "31_43");
					// Radagast, The Brown: [gandalf] Wizard ally, home 5H, 8/4; a fellowship ability that wants a
					// Gandalf in the discard pile
					put("radagast", "31_17");
					// Thranduil, Elven King: [elven] Elf ally, home 5H, 9/4; a maneuver ability that wants Thorin or a
					// [dwarven] artifact, neither of which is laid
					put("thranduil", "31_12");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion who is not Bilbo, vitality 4; +2
					// only while he bears a weapon. The Hobbit sets print no [shire] companion but Bilbo.
					put("merry", "1_303");

					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, 7/1; no other text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgûl, fierce; strength +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SkillfulNegotiatorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Skillful Negotiator
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot an ally and exert Bilbo to allow that ally to participate in archery fire and skirmishes until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("negotiator");

		assertEquals("Skillful Negotiator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsBilboToLetTheAllySpottedTakePartInArcheryFireAndSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var negotiator = scn.GetFreepsCard("negotiator");
		var radagast = scn.GetFreepsCard("radagast");
		var thranduil = scn.GetFreepsCard("thranduil");
		var balin = scn.GetFreepsCard("balin");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(radagast, thranduil);
		scn.MoveCardsToHand(negotiator);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, negotiator);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertFalse(scn.FreepsCanAssign(radagast));
		assertFalse(scn.FreepsCanAssign(thranduil));
		assertTrue(scn.FreepsPlayAvailable(negotiator));

		scn.FreepsPlayCard(negotiator);
		// Either ally may be spotted, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, thranduil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balin, bilbo));
		scn.FreepsChooseCard(radagast);

		// Bilbo exerts, unasked, and Balin, who could, does not.
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertInZone(Zone.DISCARD, negotiator);
		assertTrue(scn.FreepsCanAssign(radagast));
		assertFalse(scn.FreepsCanAssign(thranduil));

		// The Marksman's archery wound may go to Radagast, and not to Thranduil.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, bilbo, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thranduil));
		scn.FreepsChooseCard(radagast);
		assertEquals(1, scn.GetWoundsOn(radagast));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(radagast, marksman);
		assertTrue(scn.IsCharAssignedAgainst(radagast, marksman));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void CannotBePlayedWithNoAllyToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var negotiator = scn.GetFreepsCard("negotiator");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(negotiator);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, negotiator);
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(negotiator));
	}

	@Test
	public void CannotBePlayedWhileBilboIsExhaustedThoughAnotherShireHobbitCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var negotiator = scn.GetFreepsCard("negotiator");
		var radagast = scn.GetFreepsCard("radagast");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(negotiator);
		// Vitality 4 with 3 wounds: Bilbo cannot exert.
		scn.AddWoundsToChar(bilbo, 3);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(negotiator));
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(merry));
	}

	@Test
	public void TheAllyMayStillBeAssignedInTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var negotiator = scn.GetFreepsCard("negotiator");
		var radagast = scn.GetFreepsCard("radagast");
		var balin = scn.GetFreepsCard("balin");
		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(negotiator);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(negotiator);
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
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TheAllyNoLongerTakesPartOnceTheRegroupPhaseBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var negotiator = scn.GetFreepsCard("negotiator");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(negotiator);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(negotiator);
		assertTrue(scn.FreepsCanAssign(radagast));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(radagast));
	}
}
