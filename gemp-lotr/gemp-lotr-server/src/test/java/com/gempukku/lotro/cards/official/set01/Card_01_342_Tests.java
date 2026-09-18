package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_342_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_342");
					put("aragorn", "1_89");  // Ranger
					put("gimli", "1_13");    // not a Ranger
					put("silinde", "1_60");  // Silinde: has the game text of your site 3
					put("toby", "1_305");    // Old Toby: something harmless to play, so the game has an event to react to
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_342");   // Rivendell Waterfall
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RivendellWaterfallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Rivendell Waterfall
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Sanctuary
		 * Subtype: 
		 * Site Number: 3
		 * Game Text: <b>Forest</b>. <b>Sanctuary</b>. While you can spot a ranger at Rivendell Waterfall, the move limit is +1 for this turn.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(3);
		var card = scn.GetFreepsCard("card");

		assertEquals("Rivendell Waterfall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	private static void ShadowPlaysSiteIfAsked(VirtualTableScenario scn) throws DecisionResultInvalidException {
		if (scn.ShadowDecisionAvailable("Choose site to play"))
			scn.ShadowChooseAnyCard();
	}

	@Test
	public void RangerAtWaterfallRaisesMoveLimitForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1058 / #1057 / #1086: under current rules the Shadow player plays the fellowship's next
		// site, so the site card belongs to the opponent; the bonus was gated on the site's owner being Free Peoples.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPassCurrentPhaseAction();          // move 1 -> 2
		ShadowPlaysSiteIfAsked(scn);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // move 2 -> 3
		ShadowPlaysSiteIfAsked(scn);                 // Shadow plays Rivendell Waterfall
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// The extra move is actually offered at the next regroup
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		ShadowPlaysSiteIfAsked(scn);
		assertEquals(4, scn.GetCurrentSiteNumber());
	}

	@Test
	public void NoRangerAtWaterfallLeavesMoveLimitAlone() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();          // move 1 -> 2
		ShadowPlaysSiteIfAsked(scn);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // move 2 -> 3
		ShadowPlaysSiteIfAsked(scn);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void RangerAtFreepsOwnedWaterfallRaisesMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		// The Free Peoples player's own copy of the site (e.g. via a site-replacement effect) must work too
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();          // move 1 -> 2
		ShadowPlaysSiteIfAsked(scn);

		var freepsWaterfall = scn.GetFreepsSite(3);
		scn.MoveCardToAdventurePath(freepsWaterfall);
		assertEquals(Zone.ADVENTURE_PATH, freepsWaterfall.getZone());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // move 2 -> 3, site already on the path
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(freepsWaterfall, scn.GetCurrentSite());
		assertEquals(3, scn.GetMoveLimit());
	}


	private static void AnyoneChoosesSiteIfAsked(VirtualTableScenario scn) throws DecisionResultInvalidException {
		if (scn.ShadowDecisionAvailable("Choose site to play"))
			scn.ShadowChooseAnyCard();
		if (scn.FreepsDecisionAvailable("Choose site to play"))
			scn.FreepsChooseAnyCard();
	}

	@Test
	public void SecondPlayerArrivingAtWaterfallGetsPlusOneNotPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1042: P1 moves to the Waterfall and ends the turn; P2 then moves there as Free Peoples.
		// The site used to trigger twice on P2's arrival (move limit 4). Along the way this also checks that the
		// Waterfall, once on the path, does nothing for a fellowship that is not at it (CR "sites": a site's
		// non-keyword text only applies while the fellowship is at that site).
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetShadowCard("aragorn");   // the second player's own Ranger
		scn.MoveCompanionsToTable(aragorn, aragorn2);

		scn.StartGame();

		// P1 turn 1: 1 -> 2 -> 3 (Rivendell Waterfall is played onto the path)
		scn.FreepsPassCurrentPhaseAction();
		AnyoneChoosesSiteIfAsked(scn);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		AnyoneChoosesSiteIfAsked(scn);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		if (scn.FreepsDecisionAvailable("reconcile")) scn.FreepsDeclineReconciliation();
		while (scn.FreepsDecisionAvailable("discard down")) scn.FreepsChooseCard((com.gempukku.lotro.game.PhysicalCardImpl) scn.GetFreepsHand().getFirst());

		// P2 turn 2 (roles inverted): P2 has a Ranger but is only at site 2; the Waterfall at site 3 is not its site
		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowPassCurrentPhaseAction();          // P2: 1 -> 2
		AnyoneChoosesSiteIfAsked(scn);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		if (scn.FreepsDecisionAvailable("reconcile")) scn.FreepsDeclineReconciliation();
		while (scn.FreepsDecisionAvailable("discard down")) scn.FreepsChooseCard((com.gempukku.lotro.game.PhysicalCardImpl) scn.GetFreepsHand().getFirst());
		assertEquals(2, scn.GetMoveLimit());

		// ...until P2 actually moves onto the Waterfall
		assertTrue(scn.ShadowDecisionAvailable("another move"));
		scn.ShadowChooseToMove();                    // P2: 2 -> 3
		AnyoneChoosesSiteIfAsked(scn);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		// finish P2's turn at the Waterfall
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		if (scn.FreepsDecisionAvailable("reconcile")) scn.FreepsDeclineReconciliation();
		while (scn.FreepsDecisionAvailable("discard down")) scn.FreepsChooseCard((com.gempukku.lotro.game.PhysicalCardImpl) scn.GetFreepsHand().getFirst());
		if (scn.ShadowDecisionAvailable("another move")) scn.ShadowChooseToStay();
		if (scn.ShadowDecisionAvailable("reconcile")) scn.ShadowDeclineReconciliation();
		while (scn.ShadowDecisionAvailable("discard down")) scn.ShadowChooseCard((com.gempukku.lotro.game.PhysicalCardImpl) scn.GetShadowHand().getFirst());

		// P1 turn 3: both fellowships are now at the Waterfall (the #1042 scenario). P1 gets +1, not +2.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPassCurrentPhaseAction();          // P1: 3 -> 4
		AnyoneChoosesSiteIfAsked(scn);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void SilindeCopyOnlyWorksWhileTheFellowshipIsAtRivendellWaterfallAndStacksWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1045. Silinde has the game text of your site 3, name-drop included: "While you can spot a
		// ranger at Rivendell Waterfall" only holds while the fellowship is at a Rivendell Waterfall. There it does
		// apply, and stacks with the site itself.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(silinde);

		var freepsWaterfall = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);                           // next turn, fellowship phase at site 2
		scn.MoveCardsToHand(toby);
		assertEquals(2, scn.GetMoveLimit());

		// Your Rivendell Waterfall is on the path as site 3 while the fellowship is still at site 2. Silinde has its
		// text (the copy is looked up live), but the fellowship is not at the Waterfall, so playing a card gives the
		// check something to react to and still no bonus.
		scn.MoveCardToAdventurePath(freepsWaterfall);
		assertEquals(Zone.ADVENTURE_PATH, freepsWaterfall.getZone());
		scn.FreepsPlayCard(toby);
		if (scn.FreepsHasOptionalTriggerAvailable())
			scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetMoveLimit());

		// At the Waterfall both the site and Silinde's copy apply
		scn.FreepsPassCurrentPhaseAction();          // move 2 -> 3
		assertEquals(freepsWaterfall, scn.GetCurrentSite());
		// Two required triggers (the site's and Silinde's copy) are collected; order them
		while (scn.FreepsDecisionAvailable("Required responses"))
			scn.FreepsChooseAction("Rivendell Waterfall");
		assertEquals(4, scn.GetMoveLimit());
	}
}
