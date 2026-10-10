package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_071_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "13_71");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetReconcileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("isildur", "13_71");
					// Goblin Patrol Troop, a card with no text, ten times over: six for a hand that keeps
					// Isildur's resistance up, four for a draw deck deep enough to reconcile from and draw again
					put("card1", "1_177");
					put("card2", "1_177");
					put("card3", "1_177");
					put("card4", "1_177");
					put("card5", "1_177");
					put("card6", "1_177");
					put("card7", "1_177");
					put("card8", "1_177");
					put("card9", "1_177");
					put("card10", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsildurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Isildur, Heir of Elendil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 1
		 * Game Text: <b>Knight</b>.<br>Isildur is resistance +1 for each card in your hand.<br>At the end of any phase, if Isildur has resistance 0, place him in your dead pile.<br>Each time you reconcile, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Isildur", card.getBlueprint().getTitle());
		assertEquals("Heir of Elendil", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void ReconcilingOffersIsildursOwnerACardAndTakingItDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReconcileScenario();

		var isildur = scn.GetFreepsCard("isildur");
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"));

		scn.StartGame();
		scn.MoveCompanionsToTable(isildur);

		// No minion is played: the regroup phase follows the Shadow phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();

		// The Shadow player has reconciled, and that is not Isildur's owner reconciling: nothing is offered.
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(isildur));
		scn.FreepsChooseToStay();

		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		scn.FreepsDeclineReconciliation();

		// He has reconciled, a hand of 6 made up to 8, and now Isildur makes his offer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isildur));
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(9, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInZone(Zone.FREE_CHARACTERS, isildur);

		// The turn is over: the other player's begins.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithAnEmptyDrawDeckTheDrawIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReconcileScenario();

		var isildur = scn.GetFreepsCard("isildur");
		// A hand of eight, and the two cards left over out of the draw deck.
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"),
				scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"),
				scn.GetFreepsCard("card7"), scn.GetFreepsCard("card8"));
		scn.MoveCardsToDiscard(scn.GetFreepsCard("card9"), scn.GetFreepsCard("card10"));

		scn.StartGame();
		scn.MoveCompanionsToTable(isildur);

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();
		scn.FreepsChooseToStay();

		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.FreepsDeclineReconciliation();

		// An optional draw is offered though there is no card to draw (Ketura's ruling on Q-0029).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(isildur));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInZone(Zone.FREE_CHARACTERS, isildur);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
