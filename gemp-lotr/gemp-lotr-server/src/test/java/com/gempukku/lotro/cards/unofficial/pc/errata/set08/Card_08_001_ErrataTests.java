package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_001_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aggression", "58_1");
					put("aggression2", "58_1"); // a second copy, to show which one is discarded

					put("guard", "1_7");       // Dwarf Guard: Dwarf companion, no damage bonus; to play, spot a Dwarf
					put("guard2", "1_7");
					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion, printed damage +1; a skirmish ability only
					put("thrarin", "1_27");    // Thrarin, Dwarven Smith: Dwarf ally; a maneuver ability only
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose

					put("filler1", "1_177");   // Goblin Patrol Troop: no text; only ever held in hand
					put("filler2", "1_177");
					put("filler3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AggressionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: 8
		 * Name: Aggression
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you have initiative, each Dwarf is damage +1.
		 * Fellowship: Spot a Dwarf and discard this condition to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aggression");

		assertEquals("Aggression", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AggressionPlaysToTheSupportAreaForOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		scn.MoveCardsToHand(aggression);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInHand(aggression);

		scn.FreepsPlayCard(aggression);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, aggression);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileTheFreePeoplesPlayerHasInitiativeEachDwarfIsDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(aggression);
		scn.MoveCompanionsToTable(guard, gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		// Aragorn is of the [dwarven] culture but no Dwarf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));
		scn.MoveCardsToHand(guard2, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));

		scn.StartGame();

		// 4 cards in hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(1, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		// Printed damage +1, and +1 more.
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// Playing the second Dwarf Guard takes the hand to 3 and the initiative away.
		scn.FreepsPlayCard(guard2);

		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsHasInitiative());
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipSpotsADwarfAndDiscardsAggressionToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var aggression2 = scn.GetFreepsCard("aggression2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToSupportArea(aggression, aggression2);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, aggression);
		assertInZone(Zone.SUPPORT, aggression2);
		assertTrue(scn.FreepsActionAvailable(aggression));

		scn.FreepsUseCardAction(aggression);

		// Only the copy whose action was used is discarded.
		assertInDiscard(aggression);
		assertInZone(Zone.SUPPORT, aggression2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipIsNotOfferedUntilADwarfCanBeSpottedAndADwarvenAllyIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aggression = scn.GetFreepsCard("aggression");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(aggression);
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn is of the [dwarven] culture but no Dwarf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));
		scn.MoveCardsToHand(thrarin);

		scn.StartGame();

		assertFalse(scn.FreepsActionAvailable(aggression));

		scn.FreepsPlayCard(thrarin);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(aggression));

		scn.FreepsUseCardAction(aggression);

		assertInDiscard(aggression);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
