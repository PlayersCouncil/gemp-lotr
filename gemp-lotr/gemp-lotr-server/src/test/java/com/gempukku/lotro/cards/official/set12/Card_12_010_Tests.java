package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spills", "12_10");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion; "To play, spot a Dwarf" is its whole text
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: no text; stands in the draw deck
					put("troop2", "1_177");
					put("runner", "1_178");     // Goblin Runner: stands in the draw deck
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoPausesNoSpillsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: No Pauses, No Spills
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Toil 2</b>. <helper>(For each [dwarven] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot 2 Dwarves to draw up to 4 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spills");

		assertEquals("No Pauses, No Spills", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingADwarfCompanionAndADwarfAllyItDrawsUpToFourAndToilTakesTwoForAnExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var spills = scn.GetFreepsCard("spills");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(spills);

		scn.StartGame();
		// Five cards in the deck, one more than can be drawn.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("guard"),
				scn.GetFreepsCard("troop2"), scn.GetFreepsCard("runner2"));
		assertInHand(spills);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Two Dwarves: a companion and an ally.
		assertTrue(scn.FreepsPlayAvailable(spills));
		scn.FreepsPlayCard(spills);

		// Toil: any [dwarven] character, none needed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		// 4 - 2 for the one exertion
		assertEquals(2, scn.GetTwilight());

		// Up to 4.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("4");

		assertInZone(Zone.DISCARD, spills);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FewerThanFourMayBeDrawnAndToilTakesFourForTwoExertions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spills = scn.GetFreepsCard("spills");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(spills);

		scn.StartGame();
		// Five cards in the deck, one more than can be drawn.
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("troop"), scn.GetFreepsCard("runner"), scn.GetFreepsCard("guard"),
				scn.GetFreepsCard("troop2"), scn.GetFreepsCard("runner2"));
		assertInHand(spills);
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		scn.FreepsPlayCard(spills);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gimli, grimir);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		// 4 - 2 - 2 for the two exertions
		assertEquals(0, scn.GetTwilight());

		scn.FreepsChoose("2");

		assertInZone(Zone.DISCARD, spills);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilASecondDwarfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spills = scn.GetFreepsCard("spills");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(spills, guard);

		scn.StartGame();
		assertInHand(spills, guard);

		// One Dwarf and a companion who is no Dwarf.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(spills));

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(spills));
	}
}
