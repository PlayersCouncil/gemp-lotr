package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_017_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. With no minion in
	// play each move goes straight on to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "31_17");
					// Gandalf, Friend of Thorin and Gandalf, The Grey: two Gandalfs (every Gandalf is a [gandalf]
					// Wizard companion). Neither has a "when you play" text.
					put("gandalf1", "30_26");
					put("gandalf2", "30_27");
					// Saruman, The White: a [gandalf] Wizard who is no Gandalf (an ally)
					put("saruman", "32_24");
					// Ent Avenger: a [gandalf] companion who is no Gandalf (an Ent)
					put("avenger", "6_27");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RadagastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Radagast, The Brown
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 5H
		 * Game Text: <b>Wise</b>.
		 * 	<b>Fellowship:</b> Play Gandalf from your discard pile to make the move limit for this turn +1. Choose an opponent who may draw 2 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("radagast");

		assertEquals("Radagast", card.getBlueprint().getTitle());
		assertEquals("The Brown", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.HOBBIT, 5)));
	}

	@Test
	public void PlaysGandalfFromTheDiscardPileToMakeTheMoveLimitPlusOneForTheTurnAndTheOpponentMayDrawTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var gandalf1 = scn.GetFreepsCard("gandalf1");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var saruman = scn.GetFreepsCard("saruman");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToDiscard(gandalf1, gandalf2, saruman, avenger);

		scn.StartGame();
		assertInDiscard(gandalf1, gandalf2, saruman, avenger);
		assertEquals(2, scn.GetMoveLimit());
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		assertTrue(scn.FreepsActionAvailable(radagast));
		scn.FreepsUseCardAction(radagast);

		// Either Gandalf may be played; a [gandalf] Wizard and a [gandalf] companion who are no Gandalf may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf1, gandalf2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman, avenger));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf2);
		assertInZone(Zone.FREE_CHARACTERS, gandalf2);
		assertInDiscard(gandalf1, saruman, avenger);
		assertEquals(3, scn.GetMoveLimit());

		// The opponent is asked, and draws.
		assertTrue(scn.ShadowDecisionAvailable("draw 2 cards"));
		scn.ShadowChooseYes();
		assertEquals(shadowHand + 2, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The limit holds for the whole turn: a third move is offered in the regroup phase after the second.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// On the player's next turn it is 2 again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void TheOpponentMayDeclineToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var gandalf1 = scn.GetFreepsCard("gandalf1");
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToDiscard(gandalf1);

		scn.StartGame();
		assertInDiscard(gandalf1);
		int shadowHand = scn.GetShadowHandCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.FreepsUseCardAction(radagast);
		// The one Gandalf in the discard pile is played unasked.
		assertInZone(Zone.FREE_CHARACTERS, gandalf1);
		assertEquals(3, scn.GetMoveLimit());

		assertTrue(scn.ShadowDecisionAvailable("draw 2 cards"));
		scn.ShadowChooseNo();
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGandalfInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var gandalf1 = scn.GetFreepsCard("gandalf1");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var saruman = scn.GetFreepsCard("saruman");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToDiscard(saruman, avenger);

		scn.StartGame();
		assertInDiscard(saruman, avenger);

		// No Gandalf there: not offered.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(radagast));

		// A Gandalf there, but a Gandalf already in play: the one in the pile cannot be played, and it is still not offered.
		scn.MoveCompanionsToTable(gandalf1);
		scn.MoveCardsToDiscard(gandalf2);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.FREE_CHARACTERS, gandalf1);
		assertInDiscard(gandalf2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(radagast));
		assertEquals(2, scn.GetMoveLimit());
	}
}
