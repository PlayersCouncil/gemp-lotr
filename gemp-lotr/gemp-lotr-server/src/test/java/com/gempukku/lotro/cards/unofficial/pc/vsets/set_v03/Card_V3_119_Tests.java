package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_119_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("treasure", "103_119");    // Treasured Pipe
					put("pipe1", "103_119");       // Second copy (for X test)
					put("pipe2", "103_119");       // Third copy (for X test)
					put("pipeweed1", "1_300");     // Longbottom Leaf
					put("pipeweed2", "1_305");     // Old Toby
					put("merry", "1_302");         // Merry - unbound Hobbit
					put("aragorn", "1_89");        // Aragorn - non-Hobbit companion
					put("bilbo", "1_284");         // Bilbo - ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreasuredPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Treasured Pipe
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: To play, spot a pipe or an unbound Hobbit. Bearer must be a companion.
		 * 	Fellowship: Discard 2 pipeweeds and spot X pipes to make X companions strength +1
		 * 	until the regroup phase.  Hinder this pipe.
		 */

		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");

		assertEquals("Treasured Pipe", treasure.getBlueprint().getTitle());
		assertNull(treasure.getBlueprint().getSubtitle());
		assertFalse(treasure.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, treasure.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, treasure.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, treasure.getBlueprint().getCardType());
		assertTrue(treasure.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, treasure.getBlueprint().getTwilightCost());
	}

	@Test
	public void TreasuredPipeRequiresUnboundHobbitToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCardsToHand(treasure, merry);

		scn.StartGame();

		// Frodo is Ring-bound (not unbound), no pipes in play — can't play
		assertFalse(scn.FreepsPlayAvailable(treasure));

		// Add Merry — an unbound Hobbit
		scn.FreepsPlayCard(merry);

		assertTrue(scn.FreepsPlayAvailable(treasure));
	}

	@Test
	public void TreasuredPipePlayConditionSatisfiedByPipeAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var pipe1 = scn.GetFreepsCard("pipe1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");

		// No unbound hobbits — only Frodo (Ring-bound)
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, pipe1);
		scn.MoveCardsToHand(treasure);

		scn.StartGame();

		// pipe1 in play satisfies "spot a pipe"
		assertTrue(scn.FreepsPlayAvailable(treasure));
	}

	@Test
	public void TreasuredPipeBearerMustBeCompanionNotAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var bilbo = scn.GetFreepsCard("bilbo");

		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(bilbo);
		scn.MoveCardsToHand(treasure);

		scn.StartGame();

		scn.FreepsPlayCard(treasure);

		// Frodo and Aragorn are companions — valid bearers
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, merry));
		// Bilbo is an ally — not a valid bearer
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo));

		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(treasure, aragorn);
	}

	@Test
	public void TreasuredPipeRequires2PipeweedsToActivate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, treasure);
		scn.MoveCardsToSupportArea(pipeweed1);
		scn.MoveCardsToHand(pipeweed2);

		scn.StartGame();

		// With only 1 pipeweed, the ability should not be available
		assertFalse(scn.FreepsActionAvailable(treasure));

		// Add a second pipeweed — now it should be enough
		scn.FreepsPlayCard(pipeweed2);
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.FreepsActionAvailable(treasure));
	}

	@Test
	public void TreasuredPipeBuffsXCompanionsAndHindersSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var treasure = scn.GetFreepsCard("treasure");
		var pipe1 = scn.GetFreepsCard("pipe1");
		var pipe2 = scn.GetFreepsCard("pipe2");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");

		// 3 companions
		scn.MoveCompanionsToTable(merry, aragorn);
		// 3 pipes in play
		scn.AttachCardsTo(frodo, treasure);
		scn.AttachCardsTo(merry, pipe1);
		scn.AttachCardsTo(aragorn, pipe2);
		// 2 pipeweeds in support area (discard cost)
		scn.MoveCardsToSupportArea(pipeweed1, pipeweed2);

		scn.StartGame();

		int frodoStr = scn.GetStrength(frodo);
		int merryStr = scn.GetStrength(merry);
		int aragornStr = scn.GetStrength(aragorn);

		scn.FreepsUseCardAction(treasure);

		// Pipeweeds auto-selected (exactly 2 available)
		// Choose to spot 2 of 3 pipes
		scn.FreepsChoose("2");

		// Choose 2 of 3 companions to buff
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, aragorn));
		scn.FreepsChooseCards(merry, aragorn);

		// Merry and Aragorn gained +1 strength
		assertEquals(merryStr + 1, scn.GetStrength(merry));
		assertEquals(aragornStr + 1, scn.GetStrength(aragorn));
		// Frodo unchanged
		assertEquals(frodoStr, scn.GetStrength(frodo));

		// Treasured Pipe is hindered
		assertTrue(scn.IsHindered(treasure));

		// Pipeweeds were discarded as cost
		assertInDiscard(pipeweed1);
		assertInDiscard(pipeweed2);
	}
}
