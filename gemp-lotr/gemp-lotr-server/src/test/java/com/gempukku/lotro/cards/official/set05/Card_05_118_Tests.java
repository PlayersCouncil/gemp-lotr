package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_05_118_Tests
{

	// The Towers path with Hornburg Wall as site 7.  Site 6 is Caves of Aglarond (a maneuver ability for an
	// Uruk-hai, used only on purpose) and site 8 Ring of Isengard (a Shadow ability that plays a [dunland] minion
	// from hand, used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only

					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its play trigger is not reached by a cheat
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("poison", "8_28");      // Spider Poison: [gollum] Shadow possession in the support area; a skirmish ability
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site7", "5_118");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornburgWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Hornburg Wall
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 7T
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>Regroup:</b> Spot your minion and remove (9) to make the fellowship move again this turn
		 * (if the move limit allows).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Hornburg Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void SpottingHisMinionAndRemovingNineTheShadowPlayerMakesTheFellowshipMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable("aragorn", "boromir");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		// The minion is laid before the regroup phase begins, so that the Free Peoples player's offers are
		// worked out with it in play.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		var site = scn.GetCurrentSite();
		assertEquals("Hornburg Wall", site.getBlueprint().getTitle());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());

		// "Your minion": the Free Peoples player, who could pay the (9), has no minion of his own.
		scn.SetTwilight(9);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
		scn.FreepsPass();

		// One short of the (9): not offered.  At 9: offered.
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(site));
		scn.SetTwilight(9);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetTwilight());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		// No "another move" question: the fellowship moves on to site 8.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoMinionButAShadowPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable("aragorn", "boromir");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(poison);
		scn.SetTwilight(9);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Hornburg Wall", site.getBlueprint().getTitle());
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(site));

		// The move is the Free Peoples player's to choose, as usual.
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void AtTheMoveLimitTheNineIsRemovedAndTheFellowshipDoesNotMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable("aragorn", "boromir");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();
		// To site 7 by the regroup move from site 6: two moves of two.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Hornburg Wall", site.getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		// An ability with a cost is offered though its effect can do nothing (Q-0034).
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		// 10 - 9
		assertEquals(1, scn.GetTwilight());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		// The move limit does not allow it: the turn ends at site 7, and the fellowship is not moved.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(7, scn.FreepsGetSiteNumber());
		scn.FreepsDeclineReconciliation();
		assertEquals(7, scn.FreepsGetSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheFellowshipIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable("aragorn", "boromir");
		// A move limit of 3, so that after the move the site demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand);
		scn.SetTwilight(9);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Hornburg Wall", site.getBlueprint().getTitle());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		// The [dunland] Man is spotted as well as the [moria] Orc.
		scn.ShadowUseCardAction(site);
		assertEquals(0, scn.GetTwilight());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		// The second move is made without the Free Peoples player being asked.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// The regroup phase at site 8: the limit allows a third move, and it is his to choose.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
