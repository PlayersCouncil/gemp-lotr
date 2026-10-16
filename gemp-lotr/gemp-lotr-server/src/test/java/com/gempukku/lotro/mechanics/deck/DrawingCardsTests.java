package com.gempukku.lotro.mechanics.deck;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Drawing cards: each card drawn is the top card of its player's draw deck and is a happening of its own.
 *
 * So a text that answers "each time ... draws a card" is carried out once for one card drawn and once for each
 * of several drawn by a single effect (Comprehensive Rules 4.2, "When, Each Time, and While": "Each time is used
 * if an effect can happen more than once").  And a draw of more cards than the deck holds draws what is there
 * and stops: CR 4.2, "effect": "If the effect of a card or special ability requires you to perform an action and
 * you cannot, you must perform as much as you can and ignore the rest"; "draw deck": "When you draw the last card
 * from your draw deck, you don't lose the game."
 *
 * The text that answers a card drawn is Verily I Come, "Each time the Free Peoples player draws a card (or takes
 * a card into hand) during the fellowship phase, add a burden": burdens have no cap, so they count the cards.
 * The draws are of 1 and of 3, which together stay inside the Rule of 4 (RuleOfFourTests).
 *
 * A draw that is prevented, and one that another player may pay to prevent, are in
 * PreventionIsAskedOnlyWhenItsEffectWillHappenTests.
 */
public class DrawingCardsTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, vitality 3
					put("delving", "1_6");      // Delving.  Fellowship: Exert a Dwarf companion to draw 3 cards.
					put("elrond", "1_40");      // Elrond, Lord of Rivendell.  Fellowship: Exert Elrond to draw a card.
					// Verily I Come: [sauron] condition on the Ring-bearer.  "Each time the Free Peoples player
					// draws a card (or takes a card into hand) during the fellowship phase, add a burden."
					put("verily", "2_94");

					// Five cards to draw, named for where they are laid in the deck.  Axe Strike is a skirmish event.
					put("first", "1_3");
					put("second", "1_3");
					put("third", "1_3");
					put("fourth", "1_3");
					put("fifth", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same table with one card to draw.
	protected VirtualTableScenario GetShortDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, vitality 3
					put("delving", "1_6");      // Delving.  Fellowship: Exert a Dwarf companion to draw 3 cards.
					put("verily", "2_94");      // Verily I Come: as above
					put("last", "1_3");         // Axe Strike: the one card in the draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EachCardDrawnComesFromTheTopOfTheDeckAndIsAnsweredOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var delving = scn.GetFreepsCard("delving");
		var first = scn.GetFreepsCard("first");
		var second = scn.GetFreepsCard("second");
		var third = scn.GetFreepsCard("third");
		var fourth = scn.GetFreepsCard("fourth");
		var fifth = scn.GetFreepsCard("fifth");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(delving);
		// The Free Peoples player's own copy is out of his draw deck; the Shadow player's is on the Ring-bearer
		scn.MoveCardsToDiscard(scn.GetFreepsCard("verily"));
		scn.AttachCardsTo(frodo, scn.GetShadowCard("verily"));

		scn.StartGame();
		// From the top: first, second, third, fourth, fifth
		scn.MoveCardsToTopOfDeck(fifth, fourth, third, second, first);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(first, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		// A draw of one card
		scn.FreepsUseCardAction(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInZone(Zone.HAND, first);
		assertInZone(Zone.DECK, second, third, fourth, fifth);
		assertEquals(second, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetBurdens());

		// A draw of three by one effect
		scn.FreepsPlayCard(delving);

		// The three that lay on top, and the fifth is still in the deck
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, delving);
		assertInZone(Zone.HAND, first, second, third, fourth);
		assertInZone(Zone.DECK, fifth);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsHandCount());
		// One burden for each of the three cards, and not one for the draw: 1 + 3
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADrawOfMoreCardsThanTheDeckHoldsDrawsWhatIsThereAndIsAnsweredForThoseCardsOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShortDeckScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var delving = scn.GetFreepsCard("delving");
		var last = scn.GetFreepsCard("last");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(delving);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("verily"));
		scn.AttachCardsTo(frodo, scn.GetShadowCard("verily"));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInZone(Zone.DECK, last);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		// Three cards asked for, and one to be had
		scn.FreepsPlayCard(delving);

		// The cost was paid and the one card drawn.  Verily I Come answered that card and nothing else: one
		// burden, where a burden for each card asked for would be three.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.DISCARD, delving);
		assertInZone(Zone.HAND, last);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetBurdens());
		// The last card of the draw deck is drawn and the game goes on
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
