package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_317_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: a second companion, so that two threats can lie on the dead pile
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Hope of Free Peoples is the card under test, laid as the table's Ring-bearer.
				"7_317",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Frodo, Hope of Free Peoples
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Aragorn
		 * Game Text: <b>Ring-bearer (resistance 10)</b>. <b>Ring-bound</b>.<br>Each time the fellowship moves, you may add (3) to remove a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Hope of Free Peoples", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void EachMoveOfTheTurnOffersToAddThreeToRemoveAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		// 3 added by Frodo, then 2 for Ettenmoors and 2 for the companions
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move of the same turn: offered again.
		scn.SkipToMovementDecision();
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		// 1 + 3 added by Frodo, then 0 for Rivendell Valley and 2 for the companions
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheThreatsAndAddsNoTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetThreats());
		// 2 for Ettenmoors and 2 for the companions, and nothing from Frodo
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatTheOfferIsStillMadeAndTakingItAddsTheTwilightForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		// The (3) is a cost: the offer is made though nothing can be removed (Q-0034).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(frodo));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		// 3 added by Frodo, then 2 for Ettenmoors and 2 for the companions
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupMoveDecisionOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		// The move that ends the fellowship phase is declined on the way.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetThreats());
		scn.FreepsChooseToStay();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
