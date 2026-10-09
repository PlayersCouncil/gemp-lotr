package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_026_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "59_26");
					put("radagast2", "59_26");
					put("guard", "1_7");       // Dwarf Guard: strength 4, vitality 2
					put("gandalf", "1_72");    // Gandalf, Friend of the Shirefolk: vitality 4; here to be spotted by Saved From the Fire
					put("saved", "8_20");      // Saved From the Fire: Fellowship event, cost 3: "Spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to 3 cards from that companion's culture into hand from your draw deck."
					put("sentback", "59_27");  // Sent Back: "Fellowship: Play a Wizard (even if another copy of that Wizard is in your dead pile)."

					// Axe Strike: a [dwarven] Skirmish event, here only as cards in hands and decks
					put("chaff1", "1_3");
					put("chaff2", "1_3");
					put("chaff3", "1_3");
					put("chaff4", "1_3");
					put("chaff5", "1_3");
					put("chaff6", "1_3");

					put("slayer1", "3_93");    // Morgul Slayer: "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("slayer2", "3_93");
					put("lemenya", "6_86");    // Ulaire Lemenya, Winged Hunter: "Regroup: Exert Ulaire Lemenya and discard 3 cards from hand to return a companion (except the Ring-bearer) to owner's hand."
					put("nomad1", "7_132");    // Desert Nomad: a [raider] Man; damage +1 only at 2 threats
					put("nomad2", "7_132");
					put("despair", "7_148");   // Fierce in Despair: Regroup event, cost 7: "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RadagastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Radagast, The Brown
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: The move limit is +1. When Radagast leaves play, this bonus is immediately lost.
		 * 	Each time the fellowship moves, each opponent may discard 2 cards from hand to draw 2 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("radagast");

		assertEquals("Radagast", card.getBlueprint().getTitle());
		assertEquals("The Brown", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFromHandForFourMakesTheMoveLimitThreeAndAThirdMoveIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(radagast);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, radagast);
		assertEquals(3, scn.GetMoveLimit());

		// The Shadow player's hand is empty at the first move, so he is offered nothing.
		scn.FreepsPass();
		assertEquals(1, scn.GetMoveCount());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		// He has reconciled since and has cards to discard: declined.
		scn.ShadowChooseNo();
		assertEquals(2, scn.GetMoveCount());

		// Two moves made, and a third is still on offer.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		scn.ShadowChooseNo();
		assertEquals(3, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		// At the limit the regroup phase ends the turn with no question.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheBonusIsLostAtOnceWhenRadagastIsKilledAndTheTurnEndsAfterTwoMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(radagast, guard);
		// Each is one wound from death.
		scn.AddWoundsToChar(radagast, 3);
		scn.AddWoundsToChar(guard, 1);

		var slayer1 = scn.GetShadowCard("slayer1");
		var slayer2 = scn.GetShadowCard("slayer2");

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseNo();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		scn.MoveMinionsToTable(slayer1, slayer2);
		scn.FreepsPass();

		// Another companion's death takes nothing away.
		scn.ShadowUseCardAction(slayer1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(radagast, guard));
		scn.ShadowChooseCard(guard);
		assertInZone(Zone.DEAD, guard);
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer2);
		assertInZone(Zone.DEAD, radagast);
		// His death and the loss of the bonus answer the same wound; the Free Peoples player puts them in order.
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetMoveLimit());

		// Two moves of two: where a third was on offer with Radagast alive, the turn now ends.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheBonusIsLostAtOnceWhenRadagastIsReturnedToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(radagast, guard);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetMoveLimit());
		// Lemenya's ability discards 3 cards from hand.
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToHand(scn.GetShadowCard("chaff1"), scn.GetShadowCard("chaff2"), scn.GetShadowCard("chaff3"));
		scn.FreepsPass();

		scn.ShadowUseCardAction(lemenya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(radagast, guard));
		scn.ShadowChooseCard(radagast);

		assertInZone(Zone.HAND, radagast);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBonusIsLostAtOnceWhenRadagastIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(radagast);

		var nomad1 = scn.GetShadowCard("nomad1");
		var nomad2 = scn.GetShadowCard("nomad2");
		var despair = scn.GetShadowCard("despair");

		scn.StartGame();
		// Here he comes into play this very turn.
		scn.FreepsPlayCard(radagast);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetMoveLimit());
		// Fierce in Despair costs 7 and spots 2 [raider] Men.
		scn.MoveMinionsToTable(nomad1, nomad2);
		scn.MoveCardsToHand(despair);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(radagast, guard));
		scn.ShadowChooseCard(radagast);

		assertInDiscard(radagast);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBonusStaysLostForTheRestOfTheTurnWhenRadagastLeavesInTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saved = scn.GetFreepsCard("saved");
		scn.MoveCompanionsToTable(radagast, gandalf);
		scn.MoveCardsToHand(saved);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		// The first phase of the turn: Saved From the Fire places Radagast in the dead pile.
		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(radagast);
		scn.FreepsResolveRuleFirst();
		// The search of the deck: shown first, and nothing taken.
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, radagast);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertEquals(1, scn.GetMoveCount());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());

		// Two phases on, the loss still holds: the limit is 2, and the second move was the last.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetMoveLimit());
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The loss ends with the turn: on the next turn of the Free Peoples player the limit is the usual 2.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void ASecondRadagastLeavingPlayInTheSameTurnTakesAwayOnlyHisOwnBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var radagast2 = scn.GetFreepsCard("radagast2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saved = scn.GetFreepsCard("saved");
		var sentback = scn.GetFreepsCard("sentback");
		scn.MoveCompanionsToTable(radagast, gandalf);
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToHand(radagast2, saved);

		var slayer1 = scn.GetShadowCard("slayer1");

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		// The first Radagast is placed in the dead pile in the fellowship phase.
		scn.FreepsPlayCard(saved);
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, gandalf));
		scn.FreepsChooseCard(radagast);
		scn.FreepsResolveRuleFirst();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, radagast);
		assertEquals(2, scn.GetMoveLimit());

		// Sent Back plays the second, though the first is in the dead pile.
		scn.FreepsUseCardAction(sentback);
		assertInZone(Zone.FREE_CHARACTERS, radagast2);
		assertEquals(3, scn.GetMoveLimit());

		// One wound from death, and one move made.
		scn.AddWoundsToChar(radagast2, 3);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer1);
		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer1);
		scn.ShadowChooseCard(radagast2);
		scn.FreepsResolveRuleFirst();

		// No Radagast in play, no bonus: the move limit is 2 again, and with one move made a second is on offer.
		assertInZone(Zone.DEAD, radagast2);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void EachTimeTheFellowshipMovesTheShadowPlayerMayDiscardTwoCardsFromHandToDrawTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);
		// Two cards in Radagast's owner's hand as well: he is no opponent of his own.
		scn.MoveCardsToHand(scn.GetFreepsCard("chaff1"), scn.GetFreepsCard("chaff2"));

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		var despair = scn.GetShadowCard("despair");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(despair, lemenya);

		int deck = scn.GetShadowDeckCount();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		// The move of the fellowship phase.
		scn.FreepsPass();
		assertEquals(1, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2 cards"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseYes();

		// Exactly 2 of his 3 cards.
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(chaff1, chaff2);

		assertInDiscard(chaff1, chaff2);
		assertEquals(2, scn.GetShadowDiscardCount());
		// The third card stays, and the two from the top of his deck join it.
		assertInZone(Zone.HAND, chaff3, despair, lemenya);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// And again on the second move of the turn, in the regroup phase. He has reconciled to 8 by then.
		scn.SkipToMovementDecision();
		int hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();
		assertInZone(Zone.HAND, chaff3, despair);
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2 cards"));
		scn.ShadowChooseYes();
		scn.ShadowChooseCards(chaff3, despair);

		assertInDiscard(chaff3, despair);
		assertEquals(4, scn.GetShadowDiscardCount());
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheShadowPlayersHandAsItWasAndStayingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		scn.StartGame();

		int deck = scn.GetShadowDeckCount();
		scn.FreepsPass();
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards to draw 2 cards"));
		scn.ShadowChooseNo();

		assertInZone(Zone.HAND, chaff1, chaff2, chaff3);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship stays where it is: no move, and nothing is offered, with cards in his hand to pay with.
		scn.SkipToMovementDecision();
		int hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();
		assertTrue(hand >= 2);
		scn.FreepsChooseToStay();

		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards to draw 2 cards"));
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithOneCardInHandTheShadowPlayerIsOfferedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		var chaff1 = scn.GetShadowCard("chaff1");
		scn.MoveCardsToHand(chaff1);

		scn.StartGame();

		int deck = scn.GetShadowDeckCount();
		scn.FreepsPass();

		assertEquals(1, scn.GetMoveCount());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards to draw 2 cards"));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
