package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shot", "13_14");
					put("lorien", "1_53");      // Lórien Elf: [elven] Elf companion, strength 4, vitality 2; its only text is its play requirement
					put("legolas", "11_21");    // Legolas, Companion of the Ring: [elven] Elf companion, strength 6; +2 only at a battleground or forest, which no site here is
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally; a fellowship ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalShotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Final Shot
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [elven] companion strength +2 (or +5 if the fellowship has moved more than once this turn).
		*/

		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");

		assertEquals("Final Shot", shot.getBlueprint().getTitle());
		assertNull(shot.getBlueprint().getSubtitle());
		assertFalse(shot.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, shot.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, shot.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, shot.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(shot, Timeword.SKIRMISH));
		assertEquals(1, shot.getBlueprint().getTwilightCost());
	}

	@Test
	public void AfterOneMoveItMakesAnElvenCompanionStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var frodo = scn.GetRingBearer();
		var lorien = scn.GetFreepsCard("lorien");
		var legolas = scn.GetFreepsCard("legolas");
		var celeborn = scn.GetFreepsCard("celeborn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(lorien, legolas, knight);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(shot);
		assertInHand(shot);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shot));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{lorien, troop}, new PhysicalCardImpl[]{knight, savage});
		scn.FreepsResolveSkirmish(lorien);

		// One move this turn, the one at the end of the fellowship phase.
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(legolas));
		assertEquals(4, scn.GetStrength(lorien));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(celeborn));

		scn.FreepsPlayCard(shot);
		// The [elven] companions, the one in this skirmish and the one in none; not the Elf ally, nor the [gondor]
		// companion, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);

		// +2, not +5: the fellowship has moved once.
		assertEquals(8, scn.GetStrength(legolas));
		assertEquals(4, scn.GetStrength(lorien));
		assertEquals(6, scn.GetStrength(knight));
		assertInDiscard(shot);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone there.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(legolas));
	}

	@Test
	public void AfterTheFellowshipHasMovedTwiceThisTurnItMakesTheCompanionStrengthPlus5ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(lorien);
		scn.MoveCardsToHand(shot);
		assertInHand(shot);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// The move at the end of the fellowship phase; no minion, so straight on to the regroup phase, and a second move.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(lorien, troop);
		scn.FreepsResolveSkirmish(lorien);
		assertEquals(4, scn.GetStrength(lorien));

		scn.FreepsPlayCard(shot);
		// The one [elven] companion is chosen unasked: 4 + 5.
		assertEquals(9, scn.GetStrength(lorien));
		assertInDiscard(shot);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 9 against 13: Lórien Elf loses and takes a wound, and lives.  The bonus ended with the skirmish.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(4, scn.GetStrength(lorien));
	}
}
