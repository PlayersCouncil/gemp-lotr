package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_072_Tests
{

	// Frodo, Son of Drogo, the Ring-bearer, is a Hobbit in the fellowship.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");
					put("gimli", "1_12");       // Gimli, Dwarf of Erebor: [dwarven] Dwarf companion; a fellowship ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; strength +3 only against a Nazgûl
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man companion, strength 7; an overwhelm bar only
					put("sam", "1_311");        // Sam, Son of Hamfast: a second Hobbit companion; his abilities used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability used only on purpose
					put("rumil", "1_57");       // Rúmil, Elven Protector: Elf ally; an archery ability used only on purpose
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: Man ally; a maneuver ability used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a Man minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges, the Ring-bearer, is a Dwarf in the fellowship, and no Hobbit companion is there.
	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");
					put("bounder", "1_286");    // Bounder: Hobbit ally; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion (cheated in); a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Gandalf, Friend of the Shirefolk
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 6
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Gandalf is strength +1 for each of these races you can spot in the fellowship: Hobbit, Dwarf, Elf, and Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Friend of the Shirefolk", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void StrengthRisesByOneForEachRaceInTheFellowshipUpToAllFourAndNotForASecondCompanionOfARace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(gimli, arwen, boromir, sam);

		scn.StartGame();

		// 6 printed + 1 for the Hobbit Ring-bearer.  Gandalf himself is a Wizard, which is not counted.
		assertEquals(7, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(gimli);
		// + 1 for a Dwarf
		assertEquals(8, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(arwen);
		// + 1 for an Elf
		assertEquals(9, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(boromir);
		// + 1 for a Man: all four races
		assertEquals(10, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(sam);
		// A second Hobbit is no new race.
		assertEquals(10, scn.GetStrength(gandalf));
		// Gandalf alone is made stronger.
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AlliesAndAMinionOfTheseRacesAreNotInTheFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		var rumil = scn.GetFreepsCard("rumil");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(grimir, rumil, albert);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();

		// 6 + 1 for the Hobbit Ring-bearer: the Dwarf, Elf and Man allies add nothing.
		assertEquals(7, scn.GetStrength(gandalf));

		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		// Nor does a Man minion.
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AHobbitAllyIsNotCountedAndAHobbitOfAnotherCultureIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var bounder = scn.GetFreepsCard("bounder");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(guard);

		scn.StartGame();

		// 6 + 1 for the Dwarf Ring-bearer: the Hobbit ally adds nothing.
		assertEquals(7, scn.GetStrength(gandalf));

		scn.FreepsPlayCard(guard);
		// + 1 for a [rohan] Man
		assertEquals(8, scn.GetStrength(gandalf));

		scn.MoveCompanionsToTable(pippin);
		// + 1 for a [gondor] Hobbit, and nothing more for his culture
		assertEquals(9, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
