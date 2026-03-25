package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_91_012_Tests
{
	private final HashMap<String, String> cards = new HashMap<>()
	{{
		// Gimli, Dwarf of Erebor: Dwarf companion
		put("gimli", "1_13");
		// Legolas, Greenleaf: Elf companion
		put("legolas", "1_50");
		// Gandalf, Friend of the Shirefolk
		put("gandalf", "1_72");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_12", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_12"
		);
	}

	@Test
	public void MoveLimitPlusOneStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_12
		 * Type: MetaSite
		 * Game Text: While you can spot a Dwarf companion, an Elf companion, and Gandalf, your move limit is +1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_12", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void MoveLimitIncreasedWithAllThree() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Move limit +1 when spotting Dwarf, Elf, and Gandalf.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf");

		scn.StartGame();

		// Normal move limit is 2, should be 3 with the modifier
		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void MoveLimitUnchangedWithoutDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Missing a Dwarf — move limit should remain normal.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("legolas", "gandalf");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void MoveLimitUnchangedWithoutElf() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Missing an Elf — move limit should remain normal.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "gandalf");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void MoveLimitUnchangedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: Missing Gandalf — move limit should remain normal.

		var scn = GetFreepsScenario();

		scn.MoveCompanionsToTable("gimli", "legolas");

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ShadowCopyDoesNotAffectFreeps() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_12: "your move limit" — Shadow's copy should not affect FP move limit.

		var scn = GetShadowScenario();

		scn.MoveCompanionsToTable("gimli", "legolas", "gandalf");

		scn.StartGame();

		// Shadow's copy should not grant +1 to FP
		assertEquals(2, scn.GetMoveLimit());
	}
}
