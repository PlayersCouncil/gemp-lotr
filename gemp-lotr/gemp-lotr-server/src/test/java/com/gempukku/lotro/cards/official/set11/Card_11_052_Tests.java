package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_052_Tests
{

	// Pass of Caradhras, a mountain, as site 4; sites 2 and 3 are no mountains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strange", "11_52");
					// Gollum, Nasty Treacherous Creature: [gollum] minion; his regroup ability is never used
					put("gollum", "5_24");
					// Shelob, Last Child of Ungoliant: a [gollum] minion of another title; enduring, fierce
					put("shelob", "8_26");
					// They Stole It: a [gollum] Shadow condition, no minion; its skirmish ability is never used
					put("stoleit", "6_46");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; its (2) is never added, being cheated in
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");      // Rivendell Valley: sanctuary, no mountain
					put("site4", "1_348");      // Pass of Caradhras: mountain; its Shadow ability wants an [isengard] minion
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrangeandTerribleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Strange and Terrible
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot a [gollum] minion at a mountain site to play a minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strange");

		assertEquals("Strange and Terrible", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGollumAtAMountainItPlaysAMinionOfTheShadowPlayersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strange = scn.GetShadowCard("strange");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var stoleit = scn.GetShadowCard("stoleit");
		var freepsrunner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(strange);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToDiscard(troop, savage, stoleit, freepsrunner);
		// The Free Peoples player's Goblin Runner lies in his own discard pile; the Shadow player's is in his deck.
		assertInZone(Zone.DISCARD, troop, savage, stoleit, freepsrunner);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(strange));

		scn.ShadowPlayCard(strange);
		// Minions of two cultures and races are offered; a [gollum] condition and the other player's minion are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stoleit, freepsrunner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, savage, stoleit, freepsrunner, strange);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithGollumAwayFromAMountainItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strange = scn.GetShadowCard("strange");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(strange);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToDiscard(troop);
		assertInZone(Zone.DISCARD, troop);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		// Rivendell Valley is no mountain.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strange));
		assertInZone(Zone.DISCARD, troop);
	}

	@Test
	public void AGollumConditionAndAMinionOfAnotherCultureDoNotLetItBePlayedAndShelobDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strange = scn.GetShadowCard("strange");
		var shelob = scn.GetShadowCard("shelob");
		var stoleit = scn.GetShadowCard("stoleit");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(strange);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(stoleit);
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToDiscard(troop);
		assertInZone(Zone.DISCARD, troop);
		assertInZone(Zone.SUPPORT, stoleit);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, stoleit);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strange));

		scn.MoveMinionsToTable(shelob);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(strange));
		// The Troop is the only minion in the discard pile, so it is played with no question.
		scn.ShadowPlayCard(strange);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, strange);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ItCannotBePlayedWithNoMinionInTheDiscardPileNorWithOneThePoolCannotPayFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strange = scn.GetShadowCard("strange");
		var gollum = scn.GetShadowCard("gollum");
		var stoleit = scn.GetShadowCard("stoleit");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(strange);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(gollum);
		scn.MoveCardsToDiscard(stoleit);
		assertInZone(Zone.DISCARD, stoleit);
		scn.FreepsPass();

		// No minion in the discard pile: nothing to play.
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strange));

		// The Troop costs 6 at site 4, one more than the pool holds.
		scn.MoveCardsToDiscard(troop);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.DISCARD, troop);
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(strange));

		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(strange));
		scn.ShadowPlayCard(strange);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.DISCARD, strange);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
