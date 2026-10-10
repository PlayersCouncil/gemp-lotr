package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Muster.
 *
 * Comprehensive Rules 4.2, "muster": "At the start of the regroup phase, for each of your characters who has the
 * muster keyword, you may discard a card from hand, then draw a card.  You must discard all at once."
 *
 * Its example: "If you have two muster characters, you cannot choose to discard a card, draw a card, then discard
 * another card and draw a card.  You must instead choose to discard 0, 1, or 2 cards, then draw that same number of
 * cards."
 *
 * "Your characters" has no side in it: a Shadow player whose minion has muster is made the offer as a Free
 * Peoples player is for a companion.  A player with no card in hand has nothing to discard and is not asked.  A
 * card with muster and a start-of-regroup text of its own is in OptionalActionsAnsweringOneTriggerTests.
 */
public class MusterTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Witch-king, Captain of the Nine Riders: fierce, toil 2 and muster, and no other text.
					put("witchking", "11_226");
					put("runner", "1_178");     // Goblin Runner and Goblin Patrol Troop: the two cards in the Shadow hand
					put("troop", "1_177");
					put("marksman", "1_176");   // Goblin Marksman: the top card of the Shadow draw deck

					// Elven Scout: "While you can spot another Elf, this companion has muster."  Two of them
					// are two muster companions with no other text.
					put("scout1", "11_18");
					put("scout2", "11_18");
					put("guard", "1_7");        // Dwarf Guard and Dwarven Armor: the top two cards of the Free Peoples draw deck
					put("armor", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheShadowPlayerIsOfferedMusterForHisMinionAtTheStartOfTheRegroupPhaseAndDiscardsACardToDrawOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(runner, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.MoveCardsToTopOfDeck(marksman);
		// Nobody is assigned to the Witch-king in either round (he is fierce), so he is in play when the regroup
		// phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(marksman, scn.GetShadowTopOfDeck());

		// The offer is the Shadow player's; the Free Peoples player, who has no muster character, is asked
		// nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking, "Use Muster"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// One muster character: no card or one.
		scn.ShadowChooseOptionalTrigger(witchking, "Use Muster");
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));

		// The Runner is discarded and the top card of the deck drawn: the hand is the size it was.
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.HAND, marksman);
		assertInZone(Zone.HAND, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoMusterCharactersUpToTwoCardsAreDiscardedAllAtOnceAndAsManyAreDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetFreepsCard("scout1");
		var scout2 = scn.GetFreepsCard("scout2");
		// The Free Peoples player's own copies of the three Goblins are his hand.
		var runner = scn.GetFreepsCard("runner");
		var troop = scn.GetFreepsCard("troop");
		var marksman = scn.GetFreepsCard("marksman");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(scout1, scout2);
		scn.MoveCardsToHand(runner, troop, marksman);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(guard, armor);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.HasKeyword(scout1, Keyword.MUSTER));
		assertTrue(scn.HasKeyword(scout2, Keyword.MUSTER));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();

		// One offer for the two of them, and nothing for the Shadow player, who has no muster character.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// The rules' own example: "You must instead choose to discard 0, 1, or 2 cards, then draw that same
		// number of cards."
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, troop, marksman));

		// Both are discarded by the one answer, and then the top two cards of the deck are drawn.
		scn.FreepsChooseCards(runner, troop);
		assertInZone(Zone.DISCARD, runner, troop);
		assertInZone(Zone.HAND, marksman, armor, guard);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		// There is no second offer for the second Scout.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCardInHandMusterIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.MoveCardsToTopOfDeck(marksman);
		scn.SkipToPhase(Phase.REGROUP);

		// The board of the first test, with a card to draw and none to discard.
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertTrue(scn.HasKeyword(witchking, Keyword.MUSTER));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(marksman, scn.GetShadowTopOfDeck());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MusterDeclinedOrTakenWithNoCardDiscardedChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetFreepsCard("scout1");
		var scout2 = scn.GetFreepsCard("scout2");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var freepsRunner = scn.GetFreepsCard("runner");
		var freepsTroop = scn.GetFreepsCard("troop");
		var witchking = scn.GetShadowCard("witchking");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(scout1, scout2);
		scn.MoveCardsToHand(freepsRunner, freepsTroop);
		scn.MoveCardsToHand(runner, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.MoveCardsToTopOfDeck(armor, marksman);
		// Nobody is assigned to the Witch-king in either round.
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		assertEquals(marksman, scn.GetShadowTopOfDeck());
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();

		// Each player has muster characters and a hand, and each is made the offer: the Free Peoples player
		// first.  He takes it and discards no card: none is drawn.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable("Use Muster"));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.HAND, freepsRunner, freepsTroop);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(armor, scn.GetFreepsTopOfDeck());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The Shadow player declines the offer itself.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking, "Use Muster"));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.HAND, runner, troop);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(marksman, scn.GetShadowTopOfDeck());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
