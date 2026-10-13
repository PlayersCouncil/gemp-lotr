package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "8_15");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: a companion of cost 4; a maneuver ability only
					put("knight", "5_35");     // Gondorian Knight: a companion of cost 2 with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gandalf, Leader of Men
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: When Gandalf is in your starting fellowship, his twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Leader of Men", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void InTheStartingFellowshipHeCostsTwoLessAndNoOtherCompanionDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");

		// While a player chooses his starting fellowship the game keeps the total of what he has played in the
		// twilight pool.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, aragorn, knight));
		// 4 - 2; Aragorn and the Knight keep their printed 4 and 2.
		assertEquals(2, scn.GetTwilightCost(gandalf));
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(2, scn.GetTwilightCost(knight));

		scn.FreepsChooseCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(2, scn.GetTwilight());
		// The Knight's 2 still fits under the 4, Aragorn's 4 no longer does.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(2, scn.GetTwilightCost(knight));
	}

	@Test
	public void PlayedFromHandOutsideTheStartingFellowshipHeCostsHisPrintedFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		assertEquals(4, scn.GetTwilightCost(gandalf));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gandalf));
		scn.FreepsPlayCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
