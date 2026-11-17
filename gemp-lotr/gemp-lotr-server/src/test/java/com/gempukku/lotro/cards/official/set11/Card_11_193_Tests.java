package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_193_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lookout", "11_193");
				}},
				// The King path: site 1 (Edoras Hall) and site 2 (King's Tent) have no terrain.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LookoutUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Lookout Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>The fellowship's current site gains <b>battleground</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lookout");

		assertEquals("Lookout Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheFellowshipsCurrentSiteGainsBattlegroundAndOnlyThatSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lookout = scn.GetShadowCard("lookout");

		scn.StartGame();

		var edorasHall = scn.GetCurrentSite();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(edorasHall, Keyword.BATTLEGROUND));

		// Laid early, in the fellowship phase at site 1, to be read before and after (Q-0228).
		scn.MoveMinionsToTable(lookout);
		assertTrue(scn.HasKeyword(edorasHall, Keyword.BATTLEGROUND));

		// The fellowship moves to King's Tent: the battleground goes with the current site.
		scn.FreepsPass();
		var kingsTent = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(kingsTent, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(edorasHall, Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
