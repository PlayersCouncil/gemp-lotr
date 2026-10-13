package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_359_Tests
{

	// The King-block path with Northern Ithilien as site 8.  Each test skips to site 7 and moves here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "9_28");          // Gollum, Dark as Darkness: [gollum] minion, cost 2, site 3; abilities used only on purpose
					put("vilegollum", "7_59");      // Gollum, Vile Creature: [gollum] minion, cost 2, site 3; a skirmish ability
					put("nastygollum", "5_24");     // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; a regroup ability
					// Gollum, Half a Wraith Himself: the one Gollum who is not [gollum]: a [wraith] minion, cost 2, site 3.
					// His trigger answers a Nazgul being played and his ability is an assignment action.
					put("wraithgollum", "103_66");
					// Gollum, Old Villain: [gollum] minion, cost 2; a skirmish ability.  The Free Peoples player's own
					// Gollum, of a version no other test card is: an offer from a discard pile is read by version.
					put("oldgollum", "6_40");
					put("shelob", "8_25");          // Shelob, Eater of Light: [gollum] minion that is not Gollum, cost 6, site 8
					put("refuge", "4_29");          // No Refuge: [dunland] Shadow condition, cost 0, support area; nothing without a stacked Man
					put("watcher", "2_73");         // Watcher in the Water: discards all other minions (except tentacles)
					put("fens", "5_22");            // Evil-smelling Fens: every site is a marsh, which keeps the Watcher in play

					// Three companions beside Frodo: room for four threats.
					put("companion1", "1_7");       // Dwarf Guard
					put("companion2", "1_11");      // Farin, Dwarven Emissary
					put("companion3", "1_12");      // Gimli, Dwarf of Erebor
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
		 * Type: Site
		 * Block: King
		 * Site Number: 8K
		 * Shadow Number: 8
		 * Direction: Right
		 * Game Text: <b>Shadow:</b> Remove 2 threats and play Gollum from your discard pile to add 2 burdens.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Northern Ithilien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void RemovesTwoThreatsAndPlaysGollumFromTheDiscardPileToAddTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var vileGollum = scn.GetShadowCard("vilegollum");
		var nastyGollum = scn.GetShadowCard("nastygollum");
		var wraithGollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var freepsGollum = scn.GetFreepsCard("oldgollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(7);
		// Three Gollums in the Shadow player's discard pile, two of them [gollum] and one [wraith]; Shelob, who is
		// [gollum] and no Gollum, beside them; a Gollum in his hand; and a Gollum in the other player's discard pile.
		scn.MoveCardsToDiscard(vileGollum, nastyGollum, wraithGollum, shelob, freepsGollum);
		scn.MoveCardsToHand(gollum);
		scn.AddThreats(4);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		assertEquals(4, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(2, scn.GetThreats());
		// "Gollum" is the title and nothing more: the [wraith] Gollum is one of them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vileGollum, nastyGollum, wraithGollum));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, gollum, freepsGollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(wraithGollum);

		assertInZone(Zone.SHADOW_CHARACTERS, wraithGollum);
		assertInDiscard(vileGollum, nastyGollum, shelob);
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("vilegollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(gollum);
		scn.AddThreats(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		assertEquals(1, scn.GetThreats());
		assertInDiscard(gollum);
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenGollumIsInHandAndNotInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("vilegollum");
		var shelob = scn.GetShadowCard("shelob");
		var freepsGollum = scn.GetFreepsCard("oldgollum");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(7);
		// His Gollum in hand, Shelob in his discard pile, and a Gollum in the other player's discard pile.
		scn.MoveCardsToHand(gollum);
		scn.MoveCardsToDiscard(shelob, freepsGollum);
		scn.AddThreats(4);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.ShadowPlayAvailable(gollum));
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenThePoolCannotPayForGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("vilegollum");
		var refuge = scn.GetShadowCard("refuge");
		scn.MoveCompanionsToTable("companion1", "companion2", "companion3");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(refuge);
		scn.AddThreats(4);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertTrue(scn.ShadowActionAvailable(site));

		// One short of Gollum's cost of 2 (site 3 is not roaming at site 8); playing No Refuge for nothing asks
		// the Shadow player afresh.
		scn.SetTwilight(1);
		scn.ShadowPlayCard(refuge);
		assertEquals(1, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
		assertInDiscard(gollum);
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
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
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
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
		assertInDiscard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertEquals(0, scn.GetBurdens());

		//Gollum is back in the discard pile, so it can be done again, with the same result.
		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);

		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetTwilight());
		assertInDiscard(gollum);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
