package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_327_Tests
{

	// The Fellowship path with Bree Gate as its site 2.  Site 1 is Bag End (a Fellowship action used only on
	// purpose), site 3 Rivendell Valley (a sanctuary; nobody here is wounded) and site 4 Balin's Tomb.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man ranger; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf ranger; a strength bonus against Nazgul only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no ranger; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man, as most rangers are, who is no ranger; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: there for Saved From the Fire to spot; a standing strength bonus only
					put("saved", "8_20");       // Saved From the Fire: Fellowship event, spot Gandalf and place a companion (except the Ring-bearer) in the dead pile; it takes the ranger out of play
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
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
	public void BreeGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bree Gate
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 2
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: While you can spot a ranger, the move limit is +1 for this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Bree Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ArrivingAtBreeGateWithARangerRaisesTheMoveLimitForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();          // move 1 -> 2
		assertEquals("Bree Gate", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		// The fellowship has left Bree Gate, and the limit stays where it was for the rest of the turn.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Three moves of three: the turn ends.  On the turns that follow the limit is 2 again, though Aragorn is
		// still with the fellowship.
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
	public void WithNoRangerAtBreeGateTheMoveLimitStays2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(gimli, boromir);

		scn.StartGame();
		// Frodo, Gimli and Boromir: none of them a ranger.
		assertInZone(Zone.FREE_CHARACTERS, gimli, boromir);
		scn.FreepsPass();          // move 1 -> 2
		assertEquals("Bree Gate", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());

		// Two moves of two: the regroup phase ends the turn, and no question of another move stops the skip.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ARangerPlayedAtBreeGateRaisesTheMoveLimitForTheTurnAndASecondAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Bree Gate", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetFreepsHand(arwen, aragorn);
		scn.FreepsRefreshDecision();

		// Frodo alone is at Bree Gate.
		assertInHand(arwen, aragorn);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertEquals(3, scn.GetMoveLimit());

		// A second ranger: the limit is +1, not +1 for each ranger.
		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());

		// Two moves made in a turn begun at Bree Gate, and a third is offered.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheMoveLimitStaysRaisedWhenTheRangerHasLeftPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saved = scn.GetFreepsCard("saved");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Bree Gate", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetFreepsHand(aragorn, saved);
		scn.FreepsRefreshDecision();

		// Frodo and Gandalf are at Bree Gate, and neither is a ranger.
		assertInHand(aragorn, saved);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetMoveLimit());

		// Saved From the Fire places Aragorn in the dead pile, the ranger gone with the turn still in its fellowship phase.
		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(aragorn);
		// The search of the deck for [gondor] cards: shown first, and nothing taken.
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());

		// Two moves made, no ranger anywhere, and a third is offered: the limit is the turn's.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ARangerOfAnotherCultureRaisesTheMoveLimitOfWhicheverFellowshipIsAtBreeGate() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var shadowAragorn = scn.GetShadowCard("aragorn");    // the second player's own ranger
		scn.MoveCompanionsToTable(arwen, shadowAragorn);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertEquals(2, scn.GetMoveLimit());

		// The first player's turn 1: Arwen, an [elven] Elf ranger, arrives at Bree Gate.
		scn.FreepsPass();          // move 1 -> 2
		assertEquals("Bree Gate", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// The second player's turn 2: his Aragorn is at site 1, and Bree Gate lies ahead under the first player's marker.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.GetMoveLimit());

		// ...until his fellowship arrives there.
		scn.ShadowPass();          // move 1 -> 2
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));

		// The first player's turn 3, begun at Bree Gate with Arwen: the +1 again, for this turn.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
	}
}
