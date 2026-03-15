package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentry", "15_170");
					put("ugluk", "15_172");
					put("scout", "17_130");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentryUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sentry Uruk
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: To play, spot an [uruk-hai] minion.
		 * 	While you can spot a fierce minion, this minion is <b>fierce</b>.
		 * 	While you can spot a hunter, this minion gains <b>hunter 1</b>.
		 * 	While you can spot a character that is damage +1, this minion is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");

		assertEquals("Sentry Uruk", sentry.getBlueprint().getTitle());
		assertNull(sentry.getBlueprint().getSubtitle());
		assertTrue(sentry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sentry.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, sentry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sentry.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, sentry.getBlueprint().getRace());
		assertEquals(4, sentry.getBlueprint().getTwilightCost());
		assertEquals(10, sentry.getBlueprint().getStrength());
		assertEquals(3, sentry.getBlueprint().getVitality());
		assertEquals(5, sentry.getBlueprint().getSiteNumber());
	}

	@Test
	public void SentryUrukRequiresUrukHaiMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ugluk = scn.GetShadowCard("ugluk");

		scn.MoveCardsToHand(sentry, ugluk);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.SkipToPhase(Phase.SHADOW);

		// No Uruk-hai minion on table — can't play Sentry
		assertFalse(scn.ShadowPlayAvailable(sentry));

		// Put Ugluk on table to satisfy requirement
		scn.ShadowPlayCard(ugluk);

		// Now Sentry should be playable
		assertTrue(scn.ShadowPlayAvailable(sentry));
	}

	@Test
	public void SentryUrukGainsFierceFromFierceMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ugluk = scn.GetShadowCard("ugluk");

		// Ugluk is fierce natively
		scn.MoveMinionsToTable(sentry, ugluk);

		scn.StartGame();

		// Ugluk is fierce — Sentry should gain fierce
		assertTrue(scn.HasKeyword(ugluk, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(sentry, Keyword.FIERCE));
	}

	@Test
	public void SentryUrukGainsHunterFromHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ugluk = scn.GetShadowCard("ugluk");

		// Ugluk has Hunter 3
		scn.MoveMinionsToTable(sentry, ugluk);

		scn.StartGame();

		// Ugluk is a hunter — Sentry should gain hunter 1
		assertTrue(scn.HasKeyword(ugluk, Keyword.HUNTER));
		assertTrue(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.HUNTER));
	}

	@Test
	public void SentryUrukGainsDamageFromDamageCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var ugluk = scn.GetShadowCard("ugluk");
		var scout = scn.GetShadowCard("scout");

		// Ugluk is NOT damage +1; Sentry should not gain damage from Ugluk alone
		scn.MoveMinionsToTable(sentry, ugluk);

		scn.StartGame();

		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));

		// Add White Hand Scout (naturally damage +1)
		scn.MoveMinionsToTable(scout);
		assertTrue(scn.HasKeyword(scout, Keyword.DAMAGE));

		// Now Sentry should gain damage +1
		assertTrue(scn.HasKeyword(sentry, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(sentry, Keyword.DAMAGE));
	}

	@Test
	public void SentryUrukGainsNoKeywordsWithoutMatchingMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");

		// Sentry alone on table — no fierce, hunter, or damage minions to spot
		// (Sentry doesn't count itself since it doesn't have the keywords yet)
		scn.MoveMinionsToTable(sentry);

		scn.StartGame();

		assertFalse(scn.HasKeyword(sentry, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(sentry, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(sentry, Keyword.DAMAGE));
	}
}
