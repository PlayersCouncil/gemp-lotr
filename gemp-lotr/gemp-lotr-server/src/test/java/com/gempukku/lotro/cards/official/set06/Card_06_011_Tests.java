package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
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

public class Card_06_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "6_11");
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
					put("toss", "6_11");
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6
					put("guard", "1_7");        // Dwarf Guard: another Dwarf, strength 4, whom the text does not name
					// Here Is Good Rock: [dwarven] condition in the support area.  "Skirmish: Stack a Free Peoples
					// card from hand here to make a Dwarf damage +1" is how a card comes to be stacked on it.
					put("rock", "4_50");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TossMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Toss Me
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: While this card is stacked on a [dwarven] condition, Gimli is strength +1.<br><b>Fellowship:</b> Place this card or another [dwarven] card from hand on top of or beneath your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Toss Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileStackedOnADwarvenConditionGimliIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackScenario();

		var toss = scn.GetFreepsCard("toss");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var rock = scn.GetFreepsCard("rock");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToSupportArea(rock);
		scn.MoveCardsToHand(toss);

		scn.StartGame();

		// In hand the card does nothing.
		assertInHand(toss);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));

		// The stacking is arrangement: what is shown is the text of a card that lies on a stack and is not in play.
		scn.StackCardsOn(rock, toss);

		assertInZone(Zone.STACKED, toss);
		assertEquals(List.of(toss), scn.GetStackedCards(rock));
		// 6 + 1, and for Gimli alone.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
