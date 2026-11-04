package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_238_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("party", "1_158");       // Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, site 5; damage +1 only
					put("hunter", "1_256");      // Morgul Hunter: [sauron] Orc, cost 5, site 6; a strength bonus only
					put("pinned", "1_192");      // Pinned Down: [moria] condition, cost 1, for the support area; no minion
					put("runner", "1_178");      // Goblin Runner: [moria] Orc; its only text is on being played
					put("sneak", "1_181");       // Goblin Sneak: [moria] Orc; its only text is on being played
				}},
				new HashMap<>()
				{{
					// Expanding Marshland is a Shadows-block site; each test lays the Shadow player's copy as site 6 of
					// the Fellowship-block path (in place of site 9 in the adventure decks, which no test reaches), so
					// that no minion here is roaming.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("marshland", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ExpandingMarshlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Expanding Marshland
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Marsh</b>. <b>Shadow:</b> If the total number of minions and twilight tokens is 3 or fewer, play a minion at twilight cost -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("marshland");

		assertEquals("Expanding Marshland", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MARSH));
	}

	@Test
	public void WithThreeTwilightAndNoMinionAMinionFromHandIsPlayedForThreeLessButNotWithFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		var hunter = scn.GetShadowCard("hunter");
		var pinned = scn.GetShadowCard("pinned");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("marshland"));
		scn.FreepsPass();

		var marshland = scn.GetCurrentSite();
		assertEquals("11_238", marshland.getBlueprintId());
		assertEquals(6, scn.GetCurrentSiteNumber());
		// The turns skipped have drawn the Shadow player's cards: the hand is laid afresh, with a minion in the
		// discard pile that the pool could pay for at -3.
		scn.MoveCardsToHand(troop, party, pinned);
		scn.MoveCardsToDiscard(hunter);
		scn.MoveCardsToBottomOfDeck(runner, sneak);
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, troop, party, pinned);
		assertInZone(Zone.DISCARD, hunter);

		// No minion and 4 twilight: 4 in all.  The Raiding Party would cost 4 - 3 = 1.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshland));

		// No minion and 3 twilight: 3 in all.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(marshland));

		// Minions in hand of two cultures and races are offered; not the condition in hand, nor the minion in the
		// discard pile.
		scn.ShadowUseCardAction(marshland);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, party));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pinned, hunter));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// 3 - (6 - 3) for the Troop
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, party, pinned);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MinionsInPlayCountTowardTheTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var party = scn.GetShadowCard("party");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var pinned = scn.GetShadowCard("pinned");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("marshland"));
		scn.FreepsPass();

		var marshland = scn.GetCurrentSite();
		assertEquals("11_238", marshland.getBlueprintId());
		// The turns skipped have drawn the Shadow player's cards: the hand is laid afresh.
		scn.MoveCardsToHand(party);
		scn.MoveCardsToBottomOfDeck(troop, hunter, pinned, sneak);
		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, party);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		// One minion and 2 twilight: 3 in all.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(marshland));

		// Two minions and 2 twilight: 4 in all, though the Raiding Party would still cost only 4 - 3 = 1.
		scn.MoveMinionsToTable(sneak);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshland));
		assertInZone(Zone.HAND, party);
	}

	@Test
	public void IsNotOfferedWhenThePoolCannotPayForTheMinionAtThreeLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var pinned = scn.GetShadowCard("pinned");
		var party = scn.GetShadowCard("party");
		var hunter = scn.GetShadowCard("hunter");
		var runner = scn.GetShadowCard("runner");
		var sneak = scn.GetShadowCard("sneak");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("marshland"));
		scn.FreepsPass();

		var marshland = scn.GetCurrentSite();
		assertEquals("11_238", marshland.getBlueprintId());
		// The turns skipped have drawn the Shadow player's cards: the hand is laid afresh.
		scn.MoveCardsToHand(troop, pinned);
		scn.MoveCardsToBottomOfDeck(party, hunter, runner, sneak);
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, troop, pinned);

		// No minion and 2 twilight: the total is met, but the Troop costs 6 - 3 = 3 and the Pinned Down in hand is
		// no minion.  At -4 the Troop would cost 2.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marshland));
		assertTrue(scn.ShadowPlayAvailable(pinned));
	}
}
