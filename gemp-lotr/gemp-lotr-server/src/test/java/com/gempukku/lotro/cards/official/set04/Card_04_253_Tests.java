package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_253_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentry", "4_253");
					put("sentry2", "4_253");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronSentryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Sentry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Southron</b>.<br><b>Maneuver:</b> Exert this minion to add (2) for each site you control.
		*/

		var scn = GetScenario();

		var sentry = scn.GetFreepsCard("sentry");

		assertEquals("Southron Sentry", sentry.getBlueprint().getTitle());
		assertNull(sentry.getBlueprint().getSubtitle());
		assertFalse(sentry.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sentry.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, sentry.getBlueprint().getCulture());
		assertEquals(CardType.MINION, sentry.getBlueprint().getCardType());
		assertEquals(Race.MAN, sentry.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(sentry, Keyword.SOUTHRON));
		assertEquals(4, sentry.getBlueprint().getTwilightCost());
		assertEquals(9, sentry.getBlueprint().getStrength());
		assertEquals(2, sentry.getBlueprint().getVitality());
		assertEquals(4, sentry.getBlueprint().getSiteNumber());
	}

	@Test
	public void ManeuverAbilityExertsItToAddTwoForEachOfTheTwoSitesItsPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var sentry2 = scn.GetShadowCard("sentry2");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 to the Free Peoples player, sites 2 and 3 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertTrue(scn.ShadowControls(scn.GetSite(3)));

		scn.MoveMinionsToTable(sentry, sentry2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(sentry2));
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);

		// (2) for each of the two sites the Shadow player controls; the Free Peoples player's site 1 is not counted.
		assertEquals(pool + 4, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(sentry2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOneSiteControlledItAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(3);
		// Site 1 to the Free Peoples player, site 2 to the Shadow player.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));

		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);

		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoSiteOfItsPlayersItIsOfferedAndExertsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		scn.SkipToSite(3);
		// Only the Free Peoples player controls a site.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));

		scn.MoveMinionsToTable(sentry);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.ShadowActionAvailable(sentry));

		scn.ShadowUseCardAction(sentry);

		assertEquals(pool, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ExhaustedItIsNotOfferedWhileAnotherCopyIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentry = scn.GetShadowCard("sentry");
		var sentry2 = scn.GetShadowCard("sentry2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));

		scn.MoveMinionsToTable(sentry, sentry2);
		// Vitality 2 with 1 wound: exhausted, and unable to exert.
		scn.AddWoundsToChar(sentry, 1);
		assertEquals(1, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(sentry2));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sentry));
		assertTrue(scn.ShadowActionAvailable(sentry2));
	}
}
