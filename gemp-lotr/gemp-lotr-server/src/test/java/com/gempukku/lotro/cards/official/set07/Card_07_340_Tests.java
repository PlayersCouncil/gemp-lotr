package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_340_Tests
{

	// The King path with Tower of Ecthelion as its site 3.  Site 4 is Osgiliath Fallen (a skirmish ability) and site 5
	// Pelennor Flat (a start-of-maneuver text): with no minion in play neither phase comes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Household Guard: [rohan] Man companion, not unique; Valiant is its whole text
					put("guard1", "5_83");
					put("guard2", "5_83");
					put("guard3", "5_83");
					put("guard4", "5_83");
					put("guard5", "5_83");
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "7_340");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerofEcthelionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Tower of Ecthelion
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> If you cannot spot 3 threats, add 3 threats to make the move limit for this turn +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Tower of Ecthelion", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void FellowshipActionAddsThreeThreatsToRaiseTheMoveLimitByOneForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowSite(3);
		// Frodo and two Guards: three companions, room for three threats.
		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.SkipToSite(3);

		assertSame(tower, scn.GetCurrentSite());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsActionAvailable(tower));

		scn.FreepsUseCardAction(tower);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The first move, to site 4, and a second in the regroup phase, to site 5.  No minion is played, so each
		// Shadow phase goes straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// After the second move the +1 is still in force, and a third move is offered.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToStay();
		// The hand the earlier turns' reconciles drew is reconciled as the turn ends.
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();

		// It was for that turn only.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void IsOfferedWithTwoThreatsAndNotWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowSite(3);
		// Frodo and five Guards: six companions, so that three threats can be added on top of either count.
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3", "guard4", "guard5");

		scn.StartGame();
		scn.SkipToSite(3);

		assertSame(tower, scn.GetCurrentSite());
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(tower));

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.FreepsActionAvailable(tower));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenThreeThreatsCannotAllBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tower = scn.GetShadowSite(3);
		var guard2 = scn.GetFreepsCard("guard2");
		// Frodo and one Guard: two companions, so no more than two threats can be added.
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.SkipToSite(3);

		assertSame(tower, scn.GetCurrentSite());
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.FreepsActionAvailable(tower));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A third companion makes room for all three.
		scn.MoveCompanionsToTable(guard2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(tower));
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
