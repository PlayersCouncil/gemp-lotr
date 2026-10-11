package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_12");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli in a skirmish against two Uruk Savages, with two [dwarven] cards stacked on the condition.
	protected VirtualTableScenario GetTwoMinionsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("preparations", "7_12");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] card
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] card
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PreparationsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Preparations
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Regroup:</b> Exert a Dwarf companion to stack a card from your discard pile here.<br><b>Skirmish:</b> Wound a minion skirmishing a Dwarf once for each [dwarven] card stacked here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Preparations", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Wound a minion skirmishing a Dwarf once for each [dwarven] card stacked here": with "for each" the wounds
	// may go to several minions or to one more than once (CR 4.2 "for each"; the shape of F-0224).  The
	// definition chose one minion and wounded it once for each card.
	// F-0459.
	@Test
	public void TheWoundsForEachStackedDwarvenCardMayGoToDifferentMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoMinionsScenario();

		var preparations = scn.GetFreepsCard("preparations");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(preparations);
		scn.StackCardsOn(preparations, scn.GetFreepsCard("axe"), scn.GetFreepsCard("armor"));

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage1);
		scn.ShadowAssignToMinions(gimli, savage2);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(preparations));
		scn.FreepsUseCardAction(preparations);

		// The first card's wound: either minion.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));

		// The second card's wound: either minion again, and it goes to the other.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage2);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));

		assertInZone(Zone.DISCARD, preparations);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void PreparationsTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
