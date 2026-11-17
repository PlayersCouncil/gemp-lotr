package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_018_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("advice", "33_18");
					// Elrond, Elven Lord: [elven] Elf ally, Wise, 8/4; a start-of-turn heal (nothing is wounded) and a
					// regroup ability, not used
					put("elrond", "30_21");
					// Elrond, Lord of Rivendell: a Decipher Elrond who is an ally; a start-of-turn heal of allies whose
					// home is site 3 (none wounded) and a fellowship ability, not used
					put("elrond1", "1_40");
					// Elrond, Elven Lord: the Elrond who is a companion; a start-of-turn exert-to-heal, not reached
					put("elrondcomp", "7_21");
					// Galadriel, Elven Lady: [elven] Elf ally, Wise, no Elrond; a start-of-turn heal of Gandalf and a
					// skirmish ability, not used
					put("galadriel", "32_13");

					// For the hand: a companion, a possession, an event and a Shadow card
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, hand weapon
					put("lore", "30_13");       // Lore of Imladris: [dwarven] maneuver event
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc minion

					// For the draw deck: plain cards to draw
					put("deck1", "31_1");
					put("deck2", "31_1");
					put("deck3", "30_13");
					put("deck4", "30_13");
					put("deck5", "30_39");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void WiseAdviceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Wise Advice
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Elrond to draw 4 cards. Then place 2 cards from hand beneath your draw deck in any order.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("advice");

		assertEquals("Wise Advice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WiseAdviceSpotsElrondToDrawFourCardsThenPlacesTwoFromHandBeneathTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advice = scn.GetFreepsCard("advice");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var lore = scn.GetFreepsCard("lore");
		var aggressor = scn.GetFreepsCard("aggressor");
		var deck1 = scn.GetFreepsCard("deck1");
		var deck2 = scn.GetFreepsCard("deck2");
		var deck3 = scn.GetFreepsCard("deck3");
		var deck4 = scn.GetFreepsCard("deck4");
		var deck5 = scn.GetFreepsCard("deck5");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SetFreepsHand(advice, balin, axe, lore, aggressor);
		scn.SetFreepsDrawDeck(deck1, deck2, deck3, deck4, deck5);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, deck1, deck2, deck3, deck4, deck5);

		assertTrue(scn.FreepsPlayAvailable(advice));
		scn.FreepsPlayCard(advice);

		// The top four are drawn: 4 in hand beside the event's own 5, less the event.
		assertInHand(deck1, deck2, deck3, deck4);
		assertInZone(Zone.DECK, deck5);
		// The event is still being played while its second sentence asks.
		assertInZone(Zone.VOID, advice);

		// Two cards from hand, chosen together: every card in it is offered, a companion, a possession, an event,
		// a Shadow card and the cards just drawn.
		assertEquals(8, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, axe, lore, aggressor, deck1, deck2, deck3, deck4));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(balin, aggressor);

		// Then the order: the first chosen goes beneath first, and the other, unasked, is the bottom card.
		assertTrue(scn.FreepsDecisionAvailable("Choose card to put beneath draw deck"));
		scn.FreepsChooseCardBPFromSelection(aggressor);
		assertEquals(6, scn.GetFreepsHandCount());
		assertInHand(axe, lore, deck1, deck2, deck3, deck4);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertSame(balin, scn.GetFreepsBottomOfDeck());
		assertSame(aggressor, scn.GetFromBottomOfFreepsDeck(2));
		assertSame(deck5, scn.GetFromBottomOfFreepsDeck(3));
		assertInDiscard(advice);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WiseAdviceSpotsADecipherElrondWhoIsAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advice = scn.GetFreepsCard("advice");
		var elrond1 = scn.GetFreepsCard("elrond1");
		var galadriel = scn.GetFreepsCard("galadriel");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var deck1 = scn.GetFreepsCard("deck1");
		var deck2 = scn.GetFreepsCard("deck2");
		var deck3 = scn.GetFreepsCard("deck3");
		var deck4 = scn.GetFreepsCard("deck4");
		var deck5 = scn.GetFreepsCard("deck5");
		scn.MoveAlliesToTable(elrond1, galadriel);

		scn.StartGame();
		scn.SetFreepsHand(advice);
		scn.SetFreepsDrawDeck(deck1, deck2, deck3, deck4, deck5);
		assertInHand(advice);
		assertInZone(Zone.DECK, deck1, deck2, deck3, deck4, deck5);

		assertTrue(scn.FreepsPlayAvailable(advice));
		scn.FreepsPlayCard(advice);
		assertInHand(deck1, deck2, deck3, deck4);
		scn.FreepsChooseCards(deck1, deck3);
		scn.FreepsChooseCardBPFromSelection(deck1);
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(deck3, scn.GetFreepsBottomOfDeck());
		assertInDiscard(advice);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WiseAdviceSpotsTheElrondWhoIsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advice = scn.GetFreepsCard("advice");
		var elrondcomp = scn.GetFreepsCard("elrondcomp");
		var deck1 = scn.GetFreepsCard("deck1");
		var deck2 = scn.GetFreepsCard("deck2");
		var deck3 = scn.GetFreepsCard("deck3");
		var deck4 = scn.GetFreepsCard("deck4");
		var deck5 = scn.GetFreepsCard("deck5");

		scn.StartGame();
		// Laid after the start of the turn, so that his own start-of-turn offer is not made.
		scn.MoveCompanionsToTable(elrondcomp);
		scn.SetFreepsHand(advice);
		scn.SetFreepsDrawDeck(deck1, deck2, deck3, deck4, deck5);
		assertInHand(advice);
		assertInZone(Zone.DECK, deck1, deck2, deck3, deck4, deck5);

		assertTrue(scn.FreepsPlayAvailable(advice));
		scn.FreepsPlayCard(advice);
		assertInHand(deck1, deck2, deck3, deck4);
		scn.FreepsChooseCards(deck1, deck3);
		scn.FreepsChooseCardBPFromSelection(deck1);
		assertEquals(2, scn.GetFreepsHandCount());
		assertSame(deck3, scn.GetFreepsBottomOfDeck());
		assertInDiscard(advice);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WiseAdviceCannotBePlayedWithNoElrond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var advice = scn.GetFreepsCard("advice");
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);

		scn.StartGame();
		scn.SetFreepsHand(advice);
		assertInHand(advice);

		// Galadriel, another Wise [elven] Elf ally, is no Elrond.
		assertFalse(scn.FreepsPlayAvailable(advice));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
