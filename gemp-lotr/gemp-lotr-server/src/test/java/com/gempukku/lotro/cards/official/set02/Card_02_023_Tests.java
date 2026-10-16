package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wisdom", "2_23");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: he has the Frodo signet, not the Gandalf signet
					put("glamdring", "1_75");   // Glamdring: [gandalf] possession, hand weapon, cost 2, bearer must be Gandalf; damage +1 and an ability never used
					put("staff", "2_22");       // Gandalf's Staff: [gandalf] artifact, cost 2, bearer must be Gandalf; makes [gandalf] spells -1
					put("stern", "19_9");       // Stern Words: [gandalf] condition, cost 2, bearer must be a companion; to play, spot a [gandalf] Wizard

					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, Gandalf signet; a skirmish ability
					put("axe", "1_14");         // Gimli's Battle Axe: [dwarven] possession, cost 2, bearer must be Gimli
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, Gandalf signet; a maneuver ability
					put("anduril", "7_80");     // Anduril, King's Blade: [gondor] artifact, cost 3, bearer must be Aragorn
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion with the Frodo signet; an archery ability
					put("cloak", "1_42");       // Elven Cloak: [elven] possession, cost 1, bearer must be a companion; to play, spot an Elf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsWisdomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Gandalf's Wisdom
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>The twilight cost of each possession or artifact played on Gandalf or a character who has the Gandalf signet is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wisdom");

		assertEquals("Gandalf's Wisdom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void APossessionOrArtifactPlayedOnGandalfCostsOneLessAndAConditionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisdom = scn.GetFreepsCard("wisdom");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var staff = scn.GetFreepsCard("staff");
		var stern = scn.GetFreepsCard("stern");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(wisdom);
		scn.MoveCardsToHand(glamdring, staff, stern);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wisdom);
		assertEquals(0, scn.GetTwilight());

		// Gandalf has the Frodo signet: it is his name that brings the discount.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		// 2 printed - 1
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, gandalf);
		// 1 + (2 printed - 1)
		assertEquals(2, scn.GetTwilight());

		// A condition played on him pays all of its 2.
		scn.FreepsPlayCard(stern);
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(stern, gandalf);
		assertEquals(2 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionOrArtifactPlayedOnACompanionWithTheGandalfSignetCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wisdom = scn.GetFreepsCard("wisdom");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var legolas = scn.GetFreepsCard("legolas");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(gimli, aragorn, legolas);
		scn.MoveCardsToSupportArea(wisdom);
		scn.MoveCardsToHand(axe, anduril, cloak);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, wisdom);
		assertEquals(0, scn.GetTwilight());

		// Gimli, a [dwarven] Dwarf with the Gandalf signet.
		scn.FreepsPlayCard(axe);
		assertAttachedTo(axe, gimli);
		// 2 printed - 1
		assertEquals(1, scn.GetTwilight());

		// Aragorn, a [gondor] Man with the Gandalf signet.
		scn.FreepsPlayCard(anduril);
		assertAttachedTo(anduril, aragorn);
		// 1 + (3 printed - 1)
		assertEquals(3, scn.GetTwilight());

		// Legolas has the Frodo signet: the cloak pays all of its 1.
		scn.FreepsPlayCard(cloak);
		scn.FreepsChooseCard(legolas);
		assertAttachedTo(cloak, legolas);
		assertEquals(3 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
