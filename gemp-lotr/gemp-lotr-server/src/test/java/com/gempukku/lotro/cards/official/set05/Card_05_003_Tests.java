package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blaze", "5_3");

					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text
					// Wild Man of Dunland: a [dunland] Man; his text answers a skirmish he wins, and no test fights one
					put("wildman", "4_38");
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  Laid after the move,
					// so his "each time the fellowship moves" asks nothing.
					put("saruman", "4_33");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					// Constantly Threatening: a [dunland] condition in the support area; its Shadow ability
					// needs the fellowship to have moved twice this turn, and is never offered here.
					put("threatening", "4_6");

					// Cards to hold in hand or lay in a draw deck.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion
					put("armor", "1_8");        // Dwarven Armor: a possession
					put("stout", "1_315");      // Stout and Sturdy: a Free Peoples event
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeapingBlazeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Leaping Blaze
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot 3 [dunland] minions to shuffle your hand into your draw deck and draw 8 cards. The Free Peoples player may discard 3 cards from hand to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blaze");

		assertEquals("Leaping Blaze", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeDunlandMinionsShufflesTheShadowHandIntoTheDeckAndDrawsEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blaze = scn.GetShadowCard("blaze");
		var brigand = scn.GetShadowCard("brigand");
		var wildman = scn.GetShadowCard("wildman");
		var saruman = scn.GetShadowCard("saruman");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");
		// Two cards in the Free Peoples hand: one short of the three he would have to discard, so he is asked nothing.
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCardsToHand(aragorn, armor);

		scn.StartGame();
		assertInHand(aragorn, armor);
		scn.SetTwilight(1);
		scn.FreepsPass();
		// Three [dunland] minions of three titles, one of them no Man.
		scn.MoveMinionsToTable(brigand, wildman, saruman);
		scn.SetShadowHand(blaze, filler1, filler2);
		scn.SetShadowDrawDeck(scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"), scn.GetShadowCard("stout"),
				scn.GetShadowCard("legolas"), scn.GetShadowCard("troop"), scn.GetShadowCard("threatening"),
				scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"));
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(blaze));

		scn.ShadowPlayCard(blaze);
		assertFalse(scn.FreepsDecisionAvailable("discard 3 cards"));
		assertInDiscard(blaze);
		// The two other cards went into the deck of 8, which was shuffled, and 8 of the 10 were drawn.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
		// "Your" hand and deck: the Free Peoples player's are untouched.
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTwoDunlandMinionsBesideAMinionOfAnotherCultureAndADunlandCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blaze = scn.GetShadowCard("blaze");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCardsToHand(blaze);
		scn.MoveCardsToSupportArea(threatening);

		scn.StartGame();
		assertInHand(blaze);
		assertInZone(Zone.SUPPORT, threatening);
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, saruman, troop);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowPlayAvailable(blaze));
		assertInHand(blaze);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardThreeCardsOfHisChoiceToPreventTheShuffleAndTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blaze = scn.GetShadowCard("blaze");
		var brigand = scn.GetShadowCard("brigand");
		var wildman = scn.GetShadowCard("wildman");
		var saruman = scn.GetShadowCard("saruman");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");
		// A companion, a possession, an event and a Shadow card of his own: any card in his hand will do.
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(aragorn, armor, stout, troop);

		scn.StartGame();
		assertInHand(aragorn, armor, stout, troop);
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, wildman, saruman);
		scn.SetShadowHand(blaze, filler1, filler2);
		scn.SetShadowDrawDeck(scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"), scn.GetShadowCard("stout"),
				scn.GetShadowCard("legolas"), scn.GetShadowCard("troop"), scn.GetShadowCard("threatening"),
				scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"));
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(blaze);
		assertTrue(scn.FreepsDecisionAvailable("discard 3 cards"));

		scn.FreepsChooseYes();
		// Exactly three, of the four he holds.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, stout, troop));
		scn.FreepsChooseCards(aragorn, stout, troop);

		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(armor);
		assertInDiscard(aragorn, stout, troop);
		// Nothing shuffled and nothing drawn; the event is spent all the same.
		assertInDiscard(blaze);
		assertEquals(2, scn.GetShadowHandCount());
		assertInHand(filler1, filler2);
		assertEquals(8, scn.GetShadowDeckCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDeclineAndTheShadowPlayerShufflesAndDraws() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blaze = scn.GetShadowCard("blaze");
		var brigand = scn.GetShadowCard("brigand");
		var wildman = scn.GetShadowCard("wildman");
		var saruman = scn.GetShadowCard("saruman");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(aragorn, armor, stout, troop);

		scn.StartGame();
		assertInHand(aragorn, armor, stout, troop);
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, wildman, saruman);
		scn.SetShadowHand(blaze, filler1, filler2);
		scn.SetShadowDrawDeck(scn.GetShadowCard("aragorn"), scn.GetShadowCard("armor"), scn.GetShadowCard("stout"),
				scn.GetShadowCard("legolas"), scn.GetShadowCard("troop"), scn.GetShadowCard("threatening"),
				scn.GetShadowCard("filler3"), scn.GetShadowCard("filler4"));
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(8, scn.GetShadowDeckCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(blaze);
		assertTrue(scn.FreepsDecisionAvailable("discard 3 cards"));

		scn.FreepsChooseNo();
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor, stout, troop);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNothingToShuffleInAndNothingToDrawThePreventionIsStillAskedAndTheShuffleIsDone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blaze = scn.GetShadowCard("blaze");
		var brigand = scn.GetShadowCard("brigand");
		var wildman = scn.GetShadowCard("wildman");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCardsToHand(aragorn, armor, stout, troop);

		scn.StartGame();
		assertInHand(aragorn, armor, stout, troop);
		scn.SetTwilight(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(brigand, wildman, saruman);
		// The event is his only card in hand and the draw deck is empty, but a shuffle is something done even
		// when nothing is shuffled in (Ketura on Q-0102), so there is still something to prevent.
		scn.SetShadowHand(blaze);
		scn.SetShadowDrawDeck();
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(blaze);
		assertTrue(scn.FreepsDecisionAvailable("discard 3 cards"));

		scn.FreepsChooseNo();
		assertInDiscard(blaze);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(4, scn.GetFreepsHandCount());
		assertInHand(aragorn, armor, stout, troop);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
