package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_200_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanguard", "4_200");
					put("vanguard2", "4_200");    // a second copy: each copy's damage +1 is its own, and they do not add up
					put("savage", "1_151");       // Uruk Savage: another Uruk-hai; damage +1 is its whole text
				}},
				// The Towers path, with Uruk Camp as site 2 ("Battleground. Plains. Each time an Uruk-hai is played,
				// that minion must exert": nobody is played here). Site 1, Eastemnet Gullies, has no keyword (its
				// fellowship ability is never used); site 3, Barrows of Edoras, is a sanctuary; site 4 is
				// Westemnet Hills, a plains and no battleground (it heals mounted characters, and nobody is mounted).
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanguard", "4_200");
					put("savage", "1_151");       // Uruk Savage: an [isengard] Uruk-hai; damage +1 is its whole text
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] Uruk-hai; damage +1 is its whole text
					put("forger", "3_56");        // Isengard Forger: an [isengard] Orc, no Uruk-hai; a Shadow ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukVanguardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Vanguard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 17
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. To play, spot an Uruk-hai.<br>While at a battleground, this minion is <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vanguard");

		assertEquals("Uruk Vanguard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedBesideAnIsengardOrcAndCanOnceASauronUrukHaiIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var vanguard = scn.GetShadowCard("vanguard");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToHand(vanguard, sentry);
		assertInHand(vanguard, sentry);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		// Enough for the Sentry and then this minion: 2 + 2 roaming, 10 + 2 roaming.
		scn.SetTwilight(20);
		scn.FreepsPass();

		// An [isengard] Orc is no Uruk-hai: nothing to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(vanguard);
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertFalse(scn.ShadowPlayAvailable(vanguard));

		// A [sauron] Uruk-hai played: now there is one to spot.
		scn.ShadowPlayCard(sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertTrue(scn.ShadowPlayAvailable(vanguard));
		scn.ShadowPlayCard(vanguard);
		assertInZone(Zone.SHADOW_CHARACTERS, vanguard);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhileAnIsengardUrukHaiIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var vanguard = scn.GetShadowCard("vanguard");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(vanguard);
		assertInHand(vanguard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(vanguard);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.ShadowPlayAvailable(vanguard));
		scn.ShadowPlayCard(vanguard);
		assertInZone(Zone.SHADOW_CHARACTERS, vanguard);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsFierceAndDamagePlus1WhileAtABattlegroundAndNotBeforeOrAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vanguard = scn.GetShadowCard("vanguard");
		var vanguard2 = scn.GetShadowCard("vanguard2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(vanguard, vanguard2, savage);

		// At Eastemnet Gullies, no battleground.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(vanguard, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(vanguard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(vanguard2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		// The fellowship moves to Uruk Camp, a battleground: each copy is fierce and damage +2 (1 printed + 1),
		// not +3; the other Uruk-hai is neither fierce nor stronger in damage.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(vanguard, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(vanguard, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(vanguard2, Keyword.FIERCE));
		assertEquals(2, scn.GetKeywordCount(vanguard2, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// And on to Barrows of Edoras by the regroup move: no battleground, neither any more.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(vanguard, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(vanguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABattlegroundItsOwnerControlsAwayFromTheFellowshipMakesItNeitherFierceNorDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vanguard = scn.GetShadowCard("vanguard");

		scn.StartGame();
		scn.SkipToSite(3);
		var camp = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(vanguard);

		// Uruk Camp is in the Shadow player's support area; the fellowship is at Barrows of Edoras.
		assertTrue(scn.ShadowControls(camp));
		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(vanguard, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(vanguard, Keyword.DAMAGE));

		// Westemnet Hills, a plains and no battleground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(vanguard, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(vanguard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
