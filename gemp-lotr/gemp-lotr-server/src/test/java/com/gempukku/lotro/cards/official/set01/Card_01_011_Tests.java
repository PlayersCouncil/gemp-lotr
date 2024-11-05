package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_011_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("farin", "1_11");
					put("gimli", "1_13");

					put("runner", "1_178");
					put("nazgul", "1_230");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Farin, Dwarven Emissary
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 5
		 * Vitality: 3
		 * Game Text: To play, spot a Dwarf.
		 * 	While skirmishing an Orc, Farin is strength +2.
		 * Resistance: 6
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("farin");

		assertEquals("Farin", card.getBlueprint().getTitle());
		assertEquals("Dwarven Emissary", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void FarinRequiresDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(farin, gimli);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(farin));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(farin));
	}

	@Test
	public void FarinStrengthBonusAgainstOrcs() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		scn.FreepsMoveCharToTable(farin);

		var orc = scn.GetShadowCard("runner");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.ShadowMoveCharToTable(orc, nazgul);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, orc);
		scn.ShadowDeclineAssignments();

		assertEquals(5, scn.GetStrength(farin));
		scn.FreepsResolveSkirmish(farin);
		assertEquals(7, scn.GetStrength(farin));
		scn.PassCurrentPhaseActions();

		//fierce
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, nazgul);
		scn.FreepsResolveSkirmish(farin);
		assertEquals(5, scn.GetStrength(farin));
	}
}
