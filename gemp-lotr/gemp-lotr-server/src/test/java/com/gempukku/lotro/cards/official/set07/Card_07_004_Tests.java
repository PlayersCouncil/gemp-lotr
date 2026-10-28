package com.gempukku.lotro.cards.official.set07;

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

public class Card_07_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("risk", "7_4");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion, here only stacked
					put("lord", "1_21");        // Lord of Moria: a [dwarven] condition, which is no Dwarf; each Dwarf is damage +1
					put("rock", "4_50");        // Here Is Good Rock: [dwarven] condition that play stacks Free Peoples cards on; a skirmish ability only

					// Filler for the draw deck.
					put("troop1", "1_177");     // Goblin Patrol Troop: no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CalculatedRiskStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Calculated Risk
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot a Dwarf and add a threat to draw 3 cards.
		*/

		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");

		assertEquals("Calculated Risk", risk.getBlueprint().getTitle());
		assertNull(risk.getBlueprint().getSubtitle());
		assertFalse(risk.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, risk.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, risk.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, risk.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(risk, Timeword.FELLOWSHIP));
		assertEquals(2, risk.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingADwarfCompanionAndAddingAThreatDrawsThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(risk);

		scn.StartGame();

		assertInHand(risk);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetThreats());
		// Frodo, Gimli and Aragorn: room for 3 threats.
		assertEquals(3, scn.GetThreatLimit());
		assertTrue(scn.FreepsPlayAvailable(risk));

		scn.FreepsPlayCard(risk);

		assertEquals(1, scn.GetThreats());
		// The event leaves the hand and 3 cards come in.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(risk);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsADwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(risk);

		scn.StartGame();

		assertInHand(risk);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(risk));

		scn.FreepsPlayCard(risk);

		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertInDiscard(risk);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoDwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var rock = scn.GetFreepsCard("rock");
		var guard = scn.GetFreepsCard("guard");
		// A companion who is no Dwarf, a [dwarven] card that is no Dwarf, and a Dwarf stacked, who is not in play.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord, rock);
		scn.StackCardsOn(rock, guard);
		scn.MoveCardsToHand(risk);

		scn.StartGame();

		assertInHand(risk);
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(risk));
	}

	@Test
	public void IsNotPlayableWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var risk = scn.GetFreepsCard("risk");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(risk);

		scn.StartGame();
		// Frodo and Gimli: a threat limit of 2, reached.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		assertInHand(risk);
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(risk));
	}
}
