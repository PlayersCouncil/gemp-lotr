package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_007_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("guard", "1_7");
					put("gimli", "1_13");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DwarfGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarf Guard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 4
		 * Vitality: 2
		 * Game Text: To play, spot a Dwarf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Dwarf Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void DwarfGuardRequiresDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(guard, gimli);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(guard));
	}
}
