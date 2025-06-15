package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_053_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elf", "51_53");
					put("arwen", "1_30");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienElfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Lorien Elf
		* Side: Free Peoples
		* Culture: Elven
		* Twilight Cost: 1
		* Type: companion
		* Subtype: Elf
		* Strength: 5
		* Vitality: 2
		* Game Text: To play, spot an Elf.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl elf = scn.GetFreepsCard("elf");

		assertFalse(elf.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, elf.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, elf.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, elf.getBlueprint().getCardType());
		assertEquals(Race.ELF, elf.getBlueprint().getRace());
		assertEquals(1, elf.getBlueprint().getTwilightCost());
		assertEquals(5, elf.getBlueprint().getStrength());
		assertEquals(2, elf.getBlueprint().getVitality());
	}

	@Test
	public void LorienElfRequiresAnElfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl elf = scn.GetFreepsCard("elf");
		PhysicalCardImpl arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(elf, arwen);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(elf));
		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(elf));
	}
}
