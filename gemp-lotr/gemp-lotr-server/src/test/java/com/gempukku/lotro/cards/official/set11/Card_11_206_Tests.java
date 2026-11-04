package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_206_Tests
{

	// Site 2 is Eastfold 4_331 (plains, no battleground); site 4 is White Rocks 4_346 (battleground only).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watchman", "11_206");
					put("watchman2", "11_206");
					// Murderous Uruk: another [uruk-hai] Uruk-hai minion; damage +1 and muster (a regroup trigger) only.
					put("murderous", "11_195");
					// Cirith Ungol Guard: a [sauron] Uruk-hai with damage +1 alone, left in the draw deck to be drawn.
					put("guard", "10_81");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 2 is Uruk Camp 4_335 (battleground and plains; each Uruk-hai played must exert).
	protected VirtualTableScenario GetUrukCampScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watchman", "11_206");
					// Cirith Ungol Guard: a [sauron] Uruk-hai with damage +1 alone, left in the draw deck to be drawn.
					put("guard", "10_81");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WatchmanUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Watchman Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>When you play this minion at a battleground site, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watchman");

		assertEquals("Watchman Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedAtABattlegroundItMayDrawACardAndAnotherUrukHaiPlayedAfterOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchman = scn.GetShadowCard("watchman");
		var watchman2 = scn.GetShadowCard("watchman2");
		var murderous = scn.GetShadowCard("murderous");
		scn.MoveCardsToHand(watchman, murderous);
		assertInHand(watchman, murderous);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetTwilight(15);
		scn.FreepsPass();
		// A second copy already in play: its trigger answers its own play only.
		scn.MoveMinionsToTable(watchman2);
		// The turns played on the way have drawn the deck down: the Guard is put on top to be drawn.
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToTopOfDeck(guard);
		scn.ShadowRefreshDecision();

		// White Rocks, site 4: a battleground.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();
		assertEquals(guard, scn.GetShadowTopOfDeck());
		assertInZone(Zone.DECK, guard);
		assertInZone(Zone.SHADOW_CHARACTERS, watchman2);

		scn.ShadowPlayCard(watchman);
		assertInZone(Zone.SHADOW_CHARACTERS, watchman, watchman2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watchman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(watchman2));
		scn.ShadowAcceptOptionalTrigger();

		// One card drawn: the Watchman left the hand and the top card of the deck came into it.
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertInHand(guard);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Uruk-hai played at the same battleground sets nothing off.
		scn.ShadowPlayCard(murderous);
		assertInZone(Zone.SHADOW_CHARACTERS, murderous);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAtABattlegroundThatIsAlsoAPlainsTheDrawIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukCampScenario();

		var watchman = scn.GetShadowCard("watchman");
		scn.MoveCardsToHand(watchman);
		assertInHand(watchman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Uruk Camp, site 2: a battleground and a plains.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(watchman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(watchman));
		scn.ShadowDeclineOptionalTrigger();

		// The site's exertion is the site's own; nothing is drawn.
		assertEquals(1, scn.GetWoundsOn(watchman));
		assertInZone(Zone.SHADOW_CHARACTERS, watchman);
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAwayFromABattlegroundNoDrawIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watchman = scn.GetShadowCard("watchman");
		scn.MoveCardsToHand(watchman);
		assertInHand(watchman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Eastfold, site 2: a plains, and no battleground.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		int hand = scn.GetShadowHandCount();
		int deck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(watchman);

		assertInZone(Zone.SHADOW_CHARACTERS, watchman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
