package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_93_003_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: Gondor companion (sacrifice target)
					put("boromir", "1_96"); // Boromir: Gondor companion (second companion to prove selectivity)
					put("sam", "1_311");    // Sam, Son of Hamfast: Ring-bound, and not the Ring-bearer
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_3", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn: Gondor companion (sacrifice target)
					put("boromir", "1_96"); // Boromir: Gondor companion (second companion to prove selectivity)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_3"
		);
	}

	// Gimli, Bearer of Grudges as the Ring-bearer: a Ring-bearer who is not Frodo
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bounder", "1_286"); // Bounder: a [shire] Hobbit ally, no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing,
				"93_3", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_3
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: Each time your fellowship moves, you may kill a companion (except the
		 * Ring-bearer) to make the move limit +1.
		 */
		var scn = GetFreepsScenario();
		var card = scn.GetFreepsCard("mod");
		assertEquals("Race Text 93_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void KillCompanionToIncreaseMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, aragorn, boromir);

		// Move from site 1 to site 2; trigger fires on move
		scn.FreepsPass();
		assertEquals(2, scn.GetMoveLimit());

		// Optional trigger — choose to kill a companion
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();

		// Ring-bearer should not be in the choices
		assertFalse(scn.FreepsHasCardChoiceAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);

		// Aragorn should be dead, and nobody else
		assertInZone(Zone.DEAD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, boromir, frodo);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferIsMadeAgainWhenTheFellowshipMovesInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");

		scn.MoveCompanionsToTable(aragorn, boromir, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		// The first move of the turn, from site 1 to site 2. Sam is Ring-bound and bears no Ring: he may
		// be killed. The Ring-bearer may not, and an ally is no companion.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(3, scn.GetMoveLimit());

		// The limit is still 3 when the regroup phase asks about a second move
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The second move, from site 2 to site 3: the offer is made again, and a second companion pays
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));
		scn.FreepsChooseCard(sam);

		assertInZone(Zone.DEAD, aragorn, sam);
		assertInZone(Zone.FREE_CHARACTERS, boromir, frodo);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AKillOnTheFirstMoveLetsTheFellowshipMoveAThirdTimeAndTheLimitIsTwoAgainNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveCompanionsToTable(aragorn, boromir, sam);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetCurrentSiteNumber());

		// The first move: one kill, and the limit is 3 for the turn
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(3, scn.GetMoveLimit());

		// The second move, with the offer declined: the limit is still 3
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		// So after two moves the fellowship is asked about a third, and makes it
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();

		// A second kill on that third move: 2, and 1 for each kill of the turn
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(boromir);
		assertInZone(Zone.DEAD, aragorn, boromir);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other player's turn: the limit is 2 again
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void NotOfferedWhenTheRingBearerIsTheOnlyCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var gimli = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");

		// Gimli bears the Ring, and the only other character is an ally: nobody to kill
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanDeclineToKill() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Move from site 1 to site 2
		scn.FreepsPass();

		// Optional trigger — decline
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mod));
		scn.FreepsDeclineOptionalTrigger();

		// Aragorn should still be alive
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetMoveLimit());
		// "You may" is the modifier's owner and nobody else: the other player is not offered the kill
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OwnerGatingDoesNotFireForShadowOwner() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Move from site 1 to site 2
		scn.FreepsPass();

		// Shadow owns the mod — trigger should not fire for Freeps
		// Should proceed directly to shadow phase without offering to kill
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		assertFalse(scn.FreepsDecisionAvailable("Kill"));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowOwnerMayKillHisOwnCompanionWhenHisOwnFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and these are his own companions
		var mod = scn.GetShadowCard("mod");
		var frodo = scn.GetShadowRingBearer();
		var aragorn = scn.GetShadowCard("aragorn");
		var boromir = scn.GetShadowCard("boromir");

		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();

		// Player 2's turn: the fellowship that moves is now his
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
		assertInZone(Zone.FREE_CHARACTERS, aragorn, boromir);

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(mod));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(boromir);

		assertInZone(Zone.DEAD, boromir);
		assertInZone(Zone.FREE_CHARACTERS, aragorn, frodo);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
