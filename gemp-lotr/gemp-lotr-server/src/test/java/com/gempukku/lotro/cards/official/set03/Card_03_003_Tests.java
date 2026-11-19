package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mines", "3_3");
					put("mines2", "3_3");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion of another title, vitality 2, no text but its requirement to play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ALLY, vitality 3; a fellowship ability used only on purpose
					// Stairs of Khazad-dum: [dwarven] tale, a support-area CONDITION of cost 0; its strength bonus needs a skirmish
					put("stairs", "1_24");
					// Book of Mazarbul: [dwarven] tale, a POSSESSION a Dwarf bears; its text answers the start of a fellowship phase at site 4 or higher
					put("book", "3_1");
					// Dwarven Axe: a [dwarven] possession that is no tale; its text answers a skirmish lost to its bearer
					put("axe", "1_9");
					// There and Back Again: a [shire] tale on a Hobbit companion; a maneuver ability used only on purpose
					put("tale", "1_317");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinesofKhazadDumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Mines of Khazad-Dûm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each time you play a [dwarven] tale, you may heal a Dwarf companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mines");

		assertEquals("Mines of Khazad-Dûm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachDwarvenTalePlayedOffersAHealOfADwarfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mines = scn.GetFreepsCard("mines");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var stairs = scn.GetFreepsCard("stairs");
		var book = scn.GetFreepsCard("book");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(mines);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(stairs, book);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.StartGame();

		// A [dwarven] tale that is a condition
		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mines));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second [dwarven] tale, a possession: offered again
		scn.FreepsPlayCard(book);
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(book, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mines));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, frodo));
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedTheWoundsStay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mines = scn.GetFreepsCard("mines");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToSupportArea(mines);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(stairs);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.StartGame();

		scn.FreepsPlayCard(stairs);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mines));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarvenCardsThatAreNoTalesAndATaleOfAnotherCultureOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mines = scn.GetFreepsCard("mines");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var mines2 = scn.GetFreepsCard("mines2");
		var axe = scn.GetFreepsCard("axe");
		var tale = scn.GetFreepsCard("tale");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(mines);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveCardsToHand(mines2, axe, tale);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.StartGame();

		// A [dwarven] condition that is no tale: a second copy of this card
		scn.FreepsPlayCard(mines2);
		assertInZone(Zone.SUPPORT, mines2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [dwarven] possession that is no tale
		scn.FreepsPlayCard(axe);
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(axe, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A tale of the [shire] culture; Frodo is its one bearer
		scn.FreepsPlayCard(tale);
		assertAttachedTo(tale, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedDwarfCompanionTheHealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mines = scn.GetFreepsCard("mines");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var stairs = scn.GetFreepsCard("stairs");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(mines);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(stairs);
		// Only a Dwarf ally and a companion who is no Dwarf are wounded
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.StartGame();

		scn.FreepsPlayCard(stairs);
		assertInZone(Zone.SUPPORT, stairs);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
