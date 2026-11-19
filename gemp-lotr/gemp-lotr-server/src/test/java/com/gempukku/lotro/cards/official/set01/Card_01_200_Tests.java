package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_200_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("underdeeps", "1_200");

					put("scout", "1_191");      // Moria Scout: [moria] Orc, cost 2; text only when played
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, no Orc
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheUnderdeepsofMoriaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Underdeeps of Moria
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * <b>Shadow:</b> Remove (3) to reveal the bottom card of your draw deck. If it is a [moria] Orc, take it into hand. Otherwise, discard it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("underdeeps");

		assertEquals("The Underdeeps of Moria", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesThreeToRevealTheBottomCardAndTakesAMoriaOrcIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underdeeps = scn.GetShadowCard("underdeeps");
		var scout = scn.GetShadowCard("scout");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var freepsScout = scn.GetFreepsCard("scout");
		scn.MoveCardsToSupportArea(underdeeps);

		scn.StartGame();
		// The Shadow player's deck: the Troll on top, the Scout at the bottom and the Troop just above it.  The
		// Free Peoples player's own bottom card is a [moria] Orc as well.
		scn.MoveCardsToTopOfDeck(troll);
		scn.MoveCardsToBottomOfDeck(troop, scout, freepsScout);
		assertEquals(troll, scn.GetShadowTopOfDeck());
		assertEquals(scout, scn.GetShadowBottomOfDeck());
		assertEquals(troop, scn.GetFromBottomOfShadowDeck(2));
		assertEquals(freepsScout, scn.GetFreepsBottomOfDeck());
		assertInZone(Zone.DECK, troll, troop, scout, freepsScout);
		assertInZone(Zone.SUPPORT, underdeeps);
		scn.SetTwilight(3);

		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight()); // 3 set + 1 companion + 2 for Ettenmoors
		assertEquals(0, scn.GetShadowHandCount());
		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowActionAvailable(underdeeps));

		scn.ShadowUseCardAction(underdeeps);
		assertEquals(3, scn.GetTwilight());
		// The bottom card is shown to both players, and the top card is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(troll, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, false));
		scn.DismissRevealedCards();

		assertInHand(scout);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(troll, scn.GetShadowTopOfDeck());
		assertEquals(troop, scn.GetShadowBottomOfDeck());
		assertEquals(freepsScout, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Again: another [moria] Orc, of another title and cost.
		scn.ShadowUseCardAction(underdeeps);
		assertEquals(0, scn.GetTwilight());
		scn.DismissRevealedCards();
		assertInHand(scout, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(troll, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABottomCardThatIsNoMoriaOrcIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underdeeps = scn.GetShadowCard("underdeeps");
		var scout = scn.GetShadowCard("scout");
		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(underdeeps);

		scn.StartGame();
		// A [moria] Orc on top; at the bottom a [moria] minion who is no Orc, and above it an Orc of another culture.
		scn.MoveCardsToTopOfDeck(scout);
		scn.MoveCardsToBottomOfDeck(soldier, troll);
		assertEquals(scout, scn.GetShadowTopOfDeck());
		assertEquals(troll, scn.GetShadowBottomOfDeck());
		assertEquals(soldier, scn.GetFromBottomOfShadowDeck(2));
		assertInZone(Zone.DECK, scout, troll, soldier);
		scn.SetTwilight(3);

		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight()); // 3 set + 1 companion + 2 for Ettenmoors
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowUseCardAction(underdeeps);
		assertEquals(3, scn.GetTwilight());
		scn.DismissRevealedCards();
		assertInDiscard(troll);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(scout, scn.GetShadowTopOfDeck());
		assertEquals(soldier, scn.GetShadowBottomOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowUseCardAction(underdeeps);
		assertEquals(0, scn.GetTwilight());
		scn.DismissRevealedCards();
		assertInDiscard(troll, soldier);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 2, scn.GetShadowDeckCount());
		assertEquals(scout, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoInThePoolAndOfferedWithThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underdeeps = scn.GetShadowCard("underdeeps");
		scn.MoveCardsToSupportArea(underdeeps);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, underdeeps);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(underdeeps));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(underdeeps));
	}

	@Test
	public void WithAnEmptyDrawDeckItRemovesThreeForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var underdeeps = scn.GetShadowCard("underdeeps");
		scn.MoveCardsToSupportArea(underdeeps);

		scn.StartGame();
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetShadowHandCount());
		int discard = scn.GetShadowDiscardCount();
		assertTrue(scn.ShadowActionAvailable(underdeeps));

		scn.ShadowUseCardAction(underdeeps);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
