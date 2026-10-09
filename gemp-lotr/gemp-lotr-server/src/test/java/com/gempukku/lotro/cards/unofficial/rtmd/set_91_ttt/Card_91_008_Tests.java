package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_008_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("guard", "1_7");        // Dwarf Guard: vitality 2; to play, spot a Dwarf
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a companion for Durin's Secret to spot; a fellowship ability used only on purpose
					put("secret", "1_71");      // Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_8", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("boromir", "1_97");
					put("guard", "1_7");        // Dwarf Guard: vitality 2; to play, spot a Dwarf
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_8"
		);
	}

	@Test
	public void MoveLimitMinusOneStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_8
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: While you can spot 5 or more companions, your move limit is -1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_8", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(3, card.getBlueprint().getIntensity());
	}

	@Test
	public void MoveLimitReducedWith5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: With 5 companions (Frodo + 4), move limit should be reduced from 2 to 1.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		// Normal move limit is 2, should be reduced to 1
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayingAFifthCompanionLowersTheMoveLimitAndTheTurnEndsAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		// Frodo + 3 = 4 companions, and an ally beside them
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");
		scn.MoveAlliesToTable("bounder");
		scn.MoveCardsToHand(boromir, guard);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		// The fifth companion
		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(1, scn.GetMoveLimit());

		// "5 or more": a sixth changes nothing
		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		// At the move limit the turn ends without the question of another move.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WhenAFifthCompanionDiesTheMoveLimitIsTwoAgainAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var guard = scn.GetFreepsCard("guard");
		var slayer = scn.GetShadowCard("slayer");
		// Frodo + 4 = 5 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "guard");

		scn.StartGame();
		// Vitality 2: one wound more kills him.
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		// Four companions are left: the "while" no longer holds
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void MoveLimitUnchangedWithFewerThan5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: Move limit should remain normal with < 5 companions.

		var scn = GetFreepsScenario();

		// Only Frodo + 3 = 4 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas");
		// A fifth character who is no companion
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();

		// Normal move limit should be unchanged
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowCopyDoesNotAffectFreePeoples() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_8: Shadow's copy should not reduce FP's move limit.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheMoveLimitIsLoweredByExactlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var secret = scn.GetFreepsCard("secret");
		// Frodo and these four are five companions; Gimli is the Dwarf and Legolas the Elf that Durin's Secret needs
		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf", "aragorn");
		scn.MoveCardsToHand(secret);

		scn.StartGame();

		// The limit cannot go below 1, so from 2 a penalty of 1 and a penalty of 2 read the same: raise the limit first
		assertEquals(1, scn.GetMoveLimit()); // 2 - 1
		scn.FreepsPlayCard(secret);
		assertInDiscard(secret);
		assertEquals(2, scn.GetMoveLimit()); // 2 + 1 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOwnersMoveLimitIsLoweredOnHisOwnTurnWhenHeIsPlayerTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		// Player 2 owns the modifier, and his own fellowship is the five: Frodo and these four
		scn.MoveCompanionsToTable(scn.GetShadowCard("gimli"), scn.GetShadowCard("aragorn"), scn.GetShadowCard("legolas"), scn.GetShadowCard("boromir"));

		scn.StartGame();

		// Player 1's turn: his own fellowship is not the five
		assertEquals(2, scn.GetMoveLimit());

		// Player 2's turn
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
