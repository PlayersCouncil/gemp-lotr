package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "103_85");
					put("eowyn", "5_122");
					put("rider1", "4_286");
					put("rider2", "4_286");
					put("rider3", "4_286");
					put("mount", "4_287");
					put("aragorn", "1_89");

					put("runner", "1_178");
					put("soldier", "1_271");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Merry, Master Holbytla
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Valiant. Enduring.
		* 	While you can spot 3 [rohan] companions (or Eowyn), Merry is considered a [Rohan] Man.
		* 	Each time another [rohan] companion exerts, you may exert Merry to heal that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Master Holbytla", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet()); 
	}


// ======== RACE MODIFIER TESTS ========

	@Test
	public void MerryIsNotManWithoutConditionMet() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		// Merry + 1 Rider = 2 Rohan companions (not enough for 3)
		scn.MoveCompanionsToTable(merry, rider1);

		scn.StartGame();

		// Merry should be Hobbit but NOT Man
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
	}

	@Test
	public void MerryIsManWhenSpottingEowyn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(merry, eowyn);

		scn.StartGame();

		// With Eowyn spotted, Merry should be both Hobbit AND Man
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertTrue(scn.IsRace(merry, Race.MAN));
	}

	@Test
	public void MerryIsManWhenSpotting3RohanCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		// Merry (Rohan Hobbit) + 2 Riders (Rohan Men) = 3 Rohan companions
		scn.MoveCompanionsToTable(merry, rider1, rider2);

		scn.StartGame();

		assertTrue(scn.IsRace(merry, Race.MAN));
	}

	@Test
	public void MerryLosesManRaceWhenConditionNoLongerMet() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(merry, eowyn);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// With Eowyn, Merry is a Man
		assertTrue(scn.IsRace(merry, Race.MAN));

		// Wound Eowyn to death (4 vitality)
		scn.AddWoundsToChar(eowyn, 4);
		scn.FreepsPass();

		// Without Eowyn (and < 3 Rohan companions), Merry is no longer a Man
		assertFalse(scn.IsRace(merry, Race.MAN));
	}

// ======== EXERTION TRIGGER TESTS ========

	@Test
	public void MerryTriggerFiresWhenAnotherRohanManExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var mount = scn.GetFreepsCard("mount");

		// Rider1 is mounted, Rider2 will skirmish
		scn.MoveCompanionsToTable(merry, rider1, rider2);
		scn.AttachCardsTo(rider1, mount);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// Assign Rider2 to Runner, leaving Rider1 unassigned
		scn.FreepsAssignToMinions(rider2, runner);
		scn.ShadowDeclineAssignments();

		// Choose Rider2's skirmish to resolve
		scn.FreepsResolveSkirmish(rider2);

		// During skirmish, Rider1 (mounted, not in skirmish) can use ability
		// Rider's ability: "If mounted, exert to exert a companion skirmishing an unbound companion"
		// Rider2 is skirmishing and unbound, so valid target
		assertTrue(scn.FreepsActionAvailable(rider1));

		assertEquals(0, scn.GetWoundsOn(rider1));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsUseCardAction(rider1);
		// Cost: Rider1 exerts (becomes 1 wound)

		// Merry's trigger should fire: "another Rohan Man exerted"
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		// Merry exerts (cost), Rider1 heals (effect)

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(rider1));
	}

	@Test
	public void MerryTriggerDoesNotFireForNonRohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Eowyn makes Merry a Man, Aragorn is a Gondor Man (not Rohan)
		scn.MoveCompanionsToTable(merry, eowyn, aragorn);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Need to cause Aragorn to exert here via game mechanics
		// For now, this test documents the expected behavior:
		// When Aragorn (Gondor Man, not Rohan) exerts, Merry's trigger should NOT fire

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsUseCardAction(aragorn);

		// Merry's trigger should NOT be available since Aragorn is not Rohan
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
	}

	@Test
	public void MerryTriggerDoesNotFireWhenMerryHimselfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup - Merry must be a Man for this to be relevant
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider1 = scn.GetFreepsCard("rider1");
		var mount = scn.GetFreepsCard("mount");

		scn.MoveCompanionsToTable(merry, eowyn, rider1);
		scn.AttachCardsTo(rider1, mount);
		scn.AddWoundsToChar(rider1, 1); // Pre-wound so Rider can be healed

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Verify Merry is a Man (due to Eowyn)
		assertTrue(scn.IsRace(merry, Race.MAN));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(rider1, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider1);

		// During Rider's skirmish, cause Rider to exert which triggers Merry
		assertTrue(scn.FreepsActionAvailable(rider1));
		scn.FreepsUseCardAction(rider1);

		// First trigger: Rider1 exerted
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		// Now Merry has exerted - but Merry exerting should NOT trigger another prompt
		// because the trigger says "another Rohan Man" - Merry is not "another" relative to himself
		assertEquals(1, scn.GetWoundsOn(merry));

		// There should be no second trigger from Merry's own exertion
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

// ======== SKIRMISH ABILITY TESTS ========

	@Test
	public void MerrySkirmishAbilityNotAvailableWhenAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// Assign Merry to Runner
		scn.FreepsAssignToMinions(merry, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(merry);

		// Merry's skirmish ability requires "self,notAssignedToSkirmish"
		// Since Merry IS assigned, ability should not be available
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void MerrySkirmishAbilityAvailableWhenNotAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry, rider1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// Assign Rider (not Merry) to Runner
		scn.FreepsAssignToMinions(rider1, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider1);

		// Merry is not assigned, so ability should be available
		assertTrue(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void MerrySkirmishAbilityHindersMerryAndEndsSkirmishWithRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var soldier = scn.GetShadowCard("soldier");

		scn.MoveCompanionsToTable(merry, rider1);
		scn.MoveMinionsToTable(soldier);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// Assign Rider (Rohan companion) to Runner
		scn.FreepsAssignAndResolve(rider1, soldier);

		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.IsCharSkirmishing(rider1));

		// Use Merry's ability
		scn.FreepsUseCardAction(merry);

		// Merry should be hindered (cost)
		assertTrue(scn.IsHindered(merry));

		// Skirmish should have ended (effect) because Rider is a Rohan companion
		//assertFalse(scn.IsCharSkirmishing(rider1));
		// However because Gemp is Gemp, the decisions are still presented, they've just been emptied
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(soldier)); //Normally
	}

	@Test
	public void MerrySkirmishAbilityHindersMerryButDoesNotEndSkirmishWithoutRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();

		// Assign Aragorn (Gondor, NOT Rohan) to Runner
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.IsCharSkirmishing(aragorn));

		// Use Merry's ability
		scn.FreepsUseCardAction(merry);

		// Merry should be hindered (cost paid)
		assertTrue(scn.IsHindered(merry));

		// Skirmish should NOT have ended (effect fizzles - no Rohan companion in skirmish)
		assertTrue(scn.IsCharSkirmishing(aragorn));
	}
}
