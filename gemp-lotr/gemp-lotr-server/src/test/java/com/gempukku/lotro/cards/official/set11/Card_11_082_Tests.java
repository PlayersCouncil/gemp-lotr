package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_082_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("footman", "11_82");
				}},
				// The King path: site 1 (Edoras Hall) and site 2 (King's Tent) have no terrain.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FootmanofDunlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Footman of Dunland
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: The fellowship's current site gains <b>plains</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("footman");

		assertEquals("Footman of Dunland", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheFellowshipsCurrentSiteGainsPlainsAndOnlyThatSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var footman = scn.GetShadowCard("footman");

		scn.StartGame();

		var edorasHall = scn.GetCurrentSite();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(edorasHall, Keyword.PLAINS));

		// Laid early, in the fellowship phase at site 1, to be read before and after (Q-0228).
		scn.MoveMinionsToTable(footman);
		assertTrue(scn.HasKeyword(edorasHall, Keyword.PLAINS));

		// The fellowship moves to King's Tent: the plains go with the current site.
		scn.FreepsPass();
		var kingsTent = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(kingsTent, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(edorasHall, Keyword.PLAINS));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
