
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V1_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("gimli", "101_3");
					put("dwaxe", "1_9");
					put("handaxe1", "2_10");
					put("ring", "9_9");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: *Gimli, Vengeful Naugrim
		* Side: Free Peoples
		* Culture: dwarven
		* Twilight Cost: 2
		* Type: companion
		* Subtype: Dwarf
		* Strength: 6
		* Vitality: 3
		* Signet: Frodo
		* Game Text: Damage +1.
		* 	 While Gimli bears 2 possessions or artifacts he is strength +2.
		* 	While Gimli bears 3 possessions or artifacts he is damage +1. 
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");

		assertTrue(gimli.getBlueprint().isUnique());
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE)); // test for keywords as needed
		assertEquals(2, gimli.getBlueprint().getTwilightCost());
		assertEquals(6, gimli.getBlueprint().getStrength());
		assertEquals(3, gimli.getBlueprint().getVitality());
		assertEquals(6, gimli.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, gimli.getBlueprint().getSignet());
		assertEquals(CardType.COMPANION, gimli.getBlueprint().getCardType());
		assertEquals(Culture.DWARVEN, gimli.getBlueprint().getCulture());
		assertEquals(Race.DWARF, gimli.getBlueprint().getRace());
		assertEquals(Side.FREE_PEOPLE, gimli.getBlueprint().getSide());
	}

	@Test
	public void GimliHasStrengthBonusWith2Items() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl dwaxe = scn.GetFreepsCard("dwaxe");
		PhysicalCardImpl handaxe1 = scn.GetFreepsCard("handaxe1");
		PhysicalCardImpl ring = scn.GetFreepsCard("ring");
		scn.MoveCardsToHand(gimli, dwaxe, handaxe1, ring);

		scn.StartGame();
		scn.FreepsPlayCard(gimli);

		assertEquals(2, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(dwaxe);
		// Base 6 + 2 from the axe
		assertEquals(8, scn.GetStrength(gimli));
		scn.FreepsPlayCard(ring);
		// Base 6 + 2 from Dwarven Axe + 1 from Ring + 2 from game text
		assertEquals(11, scn.GetStrength(gimli));
	}

	@Test
	public void GimliHasDamageBonusWith3Items() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl dwaxe = scn.GetFreepsCard("dwaxe");
		PhysicalCardImpl handaxe1 = scn.GetFreepsCard("handaxe1");
		PhysicalCardImpl ring = scn.GetFreepsCard("ring");
		scn.MoveCardsToHand(gimli, dwaxe, handaxe1, ring);

		scn.StartGame();
		scn.FreepsPlayCard(gimli);

		assertEquals(2, scn.GetTwilight());

		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsPlayCard(dwaxe);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsPlayCard(ring);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsPlayCard(handaxe1);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}
}
