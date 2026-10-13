package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_17_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "17_134");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An [uruk-hai] hunter minion and a Free Peoples hunter companion, each to be assigned to a skirmish.
	protected VirtualTableScenario GetHuntersOfBothSidesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vanquisher", "17_134");
					put("tracker", "15_171");
					put("gimli", "15_5");
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandVanquisherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: White Hand Vanquisher
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>This minion is <b>damage +1</b> for each [uruk-hai] hunter assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("White Hand Vanquisher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	// "damage +1 for each [uruk-hai] hunter assigned to a skirmish": the definition counted every hunter, the Free
	// Peoples player's among them.
	@Test
	public void OnlyUrukHaiCultureHuntersAssignedToASkirmishAreCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHuntersOfBothSidesScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var vanquisher = scn.GetShadowCard("vanquisher");
		var tracker = scn.GetShadowCard("tracker");

		scn.StartGame();
		scn.MoveMinionsToTable(vanquisher, tracker);
		scn.SkipToAssignments();
		assertEquals(0, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));

		// Gimli, Feared Axeman is a hunter and is assigned, to the Vanquisher herself; Tracking Uruk is the one
		// [uruk-hai] hunter assigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, vanquisher}, new PhysicalCardImpl[]{aragorn, tracker});
		assertTrue(scn.HasKeyword(gimli, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(vanquisher, Keyword.DAMAGE));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void WhiteHandVanquisherTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}
}
