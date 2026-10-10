package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elf", "1_53");
					put("elf2", "1_53");
					put("elf3", "1_53");

					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, cost 2
					put("celeborn", "1_34");      // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man
					put("shoulder", "1_59");      // Shoulder to Shoulder: [elven] condition in the support area; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienElfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Lórien Elf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 4
		 * Vitality: 2
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elf");

		assertEquals("Lórien Elf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var inDeadPile = scn.GetFreepsCard("elf2");
		var inHand = scn.GetFreepsCard("elf3");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shoulder = scn.GetFreepsCard("shoulder");
		scn.MoveCardsToHand(elf, inHand, arwen);
		// Everything that is nearly an Elf in play: an [elven] card that is no Elf, a companion who is no
		// Elf, an Elf in the dead pile, and a second Lorien Elf in hand.
		scn.MoveCardsToSupportArea(shoulder);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToDeadPile(inDeadPile);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elf));
		assertFalse(scn.FreepsPlayAvailable(inHand));
		assertTrue(scn.FreepsPlayAvailable(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(elf));
		assertTrue(scn.FreepsPlayAvailable(inHand));

		// Arwen cost 2.
		assertEquals(2, scn.GetTwilight());
		scn.FreepsPlayCard(elf);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, elf);
		assertEquals(4, scn.GetStrength(elf));
		assertEquals(2, scn.GetVitality(elf));
		assertInZone(Zone.HAND, inHand);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnceAnElfAllyIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCardsToHand(elf, celeborn);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elf));
		assertTrue(scn.FreepsPlayAvailable(celeborn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(elf));

		scn.FreepsPlayCard(elf);
		assertInZone(Zone.FREE_CHARACTERS, elf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ALorienElfInPlayIsAnElfForASecondCopyToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var inPlay = scn.GetFreepsCard("elf2");
		scn.MoveCardsToHand(elf);
		// The only Elf on the table is another copy.
		scn.MoveCompanionsToTable(inPlay);

		scn.StartGame();

		assertInZone(Zone.HAND, elf);
		assertTrue(scn.FreepsPlayAvailable(elf));

		scn.FreepsPlayCard(elf);
		assertInZone(Zone.FREE_CHARACTERS, elf, inPlay);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
