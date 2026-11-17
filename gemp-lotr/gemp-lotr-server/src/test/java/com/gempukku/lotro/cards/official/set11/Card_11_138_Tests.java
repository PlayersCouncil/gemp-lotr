package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("goblin", "11_138");
				}},
				// The King path: site 1 (Edoras Hall) and site 2 (King's Tent) have no terrain, and neither is underground.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SkulkingGoblinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Skulking Goblin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: The fellowship's current site gains <b>underground</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("goblin");

		assertEquals("Skulking Goblin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheFellowshipsCurrentSiteGainsUndergroundAndOnlyThatSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();

		var edorasHall = scn.GetCurrentSite();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(edorasHall, Keyword.UNDERGROUND));

		// Laid early, in the fellowship phase at site 1, to be read before and after (Q-0228).
		scn.MoveMinionsToTable(goblin);
		assertTrue(scn.HasKeyword(edorasHall, Keyword.UNDERGROUND));

		// The fellowship moves to King's Tent: the underground goes with the current site.
		scn.FreepsPass();
		var kingsTent = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(kingsTent, Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(edorasHall, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
