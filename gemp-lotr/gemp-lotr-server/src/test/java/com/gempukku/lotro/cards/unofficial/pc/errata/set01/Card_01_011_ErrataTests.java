package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_011_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farin", "51_11");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, cost 2, strength 6, damage +1
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, cost 1; a fellowship ability only
					put("guard", "1_7");          // Dwarf Guard: a Dwarf for the dead pile
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man
					put("lord", "1_21");          // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1

					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("taskmaster", "2_90");    // Orc Taskmaster: [sauron] Orc, strength 8, vitality 3; a Shadow ability only
					put("wary", "18_93");         // Wary Orc: Orc of the [orc] culture, strength 7; stronger only beside hunter companions
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1
					put("troll", "19_23");        // Troll of the Deep: a Troll of the [orc] culture, strength 13; its skirmish ability needs a threat
					put("cantea", "1_230");       // Ulaire Cantea, Lieutenant of Dol Guldur: Nazgul, strength 10, fierce; its skirmish ability discards a weapon
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Farin, Dwarven Emissary
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot a Dwarf.
		 * 	While skirmishing an Orc, Farin is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("farin");

		assertEquals("Farin", card.getBlueprint().getTitle());
		assertEquals("Dwarven Emissary", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilADwarfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(farin, gimli);
		// Everything that is nearly a Dwarf in play: a [dwarven] card that is no Dwarf, a Man of the [dwarven]
		// culture, and a Dwarf in the dead pile.
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCompanionsToTable(aragorn);
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.DWARVEN));
		scn.MoveCardsToDeadPile(guard);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(farin));
		assertTrue(scn.FreepsPlayAvailable(gimli));

		scn.FreepsPlayCard(gimli);
		assertTrue(scn.FreepsPlayAvailable(farin));

		int twilight = scn.GetTwilight();
		scn.FreepsPlayCard(farin);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, farin);
		assertEquals(6, scn.GetStrength(farin));
		assertEquals(3, scn.GetVitality(farin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedOnceADwarfAllyIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(farin, grimir);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(farin));
		assertTrue(scn.FreepsPlayAvailable(grimir));

		scn.FreepsPlayCard(grimir);
		assertInZone(Zone.SUPPORT, grimir);
		assertTrue(scn.FreepsPlayAvailable(farin));

		scn.FreepsPlayCard(farin);
		assertInZone(Zone.FREE_CHARACTERS, farin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsStrengthPlus2OnlyWhileHisOwnSkirmishWithAnOrcIsGoingOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(farin, gimli);

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveMinionsToTable(troop, taskmaster);

		scn.SkipToAssignments();
		assertEquals(6, scn.GetStrength(farin));

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{farin, troop},
				new PhysicalCardImpl[]{gimli, taskmaster});

		// Assigned to an Orc, and no skirmish begun.
		assertTrue(scn.IsCharAssignedAgainst(farin, troop));
		assertFalse(scn.IsCharSkirmishing(farin));
		assertEquals(6, scn.GetStrength(farin));

		// Another Dwarf's skirmish with an Orc, while Farin waits for his own.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, taskmaster));
		assertFalse(scn.IsCharSkirmishing(farin));
		assertEquals(6, scn.GetStrength(farin));
		assertEquals(6, scn.GetStrength(gimli));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.IsCharSkirmishingAgainst(farin, troop));
		assertEquals(8, scn.GetStrength(farin));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.PassSkirmishActions();

		// 8 against 13: Farin loses and takes one wound. The Orc is still in play; the skirmish is over.
		assertEquals(1, scn.GetWoundsOn(farin));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(6, scn.GetStrength(farin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsStrengthPlus2AndNoMoreWhileSkirmishingTwoOrcsOfOtherCulturesAndAnUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);

		scn.StartGame();

		var taskmaster = scn.GetShadowCard("taskmaster");
		var wary = scn.GetShadowCard("wary");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(taskmaster, wary, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, taskmaster);
		scn.ShadowAssignToMinions(farin, wary, savage);
		assertEquals(6, scn.GetStrength(farin));

		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.IsCharSkirmishingAgainst(farin, taskmaster));
		assertTrue(scn.IsCharSkirmishingAgainst(farin, wary));
		assertTrue(scn.IsCharSkirmishingAgainst(farin, savage));
		// +2 for skirmishing an Orc, not +2 for each of the two.
		assertEquals(8, scn.GetStrength(farin));
		assertEquals(8, scn.GetStrength(taskmaster));
		assertEquals(7, scn.GetStrength(wary));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotStrongerWhileSkirmishingMinionsThatAreNotOrcs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(farin);

		scn.StartGame();

		// An Uruk-hai, a Troll whose culture is called [orc], and a Nazgul.
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		var cantea = scn.GetShadowCard("cantea");
		// An Orc on the table that nobody is assigned to: Farin is in a skirmish and an Orc is in play, but not
		// in his skirmish.
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troll, cantea, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, savage);
		scn.ShadowAssignToMinions(farin, troll, cantea);
		assertEquals(6, scn.GetStrength(farin));

		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.IsCharSkirmishingAgainst(farin, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(farin, troll));
		assertTrue(scn.IsCharSkirmishingAgainst(farin, cantea));
		assertFalse(scn.IsCharSkirmishingAgainst(farin, troop));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(6, scn.GetStrength(farin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
