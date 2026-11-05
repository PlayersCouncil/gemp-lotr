package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_179");
					put("band2", "4_179");        // a second copy: each copy's +6 is its own, and they do not add up

					put("savage", "1_151");       // Uruk Savage: another Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					// Southron Explorer: strength 5, vitality 3. "Regroup: Exert this minion and remove (5) to take
					// control of a site."
					put("explorer1", "4_250");
					put("explorer2", "4_250");
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

	protected VirtualTableScenario GetBattlegroundsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_179");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 is its whole text
					put("sentry", "10_84");       // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2; damage +1 is its whole text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the [gondor] Man who pays for the event; his maneuver ability is not used
					// Defend It and Hope: "Maneuver: Exert a [gondor] Man to wound a minion once (or twice if that minion is an Uruk-hai)."
					put("defend", "4_115");
				}},
				// The Towers path with Uruk Camp as site 2: its two battlegrounds are Uruk Camp and White Rocks (site 4).
				// Site 1, Eastemnet Gullies, is no battleground. Site 6, Caves of Aglarond, where the maneuver phase is
				// played, is a sanctuary (nobody is wounded on arriving) whose maneuver action is the Shadow player's and unused.
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPlayScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "4_179");
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
	public void UrukAssaultBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Assault Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. To play, spot an Uruk-hai.<br>While at a battleground, this minion is <b>fierce</b>.<br>While you control a battleground, this minion is strength +6.<br>While you control 2 battlegrounds, this minion may not take wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("band");

		assertEquals("Uruk Assault Band", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedBesideAnIsengardOrcAndCanOnceASauronUrukHaiIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var band = scn.GetShadowCard("band");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToHand(band, sentry);
		assertInHand(band, sentry);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// An [isengard] Orc is no Uruk-hai: nothing to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(band);
		assertInZone(Zone.SHADOW_CHARACTERS, forger);
		assertFalse(scn.ShadowPlayAvailable(band));

		// A [sauron] Uruk-hai played: now there is one to spot.
		scn.ShadowPlayCard(sentry);
		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertTrue(scn.ShadowPlayAvailable(band));
		scn.ShadowPlayCard(band);
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhileAnIsengardUrukHaiIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlayScenario();

		var band = scn.GetShadowCard("band");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(band);
		assertInHand(band);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(band);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.ShadowPlayAvailable(band));
		scn.ShadowPlayCard(band);
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsFierceWhileAtABattlegroundAndNotBeforeOrAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(band, savage);

		// At Eastemnet Gullies, no battleground.
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		// The fellowship moves to Uruk Camp, a battleground: this minion is fierce, the other Uruk-hai is not.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(band, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// And on to Barrows of Edoras by the regroup move: no battleground, no longer fierce.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsStrengthPlus6OnceItsOwnerTakesControlOfABattlegroundAndNotOfAnotherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var savage = scn.GetShadowCard("savage");
		var explorer1 = scn.GetShadowCard("explorer1");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		scn.SkipToSite(3);
		var gullies = scn.GetSite(1);
		var camp = scn.GetSite(2);
		scn.MoveMinionsToTable(band, savage, explorer1, explorer2);
		// The fellowship moves to Westemnet Hills, site 4; both markers have passed sites 1 and 2.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(10);

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(14, scn.GetStrength(band));
		assertEquals(5, scn.GetStrength(savage));

		// Eastemnet Gullies is controlled first, and is no battleground.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer1);
		assertTrue(scn.ShadowControls(gullies));
		assertEquals(14, scn.GetStrength(band));

		// Then Uruk Camp, a battleground still in its controller's support area: +6 for this minion alone.
		scn.FreepsPass();
		scn.ShadowUseCardAction(explorer2);
		assertTrue(scn.ShadowControls(camp));
		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertEquals(20, scn.GetStrength(band));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotStrengthPlus6WhileTheOtherPlayerControlsTheBattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");

		scn.StartGame();
		scn.SkipToSite(3);
		var gullies = scn.GetSite(1);
		var camp = scn.GetSite(2);
		// The Free Peoples player took Uruk Camp on a turn of his as the Shadow player; this minion's owner
		// holds Eastemnet Gullies, no battleground.
		scn.FreepsTakeControlOfSite(camp);
		scn.ShadowTakeControlOfSite(gullies);
		scn.MoveMinionsToTable(band);

		assertTrue(scn.FreepsControls(camp));
		assertTrue(scn.HasKeyword(camp, Keyword.BATTLEGROUND));
		assertTrue(scn.ShadowControls(gullies));
		assertEquals(14, scn.GetStrength(band));

		scn.FreepsPass();
		assertEquals(14, scn.GetStrength(band));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABattlegroundItsOwnerControlsMakesItStrengthPlus6ButNotFierceAwayFromABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var band2 = scn.GetShadowCard("band2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		var camp = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(band, band2, savage);

		// Uruk Camp is in the Shadow player's support area; the fellowship is at Barrows of Edoras.
		assertTrue(scn.ShadowControls(camp));
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 14 + 6 each, not 14 + 12: a copy's text is about that copy alone.
		assertEquals(20, scn.GetStrength(band));
		assertEquals(20, scn.GetStrength(band2));
		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));

		// Westemnet Hills, a plains and no battleground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(band, Keyword.FIERCE));
		assertEquals(20, scn.GetStrength(band));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileItsOwnerControlsTwoBattlegroundsItCannotBeChosenForAWoundAndOtherUrukHaiCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundsScenario();

		var band = scn.GetShadowCard("band");
		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");

		scn.StartGame();
		scn.SkipToSite(5);
		var camp = scn.GetSite(2);
		var rocks = scn.GetSite(4);
		scn.ShadowTakeControlOfSite(camp);
		scn.ShadowTakeControlOfSite(rocks);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);
		scn.MoveMinionsToTable(band, savage, sentry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowControls(camp));
		assertTrue(scn.ShadowControls(rocks));
		assertTrue(scn.HasKeyword(rocks, Keyword.BATTLEGROUND));
		assertInHand(defend);
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(savage));
		// Two battlegrounds are still one battleground for the +6: 14 + 6.
		assertEquals(20, scn.GetStrength(band));

		assertTrue(scn.FreepsPlayAvailable(defend));
		scn.FreepsPlayCard(defend);
		// The two other Uruk-hai may take the wounds; this one may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, sentry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(band));
		scn.FreepsChooseCard(savage);

		// An Uruk-hai: wounded twice.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(band));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneBattlegroundOfItsOwnersAndTheOtherHeldByTheFreePeoplesPlayerItTakesWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBattlegroundsScenario();

		var band = scn.GetShadowCard("band");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");

		scn.StartGame();
		scn.SkipToSite(5);
		var gullies = scn.GetSite(1);
		var camp = scn.GetSite(2);
		var rocks = scn.GetSite(4);
		// Its owner holds Uruk Camp and Eastemnet Gullies (two sites, one of them a battleground); the
		// Free Peoples player holds White Rocks, the second battleground.
		scn.ShadowTakeControlOfSite(gullies);
		scn.ShadowTakeControlOfSite(camp);
		scn.FreepsTakeControlOfSite(rocks);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);
		scn.MoveMinionsToTable(band, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowControls(gullies));
		assertTrue(scn.ShadowControls(camp));
		assertTrue(scn.FreepsControls(rocks));
		assertInHand(defend);
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(20, scn.GetStrength(band));

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(band, savage));
		scn.FreepsChooseCard(band);

		// An Uruk-hai: wounded twice, and it lives (vitality 3).
		assertEquals(2, scn.GetWoundsOn(band));
		assertInZone(Zone.SHADOW_CHARACTERS, band);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
