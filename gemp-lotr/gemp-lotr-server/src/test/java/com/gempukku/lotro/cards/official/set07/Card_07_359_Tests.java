package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_359_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "9_28");
					put("watcher", "2_73");
					put("fens", "5_22");

					put("companion1", "1_7");
					put("companion2", "1_11");
					put("companion3", "1_12");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "7_359");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernIthilienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Northern Ithilien
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 8
		 * Type: Site
		 * Subtype: 
		 * Site Number: 8K
		 * Game Text: <b>Shadow:</b> Remove 2 threats and play Gollum from your discard pile to add 2 burdens.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Northern Ithilien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NorthernIthilienCannotBeUsedWithWatcherToTurnThreatsIntoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(7);
		//Evil-smelling Fens makes every site a marsh, which keeps the Watcher in play.
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.MoveCardsToDiscard(gollum);
		scn.AddThreats(4);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		assertEquals(Zone.SHADOW_CHARACTERS, watcher.getZone());
		//4 companions + 8 for the site
		assertEquals(12, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		//Pointless as it is, the ability is still offered.
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);

		//The threats are removed and Gollum is paid for, but the Watcher discards him as he comes into play:
		// he was not played, so the cost is not fulfilled and no burden is added.
		assertEquals(2, scn.GetThreats());
		assertEquals(10, scn.GetTwilight());
		assertEquals(Zone.DISCARD, gollum.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, watcher.getZone());
		assertEquals(0, scn.GetBurdens());

		//Gollum is back in the discard pile, so it can be done again, with the same result.
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);

		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetTwilight());
		assertEquals(Zone.DISCARD, gollum.getZone());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
