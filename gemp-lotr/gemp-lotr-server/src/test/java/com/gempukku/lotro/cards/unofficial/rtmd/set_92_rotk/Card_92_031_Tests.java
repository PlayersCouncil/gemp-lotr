package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_92_031_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Arwen, Daughter of Elrond (1_30): Elf companion
		put("arwen", "1_30");
		// Legolas, Greenleaf (1_50): Elf companion
		put("legolas", "1_50");
		// The Dark Lord's Summons (1_242): Sauron shadow condition
		put("summons", "1_242");
		put("hollowing", "3_54");
		// Gimli, Son of Gloin (1_13): Dwarf companion (not an elf)
		put("gimli", "1_13");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_31", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_31"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_31
		 * Type: MetaSite
		 * Game Text: Fellowship: Exert two Elf companions to make a Shadow player discard a Shadow condition.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_31", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void ExertTwoElvesToDiscardShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var summons = scn.GetShadowCard("summons");
		var hollowing = scn.GetShadowCard("hollowing");

		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToSupportArea(summons, hollowing);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
		scn.FreepsUseCardAction(mod);

		// Choose the shadow condition to discard
		scn.ShadowHasCardChoicesAvailable(summons, hollowing);
		scn.ShadowChooseCard(summons);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.DISCARD, summons);
	}

	@Test
	public void OnlyOneElfMeansNoAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		// Can't exert two elves with only one elf
		assertFalse(scn.FreepsActionAvailable(mod));
	}

	@Test
	public void NonElfCompanionDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen, gimli);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		var mod = scn.GetFreepsCard("mod");
		// Only 1 elf, Gimli is a dwarf — can't exert two elves
		assertFalse(scn.FreepsActionAvailable(mod));
	}


	@Test
	public void OpponentCanUse() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow owns the modifier; Freeps should not be able to use it
		var scn = GetShadowScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var summons = scn.GetShadowCard("summons");

		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToSupportArea(summons);

		scn.StartGame();

		var mod = scn.GetShadowCard("mod");
		assertTrue(scn.FreepsActionAvailable(mod));
	}
}
