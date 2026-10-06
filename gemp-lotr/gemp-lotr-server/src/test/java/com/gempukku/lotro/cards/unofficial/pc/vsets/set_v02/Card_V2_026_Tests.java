package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grunt1", "102_26");
					put("grunt2", "102_26");

					// Isengard Worker: an [isengard] Orc, strength 5, vitality 3.  Its regroup ability is only
					// ever used on purpose.
					put("worker1", "3_62");
					put("worker2", "3_62");
					// Isengard Rider: an [isengard] Orc, strength 8, vitality 3, fierce while not exhausted
					put("rider", "5_53");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5, vitality 1

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2, no text once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardGruntStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Isengard Grunt
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Skirmish: Exert this minion to prevent an [isengard] Orc from being overwhelmed unless its strength is tripled.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grunt1");

		assertEquals("Isengard Grunt", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandForTwoAndTwoMoreForRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt1");
		scn.MoveCardsToHand(grunt);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 for Frodo + 2 for the site: exactly the 2 to play and the 2 for roaming at site 2
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(grunt));

		scn.ShadowPlayCard(grunt);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, grunt);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsThisGruntSoThatAnIsengardOrcOneShortOfTripledIsWoundedAndNotOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt1 = scn.GetShadowCard("grunt1");
		var grunt2 = scn.GetShadowCard("grunt2");
		var worker1 = scn.GetShadowCard("worker1");
		var worker2 = scn.GetShadowCard("worker2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt1, grunt2, worker1, worker2);
		// Both companions at 14: more than double an Isengard Worker's 5 and one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 6));
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 10));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, worker1}, new PhysicalCardImpl[]{guard, worker2});
		// Neither Grunt is in a skirmish.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(worker1));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(grunt1));
		assertEquals(0, scn.GetWoundsOn(grunt2));
		assertEquals(0, scn.GetWoundsOn(worker1));
		assertTrue(scn.ShadowActionAvailable(grunt1));

		scn.ShadowUseCardAction(grunt1);
		// The Grunt that was used is the one that exerts.
		assertEquals(1, scn.GetWoundsOn(grunt1));
		assertEquals(0, scn.GetWoundsOn(grunt2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		// The Worker loses, takes the wound for losing, and is still there.
		assertInZone(Zone.SHADOW_CHARACTERS, worker1);
		assertEquals(1, scn.GetWoundsOn(worker1));

		// The other Worker against the same strength, with no Grunt doing anything for it, is overwhelmed.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(14, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(worker2));
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DISCARD, worker2);
		assertEquals(1, scn.GetWoundsOn(grunt1));
		assertEquals(0, scn.GetWoundsOn(grunt2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnIsengardOrcFacingTripleItsStrengthIsOverwhelmedThoughBothGruntsExertForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt1 = scn.GetShadowCard("grunt1");
		var grunt2 = scn.GetShadowCard("grunt2");
		var worker = scn.GetShadowCard("worker1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt1, grunt2, worker);
		// 8 + 7 = 15: exactly triple the Worker's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 7));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, worker);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(15, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(worker));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(grunt1));
		assertEquals(0, scn.GetWoundsOn(grunt2));

		scn.ShadowUseCardAction(grunt1);
		assertEquals(1, scn.GetWoundsOn(grunt1));
		scn.FreepsPass();
		// A second Grunt exerting for the same Orc does not ask for more than triple.
		assertTrue(scn.ShadowActionAvailable(grunt2));
		scn.ShadowUseCardAction(grunt2);
		assertEquals(1, scn.GetWoundsOn(grunt2));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertInZone(Zone.SHADOW_CHARACTERS, worker);

		scn.PassCurrentPhaseActions();
		// No wound killed it: it had none, and has 3 vitality.
		assertInZone(Zone.DISCARD, worker);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGruntProtectingItselfBesideAnUrukHaiIsOverwhelmedWithItAtDoubleTheirTotalAndTripleItsOwnStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt, savage);
		// 8 + 12 = 20: exactly double the 5 + 5 of the Grunt and the Savage, and more than three times the
		// Grunt's own 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, grunt);
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(grunt));
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(grunt));

		// The Grunt is the one [isengard] Orc in the skirmish, so it is the Orc protected.
		scn.ShadowUseCardAction(grunt);
		assertEquals(1, scn.GetWoundsOn(grunt));
		assertEquals(3, scn.GetOverwhelmMultiplier(grunt));
		assertEquals(2, scn.GetOverwhelmMultiplier(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, grunt, savage);

		scn.PassCurrentPhaseActions();
		// The Savage is overwhelmed at double as ever.  The Grunt, on 1 wound of its 3 vitality, could not have
		// died of the wound for losing: it is overwhelmed too.
		assertInZone(Zone.DISCARD, grunt, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGruntProtectingItselfBesideAnUrukHaiIsSparedUnderTripleItsOwnStrengthAndTheUrukHaiIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt1");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt, savage);
		// The Grunt made 13, so that double the side's 13 + 5 is under three times the Grunt's own strength.
		scn.ApplyAdHocModifier(new StrengthModifier(null, grunt, null, 8));
		// 8 + 30 = 38: more than double the side's 18, and one short of three times the Grunt's 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 30));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, grunt);
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(38, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(grunt));
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(grunt));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.ShadowUseCardAction(grunt);
		assertEquals(1, scn.GetWoundsOn(grunt));
		assertEquals(3, scn.GetOverwhelmMultiplier(grunt));
		assertEquals(2, scn.GetOverwhelmMultiplier(savage));

		scn.PassCurrentPhaseActions();
		// The protection is the Orc's alone: the Uruk-hai beside it, with 3 vitality and no wound, is overwhelmed.
		assertInZone(Zone.DISCARD, savage);
		// The Grunt has lost the skirmish and no more: 1 wound for exerting and 1 for losing.
		assertInZone(Zone.SHADOW_CHARACTERS, grunt);
		assertEquals(2, scn.GetWoundsOn(grunt));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedToAnExhaustedGruntNorInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt1 = scn.GetShadowCard("grunt1");
		var grunt2 = scn.GetShadowCard("grunt2");
		var worker = scn.GetShadowCard("worker1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt1, grunt2, worker);
		// Vitality 3 with 2 wounds: exhausted, so it cannot exert.
		scn.AddWoundsToChar(grunt1, 2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grunt1));
		assertFalse(scn.ShadowActionAvailable(grunt2));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, worker);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsExhausted(grunt1));
		assertFalse(scn.ShadowActionAvailable(grunt1));
		assertTrue(scn.ShadowActionAvailable(grunt2));
		assertEquals(2, scn.GetWoundsOn(grunt1));
		assertEquals(0, scn.GetWoundsOn(grunt2));
	}

	@Test
	public void OffersEachIsengardOrcInTheSkirmishItselfIncludedAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt1 = scn.GetShadowCard("grunt1");
		var grunt2 = scn.GetShadowCard("grunt2");
		var worker = scn.GetShadowCard("worker1");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt1, grunt2, worker, savage, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// All five against Aragorn: three [isengard] Orcs, an [isengard] Uruk-hai and a [moria] Orc.
		scn.FreepsAssignToMinions(aragorn, worker);
		scn.ShadowAssignToMinions(aragorn, grunt1, grunt2, savage, runner);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(grunt1));

		scn.ShadowUseCardAction(grunt1);
		assertEquals(1, scn.GetWoundsOn(grunt1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(grunt1, grunt2, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(grunt2);
		assertEquals(3, scn.GetOverwhelmMultiplier(grunt2));
		assertEquals(2, scn.GetOverwhelmMultiplier(grunt1));
		assertEquals(2, scn.GetOverwhelmMultiplier(worker));
		assertEquals(2, scn.GetOverwhelmMultiplier(savage));
		assertEquals(2, scn.GetOverwhelmMultiplier(runner));
		assertEquals(1, scn.GetWoundsOn(grunt1));
		assertEquals(0, scn.GetWoundsOn(grunt2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoIsengardOrcInTheSkirmishTheGruntStillExertsAndNobodyIsProtected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt1");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt, savage, runner);
		// 8 + 12 = 20: exactly double the 5 + 5 of an [isengard] Uruk-hai and a [moria] Orc together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(aragorn));
		assertFalse(scn.IsCharSkirmishing(grunt));
		scn.FreepsPass();
		// The ability has a cost and nothing else to ask for, so it is there to be used.
		assertTrue(scn.ShadowActionAvailable(grunt));
		assertEquals(0, scn.GetWoundsOn(grunt));

		scn.ShadowUseCardAction(grunt);
		assertEquals(1, scn.GetWoundsOn(grunt));
		// Neither minion in the skirmish is an [isengard] Orc: no choice is put and neither is protected.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetOverwhelmMultiplier(savage));
		assertEquals(2, scn.GetOverwhelmMultiplier(runner));

		scn.PassCurrentPhaseActions();
		// The Savage has 3 vitality and no wound could have killed it.
		assertInZone(Zone.DISCARD, savage, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, grunt);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ProtectionIsGoneInTheFierceSkirmishThatFollows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grunt = scn.GetShadowCard("grunt1");
		var rider = scn.GetShadowCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grunt, rider);
		// 8 + 8 = 16: exactly double the Rider's 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 8));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(16, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(rider));
		assertEquals(2, scn.GetOverwhelmMultiplier(rider));
		assertEquals(0, scn.GetWoundsOn(grunt));
		assertEquals(0, scn.GetWoundsOn(rider));
		scn.FreepsPass();

		scn.ShadowUseCardAction(grunt);
		assertEquals(1, scn.GetWoundsOn(grunt));
		assertEquals(3, scn.GetOverwhelmMultiplier(rider));

		scn.PassCurrentPhaseActions();
		// The protection is still there when the skirmish resolves: the Rider loses and takes a wound.
		assertInZone(Zone.SHADOW_CHARACTERS, rider);
		assertEquals(1, scn.GetWoundsOn(rider));

		// The Rider is fierce while not exhausted, and meets Aragorn again with nothing done for it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(2, scn.GetOverwhelmMultiplier(rider));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, rider);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(16, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(rider));
		scn.PassCurrentPhaseActions();
		// Had the protection lasted, a second wound would have left it alive on its vitality of 3.
		assertInZone(Zone.DISCARD, rider);
		assertEquals(1, scn.GetWoundsOn(grunt));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
