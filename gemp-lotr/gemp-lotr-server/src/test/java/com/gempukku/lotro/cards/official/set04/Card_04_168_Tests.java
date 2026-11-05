package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("race", "4_168");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, strength 8, vitality 2; Damage +1 is its whole text
					put("worker", "3_62");      // Isengard Worker: an [isengard] Orc, strength 5, vitality 3; a regroup ability

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; Damage +1, a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A format in which a skirmish involving the Ring-bearer may be cancelled.  The default one does not allow it.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("race", "4_168");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void RaceAcrosstheMarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Race Across the Mark
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Spot a site you control and remove (2) to cancel a skirmish involving an Uruk-hai.
		*/

		var scn = GetScenario();

		var race = scn.GetFreepsCard("race");

		assertEquals("Race Across the Mark", race.getBlueprint().getTitle());
		assertNull(race.getBlueprint().getSubtitle());
		assertFalse(race.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, race.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, race.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, race.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(race, Keyword.SUPPORT_AREA));
		assertEquals(0, race.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithASiteItsOwnerControlsItRemovesTwoToCancelASkirmishInvolvingAnUrukHaiOfEitherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetShadowCard("race");
		scn.MoveCardsToSupportArea(race);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var savage = scn.GetShadowCard("savage");
		var sentry = scn.GetShadowCard("sentry");

		scn.StartGame();
		// Both markers at site 2, so site 1 may be taken.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, sentry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gimli, sentry});
		scn.SetTwilight(4);

		// Aragorn against an [isengard] Uruk-hai.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(race));

		scn.ShadowUseCardAction(race);
		assertEquals(2, scn.GetTwilight());
		// The skirmish is over with nobody having won or lost it.
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage));
		// Gimli's is untouched.
		assertTrue(scn.IsCharAssignedAgainst(gimli, sentry));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli against a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, sentry));
		assertTrue(scn.ShadowActionAvailable(race));

		scn.ShadowUseCardAction(race);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsCharSkirmishing(sentry));
		assertFalse(scn.IsCharAssigned(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileOnlyTheOtherPlayerControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetShadowCard("race");
		scn.MoveCardsToSupportArea(race);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is controlled, but by the Free Peoples player, as he might have taken it on a turn of his as the
		// Shadow player.
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		assertFalse(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(race));

		// The skirmish is fought: Aragorn wins, 8 against 5.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetShadowCard("race");
		scn.MoveCardsToSupportArea(race);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(race));

		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishWithNoUrukHaiTheTwilightIsRemovedAndNothingIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetShadowCard("race");
		scn.MoveCardsToSupportArea(race);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, worker);
		scn.SkipToAssignments();
		// An Uruk-hai is assigned to Aragorn; Gimli's skirmish with an [isengard] Orc is fought first.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gimli, worker});
		scn.SetTwilight(4);
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(gimli, worker));
		assertEquals(4, scn.GetTwilight());
		// An ability with a cost is offered whenever the cost can be paid.
		assertTrue(scn.ShadowActionAvailable(race));

		scn.ShadowUseCardAction(race);
		assertEquals(2, scn.GetTwilight());
		// Nothing is cancelled: not this skirmish, and not the Uruk-hai's, which has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, worker));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli wins, 6 against 5: one wound and one more for his damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(worker));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void InFellowshipBlockCancelsASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var race = scn.GetShadowCard("race");
		scn.MoveCardsToSupportArea(race);

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(race));

		scn.ShadowUseCardAction(race);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsCharSkirmishing(savage));
		assertFalse(scn.IsCharAssigned(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathPaysItsCostAndDoesNotCancelASkirmishTheRingBearerIsIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var race = scn.GetShadowCard("race");
		scn.MoveCardsToSupportArea(race);

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(2, scn.GetTwilight());
		// The ability has a cost that can be paid, so it is offered; its cancel is what the format refuses.
		assertTrue(scn.ShadowActionAvailable(race));

		scn.ShadowUseCardAction(race);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The skirmish is fought and has a loser: Frodo, 3 + 1 for the Ring against 5.  He takes one wound and
		// one more for the Savage's damage +1, and is offered the Ring for each.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
