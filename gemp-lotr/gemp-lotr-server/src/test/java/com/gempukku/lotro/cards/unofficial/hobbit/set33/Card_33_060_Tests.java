package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_060_Tests
{
	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot. Its sites 6 to 9 are Esgaroth 30_54, Erebor 30_55, Smaug's Den 30_56 and Battle of the Five Armies 30_57.
	// The condition is laid after a SkipToSite.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lights", "33_60");

					// Cards for the Free Peoples discard pile
					put("blunt", "33_4");       // Blunt The Knives!: [dwarven] event
					put("barrels", "31_40");    // Barrels: [shire] event
					put("wizard", "30_23");     // A Wizard Is Never Late: [gandalf] event
					put("pot", "33_5");         // Dwarven Cooking Pot: [dwarven] possession
					// For the Shadow player's own discard pile: a [dwarven] event of a title the other pile lacks
					put("fury", "30_3");        // Battle Fury: [dwarven] event

					// An [esgaroth] ally in the draw deck, so that Esgaroth's start-of-regroup offer is made
					put("master", "32_20");     // The Master, Master of Lake-Town
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheLastLightsofDurinsDayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: The Last Lights of Durin's Day
		 * Unique: True
		 * Side: Shadow
		 * Culture: Smaug
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Regroup:</b> At site 7 or 8, remove (4) to make the Free Peoples player choose to move again this turn (if the move limit allows). The Free Peoples player may take a [dwarven] or [shire] event into hand from his or her discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lights");

		assertEquals("The Last Lights of Durin's Day", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SMAUG, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite7Removing4MakesTheFellowshipMoveAgainAndTheFreePeoplesPlayerTakesAnEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var blunt = scn.GetFreepsCard("blunt");
		var barrels = scn.GetFreepsCard("barrels");
		var wizard = scn.GetFreepsCard("wizard");
		var pot = scn.GetFreepsCard("pot");
		var fury = scn.GetShadowCard("fury");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		scn.MoveCardsToDiscard(blunt, barrels, wizard, pot, fury);
		assertInZone(Zone.SUPPORT, lights);
		assertInDiscard(blunt, barrels, wizard, pot, fury);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(lights));
		scn.ShadowUseCardAction(lights);
		assertEquals(1, scn.GetTwilight());

		// His own [dwarven] and [shire] events; not the [dwarven] possession, the [gandalf] event, nor the Shadow
		// player's [dwarven] event in his own pile.
		assertTrue(scn.FreepsDecisionAvailable("Dwarven or Shire event"));
		scn.FreepsChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(blunt, barrels));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(wizard, pot, fury));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(barrels);
		assertInHand(barrels);
		assertInDiscard(blunt, wizard, pot, fury);

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		// The second move is made without the Free Peoples player being asked.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite8ItMakesTheFellowshipMoveToSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var blunt = scn.GetFreepsCard("blunt");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		scn.MoveCardsToDiscard(blunt);
		assertInZone(Zone.SUPPORT, lights);
		assertInDiscard(blunt);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(lights));
		scn.ShadowUseCardAction(lights);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Dwarven or Shire event"));
		scn.FreepsChooseNo();
		assertInDiscard(blunt);

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoDwarvenOrShireEventInHisPileHeIsNotAskedAndTheFellowshipStillMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var wizard = scn.GetFreepsCard("wizard");
		var pot = scn.GetFreepsCard("pot");
		var fury = scn.GetShadowCard("fury");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		// Only the Shadow player's own pile holds a [dwarven] event.
		scn.MoveCardsToDiscard(wizard, pot, fury);
		assertInZone(Zone.SUPPORT, lights);
		assertInDiscard(wizard, pot, fury);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(4);
		scn.FreepsPass();

		scn.ShadowUseCardAction(lights);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsDecisionAvailable("Dwarven or Shire event"));
		assertInDiscard(wizard, pot, fury);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtSite6ItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		assertInZone(Zone.SUPPORT, lights);

		scn.SkipToPhase(Phase.REGROUP);
		// Esgaroth's own offer at the start of the regroup phase.
		scn.FreepsDeclineOptionalTrigger();
		scn.SetTwilight(4);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lights));
	}

	@Test
	public void AtSite7ItIsNotOfferedWith3InThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		assertInZone(Zone.SUPPORT, lights);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lights));
	}

	@Test
	public void AtTheMoveLimitTheCostIsPaidAndThereIsNoMoveAndDecliningTheTakeLeavesThePile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var blunt = scn.GetFreepsCard("blunt");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		// The regroup move from Erebor: the second move, the limit.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		scn.MoveCardsToDiscard(blunt);
		assertInZone(Zone.SUPPORT, lights);
		assertInDiscard(blunt);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		scn.SetTwilight(4);
		scn.FreepsPass();

		// Offered by rule, for its cost.
		assertTrue(scn.ShadowActionAvailable(lights));
		scn.ShadowUseCardAction(lights);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Dwarven or Shire event"));
		scn.FreepsChooseNo();
		assertInDiscard(blunt);

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		// No third move: the turn is over.
		assertEquals(8, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
	}

	@Test
	public void TheFellowshipIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lights = scn.GetShadowCard("lights");
		var blunt = scn.GetFreepsCard("blunt");
		// A move limit of 3, so that after the move the condition demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveCardsToSupportArea(lights);
		scn.MoveCardsToDiscard(blunt);
		assertInZone(Zone.SUPPORT, lights);
		assertInDiscard(blunt);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		scn.SetTwilight(4);
		scn.FreepsPass();
		scn.ShadowUseCardAction(lights);
		scn.FreepsChooseNo();
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

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
