package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farewell", "11_19");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, resistance 6; strength +3 only against a Nazgul
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally (resistance 0); an archery ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit companion, resistance 5; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, resistance 6, no Elf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarewelltoLorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Farewell to Lórien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot an Elf to heal each companion who has resistance 5 or more.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("farewell");

		assertEquals("Farewell to Lórien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HealsOneWoundFromEachCompanionOfResistance5OrMoreAndNotFromOneOfResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farewell = scn.GetFreepsCard("farewell");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, sam);
		scn.MoveCardsToHand(farewell);

		scn.StartGame();
		// Each burden takes 1 from every companion's resistance: Arwen 6 - 1 = 5, Sam 5 - 1 = 4, Frodo 10 - 1 = 9.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 1);

		assertEquals(5, scn.GetResistance(arwen));
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(farewell));

		scn.FreepsPlayCard(farewell);
		// Arwen, exactly at 5, and Frodo are healed one wound each; Sam, one short, is not.
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInDiscard(farewell);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsAnElfToSpotAndIsNoCompanionToHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farewell = scn.GetFreepsCard("farewell");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(farewell);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(orophin, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(orophin));

		// Orophin is the only Elf.
		assertTrue(scn.FreepsPlayAvailable(farewell));
		scn.FreepsPlayCard(farewell);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertInDiscard(farewell);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoElfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farewell = scn.GetFreepsCard("farewell");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(farewell);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(sam, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsPlayAvailable(farewell));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedCompanionOfResistance5OrMoreItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farewell = scn.GetFreepsCard("farewell");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(arwen, sam);
		scn.MoveCardsToHand(farewell);

		scn.StartGame();
		// Sam 5 - 1 = 4; Arwen, at 5, has no wound to heal.
		scn.AddBurdens(1);
		scn.AddWoundsToChar(sam, 1);
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(arwen));

		assertTrue(scn.FreepsPlayAvailable(farewell));
		scn.FreepsPlayCard(farewell);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(farewell);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
