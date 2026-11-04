package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_073_Tests
{
	// The Towers path: site 1 (Eastemnet Gullies) has no terrain, site 2 (Uruk Camp) is a battleground and a plains,
	// site 4 (White Rocks) a battleground and no plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("corps", "11_73");
					put("legion", "11_88");     // Legion of Harad: a [men] Man minion with no text but toil
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground. Plains. Exerts each Uruk-hai played; none is
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorpsofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Corps of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While this minion is at a battleground site, it is <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("corps");

		assertEquals("Corps of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsFierceOnlyOnceTheFellowshipIsAtABattlegroundSiteAndNoOtherMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var corps = scn.GetShadowCard("corps");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		// Laid early, in the fellowship phase at site 1, to be read before and after the move.
		scn.MoveMinionsToTable(corps, legion);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));

		// The fellowship moves to Uruk Camp.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		// Another [men] Man at the same battleground is not made fierce.
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsFierceAtABattlegroundThatIsNoPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var corps = scn.GetShadowCard("corps");
		var legion = scn.GetShadowCard("legion");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(corps, legion);

		// White Rocks.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(legion, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
