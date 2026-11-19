package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("march", "13_108");
					put("march2", "13_108");
					// Always Threatening: [orc] support-area condition; its play trigger and skirmish ability reach no test.
					put("threatening", "13_103");
					put("relics", "1_195");      // Relics of Moria: [moria] support-area condition; a Shadow ability only
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc; text on its own play only
					// Half-troll of Far Harad: [orc] Half-troll, cost 3, site 4: an [orc] minion who is no Orc.
					put("halftroll", "14_12");
					put("runner", "1_178");      // Goblin Runner: [moria] Orc; adds (2) as it is played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForcedMarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Forced March
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [orc] minions.<br><b>Regroup:</b> Make the Free Peoples player spot another [orc] condition, discard it from play, and remove (3) to make the Free Peoples player choose to move again this turn (if the move limit allows). Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("march");

		assertEquals("Forced March", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoOrcMinionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var threatening = scn.GetShadowCard("threatening");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(threatening);
		scn.MoveCardsToHand(march, halftroll);

		scn.StartGame();
		// One [orc] minion; the [moria] Runner is no [orc] minion and Always Threatening is an [orc] card that is none.
		scn.MoveMinionsToTable(goblin, runner);
		assertInHand(march, halftroll);
		assertInZone(Zone.SUPPORT, threatening);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(march));

		// A second [orc] minion, who is no Orc.
		scn.ShadowPlayCard(halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertTrue(scn.ShadowPlayAvailable(march));
		scn.ShadowPlayCard(march);
		assertInZone(Zone.SUPPORT, march);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionHasTheFreePeoplesPlayerDiscardAnotherOrcConditionAndTheFellowshipMovesAgainUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var march2 = scn.GetShadowCard("march2");
		var threatening = scn.GetShadowCard("threatening");
		var relics = scn.GetShadowCard("relics");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToSupportArea(march, march2, threatening, relics);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(goblin);
		assertInZone(Zone.SUPPORT, march, march2, threatening, relics);

		// Nobody is assigned, so the Goblin is still in play in the regroup phase at site 4.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowActionAvailable(march));
		scn.ShadowUseCardAction(march);

		// The Free Peoples player spots the condition: the other copy or Always Threatening, not this copy, not the
		// [moria] condition, and not the Goblin, an [orc] card that is no condition.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(march2, threatening));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(march, relics, goblin));
		scn.FreepsChooseCard(threatening);

		assertInDiscard(threatening);
		assertEquals(2, scn.GetTwilight());
		// This copy is discarded; the other stays.
		assertInDiscard(march);
		assertInZone(Zone.SUPPORT, march2, relics);

		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);

		// No "another move" question: the fellowship moves on to site 5.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithoutAnotherOrcConditionOrThreeTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var threatening = scn.GetShadowCard("threatening");
		var relics = scn.GetShadowCard("relics");
		scn.MoveCardsToSupportArea(march, relics);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.SUPPORT, march, relics);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// Only a [moria] condition beside it.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(march));

		scn.MoveCardsToSupportArea(threatening);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(march));

		// One twilight short of (3).
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(march));
	}

	@Test
	public void AtTheMoveLimitTheActionIsPaidForNothingAndTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(march, threatening);

		scn.StartGame();
		scn.SkipToSite(2);
		assertInZone(Zone.SUPPORT, march, threatening);

		// To site 4 by the regroup move from site 3: two moves of two.
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		// Its cost can be paid, so it is offered, though the move limit allows no move.
		assertTrue(scn.ShadowActionAvailable(march));
		scn.ShadowUseCardAction(march);
		// The one other [orc] condition goes unasked.
		assertInDiscard(threatening, march);
		assertEquals(0, scn.GetTwilight());

		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);
		scn.PassReconcile(VirtualTableScenario.P1);

		// The fellowship stayed at site 4: the turn is over, and the other player's has begun.
		assertEquals(4, scn.FreepsGetSiteNumber());
		assertEquals(VirtualTableScenario.P2, scn.GetCurrentPlayer());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheFreePeoplesPlayerIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var march = scn.GetShadowCard("march");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToSupportArea(march, threatening);
		// A move limit of 3, so that after the move the condition demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToSite(2);
		assertInZone(Zone.SUPPORT, march, threatening);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.ShadowUseCardAction(march);
		assertInDiscard(threatening, march);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);

		// The second move is made without the Free Peoples player being asked.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// The regroup phase at site 4: the limit allows a third move, and it is his to choose.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(VirtualTableScenario.P2);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
