package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paths", "6_44");
					put("smeagol", "5_29");     // Sméagol, Slinker: [gollum] companion, vitality 4; a skirmish ability only
					put("smeagol2", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, vitality 4; an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Sméagol, vitality 4; a maneuver ability only

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its play trigger is not met when cheated in
					// The Shadow player's discard pile: Shadow cards of three kinds and two cultures, and a Free Peoples card
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text
					put("hound", "7_192");      // Morgul Hound: [wraith] minion
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition
					put("hope", "2_68");        // Must Do Without Hope: [moria] event
					put("bounder", "1_286");    // Bounder: a Free Peoples ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SafePathsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Safe Paths
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Exert Sméagol 3 times and discard him to wound each minion. The move limit for this turn is +1. If the fellowship moves, each Shadow player may take up to 3 Shadow cards into hand from his or her discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("paths");

		assertEquals("Safe Paths", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupEventExertsSmeagolThreeTimesAndDiscardsHimToWoundEachMinionAndRaisesTheMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertInHand(paths);
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage, runner);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsPlayAvailable(paths));
		scn.FreepsPlayCard(paths);

		// Vitality 4: three exertions leave him exhausted, and he is then discarded, not killed.
		assertInZone(Zone.DISCARD, smeagol);
		assertEquals(0, scn.GetFreepsDeadCount());
		// Every minion is wounded, of whatever culture and race; the exhausted Goblin Runner dies of it.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.DISCARD, runner);
		// No companion is wounded.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, paths);
		// The fellowship has moved once; the limit for this turn is 3.
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShireSmeagolPaysTheCostToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol2");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(paths);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(paths));
		scn.FreepsPlayCard(paths);

		assertInZone(Zone.DISCARD, smeagol);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoSmeagolThoughAnotherCompanionCouldExertThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertInHand(paths);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// Aragorn, unwounded with vitality 4, could exert three times, but is no Sméagol.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(paths));
		assertEquals(0, scn.GetWoundsOn(savage));
	}

	@Test
	public void IsNotPlayableWhileSmeagolCannotExertThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(smeagol, 1);
		assertInHand(paths);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);

		// One wound: vitality 3, two exertions left of the three the cost asks, judged in full.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertFalse(scn.FreepsPlayAvailable(paths));

		// Healed of it: playable.
		scn.RemoveWoundsFromChar(smeagol, 1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsPlayAvailable(paths));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInHand(paths);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IfTheFellowshipMovesTheShadowPlayerMayTakeUpToThreeShadowCardsFromHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		var freepsSavage = scn.GetFreepsCard("savage");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(paths);
		// A Shadow card in the Free Peoples player's own discard pile, of a title the Shadow pile does not hold.
		scn.MoveCardsToDiscard(freepsSavage);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var hound = scn.GetShadowCard("hound");
		var fear = scn.GetShadowCard("fear");
		var hope = scn.GetShadowCard("hope");
		var bounder = scn.GetShadowCard("bounder");
		scn.MoveCardsToDiscard(troop, hound, fear, hope, bounder);

		scn.StartGame();
		assertInZone(Zone.DISCARD, freepsSavage, troop, hound, fear, hope, bounder);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(paths);
		assertInZone(Zone.DISCARD, smeagol);

		// Nothing is offered until the fellowship moves.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		// The Shadow player has reconciled in this regroup phase, drawing what his deck held.
		assertEquals(5, scn.GetShadowHandCount());
		scn.FreepsChooseToMove();

		// Minions, a condition and an event, of several cultures; no Free Peoples card, and nothing of the other pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, hound, fear, hope));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder, freepsSavage));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop, fear, hope);

		assertInHand(troop, fear, hope);
		assertEquals(8, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, hound, bounder, freepsSavage);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheTakeMayBeDeclinedAndIsNotOfferedAgainAtTheThirdMoveThatTheLimitAllows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		assertInZone(Zone.DISCARD, troop);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(paths);
		scn.SkipToMovementDecision();
		int hand = scn.GetShadowHandCount();
		scn.FreepsChooseToMove();

		// Declined: nothing taken.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertInZone(Zone.DISCARD, troop);
		assertEquals(hand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Still +1 after the regroup move: in the next regroup phase a third move is offered, and made.
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		// "If the fellowship moves" was the one move: the third offers nothing.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, troop);
		assertFalse(scn.ShadowHasCardChoiceAvailable(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenTheFellowshipStaysNothingIsTakenAndTheLimitIsTwoAgainNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var paths = scn.GetFreepsCard("paths");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(paths);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToDiscard(troop);

		scn.StartGame();
		assertInZone(Zone.DISCARD, troop);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(paths);
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		scn.PassReconcile(VirtualTableScenario.P2);
		scn.PassReconcile(VirtualTableScenario.P1);

		// The turn is over with nothing taken, and the +1 with it.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInZone(Zone.DISCARD, troop);
		assertEquals(2, scn.GetMoveLimit());
	}
}
