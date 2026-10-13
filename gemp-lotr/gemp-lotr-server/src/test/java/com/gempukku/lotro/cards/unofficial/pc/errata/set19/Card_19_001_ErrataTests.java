package com.gempukku.lotro.cards.unofficial.pc.errata.set19;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_001_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					// Frodo's Cloak: "Bearer must be Frodo." Away from sites 1 to 4 of the Towers block: "Skirmish:
					// discard this possession to remove a burden and heal Frodo."
					put("cloak", "4_303");

					put("lemenya", "1_232");      // Ulaire Lemenya, Lieutenant of Morgul: a Nazgul; its text needs a [wraith] condition
					// Ring of Rancor: "Bearer must be a Nazgul. Maneuver: Remove a burden to heal each Nazgul."
					put("rancor", "9_44");
					put("marksman1", "1_176");    // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3, Damage +1 and nothing else
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.MDFrodo,     // Frodo, Resolute Hobbit: strength 3, vitality 4, no text
				"69_1"
		);
	}

	// The Ring on a bearer who is not Frodo, with a Hobbit beside her who does not bear it (and no Frodo: none who
	// may bear the Ring is played beside another Ring-bearer).
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");          // Sam, Son of Hamfast: Hobbit, Ring-bound, strength 3, vitality 4; a fellowship ability only, and a response when Frodo dies
					put("runner1", "1_178");      // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB, // Galadriel, Bearer of Wisdom: strength 3, vitality 3; resistance 4 with the Ring
				"69_1"
		);
	}

	@Test
	public void TheGreatRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: The One Ring, The Great Ring
		 * Unique: True
		 * Side:
		 * Culture:
		 * Twilight Cost: 0
		 * Type: The One Ring
		 * Subtype:
		 * Game Text: While wearing The One Ring, burdens may not be removed and each time bearer is about to take
		 * 	a wound, add a burden instead.
		 * 	<b>Maneuver:</b> Wear The One Ring until the regroup phase.
		 * 	<b>Skirmish:</b> If wearing the One Ring, add a burden to make bearer strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetRing();

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertEquals("The Great Ring", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertNull(card.getBlueprint().getSide());
		assertNull(card.getBlueprint().getCulture());
		assertEquals(CardType.THE_ONE_RING, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(0, card.getBlueprint().getStrength());
		assertEquals(0, card.getBlueprint().getVitality());
	}

	@Test
	public void ManeuverAbilityPutsTheRingOnAtNoCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		// Not an ability of the fellowship phase.
		assertFalse(scn.FreepsActionAvailable(ring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		var pool = scn.GetTwilight();

		assertTrue(scn.FreepsActionAvailable(ring));
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		// The Great Ring gives its bearer no strength: Frodo's printed 3.
		assertEquals(3, scn.GetStrength(frodo));

		scn.FreepsUseCardAction(ring);

		// Worn, and nothing was paid for it: no burden, no exertion, no twilight.
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(pool, scn.GetTwilight());
		assertEquals(3, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedAgainWhileTheRingIsWorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		scn.ShadowPass();

		// The same maneuver phase, the Free Peoples player asked again: wearing a Ring that is worn already would
		// do nothing and costs nothing, so it is not on offer.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
	}

	@Test
	public void TheRingStaysOnThroughTheSkirmishesAndComesOffAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{frodo, savage});

		// One skirmish is fought and over: Aragorn's 8 kills the Runner.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		// In the skirmish after it the Ring is still on.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Frodo's 3 loses to the Savage's 5 (not doubled). Damage +1: two wounds, each a burden instead.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The first decision of the regroup phase: the Ring is off.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void WhileWornEachArcheryWoundOnTheBearerIsABurdenInsteadAndAnotherCompanionsIsAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2", "marksman3");
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		// Three archers: three wounds for the Free Peoples player to place.
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The same bearer again: a burden each time.
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Aragorn does not bear the Ring: his wound is a wound and adds no burden.
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhileNotWornWoundsAreWoundsABurdenCanBeRemovedAndTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var cloak = scn.GetFreepsCard("cloak");
		var marksman = scn.GetShadowCard("marksman1");
		var savage = scn.GetShadowCard("savage");

		scn.AttachCardsTo(frodo, cloak);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(marksman, savage);

		// The maneuver phase goes by and the Ring is not put on.
		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// "Maneuver:" is not the archery phase.
		assertFalse(scn.FreepsActionAvailable(ring));

		// The Marksman's archery wound, placed on Frodo, the only companion: a wound, and no burden.
		scn.SkipToArcheryWounds();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);

		// In a skirmish, with a burden there to add: the skirmish ability asks for the Ring to be worn.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));

		// Frodo's Cloak removes a burden and heals Frodo.
		assertTrue(scn.FreepsActionAvailable(cloak));
		scn.FreepsUseCardAction(cloak);
		assertInZone(Zone.DISCARD, cloak);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Frodo's 3 loses to the Savage's 5 (not doubled). Damage +1: two wounds, and no burden.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileWornAShadowCardCannotRemoveABurdenAsItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetRing();
		var lemenya = scn.GetShadowCard("lemenya");
		var rancor = scn.GetShadowCard("rancor");

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, rancor);
		scn.AddWoundsToChar(lemenya, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		// Before the Ring is worn: Ring of Rancor removes a burden to heal the Nazgul.
		scn.FreepsPass();
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.ShadowActionAvailable(rancor));
		scn.ShadowUseCardAction(rancor);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(lemenya));

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		// The same phase, a burden still there: it cannot be removed, so the cost cannot be paid.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(rancor));
	}

	@Test
	public void WhileWornAFreePeoplesEffectRemovesNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, cloak);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(cloak));

		// Frodo's Cloak: "discard this possession to remove a burden and heal Frodo". The cloak goes and Frodo
		// is healed; the burden stays. (With the Ring off the same cloak takes a burden away: the test above.)
		scn.FreepsUseCardAction(cloak);
		assertInZone(Zone.DISCARD, cloak);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsABurdenToMakeTheBearerStrengthPlus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{frodo, savage});

		// Aragorn's skirmish: the ability is the Ring's and does not ask for its bearer to be fighting.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsActionAvailable(ring));
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(ring);
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(aragorn));

		// Later in the same skirmish the +3 is still there, and a second use adds to it.
		scn.ShadowPass();
		assertEquals(6, scn.GetStrength(frodo));
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);
		assertEquals(2, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(frodo));

		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner);

		// The next skirmish: the bonus ended with the skirmish it was made in.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(2, scn.GetBurdens());

		// Used here it wins the skirmish: 3 + 3 against the Savage's 5.
		scn.FreepsUseCardAction(ring);
		assertEquals(3, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(frodo));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheRingBearerIsHinderedAndNoBurdenCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.FreepsActionAvailable(ring));

		// No burden can be added to a hindered Ring-bearer. The Ring he bears is not itself hindered and is
		// still worn; what is missing is the cost.
		scn.HinderCard(frodo);
		scn.FreepsRefreshDecision();

		assertTrue(scn.RBWearingOneRing());
		assertFalse(scn.IsHindered(ring));
		assertFalse(scn.FreepsActionAvailable(ring));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheBearerIsWhoeverBearsTheRingAndACompanionWhoDoesNotIsWoundedAndNoStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner1}, new PhysicalCardImpl[]{galadriel, runner2});

		// Sam's skirmish. The Ring's ability strengthens Galadriel, who bears it, and not him.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(sam));
		scn.FreepsUseCardAction(ring);
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(galadriel));
		assertEquals(3, scn.GetStrength(sam));

		// His 3 loses to the Runner's 5: a wound on him, and no burden for it.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());

		// Galadriel's 3 loses to the other Runner's 5: a burden instead.
		scn.FreepsResolveSkirmish(galadriel);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
