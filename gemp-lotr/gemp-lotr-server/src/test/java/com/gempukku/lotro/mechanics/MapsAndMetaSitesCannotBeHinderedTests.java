package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.HinderCardsInPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
 *
 * This is a guard: no card should be able to hinder a Map, a meta-site or a site, so none is there to try.  Each
 * test here makes the attempt itself (Ketura, 2026-10-11, Q-0241): a maneuver ability stapled onto a minion with
 * no text of its own, Goblin Patrol Troop 1_177, whose effect is the engine's own hinder (HinderCardsInPlayEffect,
 * what every card's "hinder" is carried out by) aimed at those cards by the test, with the Troop as the card that
 * hinders.
 */
public class MapsAndMetaSitesCannotBeHinderedTests
{
	// PC-Movie, a format that uses Maps: each player has the Fellowship Map 102_74 in his support area.
	protected VirtualTableScenario GetPCMovieScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("promise", "2_112");    // A Promise: [shire] condition in the support area; needs Sam to do anything
					put("pinned", "1_192");     // Pinned Down: [moria] condition in the support area; needs a [moria] archer
					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase and a card
					// for the test's hinder to be stapled onto
					put("troop", "1_177");
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
					put("promise", "2_112");    // A Promise: [shire] condition in the support area; needs Sam to do anything
					put("pinned", "1_192");     // Pinned Down: [moria] condition in the support area; needs a [moria] archer
					// Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase and a card
					// for the test's hinder to be stapled onto
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_7", "91_7"
		);
	}

	@Test
	public void AHinderAimedAtBothPlayersMapsAndTheSiteHindersTheConditionsBesideThemAndNoneOfThose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPCMovieScenario();

		var promise = scn.GetFreepsCard("promise");
		var pinned = scn.GetShadowCard("pinned");
		var troop = scn.GetShadowCard("troop");
		var freepsMap = scn.GetFreepsMap();
		var shadowMap = scn.GetShadowMap();

		scn.MoveCardsToSupportArea(promise, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		var site = scn.GetCurrentSite();

		// Stapled onto the Troop: "Maneuver: Hinder each player's Map, the fellowship's current site, A Promise
		// and Pinned Down."
		scn.ApplyAdHocAction(new AbstractActionProxy() { // lint-ok: a guard: no card should be able to hinder a Map or a site, so the attempt is stapled on (Q-0241)
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				if (!VirtualTableScenario.P2.equals(playerId) || game.getGameState().getCurrentPhase() != Phase.MANEUVER)
					return null;
				var action = new ActivateCardAction(troop, playerId);
				action.appendEffect(new HinderCardsInPlayEffect(playerId, troop, freepsMap, shadowMap, site, promise, pinned));
				return Collections.singletonList(action);
			}
		});
		scn.FreepsPass();

		// Each player's Map lies in his support area beside a condition of his
		assertInZone(Zone.SUPPORT, freepsMap, promise);
		assertInZone(Zone.SUPPORT, shadowMap, pinned);
		assertFalse(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(pinned));
		assertTrue(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(troop);
		// Either player's condition is hindered.  Not a Map, and not the site.
		assertTrue(scn.IsHindered(promise));
		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(freepsMap));
		assertFalse(scn.IsHindered(shadowMap));
		assertFalse(scn.IsHindered(site));
		assertInZone(Zone.SUPPORT, freepsMap, shadowMap);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHinderAimedAtBothPlayersMetaSitesHindersTheConditionsBesideThemAndNeitherMetaSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMetaSiteScenario();

		var promise = scn.GetFreepsCard("promise");
		var pinned = scn.GetShadowCard("pinned");
		var troop = scn.GetShadowCard("troop");
		var freepsMetaSite = scn.GetFreepsCard("mod");
		var shadowMetaSite = scn.GetShadowCard("mod");

		scn.MoveCardsToSupportArea(promise, pinned);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Stapled onto the Troop: "Maneuver: Hinder each player's meta-site, A Promise and Pinned Down."
		scn.ApplyAdHocAction(new AbstractActionProxy() { // lint-ok: a guard: no card should be able to hinder a meta-site, so the attempt is stapled on (Q-0241)
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				if (!VirtualTableScenario.P2.equals(playerId) || game.getGameState().getCurrentPhase() != Phase.MANEUVER)
					return null;
				var action = new ActivateCardAction(troop, playerId);
				action.appendEffect(new HinderCardsInPlayEffect(playerId, troop, freepsMetaSite, shadowMetaSite, promise, pinned));
				return Collections.singletonList(action);
			}
		});
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, freepsMetaSite, promise);
		assertInZone(Zone.SUPPORT, shadowMetaSite, pinned);
		assertFalse(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(pinned));
		assertTrue(scn.ShadowActionAvailable(troop));

		scn.ShadowUseCardAction(troop);
		assertTrue(scn.IsHindered(promise));
		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(freepsMetaSite));
		assertFalse(scn.IsHindered(shadowMetaSite));
		assertInZone(Zone.SUPPORT, freepsMetaSite, shadowMetaSite);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
