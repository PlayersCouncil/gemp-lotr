package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bird", "17_23");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a Wizard companion, vitality 4
					// Saruman, The White (a Hobbit-set card): a [gandalf] Wizard ALLY, the one printed Wizard who is no
					// companion (Q-0312); he makes Gandalf defender +1, and his abilities are never used.
					put("saruman", "32_24");
					put("dasron", "13_29");     // Dasron, Merchant From Dorwinion: a [gandalf] companion who is no Wizard; his trigger needs culture tokens, and there are none

					// For the top of the draw deck, by their sides alone.
					put("defendit", "4_115");   // Defend It and Hope: a Free Peoples event
					put("avenger", "6_27");     // Ent Avenger: a Free Peoples companion
					put("troop", "1_177");      // Goblin Patrol Troop: a Shadow minion
					put("wreathed", "1_238");   // Wreathed in Shadow: a Shadow condition
					put("sleep", "1_84");       // Sleep, Caradhras: a Free Peoples event
					put("toby", "1_305");       // Old Toby: a Free Peoples possession
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScintillatingBirdStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Scintillating Bird
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Spell</b>.<br><b>Fellowship:</b> Exert your Wizard and choose an opponent to name either Free Peoples or Shadow. Reveal the top three cards of your deck. If you revealed at least two of the chosen card type, take those cards into hand, otherwise, discard those cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bird");

		assertEquals("Scintillating Bird", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowNamedWithTwoShadowCardsRevealedTakesAllThreeIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bird = scn.GetFreepsCard("bird");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var dasron = scn.GetFreepsCard("dasron");
		var defendit = scn.GetFreepsCard("defendit");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetFreepsCard("troop");
		var wreathed = scn.GetFreepsCard("wreathed");
		scn.MoveCompanionsToTable(gandalf, dasron);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(bird);
		assertInZone(Zone.SUPPORT, bird);

		scn.StartGame();
		// The last named is the top card: the Free Peoples event, then two Shadow cards, then a fourth card.
		scn.MoveCardsToTopOfDeck(avenger, troop, wreathed, defendit);
		assertInZone(Zone.DECK, avenger, troop, wreathed, defendit);
		scn.FreepsRefreshDecision();
		assertSame(defendit, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(bird));

		scn.FreepsUseCardAction(bird);
		// Either Wizard of his, the companion or the ally; not a companion who is no Wizard.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dasron));
		scn.FreepsChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// The opponent names the side.
		assertFalse(scn.FreepsDecisionAvailable("Choose Free Peoples cards?"));
		assertTrue(scn.ShadowDecisionAvailable("Choose Free Peoples cards?"));
		scn.ShadowChooseNo();

		// The three are shown to both players, the fourth is not.
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(defendit, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(wreathed, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(avenger, false));
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// Two of the three are Shadow cards: all three revealed are taken into hand.
		assertInHand(defendit, wreathed, troop);
		assertEquals(3, scn.GetFreepsHandCount());
		assertSame(avenger, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreePeoplesNamedWithOneFreePeoplesCardRevealedDiscardsAllThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bird = scn.GetFreepsCard("bird");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var defendit = scn.GetFreepsCard("defendit");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetFreepsCard("troop");
		var wreathed = scn.GetFreepsCard("wreathed");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(bird);
		assertInZone(Zone.SUPPORT, bird);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(avenger, troop, wreathed, defendit);
		assertInZone(Zone.DECK, avenger, troop, wreathed, defendit);
		scn.FreepsRefreshDecision();
		assertSame(defendit, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(saruman));

		scn.FreepsUseCardAction(bird);
		scn.FreepsChooseCard(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		assertFalse(scn.FreepsDecisionAvailable("Choose Free Peoples cards?"));
		assertTrue(scn.ShadowDecisionAvailable("Choose Free Peoples cards?"));
		scn.ShadowChooseYes();
		scn.DismissRevealedCards();

		// One of the three is a Free Peoples card: all three revealed are discarded.
		assertInDiscard(defendit, wreathed, troop);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertSame(avenger, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoOfTheNamedSideTakeTheCardsAndOneDiscardsThemWhicheverSideIsNamed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bird = scn.GetFreepsCard("bird");
		var gandalf = scn.GetFreepsCard("gandalf");
		var saruman = scn.GetFreepsCard("saruman");
		var defendit = scn.GetFreepsCard("defendit");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetFreepsCard("troop");
		var wreathed = scn.GetFreepsCard("wreathed");
		var sleep = scn.GetFreepsCard("sleep");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(bird);
		assertInZone(Zone.SUPPORT, bird);

		scn.StartGame();
		// The last named is the top card: two Free Peoples cards and a Shadow card, then a Shadow card and two
		// Free Peoples cards.
		scn.MoveCardsToTopOfDeck(toby, sleep, wreathed, troop, avenger, defendit);
		assertInZone(Zone.DECK, toby, sleep, wreathed, troop, avenger, defendit);
		scn.FreepsRefreshDecision();
		assertSame(defendit, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// Free Peoples named, two of the three are Free Peoples cards: all three are taken.
		scn.FreepsUseCardAction(bird);
		scn.FreepsChooseCard(gandalf);
		assertTrue(scn.ShadowDecisionAvailable("Choose Free Peoples cards?"));
		scn.ShadowChooseYes();
		scn.DismissRevealedCards();
		assertInHand(defendit, avenger, troop);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertSame(wreathed, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Shadow named, one of the three is a Shadow card: all three are discarded.
		scn.FreepsUseCardAction(bird);
		scn.FreepsChooseCard(saruman);
		assertTrue(scn.ShadowDecisionAvailable("Choose Free Peoples cards?"));
		scn.ShadowChooseNo();
		scn.DismissRevealedCards();
		assertInDiscard(wreathed, sleep, toby);
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithItsOnlyWizardExhaustedTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bird = scn.GetFreepsCard("bird");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dasron = scn.GetFreepsCard("dasron");
		scn.MoveCompanionsToTable(gandalf, dasron);
		scn.MoveCardsToSupportArea(bird);
		// Vitality 4: three wounds exhaust him.  Dasron, who is no Wizard, could exert.
		scn.AddWoundsToChar(gandalf, 3);
		assertInZone(Zone.SUPPORT, bird);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(dasron));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bird));
	}
}
