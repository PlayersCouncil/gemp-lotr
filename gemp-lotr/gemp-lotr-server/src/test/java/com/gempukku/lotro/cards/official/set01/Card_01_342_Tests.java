package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_01_342_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");  // Aragorn, Ranger of the North: a [gondor] Man, ranger; a maneuver ability only
					put("arwen", "1_30");    // Arwen, Daughter of Elrond: an [elven] Elf, ranger; her text needs a Nazgûl
					put("boromir", "1_97");  // Boromir, Son of Denethor: a [gondor] Man who is no ranger; a skirmish ability only
					put("gimli", "1_13");    // Gimli, Son of Glóin: no ranger; a skirmish ability only
					put("silinde", "1_60");  // Silinde: has the game text of your site 3
					put("toby", "1_305");    // Old Toby: something harmless to play, so the game has an event to react to
					// Blade Tip: a Shadow condition a companion bears; it wounds its bearer at the start of each fellowship phase
					put("bladetip", "1_209");
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
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Forest</b>. <b>Sanctuary</b>. While you can spot a ranger at Rivendell Waterfall, the move limit is +1 for this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell Waterfall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void RangerAtWaterfallRaisesMoveLimitForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1058 / #1057 / #1086: the Shadow player lays the fellowship's next site, so the Waterfall
		// under the fellowship is the opponent's card; the bonus was gated on the site's owner being Free Peoples.
		var scn = GetScenario();

		var waterfall = scn.GetShadowSite(3);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPass();          // move 1 -> 2
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // move 2 -> 3
		assertSame(waterfall, scn.GetCurrentSite());
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// The extra move is actually offered at the next regroup
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The fellowship has left the Waterfall, and the limit stays where it was for the rest of the turn.
		assertEquals(3, scn.GetMoveLimit());
		assertEquals(3, scn.GetMoveCount());

		// Three moves of three: the turn ends.  On the turns that follow the limit is 2 again, though Aragorn
		// is still with the fellowship.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoRangerAtWaterfallLeavesMoveLimitAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();

		scn.FreepsPass();          // move 1 -> 2
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(2, scn.GetMoveCount());

		// Two moves of two: the regroup phase ends the turn, and no question of another move stops the skip.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void RangerAtFreepsOwnedWaterfallRaisesMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		// The Free Peoples player's own copy of the site (laid ahead as a Pathfinder would lay it) must work too
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();          // move 1 -> 2

		var freepsWaterfall = scn.GetFreepsSite(3);
		scn.MoveCardToAdventurePath(freepsWaterfall);
		assertInZone(Zone.ADVENTURE_PATH, freepsWaterfall);
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();                    // move 2 -> 3, site already on the path
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(freepsWaterfall, scn.GetCurrentSite());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SecondPlayerArrivingAtWaterfallGetsPlusOneNotPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1042: P1 moves to the Waterfall and ends the turn; P2 then moves there as Free Peoples.
		// The site used to trigger twice on P2's arrival (move limit 4). Along the way this also checks that the
		// Waterfall, once on the path, does nothing for a fellowship that is not at it.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetShadowCard("aragorn");   // the second player's own Ranger
		scn.MoveCompanionsToTable(aragorn, aragorn2);

		scn.StartGame();

		// P1 turn 1: 1 -> 2 -> 3 (Rivendell Waterfall is played onto the path)
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// P2 turn 2 (roles inverted): P2 has a Ranger but is only at site 2; the Waterfall at site 3 is not its site
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
		scn.ShadowPass();          // P2: 1 -> 2
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.ShadowDecisionAvailable("another move"));
		assertEquals(2, scn.GetMoveLimit());

		// ...until P2 actually moves onto the Waterfall
		scn.ShadowChooseToMove();                    // P2: 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		// finish P2's turn at the Waterfall: two moves of three, so a third is offered
		scn.SkipToMovementDecision();
		assertTrue(scn.ShadowDecisionAvailable("another move"));
		scn.ShadowChooseToStay();
		scn.PassReconcile(scn.P2());

		// P1 turn 3: both fellowships are now at the Waterfall. P1 gets +1, not +2.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();          // P1: 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARangerPlayedAtTheWaterfallRaisesTheMoveLimitAndASecondRangerAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");

		scn.StartGame();
		scn.SkipToSite(3);
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetFreepsHand(arwen, aragorn);
		scn.FreepsRefreshDecision();

		// Frodo alone is at the Waterfall.
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second ranger is no second +1.
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();          // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();                    // move 4 -> 5
		assertEquals(5, scn.GetCurrentSiteNumber());

		// Two moves made in a turn begun at the Waterfall, and a third is offered.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		// The first player's next turn, at site 5 with both rangers
		scn.SkipToOtherPlayersTurn();
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SilindeCopyOnlyWorksWhileTheFellowshipIsAtRivendellWaterfallAndStacksWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1045. Silinde has the game text of your site 3, the site's own name included: "While you
		// can spot a ranger at Rivendell Waterfall" only holds while the fellowship is at a Rivendell Waterfall.
		// There it does apply, and stacks with the site itself.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);

		var freepsWaterfall = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);                           // next turn, fellowship phase at site 2
		scn.SetFreepsHand(toby);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetMoveLimit());

		// Your Rivendell Waterfall is on the path as site 3 while the fellowship is still at site 2. Silinde has its
		// text, but the fellowship is not at the Waterfall, so playing a card gives the check something to react to
		// and still no bonus.
		scn.MoveCardToAdventurePath(freepsWaterfall);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.ADVENTURE_PATH, freepsWaterfall);
		scn.FreepsPlayCard(toby);
		// Old Toby's own offer of a draw
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetMoveLimit());

		// At the Waterfall both the site and Silinde's copy apply: two required triggers, which the Free Peoples
		// player orders.
		scn.FreepsPass();          // move 2 -> 3
		assertSame(freepsWaterfall, scn.GetCurrentSite());
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Rivendell Waterfall"));
		assertTrue(scn.FreepsActionAvailable("Silinde"));
		scn.FreepsResolveActionOrder("Rivendell Waterfall");

		// 2 + 1 for the site + 1 for Silinde's copy
		assertEquals(4, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StartingATurnAtWaterfallWhileOtherTriggersResolveGivesOnlyPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		// Regression for #1091. A turn begun at the Waterfall with a ranger who bears Blade Tip: at the start of the
		// fellowship phase Blade Tip wounds (and kills) that ranger while the site's own check is waiting to be
		// carried out. The check used to be collected again for everything that happened before its first copy was
		// carried out, so the move limit went 2 -> 3 -> 4 -> 5 instead of 2 -> 3.
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bladeTip = scn.GetShadowCard("bladetip");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.FreepsPass();          // P1: 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());

		// Blade Tip goes on the ranger, who is left with one wound to take; P1 stays at the Waterfall and P2
		// plays a turn.
		scn.AttachCardsTo(aragorn, bladeTip);
		scn.AddWoundsToChar(aragorn, 3);
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SkipToOtherPlayersTurn();

		// P1's next turn starts at the Waterfall, a sanctuary: the heals are declined.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		scn.FreepsDeclineSanctuaryHealing();
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// At the start of the fellowship phase both Blade Tip and the Waterfall's own check are to be carried out;
		// the Free Peoples player orders them.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Blade Tip"));
		assertTrue(scn.FreepsActionAvailable("Rivendell Waterfall"));

		// Blade Tip is carried out first and its wound is the ranger's last; then the Waterfall's check, which the
		// Free Peoples player takes ahead of the ranger's death. The order is his to choose, and what the other
		// order would give is his own doing: no test reads it.
		scn.FreepsResolveActionOrder("Blade Tip");
		assertEquals(4, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Rivendell Waterfall");

		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// P1 has not moved yet this turn and gets exactly one extra move
		assertEquals(0, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
	}
}
