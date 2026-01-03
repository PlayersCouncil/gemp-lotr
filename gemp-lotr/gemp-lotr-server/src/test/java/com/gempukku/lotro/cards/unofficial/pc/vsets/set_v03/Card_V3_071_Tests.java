package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hour", "103_71");
					put("hour2", "103_71");
					put("witchking", "1_237");
					put("rider1", "12_161");
					put("rider2", "12_161");

					put("sam", "1_311");      // 3/4 - survives losing
					put("aragorn", "1_89");   // 8/4 - for testing strength
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThisisMyHourStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: This is My Hour
		 * Unique: false
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If your Nazgul wins a skirmish, exert it to reconcile your hand.  You may discard a Nazgul from hand to wound a companion it was skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hour");

		assertEquals("This is My Hour", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}


// ======== BASIC FUNCTIONALITY ========

	@Test
	public void ThisIsMyHourReconcilesHandWhenNazgulWinsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Witch-king (14 str) beats Aragorn (8 str)
		// Response available
		int shadowHandBefore = scn.GetShadowHandCount();
		assertEquals(0, scn.GetWoundsOn(witchking));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Witch-king exerted
		assertEquals(1, scn.GetWoundsOn(witchking));

		// Decline optional wound (no Nazgul in hand anyway after playing event)
		if (scn.ShadowDecisionAvailable("Would you like to discard a Nazgul")) {
			scn.ShadowChooseNo();
		}

		//We drew cards due to the reconcile
		assertTrue(scn.GetShadowHandCount() > shadowHandBefore);
	}

	@Test
	public void ThisIsMyHourCanDiscardNazgulToWoundLoser() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour, rider1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Aragorn takes 1 wound from losing, survives (4 vitality)
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowPass(); //Don't want to discard any card in hand during reconcile

		// Optional: discard Nazgul to wound loser
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard a Nazgul"));
		scn.ShadowChooseYes();
		scn.ShadowChooseCard(rider1);

		// Aragorn takes additional wound
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(rider1);
	}

	@Test
	public void ThisIsMyHourCanDeclineOptionalWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour, rider1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowPass(); //Reconcile discard

		// Decline optional wound
		scn.ShadowChooseNo();

		// Aragorn still at 1 wound, rider still in hand
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInHand(rider1);
	}

	@Test
	public void ThisIsMyHourOptionalWoundNotOfferedWithoutNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(hour);
		scn.MoveCardsToShadowDiscard("rider1", "rider2");
		// No Nazgul in hand

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		scn.ShadowAcceptOptionalTrigger();

		// Optional wound decision should not appear - no Nazgul in hand
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard a Nazgul"));

		// Sam only has skirmish wound
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ThisIsMyHourCanStillDiscardNazgulEvenIfLoserDied() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(hour, rider1);

		scn.StartGame();
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(sam, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		// Sam should be dead (got overwhelmed when losing skirmish)
		assertInZone(Zone.DEAD, sam);

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowPass(); //Decline reconcile discard

		// Can still choose to discard Nazgul (for cycling) even though wound fizzles
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard a Nazgul"));
		scn.ShadowChooseYes();
		scn.ShadowChooseCard(rider1);

		// Rider discarded (for cycling value)
		assertInDiscard(rider1);
	}

	@Test
	public void ThisIsMyHourRestoresHinderedCardsOnReconcile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var rider1 = scn.GetShadowCard("rider1");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(witchking, rider1);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(hour);
		scn.HinderCard(rider1);

		scn.StartGame();
		scn.SkipToAssignments();

		assertTrue(scn.IsHindered(rider1));

		scn.FreepsAssignToMinions(sam, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		scn.ShadowAcceptOptionalTrigger();

		// Reconcile restores hindered cards
		assertFalse(scn.IsHindered(rider1));
	}

	@Test
	public void ThisIsMyHourNotAvailableIfWinnerExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hour = scn.GetShadowCard("hour");
		var witchking = scn.GetShadowCard("witchking");
		var sam = scn.GetFreepsCard("sam");

		scn.MoveMinionsToTable(witchking);
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(hour);
		scn.AddWoundsToChar(witchking, 3);  // 4 vitality, exhausted

		scn.StartGame();
		scn.SkipToAssignments();

		assertTrue(scn.IsExhausted(witchking));

		scn.FreepsAssignToMinions(sam, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(sam);
		scn.PassCurrentPhaseActions();

		// Response should not be available - can't exert exhausted Nazgul
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}
}
