package com.gempukku.lotro.cards.official.set04;

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

public class Card_04_056_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("search", "4_56");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability only
					// Thrarin, Dwarven Smith: a Dwarf ally, home 3F; a maneuver ability only
					put("thrarin", "1_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, no Dwarf; a maneuver ability only
					// From the Armory: a [dwarven] support-area condition, no Dwarf; a skirmish ability only
					put("armory", "4_47");
					// Hobbit Sword: four known cards for the draw deck, with no text but a bearer
					put("sword1", "1_299");
					put("sword2", "1_299");
					put("sword3", "1_299");
					put("sword4", "1_299");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SearchFarandWideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Search Far and Wide
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot a Dwarf and discard the top card of your draw deck to draw 2 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("search");

		assertEquals("Search Far and Wide", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsADwarfAndDiscardsTheTopCardOfTheDrawDeckToDrawTheNextTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var search = scn.GetFreepsCard("search");
		var gimli = scn.GetFreepsCard("gimli");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		var sword4 = scn.GetFreepsCard("sword4");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(search);

		scn.StartGame();
		scn.SetFreepsDrawDeck(sword1, sword2, sword3, sword4);
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(sword1, scn.GetFreepsDrawDeck().get(0));
		assertEquals(1, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsPlayAvailable(search));
		scn.FreepsPlayCard(search);

		// The top card is discarded, the next two are drawn, and the fourth is left on top.
		assertInDiscard(sword1, search);
		assertInHand(sword2, sword3);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(sword4, scn.GetFreepsDrawDeck().get(0));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsSpotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var search = scn.GetFreepsCard("search");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(search);

		scn.StartGame();
		scn.SetFreepsDrawDeck(sword1, sword2, sword3);
		assertEquals(3, scn.GetFreepsDeckCount());

		assertTrue(scn.FreepsPlayAvailable(search));
		scn.FreepsPlayCard(search);

		assertInDiscard(sword1, search);
		assertInHand(sword2, sword3);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoDwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var search = scn.GetFreepsCard("search");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetFreepsCard("armory");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var sword3 = scn.GetFreepsCard("sword3");
		// A companion who is no Dwarf, and a [dwarven] card that is no Dwarf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(armory);
		scn.MoveCardsToHand(search);

		scn.StartGame();
		scn.SetFreepsDrawDeck(sword1, sword2, sword3);
		assertEquals(3, scn.GetFreepsDeckCount());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(search));
		assertInHand(search);
		assertEquals(3, scn.GetFreepsDeckCount());
	}

	@Test
	public void IsNotPlayableWithAnEmptyDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var search = scn.GetFreepsCard("search");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(search);

		scn.StartGame();
		// The top card of the draw deck cannot be discarded: the cost cannot be paid.
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(search));
		assertInHand(search);
	}
}
