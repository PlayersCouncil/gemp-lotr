package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * The Rule of 4.
 *
 * Comprehensive Rules 4.2, "Rule of 4": "You cannot draw (or take into hand) more than 4 cards during your
 * fellowship phase.  This applies to cards taken into hand by any means."
 *
 * The rule is one a format can switch off (ruleOfFour in lotrFormats.hjson): every format has it but 2nd_ed.
 */
public class RuleOfFourTests
{
	protected HashMap<String, String> Cards() {
		return new HashMap<>()
		{{
			put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
			put("delving1", "1_6");     // Delving.  Fellowship: Exert a Dwarf companion to draw 3 cards.
			put("delving2", "1_6");
			put("elrond", "1_40");      // Elrond, Lord of Rivendell.  Fellowship: Exert Elrond to draw a card.
			// Barliman Butterbur.  Fellowship: Exert Barliman Butterbur to take a [gandalf] event into hand from
			// your discard pile.
			put("barliman", "1_70");
			put("sleep", "1_84");       // Sleep, Caradhras: a [gandalf] event, never played here

			put("deck1", "1_3");        // ten cards to draw
			put("deck2", "1_3");
			put("deck3", "1_3");
			put("deck4", "1_3");
			put("deck5", "1_3");
			put("deck6", "1_3");
			put("deck7", "1_3");
			put("deck8", "1_3");
			put("deck9", "1_3");
			put("deck10", "1_3");
		}};
	}

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(Cards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetScenarioWithoutTheRule() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(Cards(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"2nd_ed"
		);
	}

	@Test
	public void NoMoreThanFourCardsAreDrawnInAFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var delving1 = scn.GetFreepsCard("delving1");
		var delving2 = scn.GetFreepsCard("delving2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(delving1, delving2);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(delving1);
		// 2 - 1 for the event + 3 drawn
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(9, scn.GetFreepsDeckCount());

		// The second Delving would draw 3 more.  Only the fourth card of the phase is drawn.
		scn.FreepsPlayCard(delving2);
		assertInZone(Zone.DISCARD, delving2);
		// 4 - 1 for the event + 1 drawn
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetFreepsDeckCount());

		// A fifth draw from another card does not happen either.
		scn.FreepsUseCardAction(elrond);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardTakenIntoHandIsOneOfTheFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var barliman = scn.GetFreepsCard("barliman");
		var delving1 = scn.GetFreepsCard("delving1");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond, barliman);
		scn.MoveCardsToHand(delving1);
		scn.MoveCardsToDiscard(sleep);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(delving1);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetFreepsDeckCount());

		// The fourth card of the phase is not drawn but taken from the discard pile.
		assertInZone(Zone.DISCARD, sleep);
		scn.FreepsUseCardAction(barliman);
		assertInZone(Zone.HAND, sleep);
		assertEquals(4, scn.GetFreepsHandCount());

		// So a draw is now a fifth, and does not happen.
		scn.FreepsUseCardAction(elrond);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(8, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AfterFourCardsDrawnACardCannotBeTakenIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var delving1 = scn.GetFreepsCard("delving1");
		var delving2 = scn.GetFreepsCard("delving2");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(delving1, delving2);
		scn.MoveCardsToDiscard(sleep);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(11, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(delving1);
		scn.FreepsPlayCard(delving2);
		assertEquals(4, scn.GetFreepsHandCount());
		// 11 less the 4 drawn
		assertEquals(7, scn.GetFreepsDeckCount());
		assertInZone(Zone.DISCARD, sleep);

		scn.FreepsUseCardAction(barliman);
		assertInZone(Zone.DISCARD, sleep);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFormatWithoutTheRuleDrawsEveryCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenarioWithoutTheRule();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		var delving1 = scn.GetFreepsCard("delving1");
		var delving2 = scn.GetFreepsCard("delving2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(delving1, delving2);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());

		scn.FreepsPlayCard(delving1);
		scn.FreepsPlayCard(delving2);
		// 2 - 2 for the events + 6 drawn
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(6, scn.GetFreepsDeckCount());

		scn.FreepsUseCardAction(elrond);
		assertEquals(7, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
