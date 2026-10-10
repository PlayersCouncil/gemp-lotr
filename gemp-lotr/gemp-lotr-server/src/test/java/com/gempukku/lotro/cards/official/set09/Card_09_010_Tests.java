package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "9_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sindri", "9_10");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a second Dwarf, strength 6; his own skirmish ability is not used
					// Here Is Good Rock: a [dwarven] condition whose own ability stacks any Free Peoples card from hand on it
					put("rock", "4_50");
					// I Would Have Gone With You to the End: a [gondor] condition that play stacks skirmish events on;
					// with no Aragorn in play it does nothing
					put("end", "101_23");
					put("blow1", "1_5");        // Cleaving Blow: [dwarven] Skirmish event, cost 1: "Make a Dwarf strength +2 and damage +1"
					put("blow2", "1_5");
					put("disquiet", "2_2");     // Disquiet of Our People: a [dwarven] Maneuver event
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event, cost 0, that Frodo could exert for
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SindriStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Sindri, Dwarven Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: You may play [dwarven] skirmish events stacked on [dwarven] conditions as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sindri", card.getBlueprint().getTitle());
		assertEquals("Dwarven Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ADwarvenSkirmishEventStackedOnADwarvenConditionIsPlayedInASkirmishAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedScenario();

		var sindri = scn.GetFreepsCard("sindri");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		var end = scn.GetFreepsCard("end");
		var blow1 = scn.GetFreepsCard("blow1");
		var blow2 = scn.GetFreepsCard("blow2");
		var disquiet = scn.GetFreepsCard("disquiet");
		var deftness = scn.GetFreepsCard("deftness");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(sindri, gimli);
		scn.MoveCardsToSupportArea(rock, end);
		// On the [dwarven] condition: the [dwarven] skirmish event, a [dwarven] event of another phase, a skirmish
		// event of another culture and a [dwarven] card that is no event.  On the [gondor] condition: the same
		// [dwarven] skirmish event.
		scn.StackCardsOn(rock, blow1, disquiet, deftness, armor);
		scn.StackCardsOn(end, blow2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner1}, new PhysicalCardImpl[]{sindri, runner2});

		// "You may play", not "Sindri may": the event is offered in the other Dwarf's skirmish as well.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(blow1));
		assertFalse(scn.FreepsPlayAvailable(blow2));
		scn.PassSkirmishActions();

		// Sindri's own skirmish: of the five stacked cards, only the [dwarven] skirmish event on the [dwarven] condition.
		scn.FreepsResolveSkirmish(sindri);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(blow1));
		assertFalse(scn.FreepsPlayAvailable(blow2));
		assertFalse(scn.FreepsPlayAvailable(disquiet));
		assertFalse(scn.FreepsPlayAvailable(deftness));
		assertFalse(scn.FreepsPlayAvailable(armor));

		int twilight = scn.GetTwilight();
		assertEquals(6, scn.GetStrength(sindri));
		assertFalse(scn.HasKeyword(sindri, Keyword.DAMAGE));
		assertEquals(4, scn.GetStackedCards(rock).size());
		scn.FreepsPlayCard(blow1);

		// "Make a Dwarf ...": either Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sindri, gimli));
		scn.FreepsChooseCard(sindri);

		// As if from hand, so its cost of 1 is paid.
		assertEquals(twilight + 1, scn.GetTwilight());
		// 6 + 2, and damage +1.
		assertEquals(8, scn.GetStrength(sindri));
		assertEquals(1, scn.GetKeywordCount(sindri, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.DISCARD, blow1);
		assertEquals(3, scn.GetStackedCards(rock).size());
		assertInZone(Zone.STACKED, blow2, disquiet, deftness, armor);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithoutSindriAStackedDwarvenSkirmishEventIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		var blow1 = scn.GetFreepsCard("blow1");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, blow1);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner1);
		scn.FreepsResolveSkirmish(gimli);

		// The same Dwarf in the same skirmish as in the test above, with Sindri still in the draw deck.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blow1));
		assertInZone(Zone.STACKED, blow1);
	}
}
