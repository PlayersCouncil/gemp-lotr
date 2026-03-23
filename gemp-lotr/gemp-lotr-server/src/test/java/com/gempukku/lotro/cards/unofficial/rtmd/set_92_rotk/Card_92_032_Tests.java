package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_032_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Arwen, Daughter of Elrond (1_30): Elf companion, Aragorn signet, vitality 3
		put("arwen", "1_30");
		// Boromir, Lord of Gondor (1_96): Man companion, Aragorn signet, vitality 3
		put("boromir", "1_96");
		// Merry, From O'er the Brandywine: Hobbit companion, Aragorn signet, vitality 4
		put("merry", "1_303");
		// Gimli, Son of Gloin (1_13): Dwarf companion, Gandalf signet, vitality 3
		put("gimli", "1_13");
		// Legolas, Greenleaf (1_50): Elf companion, Gandalf signet, vitality 3
		put("legolas", "1_50");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_32", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_32"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_32
		 * Type: MetaSite
		 * Game Text: Fellowship: Exert a companion to heal another companion with the same signet.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_32", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ExertCompanionToHealAnotherWithSameSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(arwen, boromir, merry);
		// Wound boromir so he can be healed
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(merry, 1);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Choose companion to exert
		scn.FreepsChooseCard(arwen);

		// Choose companion to heal — only boromir should be available
		// (same signet, different companion)
		assertTrue(scn.FreepsHasCardChoicesAvailable(boromir, merry));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(arwen));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merry));
	}

	@Test
	public void CannotHealCompanionWithDifferentSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(arwen, gimli);
		// Wound gimli so he could theoretically be healed
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		// Arwen has Aragorn signet, Gimli has Gandalf signet — no valid heal target
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);
		scn.FreepsChooseCard(arwen);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(gimli));
	}

	@Test
	public void OpponentCanUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCompanionsToTable(arwen, boromir);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
	}
}
