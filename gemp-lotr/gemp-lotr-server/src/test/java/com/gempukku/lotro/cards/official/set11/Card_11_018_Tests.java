package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout", "11_18");
					put("scout2", "11_18");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, cost 2, nothing needed to play him; an archery ability never used here
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; an archery ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, no Elf; a maneuver ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Elven Scout
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot another Elf, this companion has <b>muster</b>. <helper>(At the start of the regroup phase, you may discard a card from hand to draw a card.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout");

		assertEquals("Elven Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void HasNoMusterBesideCompanionsWhoAreNoElvesAndGainsItWhenAnElfCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(scout, aragorn);
		scn.MoveCardsToHand(legolas);

		scn.StartGame();

		// Frodo and Aragorn are no Elves, and the Scout does not spot himself.
		assertFalse(scn.HasKeyword(scout, Keyword.MUSTER));

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.HasKeyword(scout, Keyword.MUSTER));
		// The muster is the Scout's own, and goes to nobody else.
		assertFalse(scn.HasKeyword(legolas, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(frodo, Keyword.MUSTER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasMusterWhileAnElfAllyIsTheOtherElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(scout);
		scn.MoveAlliesToTable(orophin);

		scn.StartGame();

		assertTrue(scn.HasKeyword(scout, Keyword.MUSTER));
		assertFalse(scn.HasKeyword(orophin, Keyword.MUSTER));
	}

	@Test
	public void TwoElvenScoutsEachSpotTheOtherAndBothHaveMuster() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout = scn.GetFreepsCard("scout");
		var scout2 = scn.GetFreepsCard("scout2");
		scn.MoveCompanionsToTable(scout, scout2);

		scn.StartGame();

		assertTrue(scn.HasKeyword(scout, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(scout2, Keyword.MUSTER));
	}
}
