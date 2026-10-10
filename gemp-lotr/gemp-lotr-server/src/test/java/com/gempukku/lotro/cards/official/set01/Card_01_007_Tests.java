package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, cost 2
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, cost 1; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man
					put("lord", "1_21");          // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarfGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarf Guard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 4
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: To play, spot a Dwarf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Dwarf Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilADwarfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var inDeadPile = scn.GetFreepsCard("guard2");
		var inHand = scn.GetFreepsCard("guard3");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(guard, inHand, gimli);
		// Everything that is nearly a Dwarf in play: a [dwarven] card that is no Dwarf, a companion who is no
		// Dwarf, a Dwarf in the dead pile, and a second Guard in hand.
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDeadPile(inDeadPile);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));
		assertFalse(scn.FreepsPlayAvailable(inHand));
		assertTrue(scn.FreepsPlayAvailable(gimli));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.FreepsPlayAvailable(inHand));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(guard);
		assertEquals(twilight + 1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(4, scn.GetStrength(guard));
		assertEquals(2, scn.GetVitality(guard));
		assertInZone(Zone.HAND, inHand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnceADwarfAllyIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(guard, grimir);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));
		assertTrue(scn.FreepsPlayAvailable(grimir));

		scn.FreepsPlayCard(grimir);
		assertInZone(Zone.SUPPORT, grimir);
		assertTrue(scn.FreepsPlayAvailable(guard));

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfGuardInPlayIsADwarfForASecondCopyToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var inPlay = scn.GetFreepsCard("guard2");
		scn.MoveCardsToHand(guard);
		// The only Dwarf on the table is another copy.
		scn.MoveCompanionsToTable(inPlay);

		scn.StartGame();

		assertInZone(Zone.HAND, guard);
		assertTrue(scn.FreepsPlayAvailable(guard));

		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard, inPlay);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
