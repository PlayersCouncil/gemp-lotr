package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("counts", "8_4");

					put("gimli", "1_12");       // Gimli, Dwarf of Erebor: strength 6, damage +1; a fellowship ability only
					put("dwarfguard", "1_7");   // Dwarf Guard: a [dwarven] Dwarf companion who is no Gimli, strength 4; "To play, spot a Dwarf"
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man, strength 8; a maneuver ability
					put("guard", "5_83");       // Household Guard: an unbound [rohan] Man, strength 6; "Valiant" is its whole text
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; strength 3, vitality 4
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home site 3; a maneuver ability used only on purpose

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreeSkirmishesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("counts", "8_4");
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6; his own skirmish ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, strength 8
					put("knight", "5_35");      // Gondorian Knight: an unbound companion, strength 6; Knight is his whole text
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CountsButOneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Counts But One
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If Gimli is not assigned to a skirmish, add a threat to have him replace an unbound companion in a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("counts");

		assertEquals("Counts But One", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GimliReplacesTheUnboundCompanionInTheSkirmishForAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counts = scn.GetFreepsCard("counts");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, dwarfguard, aragorn, guard);
		scn.MoveCardsToHand(counts);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		// Aragorn fights first; the Household Guard is assigned to a skirmish still to come.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{guard, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertTrue(scn.IsCharAssignedAgainst(guard, savage2));
		assertFalse(scn.IsCharAssigned(gimli));
		assertFalse(scn.IsCharAssigned(dwarfguard));
		// Five companions, so a threat can be added.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(counts));

		scn.FreepsPlayCard(counts);

		assertEquals(1, scn.GetThreats());
		// Gimli takes Aragorn's place; the other unassigned Dwarf stays out of it.
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage1));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharSkirmishing(dwarfguard));
		// The unbound companion whose skirmish has not begun is not in a skirmish, and is left where he was.
		assertTrue(scn.IsCharAssignedAgainst(guard, savage2));
		assertFalse(scn.IsCharSkirmishing(guard));
		assertInDiscard(counts);
		// CR 4.2 "replace": the skirmish goes on with Gimli and does not start over; the Shadow player is next to act.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// Gimli's 6 beats the Savage's 5: one wound and one more for his damage +1.  Aragorn took no part.
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WhileGimliIsAssignedToASkirmishItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreeSkirmishesScenario();

		var counts = scn.GetFreepsCard("counts");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(gimli, aragorn, knight);
		scn.MoveCardsToHand(counts);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner1},
				new PhysicalCardImpl[]{gimli, runner2},
				new PhysicalCardImpl[]{knight, runner3});

		// Aragorn's skirmish: an unbound companion is there to be replaced and, with no threat beside four
		// companions, one can be added.  Gimli is assigned to a skirmish of his own that is still to come.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.IsCharAssigned(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(counts));
		scn.PassSkirmishActions();

		// Nor is it in Gimli's own skirmish.  Then Gimli, strength 6, beats his Runner, strength 5, and is assigned
		// to nothing any more.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(counts));
		scn.PassSkirmishActions();

		// The Knight's skirmish: the same event in the same hand is now offered.
		scn.FreepsResolveSkirmish(knight);
		assertFalse(scn.IsCharAssigned(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(counts));
	}

	@Test
	public void CannotBePlayedWithoutGimliThoughAnotherDwarfCompanionIsUnassigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counts = scn.GetFreepsCard("counts");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(dwarfguard, aragorn);
		scn.MoveCardsToHand(counts);
		assertInHand(counts);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		// A [dwarven] Dwarf companion nobody is assigned to, and no Gimli.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertFalse(scn.IsCharAssigned(dwarfguard));
		assertEquals(0, scn.GetThreats());
		assertInHand(counts);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(counts));
	}

	@Test
	public void CannotBePlayedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counts = scn.GetFreepsCard("counts");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(counts);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		// Three companions and three threats: no threat can be added (CR 4.2 "threats").
		scn.AddThreats(3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());
		assertFalse(scn.IsCharAssigned(gimli));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(counts));

		// One threat fewer, and the threat can be paid.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(counts));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithARingBoundCompanionFightingItIsPlayedForNothingAndTheThreatIsStillAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counts = scn.GetFreepsCard("counts");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveCardsToHand(counts);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, savage1);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage1));
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetThreats());
		// Gimli is unassigned and the threat can be paid: the event may be played though Sam is Ring-bound
		// (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(counts));
		scn.FreepsPlayCard(counts);

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage1));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertInDiscard(counts);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnAllyFightingAtHisHomeSiteItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var counts = scn.GetFreepsCard("counts");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(counts);

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
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(counts));
		scn.FreepsPlayCard(counts);

		// An ally is no companion: he stays in his skirmish, and the threat is paid all the same.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, savage1));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertInDiscard(counts);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
