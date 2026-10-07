package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

/**
 * A Map and a Race to Mount Doom meta-site cannot be hindered, as a site cannot.
 *
 * Comprehensive Rules 5.0, "hinder": sites cannot be hindered.  Ketura, 2026-10-07, on Q-0110: "Maps shouldn't be
 * hindered, if only because they don't do anything and would be a distraction ... an explicit entry should be
 * added for Maps and Meta-sites."  A Map announces the site path its owner is using and has no effect in the
 * game (Q-0055); a meta-site carries a player's handicap for the whole game.  Both lie in their owner's support
 * area, where a hinder of "a card" would otherwise find them (F-0176; Pinnacle of Doom 103_129 used to leave
 * them out by hand).
 */
public class MapsAndMetaSitesCannotBeHinderedTests
{
	// PC-Movie, a format that uses Maps: each player has the Fellowship Map 102_74 in his support area.
	protected VirtualTableScenario GetPCMovieScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test card (testCards.hjson), a Shadow condition in the support area:
					// "Maneuver: Remove (1) to hinder a card."
					put("fog", "0_8010");
					put("promise", "2_112");    // A Promise: [shire] condition in the support area; needs Sam to do anything
					put("pinned", "1_192");     // Pinned Down: [moria] condition in the support area; needs a [moria] archer
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie
		);
	}

	// Race to Mount Doom: each player has the meta-site 91_7, "Your opponent's minions are not roaming."
	protected VirtualTableScenario GetMetaSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test card (testCards.hjson), a Shadow condition in the support area:
					// "Maneuver: Remove (1) to hinder a card."
					put("fog", "0_8010");
					put("promise", "2_112");    // A Promise: [shire] condition in the support area; needs Sam to do anything
					put("pinned", "1_192");     // Pinned Down: [moria] condition in the support area; needs a [moria] archer
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_7", "91_7"
		);
	}

	@Test
	public void AHinderOfACardOffersNeitherPlayersMapNorASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPCMovieScenario();

		var promise = scn.GetFreepsCard("promise");
		var fog = scn.GetShadowCard("fog");
		var pinned = scn.GetShadowCard("pinned");
		var runner = scn.GetShadowCard("runner");
		var freepsMap = scn.GetFreepsMap();
		var shadowMap = scn.GetShadowMap();

		scn.MoveCardsToSupportArea(promise, fog, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Each player's Map lies in his support area beside a condition of his
		assertInZone(Zone.SUPPORT, freepsMap, promise);
		assertInZone(Zone.SUPPORT, shadowMap, pinned);
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(fog);
		assertEquals(twilight - 1, scn.GetTwilight());
		// "A card": either player's condition, a minion, a companion, The One Ring.  Not a Map, and not a site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, pinned, runner, scn.GetRingBearer(), scn.GetRing()));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsMap, shadowMap, scn.GetCurrentSite()));
		scn.ShadowChooseCard(promise);

		assertTrue(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(freepsMap));
		assertFalse(scn.IsHindered(shadowMap));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderOfACardOffersNeitherPlayersMetaSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMetaSiteScenario();

		var promise = scn.GetFreepsCard("promise");
		var fog = scn.GetShadowCard("fog");
		var pinned = scn.GetShadowCard("pinned");
		var runner = scn.GetShadowCard("runner");
		var freepsMetaSite = scn.GetFreepsCard("mod");
		var shadowMetaSite = scn.GetShadowCard("mod");

		scn.MoveCardsToSupportArea(promise, fog, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, freepsMetaSite, promise);
		assertInZone(Zone.SUPPORT, shadowMetaSite, pinned);
		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(fog);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, pinned, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsMetaSite, shadowMetaSite));
		scn.ShadowChooseCard(pinned);

		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(freepsMetaSite));
		assertFalse(scn.IsHindered(shadowMetaSite));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
