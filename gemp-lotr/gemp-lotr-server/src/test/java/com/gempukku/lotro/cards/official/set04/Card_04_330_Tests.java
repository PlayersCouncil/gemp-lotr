package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_330_Tests
{

	// The Towers path with Derndingle as its site 2.  Site 1 is Eastemnet Gullies (a Fellowship action that plays
	// Legolas from the draw deck, and none is there), site 3 Barrows of Edoras (a sanctuary; a Fellowship action
	// used only on purpose) and site 4 White Rocks (its Shadow number counts mounted companions, and none is).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, unbound; a skirmish ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, unbound; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion who is Ring-bound by rule
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion who is no Hobbit; a skirmish ability only
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion and so never unbound; a skirmish ability only
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_330");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DerndingleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Derndingle
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2T
		 * Shadow Number: 2
		 * Direction: Left
		 * Game Text: <b>Forest</b>. While you can spot an unbound Hobbit at Derndingle, the move limit is +1 for this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Derndingle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void ArrivingAtDerndingleWithAnUnboundHobbitRaisesTheMoveLimitForTheRestOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();          // move 1 -> 2
		assertEquals("Derndingle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();  // move 2 -> 3
		// The fellowship has left Derndingle, and the limit stays where it was for the rest of the turn.
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

		// Three moves of three: the turn ends.  On the turns that follow the limit is 2 again, though Merry is
		// still with the fellowship.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoUnboundHobbitAtDerndingleTheMoveLimitStays2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(sam, gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Frodo and Sam are Hobbits who are Ring-bound; Gimli is unbound and no Hobbit; Bounder is a Hobbit and no companion.
		assertInZone(Zone.SUPPORT, bounder);
		scn.FreepsPass();          // move 1 -> 2
		assertEquals("Derndingle", scn.GetCurrentSite().getBlueprint().getTitle());
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
	public void AnUnboundHobbitPlayedAtDerndingleRaisesTheMoveLimitForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");

		scn.StartGame();
		scn.SkipToSite(2);
		assertEquals("Derndingle", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.SetFreepsHand(merry);
		scn.FreepsRefreshDecision();

		// Frodo alone is at Derndingle.
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(merry);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();          // move 2 -> 3
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();  // move 3 -> 4
		assertEquals(4, scn.GetCurrentSiteNumber());

		// Two moves made in a turn begun at Derndingle, and a third is offered.
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
	public void AnUnboundHobbitOfAnotherCultureRaisesTheMoveLimitOfWhicheverFellowshipIsAtDerndingle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var shadowMerry = scn.GetShadowCard("merry");    // the second player's own unbound Hobbit
		scn.MoveCompanionsToTable(pippin, shadowMerry);

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());

		// The first player's turn 1: Pippin, a [gondor] Hobbit, arrives at Derndingle.
		scn.FreepsPass();          // move 1 -> 2
		assertEquals("Derndingle", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// The second player's turn 2: his Merry is at site 1, and Derndingle lies ahead under the first player's marker.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.GetMoveLimit());

		// ...until his fellowship arrives there.
		scn.ShadowPass();          // move 1 -> 2
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));

		// The first player's turn 3, begun at Derndingle with Pippin: the +1 again, for this turn.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
	}
}
