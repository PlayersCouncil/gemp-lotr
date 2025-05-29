package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_007_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "51_7");
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarfGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Dwarf Guard
		* Side: Free Peoples
		* Culture: Dwarven
		* Twilight Cost: 1
		* Type: companion
		* Subtype: Dwarf
		* Strength: 5
		* Vitality: 2
		* Game Text: To play, spot a Dwarf.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl guard = scn.GetFreepsCard("guard");

		assertFalse(guard.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, guard.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, guard.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, guard.getBlueprint().getCardType());
		assertEquals(Race.DWARF, guard.getBlueprint().getRace());
		assertEquals(1, guard.getBlueprint().getTwilightCost());
		assertEquals(5, guard.getBlueprint().getStrength());
		assertEquals(2, guard.getBlueprint().getVitality());
	}

	@Test
	public void DwarfGuardRequiresDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl guard = scn.GetFreepsCard("guard");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(guard, gimli);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(guard));
	}
}
