package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_137_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theoden", "13_137");
					put("eowyn", "5_122");      // Éowyn, Daughter of Éomund: valiant; a maneuver ability never used here
					put("eomer", "4_267");      // Éomer, Third Marshal of Riddermark: cost -1 beside a [rohan] Man; stronger only in a skirmish
					put("theodred", "13_138");  // Théodred, Second Marshal of the Mark: cost -1 beside a [rohan] Man of resistance 7; vitality 3
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man of another title; his ability needs a mount
					put("spear", "4_268");      // Éomer's Spear: hand weapon a [rohan] Man may bear; its text is about Éomer mounted
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: there for Saved From the Fire to spot
					put("saved", "8_20");       // Saved From the Fire: Fellowship event, spot Gandalf and place a companion (except the Ring-bearer) in the dead pile to take up to 3 cards of his culture from the draw deck
					put("slayer", "3_93");      // Morgul Slayer: Regroup: exert this minion to wound a companion (except the Ring-bearer)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Théoden, The Renowned
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot Éowyn, Théoden is <b>defender +1</b>.<br>While you can spot Éomer, Théoden is <b>damage +1</b>.<br>While you can spot Théodred, the move limit is +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("theoden");

		assertEquals("Théoden", card.getBlueprint().getTitle());
		assertEquals("The Renowned", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void PlayedFromHandBesideEowynEomerAndTheodredHeIsDefenderPlusOneDamagePlusOneAndTheMoveLimitIsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var eomer = scn.GetFreepsCard("eomer");
		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCardsToHand(theoden);
		scn.MoveCompanionsToTable(eowyn, eomer, theodred);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsPlayAvailable(theoden));

		scn.FreepsPlayCard(theoden);

		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheodenIsDefenderPlus1IfYouCanSpotEowyn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCardsToHand(eowyn);
		scn.MoveCompanionsToTable(theoden, rider);
		// A card with Éomer in its title, which is not Éomer
		scn.AttachCardsTo(theoden, spear);

		scn.StartGame();

		// The Rider is a [rohan] Man who is neither Éowyn, Éomer nor Théodred.
		assertFalse(scn.HasKeyword(theoden, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DEFENDER));

		scn.FreepsPlayCard(eowyn);

		assertTrue(scn.HasKeyword(theoden, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
		// Théoden alone, and for Éowyn only that.
		assertFalse(scn.HasKeyword(rider, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheodenIsDamagePlus1IfYouCanSpotEomer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCardsToHand(eomer);
		scn.MoveCompanionsToTable(theoden, rider);

		scn.StartGame();

		assertFalse(scn.HasKeyword(theoden, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));

		scn.FreepsPlayCard(eomer);

		assertTrue(scn.HasKeyword(theoden, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		// Théoden alone, and for Éomer only that.
		assertFalse(scn.HasKeyword(rider, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheodenAdds1ToMoveLimitIfYouCanSpotTheodred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCardsToHand(theodred);
		scn.MoveCompanionsToTable(theoden, rider);

		scn.StartGame();

		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPlayCard(theodred);

		assertEquals(3, scn.GetMoveLimit());
		// For Théodred only that.
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(theoden, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Given in the fellowship phase, it is the turn's and not that phase's: read again after the move.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheodredDiesInTheRegroupPhaseTheMoveLimitIsStillThreeAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(theoden, theodred);

		scn.StartGame();
		// Vitality 3: one wound more kills him.
		scn.AddWoundsToChar(theodred, 2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(slayer);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(theodred);
		assertInZone(Zone.DEAD, theodred);
		// Théodred is gone, and the limit is still 3: a change to the move limit holds for the whole turn.
		assertEquals(3, scn.GetMoveLimit());

		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void TheBonusHoldsThroughASecondMoveAfterTheodredHasGoneAndEndsWithTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saved = scn.GetFreepsCard("saved");
		scn.MoveCompanionsToTable(theoden, theodred, gandalf);
		scn.MoveCardsToHand(saved);

		scn.StartGame();

		assertEquals(3, scn.GetMoveLimit());

		// Saved From the Fire: Théodred is placed in the dead pile in the fellowship phase, the first of the turn.
		scn.FreepsPlayCard(saved);
		scn.FreepsChooseCard(theodred);
		// The search of the deck for [rohan] cards: shown first, and nothing taken.
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DEAD, theodred);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second move
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetMoveCount());
		scn.FreepsChooseToMove();

		// The Shadow phase after it: the bonus is still there.
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// So after two moves the fellowship is asked about a third.
		scn.SkipToMovementDecision();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();

		// The other player's turn: 2 again.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());

		// And on Théoden's next turn, with no Théodred, it is 2.
		scn.SkipToOtherPlayersTurn();
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusIsGivenAgainOnHisNextTurnAndNotOnTheOtherPlayersTurnBetween() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCompanionsToTable(theoden, theodred);

		scn.StartGame();
		assertEquals(3, scn.GetMoveLimit());

		// Player 2's turn: the two are not his, and the bonus of the turn before has ended with it.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// Théoden's next turn: Théodred is still there, and the limit is 3 again and no more.
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheodenMoveLimitIsNotAvailableToShadowPlayerIfPlayedInStartingFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Player 2's copies of the cards
		var theoden = scn.GetShadowCard("theoden");
		var theodred = scn.GetShadowCard("theodred");

		// The starting fellowships are what is tested here, so they are played and not skipped: player 1 takes
		// nobody, and player 2 takes Théoden and Théodred.
		scn.FreepsDeclineChoosing();
		scn.ShadowChooseCardBPFromSelection(theoden);
		scn.ShadowChooseCardBPFromSelection(theodred);
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertInZone(Zone.FREE_CHARACTERS, theodred);

		scn.SkipMulligans();

		// Player 1's first turn: the +1 that player 2's pair was given in set-up is no part of it.
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheodenMoveLimitIsNotAvailableToShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Player 2's copies of the cards
		var eviltheoden = scn.GetShadowCard("theoden");
		var eviltheodred = scn.GetShadowCard("theodred");

		// Player 1 takes nobody into his starting fellowship, and player 2 takes Théoden and Théodred.
		scn.FreepsDeclineChoosing();
		scn.ShadowChooseCardBPFromSelection(eviltheoden);
		scn.ShadowChooseCardBPFromSelection(eviltheodred);

		scn.SkipMulligans();
		// A turn of each player: player 2's, with his pair, has been and gone.
		scn.SkipToSite(2);

		assertInZone(Zone.FREE_CHARACTERS, eviltheoden);
		assertInZone(Zone.FREE_CHARACTERS, eviltheodred);
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
