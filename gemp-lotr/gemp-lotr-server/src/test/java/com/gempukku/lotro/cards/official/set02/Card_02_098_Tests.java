package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("friends", "2_98");

					// Frodo, Son of Drogo 1_290 is the Ring-bearer: [shire] Hobbit, Frodo signet.
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, Frodo signet, vitality 3; his archery ability is never reached
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, Gandalf signet, vitality 4; his maneuver ability is never reached
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion, Gandalf signet; his skirmish ability is never reached
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a [shire] Hobbit ally, vitality 3; his ability is used only on purpose

					// For the discard pile: Free Peoples cards of three types and cultures, and a Shadow card.
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] companion card
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("strike", "1_3");       // Axe Strike: a [dwarven] event
					put("runner", "1_178");     // Goblin Runner: a [moria] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DearFriendsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Dear Friends
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert a companion to place a Free Peoples card from your discard pile beneath your draw deck (or 2 Free Peoples cards if that companion has the Frodo signet).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("friends");

		assertEquals("Dear Friends", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsACompanionToPlaceAFreePeoplesCardFromTheDiscardPileBeneathTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var maggot = scn.GetFreepsCard("maggot");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var strike = scn.GetFreepsCard("strike");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToDiscard(merry, sword, strike, runner);
		scn.MoveCardsToHand(friends);

		assertInDiscard(merry, sword, strike, runner);
		scn.StartGame();

		assertNotSame(sword, scn.GetFreepsBottomOfDeck());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(friends));

		scn.FreepsPlayCard(friends);
		// Companions of three cultures and races may pay; the ally may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(maggot));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(maggot));

		// Aragorn has the Gandalf signet: one card.  A companion, a possession and an event are offered; the minion is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, sword, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sword);

		assertSame(sword, scn.GetFreepsBottomOfDeck());
		assertInDiscard(merry, strike, runner, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileEveryCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToDiscard(sword);
		scn.MoveCardsToHand(friends);

		scn.StartGame();
		// Vitality 4 with 3 wounds each: neither companion can exert.  The ally beside them could.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(maggot));
		assertInDiscard(sword);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(friends));
	}

	@Test
	public void ACompanionWithTheFrodoSignetPlacesTwoFreePeoplesCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var legolas = scn.GetFreepsCard("legolas");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var strike = scn.GetFreepsCard("strike");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToDiscard(merry, sword, strike, runner);
		scn.MoveCardsToHand(friends);

		assertInDiscard(merry, sword, strike, runner);
		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsPlayCard(friends);
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));

		// Legolas, an [elven] Elf, has the Frodo signet: exactly two of the three.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, sword, strike));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sword, strike);

		// Then which goes beneath first: the one placed last is the bottom card.
		scn.FreepsChooseCardBPFromSelection(sword);
		assertSame(strike, scn.GetFreepsBottomOfDeck());
		assertSame(sword, scn.GetFromBottomOfFreepsDeck(2));
		assertInDiscard(merry, runner, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRingBearerFrodoHasTheFrodoSignetAndPlacesTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(merry, sword, strike);
		scn.MoveCardsToHand(friends);

		assertInDiscard(merry, sword, strike);
		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPlayCard(friends);
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(merry, strike);
		scn.FreepsChooseCardBPFromSelection(strike);
		assertSame(merry, scn.GetFreepsBottomOfDeck());
		assertSame(strike, scn.GetFromBottomOfFreepsDeck(2));
		assertInDiscard(sword, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWithAnotherSignetPlacesOneThoughAFrodoSignetCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToDiscard(merry, sword, strike);
		scn.MoveCardsToHand(friends);

		assertInDiscard(merry, sword, strike);
		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsPlayCard(friends);
		// Gimli has the Gandalf signet; Legolas, who has the Frodo signet, is in play but not the one exerted.
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(merry);
		assertSame(merry, scn.GetFreepsBottomOfDeck());
		assertInDiscard(sword, strike, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFrodoSignetCompanionWithOneFreePeoplesCardInTheDiscardPilePlacesThatOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var legolas = scn.GetFreepsCard("legolas");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToDiscard(sword, runner);
		scn.MoveCardsToHand(friends);

		assertInDiscard(sword, runner);
		scn.StartGame();

		scn.FreepsPlayCard(friends);
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertSame(sword, scn.GetFreepsBottomOfDeck());
		assertInDiscard(runner, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesCardInTheDiscardPileItIsPlayedAndTheCompanionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var friends = scn.GetFreepsCard("friends");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDiscard(runner);
		scn.MoveCardsToHand(friends);

		assertInDiscard(runner);
		scn.StartGame();

		var bottom = scn.GetFreepsBottomOfDeck();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(friends));

		scn.FreepsPlayCard(friends);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertSame(bottom, scn.GetFreepsBottomOfDeck());
		assertInDiscard(runner, friends);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
