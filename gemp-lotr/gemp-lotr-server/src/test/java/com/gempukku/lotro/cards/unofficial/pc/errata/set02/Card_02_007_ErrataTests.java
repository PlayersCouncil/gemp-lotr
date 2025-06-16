package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_02_007_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gloin", "52_7");
					put("tale1", "2_9");
					put("tale2", "2_9");
					put("tale3", "2_9");
					put("tale4", "2_9");
					put("tale5", "2_9");
					put("shiretale", "2_108");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GloinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: *Gloin, Friend to Thorin
		* Side: Free Peoples
		* Culture: Dwarven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Dwarf
		* Strength: 6
		* Vitality: 3
		* Game Text: For each [Dwarven] tale you can spot, Gloin is strength +1 (limit +4).
		*/

		//Pre-game setup
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");

		assertTrue(gloin.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gloin.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, gloin.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, gloin.getBlueprint().getCardType());
		assertEquals(Race.DWARF, gloin.getBlueprint().getRace());
		assertEquals(2, gloin.getBlueprint().getTwilightCost());
		assertEquals(6, gloin.getBlueprint().getStrength());
		assertEquals(3, gloin.getBlueprint().getVitality());
		assertEquals(6, gloin.getBlueprint().getResistance());
	}

	@Test
	public void GloinIsStrengthPlus1PerDwarfTaleLimit4() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gloin = scn.GetFreepsCard("gloin");
		scn.MoveCompanionToTable(gloin);
		var tale1 = scn.GetFreepsCard("tale1");
		var tale2 = scn.GetFreepsCard("tale2");
		var tale3 = scn.GetFreepsCard("tale3");
		var tale4 = scn.GetFreepsCard("tale4");
		var tale5 = scn.GetFreepsCard("tale5");
		var shiretale = scn.GetFreepsCard("shiretale");
		scn.MoveCardsToHand(tale1, tale2, tale3, tale4, tale5, shiretale);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(gloin));
		//No effect from a non-dwarf tale
		scn.FreepsPlayCard(shiretale);
		assertEquals(6, scn.GetStrength(gloin));

		// +1 per dwarf tale
		scn.FreepsPlayCard(tale1);
		assertEquals(7, scn.GetStrength(gloin));
		scn.FreepsPlayCard(tale2);
		assertEquals(8, scn.GetStrength(gloin));
		scn.FreepsPlayCard(tale3);
		assertEquals(9, scn.GetStrength(gloin));
		scn.FreepsPlayCard(tale4);
		assertEquals(10, scn.GetStrength(gloin));

		//Fifth dwarven tale does nothing
		scn.FreepsPlayCard(tale5);
		assertEquals(10, scn.GetStrength(gloin));
	}
}
