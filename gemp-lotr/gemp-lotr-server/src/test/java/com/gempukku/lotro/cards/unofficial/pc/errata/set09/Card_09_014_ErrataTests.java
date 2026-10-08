package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_09_014_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "59_14");
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion, cost 1; to play, spot an Elf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, no text but a requirement to play
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally
					put("bow", "1_41");         // Elven Bow: [elven] possession, bearer must be an Elf, bearer is an archer
					put("nenya", "9_20");       // Nenya, Ring of Adamant: [elven] artifact, cost 0, bearer must be Galadriel; a maneuver ability used only on purpose
					put("aiglos", "9_12");      // Aiglos: [elven] artifact, bearer must be an Elf; does nothing unless borne by Gil-galad or used on purpose
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
				"59_14",                        // Galadriel is the Ring-bearer
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
		 * Resistance: 4
		 * Can be the Ring-bearer: True
		 * Game Text: While Galadriel bears an artifact or The One Ring, she is resistance +1 for each [elven] companion you can spot.
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
		assertEquals(4, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void GaladrielIsPlayedFromHandForThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCardsToHand(galadriel);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(galadriel));

		scn.FreepsPlayCard(galadriel);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
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
		// A Man of the [elven] culture is an [elven] companion
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));

		assertEquals(0, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, galadriel);
		// 4 printed + Galadriel, Legolas and the [elven] Aragorn; not Dwarf Guard, the ally Elrond or the Elven Bow
		assertEquals(7, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(lorienelf);
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertEquals(8, scn.GetResistance(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearingTheOneRingGaladrielIsResistancePlus1ForEachElvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var galadriel = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(legolas);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		// 4 printed + herself
		assertEquals(5, scn.GetResistance(galadriel));

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertEquals(6, scn.GetResistance(galadriel));
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
		assertEquals(4, scn.GetResistance(galadriel));
		// Legolas, who bears the artifact, keeps his printed 6
		assertEquals(6, scn.GetResistance(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
