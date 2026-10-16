package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "2_121");
				}},
				// Site 2 Ettenmoors (plains, Shadow number 2), 4 Balin's Tomb (underground, 3),
				// 5 The Bridge of Khazad-dum (underground, 6)
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "2_121");
				}},
				// Site 6 Caves of Aglarond: underground and a sanctuary, Shadow number 3
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Gimli, Dwarf of the Mountain-race
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Damage +1</b>.<br>Each underground site's Shadow number is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Dwarf of the Mountain-race", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}

	@Test
	public void EachUndergroundSiteIsShadowNumberMinus2AndNoOtherSiteIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetTwilight(0);

		var ettenmoors = scn.GetFreepsSite(2);
		var balinsTomb = scn.GetFreepsSite(4);
		var bridge = scn.GetFreepsSite(5);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		assertEquals(3, scn.GetTwilightCost(balinsTomb));
		assertEquals(6, scn.GetTwilightCost(bridge));

		scn.MoveCompanionsToTable(gimli);
		// Balin's Tomb is underground, Ettenmoors is not; 3 - 2 = 1 is above the floor of 0, so -2 is told from -3
		assertEquals(1, scn.GetTwilightCost(balinsTomb));
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		// The Bridge is underground too, though the fellowship is not at it yet
		assertEquals(4, scn.GetTwilightCost(bridge));

		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetTwilightCost(scn.GetCurrentSite()));
		// 2 companions + 4 for the Bridge (6 - 2)
		assertEquals(6, scn.GetTwilight());
		// Balin's Tomb, left behind, is still underground
		assertEquals(1, scn.GetTwilightCost(balinsTomb));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUndergroundSanctuaryIsShadowNumberMinus2Too() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(6);

		var caves = scn.GetFreepsSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilightCost(caves));

		scn.MoveCompanionsToTable(gimli);
		assertEquals(1, scn.GetTwilightCost(caves));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
