package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("drawing", "10_29");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
					put("oathbreaker", "8_41"); // Oathbreaker: a [gondor] Wraith companion
					put("sleepless", "8_45");   // Sleepless Dead: a third [gondor] companion, a Wraith
					put("merchant", "7_97");    // Gondorian Merchant: a [gondor] ally, no companion
					put("guard", "5_83");       // Household Guard: a [rohan] companion, Valiant and no other text
					put("runner", "1_178");     // Goblin Runner: a Shadow minion for the Shadow hand
					put("troop", "1_177");      // Goblin Patrol Troop: another
					put("teeth", "5_110");      // Teeth of Mordor: a Shadow condition for the Shadow hand
					put("corpse", "6_99");      // Corpse Lights: a Shadow minion of another culture
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("drawing", "10_29");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("runner", "1_178");     // Goblin Runner: a card for the Shadow player's hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DrawingHisEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Drawing His Eye
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: For each [gondor] companion you spot, reveal 1 card at random from a Shadow player's hand. Choose a revealed Shadow card to be discarded, then its owner draws a card.
		*/

		var scn = GetScenario();

		var drawing = scn.GetFreepsCard("drawing");

		assertEquals("Drawing His Eye", drawing.getBlueprint().getTitle());
		assertNull(drawing.getBlueprint().getSubtitle());
		assertFalse(drawing.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, drawing.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, drawing.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, drawing.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(drawing, Timeword.FELLOWSHIP));
		assertEquals(2, drawing.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsNotPlayableWithTheShadowHandEmpty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var drawing = scn.GetFreepsCard("drawing");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(drawing);

		scn.StartGame();

		// A [gondor] companion to spot, and no card in the Shadow hand to reveal: the reveal must be able to happen
		// for the rest to read it, so the event is not playable (Q-0336: B, applied by engine pass 53).
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsPlayAvailable(drawing));

		// One card there, and it is playable.
		scn.MoveCardsToHand(runner);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(drawing));
		assertInZone(Zone.HAND, drawing);
	}

	@Test
	public void ThePlayerNamesHowManyGondorCompanionsToSpotAndThatManyCardsAreRevealedAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawing = scn.GetFreepsCard("drawing");
		var knight = scn.GetFreepsCard("knight");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var teeth = scn.GetShadowCard("teeth");
		var corpse = scn.GetShadowCard("corpse");
		scn.MoveCompanionsToTable(knight, oathbreaker, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(drawing);

		scn.StartGame();
		// Four different Shadow cards, so that the card discarded can be told from another card of the hand.
		scn.MoveCardsToHand(runner, troop, teeth, corpse);
		assertInHand(runner);
		assertInHand(troop);
		assertInHand(teeth);
		assertInHand(corpse);
		scn.FreepsRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.FreepsPlayCard(drawing);

		// Two [gondor] companions, a Man and a Wraith; the [gondor] ally, the [rohan] companion and Frodo are not counted.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("1");

		// One card revealed, to the Free Peoples player and not to the hand's owner.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		// Which card is shown is chance; what is read is which it was.
		var runnerShown = scn.FreepsHasCardChoiceAvailable(runner, false);
		var troopShown = scn.FreepsHasCardChoiceAvailable(troop, false);
		var teethShown = scn.FreepsHasCardChoiceAvailable(teeth, false);
		var corpseShown = scn.FreepsHasCardChoiceAvailable(corpse, false);
		scn.FreepsDismissRevealedCards();

		// The one Shadow card revealed is the one discarded, unasked, and the Shadow player draws a card.
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		if (runnerShown) assertInDiscard(runner); else assertInHand(runner);
		if (troopShown) assertInDiscard(troop); else assertInHand(troop);
		if (teethShown) assertInDiscard(teeth); else assertInHand(teeth);
		if (corpseShown) assertInDiscard(corpse); else assertInHand(corpse);
		assertInDiscard(drawing);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheWholeHandRevealedOnlyARevealedShadowCardMayBeChosenAndItsOwnerDraws() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawing = scn.GetFreepsCard("drawing");
		var knight = scn.GetFreepsCard("knight");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var sleepless = scn.GetFreepsCard("sleepless");
		var runner = scn.GetShadowCard("runner");
		var teeth = scn.GetShadowCard("teeth");
		var shadowGuard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(knight, oathbreaker, sleepless);
		scn.MoveCardsToHand(drawing);

		scn.StartGame();
		// A Shadow minion, a Shadow condition and a Free Peoples card of the Shadow player's own.
		scn.MoveCardsToHand(runner, teeth, shadowGuard);
		assertInHand(runner);
		assertInHand(teeth);
		assertInHand(shadowGuard);
		scn.FreepsRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		assertEquals(3, scn.GetShadowHandCount());
		scn.FreepsPlayCard(drawing);

		assertEquals(3, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("3");

		// Three companions spotted, three cards in the hand: every one of them is revealed.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(teeth, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowGuard, false));
		scn.FreepsDismissRevealedCards();

		// A revealed Shadow card, either one; not the revealed Free Peoples card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, teeth));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shadowGuard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(teeth);

		assertInDiscard(teeth);
		assertInHand(runner);
		assertInHand(shadowGuard);
		assertEquals(1, scn.GetShadowDiscardCount());
		// One discarded and one drawn by its owner, the Shadow player.
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	@Ignore("Q-0350: with no Shadow card revealed nothing is discarded, and 'its owner' draws nothing; the game has the Shadow player draw")
	public void WithNoShadowCardRevealedNothingIsDiscardedAndNoCardIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var drawing = scn.GetFreepsCard("drawing");
		var knight = scn.GetFreepsCard("knight");
		var oathbreaker = scn.GetFreepsCard("oathbreaker");
		var shadowGuard = scn.GetShadowCard("guard");
		var shadowKnight = scn.GetShadowCard("knight");
		scn.MoveCompanionsToTable(knight, oathbreaker);
		scn.MoveCardsToHand(drawing);

		scn.StartGame();
		// Two Free Peoples cards of the Shadow player's own, and no Shadow card.
		scn.MoveCardsToHand(shadowGuard, shadowKnight);
		assertInHand(shadowGuard);
		assertInHand(shadowKnight);
		scn.FreepsRefreshDecision();

		int deck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.FreepsPlayCard(drawing);
		scn.FreepsChoose("2");

		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertInHand(shadowGuard);
		assertInHand(shadowKnight);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
