package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_078_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("swordsman", "4_78");
					put("savage", "1_151");
					put("runner", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void LorienSwordsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Lórien Swordsman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each minion skirmishing this companion is strength -2 for each wound on that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordsman");

		assertEquals("Lórien Swordsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void LorienSwordsmanReducesStrengthOnEachMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var swordsman = scn.GetFreepsCard("swordsman");
		scn.FreepsMoveCharToTable(swordsman);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.ShadowMoveCharToTable(savage, runner);

		scn.StartGame();

		scn.AddWoundsToChar(savage, 1);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(swordsman, savage, runner);

		assertEquals(5, scn.GetStrength(swordsman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage));

		scn.FreepsResolveSkirmish(swordsman);

		assertEquals(5, scn.GetStrength(swordsman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(3, scn.GetStrength(savage));

		scn.AddWoundsToChar(savage, 1);

		assertEquals(5, scn.GetStrength(swordsman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(1, scn.GetStrength(savage));
	}
}
