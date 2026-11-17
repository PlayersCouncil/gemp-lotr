package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_251_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stern1", "7_251");
					put("stern2", "7_251");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; Valiant is its whole text
					// Merry, Master Holbytla: [rohan] Hobbit companion; a Man only beside 3 [rohan] companions or Eowyn
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Guma, Plains Farmer: [rohan] Man ally, home 4T (no such site on this path); a fellowship ability used
					// only on purpose
					put("guma", "4_277");

					// Cards for a hand: a companion, a possession, an event and a condition.
					put("knight", "5_35");      // Gondorian Knight: [gondor] companion
					put("sword", "1_299");      // Hobbit Sword: [shire] possession
					put("sorry", "1_312");      // Sorry About Everything: [shire] Fellowship event
					put("talent", "1_316");     // A Talent for Not Being Seen: [shire] condition

					// The Shadow player's cards: for his hand, and for his discard pile.
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] minion
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession
					put("master", "1_276");     // Seeking Its Master: [sauron] condition
					put("runner", "1_178");     // Goblin Runner: [moria] minion
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a Free Peoples ally, in the Shadow player's own discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SternPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stern People
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 [rohan] Men.
		 * 	<b>Regroup:</b> Discard cards from hand equal to the number of cards in an opponent's hand to make the move limit +1 for this turn. Each Shadow player may take up to 4 cards into hand from his or her discard pile. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stern1");

		assertEquals("Stern People", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoRohanMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stern = scn.GetFreepsCard("stern1");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard, merry, boromir);
		scn.MoveCardsToHand(stern);

		scn.StartGame();

		// One [rohan] Man; Merry is a [rohan] Hobbit (two [rohan] companions make him no Man) and Boromir a
		// [gondor] Man.
		assertInHand(stern);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stern));

		// A [rohan] Man ally makes two.
		scn.MoveAlliesToTable(guma);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, guma);
		assertTrue(scn.FreepsPlayAvailable(stern));

		scn.FreepsPlayCard(stern);
		assertInZone(Zone.SUPPORT, stern);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsAsManyCardsAsTheShadowHandHoldsToRaiseTheMoveLimitForTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stern1 = scn.GetFreepsCard("stern1");
		var stern2 = scn.GetFreepsCard("stern2");
		var knight = scn.GetFreepsCard("knight");
		var sword = scn.GetFreepsCard("sword");
		var sorry = scn.GetFreepsCard("sorry");
		var talent = scn.GetFreepsCard("talent");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var master = scn.GetShadowCard("master");
		var runner = scn.GetShadowCard("runner");
		var aldor = scn.GetShadowCard("aldor");
		scn.MoveCardsToSupportArea(stern1, stern2);
		scn.MoveCardsToHand(knight, sword, sorry, talent);
		scn.MoveCardsToHand(troop1, troop2);
		scn.MoveCardsToDiscard(savage, scimitar, master, runner, aldor);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stern1, stern2);
		assertInHand(knight, sword, sorry, talent, troop1, troop2);
		assertInDiscard(savage, scimitar, master, runner, aldor);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsActionAvailable(stern1));
		scn.FreepsUseCardAction(stern1);

		// Exactly 2, the Shadow hand's count (not his own 4), of any kind of card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, sword, sorry, talent));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(knight, sword);
		assertInDiscard(knight, sword);
		assertInHand(sorry, talent);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetMoveLimit());

		// The Shadow player takes up to 4 cards of any kind from his own discard pile; the Free Peoples player's
		// discards are not his to take.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, scimitar, master, runner, aldor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(knight, sword));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(savage, scimitar, master, aldor);
		assertInHand(savage, scimitar, master, aldor);
		assertInDiscard(runner);
		assertEquals(6, scn.GetShadowHandCount());

		// The condition used is discarded; the other copy stays.
		assertInDiscard(stern1);
		assertInZone(Zone.SUPPORT, stern2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The second move: in the Shadow phase after it the limit is still 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		// In the next regroup phase a third move is offered, and made.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());

		// The next turn, the other player's, has the limit of 2.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithFewerCardsInHandThanTheShadowHandAndTheShadowPlayerMayTakeNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stern = scn.GetFreepsCard("stern1");
		var knight = scn.GetFreepsCard("knight");
		var sword = scn.GetFreepsCard("sword");
		var sorry = scn.GetFreepsCard("sorry");
		var talent = scn.GetFreepsCard("talent");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(stern);
		scn.MoveCardsToHand(knight, sword);
		scn.MoveCardsToHand(troop1, troop2, troop3);
		scn.MoveCardsToDiscard(savage, runner);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stern);
		assertInHand(knight, sword, troop1, troop2, troop3);
		assertInDiscard(savage, runner);
		scn.SkipToPhase(Phase.REGROUP);

		// 2 cards in hand against 3 in the Shadow hand: the cost cannot be paid.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.FreepsActionAvailable(stern));

		// Two more cards in hand: offered, and exactly 3 of the 4 are discarded.
		scn.MoveCardsToHand(sorry, talent);
		scn.FreepsRefreshDecision();
		assertInHand(sorry, talent);
		assertTrue(scn.FreepsActionAvailable(stern));
		scn.FreepsUseCardAction(stern);
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(knight, sword, sorry);
		assertInDiscard(knight, sword, sorry);
		assertInHand(talent);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetMoveLimit());

		// The Shadow player takes nothing: his pile and his hand stay as they were.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner));
		scn.ShadowDeclineChoosing();
		assertInDiscard(savage, runner);
		assertEquals(3, scn.GetShadowHandCount());
		assertInDiscard(stern);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
