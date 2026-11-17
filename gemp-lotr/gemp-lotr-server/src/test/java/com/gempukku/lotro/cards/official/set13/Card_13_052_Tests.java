package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snuffler", "13_52");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, cost 2, site 3; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum; abilities on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, cost 6, site 8
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, cost 1
					put("larder", "8_23");      // Larder: a [gollum] possession, no minion
					put("stinker", "5_25");     // Gollum, Stinker: a [gollum] minion, laid in the other player's discard pile only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4
					put("barliman", "1_70");    // Barliman Butterbur: an ally, vitality 2; ability on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LittleSnufflerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Little Snuffler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: If the fellowship is in region 1, spot Gollum to wound a companion.<br>If the fellowship is in region 2, spot Gollum to exert a companion.<br>If the fellowship is in region 3, play a [gollum] minion from your discard pile.
		*/

		var scn = GetScenario();

		var snuffler = scn.GetFreepsCard("snuffler");

		assertEquals("Little Snuffler", snuffler.getBlueprint().getTitle());
		assertNull(snuffler.getBlueprint().getSubtitle());
		assertFalse(snuffler.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, snuffler.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, snuffler.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, snuffler.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(snuffler, Timeword.SHADOW));
		assertEquals(2, snuffler.getBlueprint().getTwilightCost());
	}

	@Test
	public void InRegion1ItSpotsGollumToWoundACompanionAndAnExhaustedOneIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(snuffler);

		scn.StartGame();
		assertInHand(snuffler);
		scn.SkipToSite(2);
		scn.FreepsPass();
		// Site 3, the last of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(gollum);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInHand(snuffler);

		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		// Companions, the exhausted one among them; not the ally, nor the minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// A wound, not an exertion: Aragorn, exhausted, is killed.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(snuffler);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion1ItIsNotPlayableWithoutAGollumToSpotAndTheWraithGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(snuffler);
		// A [gollum] minion in the discard pile: region 3's sentence does not apply here.
		scn.MoveCardsToDiscard(gollum);

		scn.StartGame();
		assertInDiscard(gollum);
		assertInHand(snuffler);
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// A [gollum] minion who is no Gollum.
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(wraithgollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion2ItSpotsGollumToExertACompanionWhoIsNotExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(snuffler);

		scn.StartGame();
		assertInHand(snuffler);
		scn.SkipToSite(3);
		scn.FreepsPass();
		// Site 4, the first of region 2.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(gollum);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInHand(snuffler);

		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, barliman, gollum));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInDiscard(snuffler);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion2ItIsNotPlayableWithoutGollumAndWithHimItPlaysNothingFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(snuffler);

		scn.StartGame();
		assertInHand(snuffler);
		scn.SkipToSite(5);
		scn.FreepsPass();
		// Site 6, the last of region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		// A [gollum] minion who is no Gollum, and the pool to pay for the event: no Gollum to spot.
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Shelob into the discard pile, a [gollum] minion there that the pool could pay for, and the [wraith] Gollum
		// in play to spot.
		scn.MoveCardsToDiscard(shelob);
		assertInDiscard(shelob);
		scn.MoveMinionsToTable(wraithgollum);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(shelob, snuffler);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion3ItPlaysAGollumMinionFromTheDiscardPileWithNoGollumToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var larder = scn.GetShadowCard("larder");
		var theirStinker = scn.GetFreepsCard("stinker");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(snuffler);
		scn.MoveCardsToDiscard(gollum, shelob, runner, larder, theirStinker);

		scn.StartGame();
		assertInDiscard(gollum, shelob, runner, larder, theirStinker);
		assertInHand(snuffler);
		scn.SkipToSite(6);
		scn.FreepsPass();
		// Site 7, the first of region 3.
		assertEquals(7, scn.GetCurrentSiteNumber());
		// Enough for the event and any of them: Shelob is 6 + 2 roaming.
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(snuffler));
		scn.ShadowPlayCard(snuffler);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		// [gollum] minions of two titles; not a [moria] minion, a [gollum] possession, or the [gollum] minion in
		// the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gollum, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, larder, theirStinker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(gollum);

		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInDiscard(shelob, runner, larder, snuffler);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion3ItIsNotPlayableWithNoGollumMinionInTheDiscardPileOrThePoolOneShortOfTheEventAndTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snuffler = scn.GetShadowCard("snuffler");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var larder = scn.GetShadowCard("larder");
		scn.MoveCardsToHand(snuffler);
		scn.MoveCardsToDiscard(runner, larder);

		scn.StartGame();
		assertInDiscard(runner, larder);
		assertInHand(snuffler);
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// 2 for the event and 2 for Gollum, who is not roaming at site 7.
		scn.MoveCardsToDiscard(gollum);
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(snuffler));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(snuffler));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
