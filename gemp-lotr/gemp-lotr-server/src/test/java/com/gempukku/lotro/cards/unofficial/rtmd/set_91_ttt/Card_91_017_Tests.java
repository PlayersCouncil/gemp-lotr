package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_017_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_17", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_17"
		);
	}

	@Test
	public void TwilightOnMoveStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_17
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Each time your fellowship moves, add (3) (or (6) if you can spot 6 companions).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_17", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(4, card.getBlueprint().getIntensity());
	}

	@Test
	public void Adds3TwilightOnMoveWithFewerThan6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_17: "Each time your fellowship moves, add (3) (or (6) if you can spot 6 companions)."
		// With fewer than 6 companions, should add 3 twilight on move.

		var scn = GetFreepsScenario();

		// Frodo + Gimli + Legolas = 3 companions, well under 6
		scn.MoveCompanionsToTable("gimli", "legolas");

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass(); // move to site 2

		// 2 (Ettenmoors' Shadow number) + 3 (companions) + 3 (the modifier), and nobody is asked
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void Adds6TwilightOnMoveWith6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_17: With 6+ companions, should add 6 twilight instead of 3.

		var scn = GetFreepsScenario();

		// Frodo + Gimli + Aragorn + Legolas + Boromir + Merry = 6 companions
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir", "merry");

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass(); // move

		// Should include +6 from the modifier (6+ companions spotted), in place of the 3 and not on top of it
		// 2 from site, 6 from companions, +6 from meta-site
		assertEquals(14, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FiveCompanionsAndAnAllyAddOnly3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		// Frodo + Gimli + Aragorn + Legolas + Boromir = 5 companions, one short. The Bounder is a sixth
		// character of the fellowship's side, and the other player's Frodo is a companion who is not active.
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir");
		scn.MoveAlliesToTable("bounder");

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass(); // move to site 2

		// 2 (Ettenmoors' Shadow number) + 5 (companions; an ally adds none) + 3 (the modifier)
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHinderedCompanionIsNotSpottedAmongTheSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var merry = scn.GetFreepsCard("merry");

		// Frodo + Gimli + Aragorn + Legolas + Boromir + Merry are six companions, as in the test above, but Merry is hindered
		scn.MoveCompanionsToTable("gimli", "aragorn", "legolas", "boromir", "merry");

		scn.StartGame();

		scn.HinderCard(merry);

		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass(); // move to site 2

		// A hindered companion cannot be spotted or counted (CR 5.0 "hinder") and adds no twilight on moving:
		// 2 (Ettenmoors' Shadow number) + 5 (companions, Merry adding none) + 3 (the modifier: only 5 can be spotted)
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMoveInTheSameTurnAdds3Again()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		// The first move of the turn, from site 1 to site 2
		scn.FreepsPass();
		// 2 (Ettenmoors' Shadow number) + 1 (Frodo) + 3 (the modifier)
		assertEquals(6, scn.GetTwilight());

		// Nothing more is added until the fellowship moves again, at the end of the regroup phase
		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetTwilight());
		scn.FreepsChooseToMove();

		// 6 + 0 (Rivendell Valley's Shadow number) + 1 (Frodo) + 3 (the modifier again)
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowCopyDoesNotAddTwilightOnMove() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_17: Shadow has the modifier. Moving should NOT add extra twilight.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "legolas");

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		// Site 2 shadow number + 3 companions (Frodo, Gimli, Legolas)
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight()); // 2 (site) + 3 (companions: Frodo, Gimli, Legolas)
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayerTwoOwnerAdds3WhenHisOwnFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		scn.StartGame();

		// Player 1's turn: the fellowship that moves is not the owner's
		scn.FreepsPass();
		// 2 (Ettenmoors' Shadow number) + 1 (Frodo)
		assertEquals(3, scn.GetTwilight());

		// Player 2's turn: the fellowship that moves is the owner's
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(0, scn.GetTwilight());

		scn.ShadowPass();
		// 2 (Ettenmoors' Shadow number) + 1 (his Frodo) + 3 (the modifier)
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
