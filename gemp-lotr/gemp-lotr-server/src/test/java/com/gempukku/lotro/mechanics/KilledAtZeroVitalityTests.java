package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A character is killed when his vitality, as it stands, is reduced to zero.
 *
 * Comprehensive Rules 4.2, "killed": "This game term is used to describe a character that has had its vitality
 * reduced to zero ...  Place killed Free Peoples characters in your dead pile. ...  When a card that provides a
 * vitality bonus for its bearer is discarded, that bearer is immediately killed if his vitality is then reduced to
 * zero."  "Vitality": "Each wound token on a character depletes that character's vitality by 1."
 *
 * The vitality that the wounds deplete is the character's with every bonus counted, not the number printed on him.
 * Boromir, Lord of Gondor 1_96 has vitality 3; The Saga of Elendil 1_114 gives its bearer vitality +1.
 */
public class KilledAtZeroVitalityTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Boromir, Lord of Gondor: strength 7, vitality 3; "Boromir is not overwhelmed unless his
					// strength is tripled."
					put("boromir", "1_96");
					// The Saga of Elendil: vitality +1; "Bearer must be a [gondor] companion.  Skirmish: Discard
					// this condition to make bearer strength +2."
					put("saga", "1_114");
					put("assassin", "1_131");   // Orthanc Assassin: "Archer. Damage +1." is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text at all
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWoundThatLeavesNoVitalityKillsTheCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AddWoundsToChar(boromir, 2);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		// Vitality 3 with 2 wounds: exhausted, and alive.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetVitality(boromir));

		// The one archery wound is given to him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		scn.FreepsChooseCard(boromir);

		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACharacterWithAVitalityBonusLivesWithAsManyWoundsAsHisPrintedVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, saga);
		scn.AddWoundsToChar(boromir, 2);

		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.PassArcheryActions();

		// Vitality 3 + 1 with 2 wounds.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetVitality(boromir));

		// The same wound as in the test above.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		scn.FreepsChooseCard(boromir);

		// Three wounds, as many as the vitality printed on him, and 1 vitality left.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(3, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetVitality(boromir));
		assertNotInZone(Zone.DEAD, boromir);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// And he is not lost when the game moves on: nobody is assigned to the Assassin, the turn ends, and in the
		// other player's fellowship phase he is still in play with his three wounds.
		scn.SkipToOtherPlayersTurn();

		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertAttachedTo(saga, boromir);
		assertEquals(3, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void LosingAVitalityBonusKillsACharacterWhoseWoundsThenLeaveHimNoVitality() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, saga);
		scn.AddWoundsToChar(boromir, 3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);

		// Vitality 3 + 1 with 3 wounds: the Saga is all that keeps him alive.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(3, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetVitality(boromir));
		assertTrue(scn.FreepsActionAvailable(saga));

		// The Saga's own skirmish ability discards it.
		scn.FreepsUseCardAction(saga);

		assertInZone(Zone.DISCARD, saga);
		assertInZone(Zone.DEAD, boromir);
		// He is killed at once, before the skirmish is resolved: the Troop has nobody left to fight, and the game
		// has gone on to the regroup phase.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
