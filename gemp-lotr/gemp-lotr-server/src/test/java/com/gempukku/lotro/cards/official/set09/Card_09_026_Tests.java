package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "9_26");
					put("radagast2", "9_26");
					put("guard", "1_7");       // Dwarf Guard: a companion with no text that reaches these tests
					put("gandalf", "1_72");    // Gandalf, Friend of the Shirefolk: here to be spotted by Saved From the Fire
					put("saved", "8_20");      // Saved From the Fire: Fellowship event, cost 3: "Spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to 3 cards from that companion's culture into hand from your draw deck."
					put("sentback", "9_27");   // Sent Back: support-area condition; "Fellowship or Regroup: Play a Wizard (even if another copy of that Wizard is in your dead pile)."

					put("chaff1", "1_3");      // Axe Strike: a [dwarven] Skirmish event, here only as a card in a hand or a deck
					put("chaff2", "1_3");
					put("chaff3", "1_3");
					put("lemenya", "6_86");    // Ulaire Lemenya: a minion, here only as a card in a hand
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
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: The move limit is +1.<br>Each time the fellowship moves during the regroup phase, each opponent may discard 2 cards from hand to draw 2 cards.
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
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFromHandHeMakesTheMoveLimitThreeAndAThirdMoveIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(radagast);
		assertInZone(Zone.FREE_CHARACTERS, radagast);
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		assertEquals(1, scn.GetMoveCount());

		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		// A regroup move: the Shadow player, who has reconciled and holds cards, declines to discard.
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
	public void DiscardedInTheRegroupPhaseAfterTwoMovesHeLeavesTheLimitAtThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(radagast, guard);

		var nomad1 = scn.GetShadowCard("nomad1");
		var nomad2 = scn.GetShadowCard("nomad2");
		var despair = scn.GetShadowCard("despair");

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseNo();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetMoveCount());
		// Fierce in Despair costs 7 and spots 2 [raider] Men.
		scn.MoveMinionsToTable(nomad1, nomad2);
		scn.MoveCardsToHand(despair);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(despair);
		assertTrue(scn.ShadowHasCardChoiceAvailable(radagast, guard));
		scn.ShadowChooseCard(radagast);
		assertInDiscard(radagast);

		// The +1 is the turn's (CR 4.2 "move limit"): with two moves made a third is still on offer.
		assertEquals(3, scn.GetMoveLimit());
		scn.FreepsPass();
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void PlacedInTheDeadPileInTheFellowshipPhaseHeLeavesTheLimitAtThreeForTheRestOfTheTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saved = scn.GetFreepsCard("saved");
		scn.MoveCompanionsToTable(radagast, gandalf);
		scn.MoveCardsToHand(saved);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPlayCard(saved);
		assertTrue(scn.FreepsHasCardChoiceAvailable(radagast, gandalf));
		scn.FreepsChooseCard(radagast);
		// The search of the deck: shown first, and nothing taken.
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, radagast);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(2, scn.GetMoveCount());

		// Two phases on, in the Shadow phase after the second move, the limit is still 3.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetMoveLimit());
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		// It ends with the turn: the other player's turn has the usual 2.
		scn.FreepsChooseToStay();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void EachOfHisOwnersTurnsHasTheBonusAndTheOpponentsTurnDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, radagast);
		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void ASecondCopyPlayedInTheTurnTheFirstLeftMakesTheMoveLimitFour() throws DecisionResultInvalidException, CardNotFoundException {
		// CR 4.2 "modifier": "If you play a second copy of Radagast, the Brown in the same turn, the move limit is
		// an additional +1."
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var radagast2 = scn.GetFreepsCard("radagast2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saved = scn.GetFreepsCard("saved");
		var sentback = scn.GetFreepsCard("sentback");
		scn.MoveCompanionsToTable(radagast, gandalf);
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToHand(radagast2, saved);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(radagast);
		// The deck is shown, and holds no [gandalf] card to take.
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DEAD, radagast);
		assertEquals(3, scn.GetMoveLimit());

		// Sent Back plays the second, though the first is in the dead pile. Never two in play at once.
		scn.FreepsUseCardAction(sentback);
		assertInZone(Zone.FREE_CHARACTERS, radagast2);
		assertEquals(4, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtEachRegroupMoveTheShadowPlayerMayDiscardTwoCardsFromHandToDrawTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);
		// Two cards in Radagast's owner's hand as well: he is no opponent of his own.
		scn.MoveCardsToHand(scn.GetFreepsCard("chaff1"), scn.GetFreepsCard("chaff2"));

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		var lemenya = scn.GetShadowCard("lemenya");
		var despair = scn.GetShadowCard("despair");
		scn.MoveCardsToHand(chaff1, despair, lemenya);

		scn.StartGame();

		// The move of the fellowship phase: nobody is asked, though he holds 3 cards.
		scn.FreepsPass();
		assertEquals(1, scn.GetMoveCount());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards"));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The first move of the regroup phase. A hand of three kinds of card: an event of each side and a minion.
		scn.SkipToMovementDecision();
		scn.SetShadowHand(chaff1, despair, lemenya);
		scn.MoveCardsToTopOfDeck(chaff2, chaff3);
		int deck = scn.GetShadowDeckCount();
		int discard = scn.GetShadowDiscardCount();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChooseYes();

		assertTrue(scn.ShadowHasCardChoiceAvailable(chaff1, despair, lemenya));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(despair, lemenya);

		assertInDiscard(despair, lemenya);
		assertEquals(discard + 2, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, chaff1, chaff2, chaff3);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// And again at the second move of the regroup phase.
		scn.SkipToMovementDecision();
		int hand = scn.GetShadowHandCount();
		deck = scn.GetShadowDeckCount();
		discard = scn.GetShadowDiscardCount();
		scn.FreepsChooseToMove();
		// The third move is under way: the marker moves once "the fellowship moves" is answered.
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards"));
		scn.ShadowChooseYes();
		scn.ShadowChooseCards(chaff2, chaff3);

		assertInDiscard(chaff2, chaff3);
		assertEquals(discard + 2, scn.GetShadowDiscardCount());
		assertEquals(hand, scn.GetShadowHandCount());
		assertEquals(deck - 2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheShadowPlayersHandAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetShadowHand(chaff1, chaff2, chaff3);

		int deck = scn.GetShadowDeckCount();
		int discard = scn.GetShadowDiscardCount();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowDecisionAvailable("discard 2 cards"));
		scn.ShadowChooseNo();

		assertInZone(Zone.HAND, chaff1, chaff2, chaff3);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(discard, scn.GetShadowDiscardCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneCardInHandTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		var chaff1 = scn.GetShadowCard("chaff1");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.SetShadowHand(chaff1);

		int deck = scn.GetShadowDeckCount();
		scn.FreepsChooseToMove();

		assertEquals(2, scn.GetMoveCount());
		assertFalse(scn.ShadowDecisionAvailable("discard 2 cards"));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
