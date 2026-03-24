package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher1", "103_63");
					put("watcher2", "103_63");
					put("oosas", "7_204");  // Out of Sight and Shot - Ringwraith condition
					put("aragorn", "1_89");
					put("sam", "1_311");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CirithUngolWatcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cirith Ungol Watcher, Spirit of Vigilance
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot a [ringwraith] card and add a burden.
		 * 		Each time a companion exerts, draw a card (limit once per site).
		 * 		Response: If a companion is exerted by a Free Peoples card, discard another Cirith Ungol Watcher to
		 * 		add (6) and hinder that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher1");

		assertEquals("Cirith Ungol Watcher", card.getBlueprint().getTitle());
		assertEquals("Spirit of Vigilance", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ======== EXTRA COST TESTS ========

	@Test
	public void WatcherRequiresSpottingRingwraithToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		scn.MoveCardsToHand(watcher1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		// No Ringwraith card in play - cannot play Watcher
		assertFalse(scn.ShadowPlayAvailable(watcher1));
	}

	@Test
	public void WatcherCanPlayWhenSpottingRingwraithAndAddsBurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var oosas = scn.GetShadowCard("oosas");
		scn.MoveCardsToHand(watcher1);
		scn.MoveCardsToSupportArea(oosas);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(watcher1));

		scn.ShadowPlayCard(watcher1);

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, watcher1);
	}

// ======== DRAW TRIGGER TESTS ========

	@Test
	public void WatcherDrawsCardWhenCompanionExertsFromFreepsCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int shadowHandBefore = scn.GetShadowHandCount();

		// Aragorn exerts (FP source)
		scn.FreepsUseCardAction(aragorn);

		// Watcher's required trigger should fire
		assertEquals(shadowHandBefore + 1, scn.GetShadowHandCount());
	}

	@Test
	public void WatcherDrawsCardWhenCompanionExertsFromShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var oosas = scn.GetShadowCard("oosas");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(watcher1, oosas);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		int shadowHandBefore = scn.GetShadowHandCount();

		// Play Sam - Out of Sight and Shot triggers, forcing FP to exert a companion
		scn.FreepsPlayCard(sam);
		scn.FreepsChooseCard(aragorn);

		// Draw trigger fires on ANY companion exertion, regardless of source
		assertEquals(shadowHandBefore + 1, scn.GetShadowHandCount());
	}


	@Test
	public void WatcherDrawTriggerLimitedOncePerSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int shadowHandBefore = scn.GetShadowHandCount();

		// First exertion - should draw
		scn.FreepsUseCardAction(aragorn);
		assertEquals(shadowHandBefore + 1, scn.GetShadowHandCount());

		// Second exertion same site - should NOT draw (limit reached)
		// Aragorn has 4 vitality, so can exert again without exhausting
		int shadowHandAfterFirst = scn.GetShadowHandCount();
		scn.ShadowPass();
		scn.FreepsUseCardAction(aragorn);
		assertEquals(shadowHandAfterFirst, scn.GetShadowHandCount());
	}


// ======== RESPONSE TESTS ========

	@Test
	public void WatcherResponseNotAvailableWithOnlyOneCopy() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn exerts (FP source)
		scn.FreepsUseCardAction(aragorn);

		// Response requires discarding ANOTHER Watcher - with only one, not available
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void WatcherResponseAvailableWithTwoCopiesOnFPSourcedExertion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int twilightBefore = scn.GetTwilight();

		// Aragorn exerts (FP source)
		scn.FreepsUseCardAction(aragorn);

		// Both Watchers' draw triggers fire (independent limits) - 2 cards drawn

		// Response should be available since FP card caused exertion
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// One Watcher discarded
		assertEquals(1, scn.GetShadowDiscardCount());
		// +6 twilight added
		assertEquals(twilightBefore + 6, scn.GetTwilight());
		// Aragorn hindered
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void WatcherResponseNotAvailableOnShadowSourcedExertion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var oosas = scn.GetShadowCard("oosas");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(watcher1, watcher2, oosas);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sam);

		scn.StartGame();

		// Play Sam - Out of Sight and Shot triggers (Shadow source)
		scn.FreepsPlayCard(sam);
		scn.FreepsChooseCard(aragorn);

		// Response should NOT be available - exertion was Shadow-sourced
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		// Aragorn should not be hindered
		assertFalse(scn.IsHindered(aragorn));
	}

	@Test
	public void TwoWatchersEachDrawOncePerSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int shadowHandBefore = scn.GetShadowHandCount();

		// Aragorn exerts (FP source)
		scn.FreepsUseCardAction(aragorn);

		// Each Watcher has independent "once per site" limit
		// So 2 Watchers = 2 draws from one exertion
		assertEquals(shadowHandBefore + 2, scn.GetShadowHandCount());

		// Second exertion - neither Watcher should draw (both limits reached)
		int shadowHandAfterFirst = scn.GetShadowHandCount();
		scn.ShadowPass(); //Response action
		scn.ShadowPassCurrentPhaseAction(); //Maneuver action
		scn.FreepsUseCardAction(aragorn);
		assertEquals(shadowHandAfterFirst, scn.GetShadowHandCount());
	}
}
