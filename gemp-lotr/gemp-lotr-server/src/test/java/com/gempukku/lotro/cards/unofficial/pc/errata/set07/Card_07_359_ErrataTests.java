package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_07_359_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("card", "57_359");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path with this site as site 8.
	protected VirtualTableScenario GetSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: cost 2, site 3
					put("runner", "1_178");     // Goblin Runner: cost 1
					put("watcher", "2_73");     // Watcher in the Water: discards all other minions (except tentacles)
					put("fens", "5_22");        // Evil-smelling Fens: every site is a marsh, which keeps the Watcher in play
					put("aragorn", "1_89");     // three companions beside Frodo: room for four threats
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				new HashMap<String, String>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "57_359");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uncomment both @Test markers below once this is ready to be used

	//@Test
	public void NorthernIthilienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 7
		* Title: Northern Ithilien
		* Side: Free Peoples
		* Culture: 
		* Twilight Cost: 8
		* Type: site
		* Subtype: Standard
		* Site Number: 8K
		* Game Text: <b>Shadow:</b> Remove 2 threats and play Gollum from your
		* 	 discard pile to add 2 burdens (limit once per phase).
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");

		assertFalse(card.getBlueprint().isUnique());
		//assertEquals(Side., card.getBlueprint().getSide());
		//assertEquals(Culture., card.getBlueprint().getCulture());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		//assertEquals(, card.getBlueprint().getStrength());
		//assertEquals(, card.getBlueprint().getVitality());
		//assertEquals(, card.getBlueprint().getResistance());
		//assertEquals(Signet., card.getBlueprint().getSignet()); 
		assertEquals(8, card.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());

	}

	//@Test
	public void NorthernIthilienTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(8, scn.GetTwilight());
	}

	@Test
	public void ShadowAbilityIsNotOfferedAgainInThePhaseItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetSiteScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));
		PhysicalCardImpl gollum = scn.GetShadowCard("gollum");
		PhysicalCardImpl runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(runner);
		scn.AddThreats(4);
		scn.SetTwilight(20);
		scn.FreepsPass();

		PhysicalCardImpl site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		int burdens = scn.GetBurdens();
		scn.ShadowUseCardAction(site);
		assertEquals(2, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, gollum.getZone());
		assertEquals(burdens + 2, scn.GetBurdens());

		// Gollum is unique, so the rig puts him back in the discard pile for the cost to be payable again;
		// the Shadow player then plays a minion to be asked afresh.
		scn.MoveCardsToDiscard(gollum);
		scn.ShadowPlayCard(runner);

		// Limit once per phase: a limit on uses, so the ability is not offered again, though two threats
		// could still be removed and Gollum played (Ketura on Q-0093).
		assertFalse(scn.ShadowActionAvailable(site));
		assertEquals(2, scn.GetThreats());
		assertEquals(Zone.DISCARD, gollum.getZone());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityCannotBeUsedWithWatcherToTurnThreatsIntoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetSiteScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));
		PhysicalCardImpl gollum = scn.GetShadowCard("gollum");
		PhysicalCardImpl watcher = scn.GetShadowCard("watcher");
		PhysicalCardImpl fens = scn.GetShadowCard("fens");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.MoveCardsToDiscard(gollum);
		scn.AddThreats(4);
		scn.SetTwilight(0);
		scn.FreepsPass();

		PhysicalCardImpl site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		assertEquals(Zone.SHADOW_CHARACTERS, watcher.getZone());
		// 4 companions + 8 for the site
		assertEquals(12, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
		int burdens = scn.GetBurdens();
		// Pointless as it is, the ability is still offered.
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);

		// The threats are removed and Gollum is paid for, but the Watcher discards him as he comes into play:
		// he was not played, so the cost is not fulfilled and no burden is added.
		assertEquals(2, scn.GetThreats());
		// Gollum, Nasty Treacherous Creature costs 2
		assertEquals(10, scn.GetTwilight());
		assertEquals(Zone.DISCARD, gollum.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, watcher.getZone());
		assertEquals(burdens, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
