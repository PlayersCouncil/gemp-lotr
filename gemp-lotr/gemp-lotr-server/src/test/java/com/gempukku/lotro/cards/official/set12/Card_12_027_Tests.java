package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "12_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, strength 8; a maneuver ability
					put("avenger", "6_27");     // Ent Avenger: an unbound [gandalf] Ent, strength 6; a bonus beside 3 Ents only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; strength 3, vitality 4
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home site 3; a maneuver ability used only on purpose

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("savage2", "1_151");
					put("savage3", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Gandalf, The White Rider
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: <b>Skirmish:</b> If Gandalf is not assigned to a skirmish, exert him and another unbound companion skirmishing a minion to make Gandalf strength +3 and have him replace that companion in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("The White Rider", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsHimAndTheSkirmishingCompanionToReplaceHimAtStrengthPlus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		// Aragorn fights first; the Ent Avenger is assigned to a skirmish still to come.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{avenger, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		// Aragorn is the only unbound companion skirmishing: he exerts without being asked for.
		scn.FreepsUseCardAction(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(10, scn.GetStrength(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, savage1));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(avenger, savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still +3 at the next decision of that skirmish.
		scn.ShadowPass();
		assertEquals(10, scn.GetStrength(gandalf));
		scn.FreepsPass();
		// Gandalf's 10 is double the Savage's 5: it is overwhelmed (his 7 alone would only have wounded it).
		// No wound on Aragorn, who took no part.
		assertInZone(Zone.DISCARD, savage1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(avenger);
		assertEquals(7, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileGandalfIsAssignedToASkirmishStillToCome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var savage3 = scn.GetShadowCard("savage3");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, savage3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{gandalf, savage2},
				new PhysicalCardImpl[]{avenger, savage3});

		// Aragorn's skirmish: an unbound companion skirmishing, and both able to exert, but Gandalf is assigned
		// to a skirmish of his own that is still to come.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertTrue(scn.IsCharAssignedAgainst(gandalf, savage2));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
		scn.PassSkirmishActions();

		// Gandalf's own skirmish: his 7 beats the Savage's 5, and he is assigned to nothing any more.
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
		scn.PassSkirmishActions();

		// The Ent Avenger's skirmish: now it is offered.
		scn.FreepsResolveSkirmish(avenger);
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void IsNotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gandalf, 3);
		scn.MoveMinionsToTable(savage1);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void IsNotOfferedWhileTheSkirmishingCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		// Aragorn, vitality 4 with 3 wounds, is exhausted; the unassigned Ent Avenger could exert, and is in no
		// skirmish.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(savage1);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertFalse(scn.IsCharAssigned(avenger));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void IsNotOfferedForARingBoundCompanionNorForOneWhoseSkirmishHasNotBegunAndReplacesAnotherCulturesCompanionLater() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, sam, avenger);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, savage1},
				new PhysicalCardImpl[]{avenger, savage2});

		// Sam's skirmish: Sam is Ring-bound, and the Ent Avenger, though unbound, is not skirmishing yet.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage1));
		assertTrue(scn.IsCharAssignedAgainst(avenger, savage2));
		assertFalse(scn.IsCharSkirmishing(avenger));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
		scn.PassSkirmishActions();

		// Sam's 3 loses to the Savage's 5: two wounds with its damage +1.
		assertEquals(2, scn.GetWoundsOn(sam));

		// The Ent Avenger's skirmish: a [gandalf] Ent this time, and Gandalf takes his place.
		scn.FreepsResolveSkirmish(avenger);
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsActionAvailable(gandalf));
		scn.FreepsUseCardAction(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(10, scn.GetStrength(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, savage2));
		assertFalse(scn.IsCharSkirmishing(avenger));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnAllySkirmishingAtHisHomeSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(thrarin);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		// Thrarin's home is site 3: he may be assigned there.
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, savage1);
		scn.FreepsResolveSkirmish(thrarin);

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, savage1));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}
}
