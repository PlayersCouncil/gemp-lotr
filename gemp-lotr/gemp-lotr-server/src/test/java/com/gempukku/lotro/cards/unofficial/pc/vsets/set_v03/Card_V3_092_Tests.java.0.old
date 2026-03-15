package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_092_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher1", "103_92");    // Cirith Ungol Watcher
					put("watcher2", "103_92");    // Second copy for Response test
					put("stone", "9_47");         // Ithil Stone - [Sauron] artifact to spot
					put("desertlord", "4_219");   // Desert Lord - Archery exert ability

					put("aragorn", "1_89");       // Has Maneuver exert ability
					put("boromir", "1_97");       // Another companion for threat limit tests
					put("gandalf", "1_364");       // Another companion for threat limit tests
					put("runner", "1_178");       // Cheap minion for phase progression
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
		 * Name: Cirith Ungol Watcher, Hideous Warden
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot a [sauron] card and add a threat.
		* 	Each time a companion exerts, draw a card (limit once per site).
		* 	Response: If a companion is exerted by a Free Peoples card, discard another Cirith Ungol Watcher to add 3 threats and hinder that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher1");

		assertEquals("Cirith Ungol Watcher", card.getBlueprint().getTitle());
		assertEquals("Hideous Warden", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

// Extra cost tests

	@Test
	public void WatcherCannotPlayWithoutSauronCardToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		// No [Sauron] cards in play

		scn.MoveCardsToHand(watcher);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertFalse(scn.ShadowPlayAvailable(watcher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WatcherCannotPlayIfThreatLimitReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var stone = scn.GetShadowCard("stone");
		// Only Frodo in play = threat limit of 1

		scn.MoveCardsToHand(watcher);
		scn.MoveCardsToSupportArea(stone);
		scn.AddThreats(1);  // At limit

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetThreatLimit());
		assertFalse(scn.ShadowPlayAvailable(watcher));
		scn.ShadowDeclineOptionalTrigger(); //ithil stone
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WatcherPlaysSuccessfullyAndAddsThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var stone = scn.GetShadowCard("stone");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToHand(watcher);
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionsToTable(boromir);  // 2 companions = threat limit 2

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowDeclineOptionalTrigger(); //ithil stone

		assertEquals(0, scn.GetThreats());

		assertTrue(scn.ShadowPlayAvailable(watcher));
		scn.ShadowPlayCard(watcher);

		assertInZone(Zone.SUPPORT, watcher);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

// Draw trigger tests

	@Test
	public void WatcherDrawsCardWhenCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int shadowHand = scn.GetShadowHandCount();

		// Aragorn exerts via his Maneuver ability
		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(aragorn);

		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
	}

	@Test
	public void WatcherDrawLimitedOncePerSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetShadowCard("watcher1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher);
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int shadowHand = scn.GetShadowHandCount();

		// First exertion - should draw
		scn.FreepsUseCardAction(aragorn);
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());

		scn.ShadowPass(); // Back to Freeps in Maneuver

		// Second exertion same site - should NOT draw (limit once per site)
		scn.FreepsUseCardAction(aragorn);
		assertEquals(shadowHand + 1, scn.GetShadowHandCount());
	}

// Response tests

	@Test
	public void WatcherResponseAvailableWhenFPCardExertsCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCompanionsToTable("gandalf", "boromir");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		int threats = scn.GetThreats();
		assertFalse(scn.IsHindered(aragorn));

		// Aragorn exerts via his own (FP) ability
		scn.FreepsUseCardAction(aragorn);

		// Response should be available
		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Watcher"));
		scn.ShadowAcceptOptionalTrigger();
		// One Watcher discarded
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(threats + 3, scn.GetThreats());
		assertTrue(scn.IsHindered(aragorn));
	}

	@Test
	public void WatcherResponseNotAvailableWhenShadowCardExertsCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var desertlord = scn.GetShadowCard("desertlord");

		scn.MoveCardsToSupportArea(watcher1, watcher2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(desertlord);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Desert Lord exerts to exert a companion (Shadow source)
		assertTrue(scn.ShadowActionAvailable(desertlord));
		scn.ShadowUseCardAction(desertlord);
		// Aragorn auto-selected as only non-RB companion

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(desertlord));

		// Draw trigger SHOULD still fire (any exertion)
		// But Response should NOT be available (Shadow source, not FP)
		assertFalse(scn.ShadowHasOptionalTriggerAvailable("Watcher"));
	}

	@Test
	public void WatcherResponseRequiresAnotherWatcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher1 = scn.GetShadowCard("watcher1");
		var watcher2 = scn.GetShadowCard("watcher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		// Only ONE Watcher in play
		scn.MoveCardsToSupportArea(watcher1);
		scn.MoveCardsToDiscard(watcher2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Aragorn exerts
		scn.FreepsUseCardAction(aragorn);

		// Response should NOT be available - no other Watcher to discard
		assertFalse(scn.ShadowHasOptionalTriggerAvailable("Watcher"));
	}
}
