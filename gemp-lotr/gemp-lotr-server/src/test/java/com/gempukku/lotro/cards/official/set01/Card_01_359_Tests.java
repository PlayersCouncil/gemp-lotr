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

public class Card_01_359_Tests
{

	// The Fellowship sites with Shores of Nen Hithoel as site 8.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; a skirmish ability used only on purpose
					put("retainer", "3_57");    // Isengard Retainer: an [isengard] Orc; a regroup ability used only on purpose
					// Morgul Hound: a [wraith] Orc; stronger and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, no Orc; damage +1 only
					// Goblin Runner: a [moria] Orc, cost 1; adds (2) unasked as it is played
					put("runner", "1_178");
					// Aragorn, Ranger of the North: a card for the Free Peoples player's hand, so that he is asked to
					// reconcile after the question of another move
					put("aragorn", "1_89");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site8", "1_359");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShoresofNenHithoelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Shores of Nen Hithoel
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 8
		 * Shadow Number: 9
		 * Direction: Right
		 * Game Text: <b>River</b>. <b>Shadow:</b> Spot 5 Orc minions to prevent the fellowship from moving again this turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Shores of Nen Hithoel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void WithFiveOrcMinionsTheShadowPlayerStopsTheFellowshipMovingAgainThisTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var retainer = scn.GetShadowCard("retainer");
		var hound = scn.GetShadowCard("hound");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		// Laid at the end of the fellowship phase, as they will stand in the Shadow phase at site 8.
		scn.MoveMinionsToTable(troop, soldier, retainer, hound, savage);
		scn.SetShadowHand(runner);
		scn.MoveCardsToHand(aragorn);
		scn.FreepsPass();

		var shores = scn.GetCurrentSite();
		assertEquals("Shores of Nen Hithoel", shores.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Four Orcs of four cultures and an Uruk-hai, who is no Orc: four Orc minions.
		assertFalse(scn.ShadowActionAvailable(shores));

		// A fifth Orc played.
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(shores));

		scn.ShadowUseCardAction(shores);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No skirmishes: neither player assigns.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());

		scn.SkipToMovementDecision();
		// One move made of the two the move limit allows, and the fellowship is not asked to make the second:
		// the regroup phase has gone on to the mover's reconcile, which follows that question.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));

		// The next turn, the other player's fellowship moves to site 8 and may move again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void WithFourOrcMinionsItIsNotOfferedAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var retainer = scn.GetShadowCard("retainer");
		var hound = scn.GetShadowCard("hound");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(troop, soldier, retainer, hound, savage);
		scn.FreepsPass();

		var shores = scn.GetCurrentSite();
		assertEquals("Shores of Nen Hithoel", shores.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shores));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
