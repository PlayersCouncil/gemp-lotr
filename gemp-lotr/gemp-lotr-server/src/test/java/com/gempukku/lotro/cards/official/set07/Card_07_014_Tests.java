package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_14");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slaked", "7_14");
					put("gimli", "1_13");       // Gimli, Son of Glóin: the Dwarf companion spotted
					// Here Is Good Rock: [dwarven] condition in the support area.  "Skirmish: Stack a Free Peoples
					// card from hand here to make a Dwarf damage +1" is how a card comes to be stacked on it.
					put("rock", "4_50");
					put("savage", "1_151");     // Uruk Savage: vitality 3, so it can exert twice
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3 as well, and no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SlakedThirstsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Slaked Thirsts
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Choose one: Spot a Dwarf to draw a card; or, if this card is stacked on a [dwarven] condition, spot a Dwarf companion and discard this event to exert a minion twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Slaked Thirsts", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void StackedOnADwarvenConditionItIsDiscardedInTheManeuverPhaseToExertAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var slaked = scn.GetFreepsCard("slaked");
		var gimli = scn.GetFreepsCard("gimli");
		var rock = scn.GetFreepsCard("rock");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, slaked);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.STACKED, slaked);
		assertEquals(List.of(slaked), scn.GetStackedCards(rock));
		assertEquals(0, scn.GetWoundsOn(savage));
		// The event is not in hand and is not played: the stacked card itself has the action.
		assertTrue(scn.FreepsActionAvailable(slaked));
		scn.FreepsUseCardAction(slaked);

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(slaked);
		assertEquals(0, scn.GetStackedCards(rock).size());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
