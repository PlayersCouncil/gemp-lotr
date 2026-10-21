package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_014_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "9_14");
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion, cost 1; to play, spot an Elf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, no text but a requirement to play
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally
					put("bow", "1_41");         // Elven Bow: [elven] possession (ranged weapon), bearer must be an Elf, bearer is an archer
					put("nenya", "9_20");       // Nenya, Ring of Adamant: [elven] artifact (ring), cost 0, bearer must be Galadriel; a maneuver ability used only on purpose
					put("aiglos", "9_12");      // Aiglos: [elven] artifact (hand weapon), cost 2, bearer must be an Elf; does nothing unless borne by Gil-galad or used on purpose
					put("gilgalad", "9_15");    // Gil-galad, Elven High King: [elven] Elf companion; a regroup ability only
					put("vilya", "9_23");       // Vilya, Ring of Air: [elven] artifact, bearer must be Elrond or Gil-galad; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2; an archery ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,   // Galadriel, Bearer of Wisdom is the Ring-bearer
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Galadriel, Bearer of Wisdom
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 3
		 * Can be the Ring-bearer: True
		 * Game Text: While Galadriel bears an artifact or The One Ring, she is resistance +1 for each [elven] companion
		 * you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", card.getBlueprint().getTitle());
		assertEquals("Bearer of Wisdom", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void BearingAnArtifactGaladrielIsResistancePlus1ForEachElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var legolas = scn.GetFreepsCard("legolas");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var elrond = scn.GetFreepsCard("elrond");
		var bow = scn.GetFreepsCard("bow");
		var nenya = scn.GetFreepsCard("nenya");
		scn.MoveCompanionsToTable(galadriel, legolas, aragorn, guard);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(nenya, lorienelf);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, galadriel);
		// 3 printed + Galadriel and Legolas; not Aragorn or Dwarf Guard (companions of other cultures), the ally Elrond,
		// the Elven Bow or Frodo
		assertEquals(5, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(lorienelf);
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertEquals(6, scn.GetResistance(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearingTheOneRingGaladrielIsResistancePlus1ForEachElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(legolas);

		scn.StartGame();

		assertAttachedTo(scn.GetRing(), galadriel);
		assertEquals(0, scn.GetBurdens());
		// 3 printed + herself
		assertEquals(4, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertEquals(5, scn.GetResistance(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearingAPossessionWhileAnotherBearsAnArtifactGaladrielGetsNoResistance() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var aiglos = scn.GetFreepsCard("aiglos");
		scn.MoveCompanionsToTable(galadriel, legolas);
		scn.AttachCardsTo(galadriel, bow);
		scn.AttachCardsTo(legolas, aiglos);

		scn.StartGame();

		assertAttachedTo(bow, galadriel);
		assertAttachedTo(aiglos, legolas);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetResistance(galadriel));
		// Legolas, who bears the artifact, keeps his printed 6
		assertEquals(6, scn.GetResistance(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHandWeaponArtifactPlayedOnHerBesideHerBowSwitchesTheBonusOnAndGilgaladWithHisRingIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var vilya = scn.GetFreepsCard("vilya");
		var bow = scn.GetFreepsCard("bow");
		var aiglos = scn.GetFreepsCard("aiglos");
		scn.MoveCompanionsToTable(galadriel, gilgalad);
		scn.AttachCardsTo(galadriel, bow);
		scn.AttachCardsTo(gilgalad, vilya);
		scn.MoveCardsToHand(aiglos);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetResistance(galadriel));
		assertEquals(6, scn.GetResistance(gilgalad));

		// Aiglos may go on either Elf; it goes on Galadriel, beside her bow (a weapon of another class).
		scn.FreepsPlayCard(aiglos);
		assertTrue(scn.FreepsHasCardChoiceAvailable(galadriel, gilgalad));
		scn.FreepsChooseCard(galadriel);
		assertAttachedTo(aiglos, galadriel);
		assertAttachedTo(bow, galadriel);

		// 3 printed + Galadriel and Gil-galad.  Gil-galad, an [elven] companion bearing an artifact, keeps his 6.
		assertEquals(5, scn.GetResistance(galadriel));
		assertEquals(6, scn.GetResistance(gilgalad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
