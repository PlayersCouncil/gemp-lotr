package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_039_Tests
{
	// The Shadow phase of every test is at site 2 of the Fellowship path, shadow number 2, where a minion of
	// site number 4 pays 2 more for roaming.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dungeon", "102_39");
					// Two cards titled The Balrog, each costing 14 with a site number of 4.  Nothing in either's
					// text bears on being revealed or played at site 2.
					put("balrog", "102_38");     // The Balrog, Lieutenant of Morgoth
					put("demon", "19_18");       // The Balrog, Demon of Might
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] minion that is not The Balrog, cost 6, no text

					// Dwarf Guards, to raise the number of threats that may be added.  "To play, spot a Dwarf."
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FromDeepestDungeonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: From Deepest Dungeon
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time the fellowship moves, you may reveal The Balrog from your hand to add a threat.
		 * Shadow: Remove X threats.  Play The Balrog from your discard pile; its twilight cost is -X.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dungeon");

		assertEquals("From Deepest Dungeon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RevealingOneOfTwoBalrogsInHandWhenTheFellowshipMovesAddsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		var demon = scn.GetShadowCard("demon");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToHand(balrog, demon, troop);
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();

		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dungeon));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(balrog, demon));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(demon);

		// The Free Peoples player is shown the one revealed and not the other; the hand's owner is shown nothing.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(demon, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(balrog, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, balrog, demon);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveOfTheFellowshipMakesTheOfferAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToHand(balrog);
		// The rest of the Shadow player's deck, so that he draws nothing when he reconciles before the second move.
		scn.MoveCardsToShadowDiscard("demon", "troop", "guard1", "guard2", "guard3");
		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();

		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dungeon));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetThreats());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dungeon));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.HAND, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedNothingIsRevealedAndNoThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToHand(balrog);
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dungeon));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTheBalrogIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(dungeon);
		// Another [moria] minion in hand, and both Balrogs in the discard pile with the rest of the deck.
		scn.MoveCardsToHand(troop);
		scn.MoveCardsToShadowDiscard("balrog", "demon", "guard1", "guard2", "guard3");
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.FreepsPass();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToHand(balrog);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dungeon));
		scn.ShadowAcceptOptionalTrigger();
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAsManyThreatsAsCompanionsTheRevealIsStillOfferedAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		// Frodo alone: no more than 1 threat.
		scn.AddThreats(1);

		assertEquals(1, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dungeon));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovingTwoThreatsPlaysTheBalrogFromTheDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToDiscard(balrog);
		scn.MoveCompanionsToTable("guard1", "guard2", "guard3");

		scn.StartGame();
		scn.AddThreats(3);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 4 companions + 2 for the site
		assertEquals(26, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());
		assertInZone(Zone.DISCARD, balrog);
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);

		// X is any number of the threats there are, none included.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		// 14 - 2 for the threats removed + 2 for roaming
		assertEquals(26 - 14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoThreatsToRemoveTheBalrogIsPlayedFromTheDiscardPileAtFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToDiscard(balrog);

		scn.StartGame();
		scn.SetTwilight(13);
		scn.FreepsPass();

		// 13 set + 1 for Frodo + 2 for the site: exactly 14 + 2 for roaming
		assertEquals(16, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);

		// X can only be 0, and the game takes it without asking.

		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EitherBalrogInTheDiscardPileMayBePlayedAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		var demon = scn.GetShadowCard("demon");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToDiscard(balrog, demon, troop);
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.AddThreats(1);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 2 companions + 2 for the site
		assertEquals(24, scn.GetTwilight());

		scn.ShadowUseCardAction(dungeon);
		scn.ShadowChoose("1");

		assertTrue(scn.ShadowHasCardChoiceAvailable(balrog, demon));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(demon);

		assertInZone(Zone.SHADOW_CHARACTERS, demon);
		assertInZone(Zone.DISCARD, balrog, troop);
		assertEquals(0, scn.GetThreats());
		// 14 - 1 for the threat removed + 2 for roaming
		assertEquals(24 - 15, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithTheBalrogInHandAndOnlyAnotherMoriaMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToHand(balrog);
		scn.MoveCardsToDiscard(troop);
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.AddThreats(2);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// The offer to reveal the Balrog in hand
		scn.ShadowDeclineOptionalTrigger();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// The pool is enough: the Balrog can be played from hand.
		assertEquals(24, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(balrog));
		assertFalse(scn.ShadowActionAvailable(dungeon));
	}

	@Test
	public void IsNotOfferedWhenRemovingEveryThreatLeavesTheBalrogOneTwilightOutOfReach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToDiscard(balrog);
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.AddThreats(2);
		scn.SetTwilight(9);
		scn.FreepsPass();

		// 9 set + 2 companions + 2 for the site: one short of 14 - 2 for both threats + 2 for roaming
		assertEquals(13, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dungeon));
	}

	@Test
	public void IsOfferedWhenRemovingEveryThreatBringsTheBalrogJustWithinThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToDiscard(balrog);
		scn.MoveCompanionsToTable("guard1");

		scn.StartGame();
		scn.AddThreats(2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for the site: exactly 14 - 2 for both threats + 2 for roaming
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		// Both threats are needed, so 2 is the only X there is, and the game takes it without asking.
		scn.ShadowUseCardAction(dungeon);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSmallestXThatMayBeNamedIsOneThatPaysForTheBalrog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog = scn.GetShadowCard("balrog");
		scn.MoveCardsToSupportArea(dungeon);
		scn.MoveCardsToDiscard(balrog);
		scn.MoveCompanionsToTable("guard1", "guard2");

		scn.StartGame();
		scn.AddThreats(3);
		scn.SetTwilight(9);
		scn.FreepsPass();

		// 9 set + 3 companions + 2 for the site: 2 short of 14 + 2 for roaming, so X has to be 2 or 3
		assertEquals(14, scn.GetTwilight());
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);

		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
