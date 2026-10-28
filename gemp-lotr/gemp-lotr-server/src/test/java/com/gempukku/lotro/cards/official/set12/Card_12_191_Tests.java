package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_191_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: in hand, so that a reconcile is asked

					// [orc] Orcs of four titles, lurkers and not.
					put("skulker1", "12_95");       // Orc Skulker: lurker; a discount on skirmish events only
					put("skulker2", "12_95");
					put("prowler", "11_136");       // Prowling Orc: lurker; a skirmish ability used only on purpose
					put("denizen", "11_115");       // Denizen of Khazad-dum: stronger underground only
					put("goblin", "11_137");        // Scurrying Goblin: its text answers its own play at an underground site
					// Half-troll of Far Harad: an [orc] Half-troll, no Orc; its start-of-assignment trigger needs more
					// companions than minions
					put("halftroll", "14_12");
					put("runner", "1_178");         // Goblin Runner: a [moria] Orc; its text answers its own play
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");         // Fangorn Glade: its text answers a move to it; the game starts there
					put("shores", "12_191");
					// Never laid in these tests.
					put("site3", "11_234");
					put("site5", "18_138");
					put("site6", "12_187");
					put("site7", "13_185");
					put("site8", "11_230");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void ShoresofNenHithoelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Shores of Nen Hithoel
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>River</b>. <b>Shadow:</b> Spot 5 [orc] Orcs to prevent the fellowship from moving again this turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("shores");

		assertEquals("Shores of Nen Hithoel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void WithFiveOrcOrcsTheShadowPlayerStopsTheFellowshipMovingAgainThisTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var skulker1 = scn.GetShadowCard("skulker1");
		var skulker2 = scn.GetShadowCard("skulker2");
		var prowler = scn.GetShadowCard("prowler");
		var denizen = scn.GetShadowCard("denizen");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("shores"));

		var shores = scn.GetCurrentSite();
		assertEquals("Shores of Nen Hithoel", shores.getBlueprint().getTitle());
		scn.MoveMinionsToTable(skulker1, skulker2, prowler, denizen, halftroll, runner);
		scn.MoveCardsToHand(aragorn);
		scn.ShadowRefreshDecision();

		// Four [orc] Orcs, an [orc] minion who is no Orc and an Orc of another culture: four [orc] Orcs.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInHand(aragorn);
		assertFalse(scn.ShadowActionAvailable(shores));

		// A fifth [orc] Orc.
		scn.MoveMinionsToTable(goblin);
		scn.ShadowRefreshDecision();
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
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));

		// The next turn, the other player's fellowship moves to site 2 and may move again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("another move"));
	}

	@Test
	public void WithFourOrcOrcsItIsNotOfferedAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var skulker1 = scn.GetShadowCard("skulker1");
		var skulker2 = scn.GetShadowCard("skulker2");
		var prowler = scn.GetShadowCard("prowler");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("shores"));

		var shores = scn.GetCurrentSite();
		assertEquals("Shores of Nen Hithoel", shores.getBlueprint().getTitle());
		scn.MoveMinionsToTable(skulker1, skulker2, prowler, denizen, halftroll, runner);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(shores));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
