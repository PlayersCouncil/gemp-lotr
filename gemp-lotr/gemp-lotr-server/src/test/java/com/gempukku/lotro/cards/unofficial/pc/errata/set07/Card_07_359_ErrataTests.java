package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_359_ErrataTests
{
	// The King-block path with this site as site 8.  Each test skips to site 7 and moves here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; a regroup ability
					put("gollum2", "7_59");     // Gollum, Vile Creature: [gollum] minion, cost 2, site 3; a skirmish ability
					put("gollum3", "9_28");     // Gollum, Dark as Darkness: [gollum] minion, cost 2, site 3; abilities only
					// Gollum, Old Villain: [gollum] minion, cost 2; a skirmish ability.  The Free Peoples player's own
					// Gollum, of a version no other test card is: an offer from a discard pile is read by version.
					put("gollum4", "6_40");
					put("shelob", "8_25");      // Shelob, Eater of Light: [gollum] minion that is not Gollum, cost 6, site 8
					put("runner", "1_178");     // Goblin Runner: [moria] minion, cost 1; adds (2) when played, without asking
					put("refuge", "4_29");      // No Refuge: [dunland] Shadow condition, cost 0, support area; nothing without a stacked Man
					put("watcher", "2_73");     // Watcher in the Water: discards all other minions (except tentacles)
					put("fens", "5_22");        // Evil-smelling Fens: every site is a marsh, which keeps the Watcher in play
					put("aragorn", "1_89");     // three companions beside Frodo: room for four threats
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				new HashMap<>()
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
		 * Game Text: <b>Shadow:</b> Remove 2 threats and play Gollum from your discard pile to add 2 burdens
		 * (limit once per phase).
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
		var gollum2 = scn.GetShadowCard("gollum2");
		var gollum3 = scn.GetShadowCard("gollum3");
		var shelob = scn.GetShadowCard("shelob");
		var freepsGollum = scn.GetFreepsCard("gollum4");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		scn.SkipToSite(7);
		// Two Gollums in the Shadow player's discard pile; Shelob, who is no Gollum, beside them; a Gollum in his
		// hand; and a Gollum in the other player's discard pile.
		scn.MoveCardsToDiscard(gollum, gollum2, shelob, freepsGollum);
		scn.MoveCardsToHand(gollum3);
		scn.AddThreats(4);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		int twilight = scn.GetTwilight();
		int burdens = scn.GetBurdens();
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, gollum2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob, gollum3, freepsGollum));
		scn.ShadowChooseCard(gollum2);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum2);
		assertInDiscard(gollum, shelob);
		// Gollum, Vile Creature costs 2; site 3 is not roaming at site 8.
		assertEquals(twilight - 2, scn.GetTwilight());
		assertEquals(burdens + 2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(gollum);
		scn.AddThreats(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals(1, scn.GetThreats());
		assertInDiscard(gollum);
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenGollumIsInHandAndNotInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(gollum);
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

		var gollum = scn.GetShadowCard("gollum");
		var refuge = scn.GetShadowCard("refuge");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(refuge);
		scn.AddThreats(4);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertTrue(scn.ShadowActionAvailable(site));

		// One short of Gollum's cost of 2; playing No Refuge for nothing asks the Shadow player afresh.
		scn.SetTwilight(1);
		scn.ShadowPlayCard(refuge);
		assertEquals(1, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
		assertInDiscard(gollum);
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedAgainInThePhaseItWasUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(runner);
		scn.AddThreats(4);
		scn.SetTwilight(20);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		int burdens = scn.GetBurdens();
		scn.ShadowUseCardAction(site);
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(burdens + 2, scn.GetBurdens());

		// Gollum is unique, so the rig puts him back in the discard pile for the cost to be payable again;
		// the Shadow player then plays a minion to be asked afresh.
		scn.MoveCardsToDiscard(gollum);
		scn.ShadowPlayCard(runner);

		// Limit once per phase: a limit on uses, so the ability is not offered again, though two threats
		// could still be removed and Gollum played (Ketura on Q-0093).
		assertFalse(scn.ShadowActionAvailable(site));
		assertEquals(2, scn.GetThreats());
		assertInDiscard(gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityCannotBeUsedWithWatcherToTurnThreatsIntoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("sam"));
		var gollum = scn.GetShadowCard("gollum");
		var watcher = scn.GetShadowCard("watcher");
		var fens = scn.GetShadowCard("fens");
		var refuge = scn.GetShadowCard("refuge");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToSupportArea(fens);
		scn.MoveMinionsToTable(watcher);
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(refuge);
		scn.AddThreats(4);
		scn.SetTwilight(0);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Northern Ithilien", site.getBlueprint().getTitle());
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
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
		assertInDiscard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, watcher);
		assertEquals(burdens, scn.GetBurdens());

		// The use counts for the limit all the same: with two threats left and Gollum back in the discard pile,
		// it is not offered again (No Refuge, played for nothing, has the Shadow player asked afresh).
		scn.ShadowPlayCard(refuge);
		assertFalse(scn.ShadowActionAvailable(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
