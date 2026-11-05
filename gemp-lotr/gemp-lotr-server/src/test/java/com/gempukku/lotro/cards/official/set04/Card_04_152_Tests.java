package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_152_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getback", "4_152");
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai tracker, fierce, strength 5; a response to an unbound Hobbit killed
					put("scout", "2_47");       // Uruk Scout: [isengard] Uruk-hai tracker, not fierce; a response to an event that spots a ranger
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai who is no tracker, strength 5; damage +1 only
					put("band", "1_270");       // Orc Scouting Band: [sauron] Orc tracker; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Get Back
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: <b>Assignment:</b> Prevent an [isengard] tracker from being assigned to a skirmish by any player.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("getback");

		assertEquals("Get Back", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheIsengardTrackerChosenCannotBeAssignedByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getback = scn.GetShadowCard("getback");
		var pursuer = scn.GetShadowCard("pursuer");
		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		var band = scn.GetShadowCard("band");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(getback);
		assertInHand(getback);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, scout, savage, band);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(getback));
		scn.ShadowPlayCard(getback);

		// [isengard] trackers, fierce and not, are offered; an [isengard] minion who is no tracker and a [sauron]
		// tracker are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pursuer, scout));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, band));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(pursuer);
		assertInZone(Zone.DISCARD, getback);

		scn.PassAssignmentActions();
		// The Free Peoples player may not assign the Pursuer, and may assign every other minion.
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(scout));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(band));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, band});

		// "By any player": the Shadow player may not assign the Pursuer either, though the Scout is his to assign.
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(pursuer));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(scout));
		scn.ShadowAssignToMinions(aragorn, scout);

		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, scout));
		assertTrue(scn.IsCharAssignedAgainst(gimli, band));
		assertFalse(scn.IsCharAssigned(pursuer));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ThePreventionLastsTheAssignmentPhaseAndTheFierceTrackerIsAssignedInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getback = scn.GetShadowCard("getback");
		var pursuer = scn.GetShadowCard("pursuer");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(getback);
		assertInHand(getback);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// The Pursuer is the one [isengard] tracker: chosen with no question.
		scn.ShadowPlayCard(getback);
		assertInZone(Zone.DISCARD, getback);

		scn.PassAssignmentActions();
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		scn.FreepsAssignToMinions(aragorn, savage);
		assertFalse(scn.IsCharAssigned(pursuer));

		// Strength 8 against 5: Aragorn wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The fierce assignment phase is a phase of its own: the Pursuer, fierce, is offered again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		scn.FreepsAssignToMinions(aragorn, pursuer);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, pursuer));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WithNoIsengardTrackerInPlayTheEventIsStillPlayedAndPreventsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getback = scn.GetShadowCard("getback");
		var savage = scn.GetShadowCard("savage");
		var band = scn.GetShadowCard("band");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(getback);
		assertInHand(getback);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, band);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(getback));
		scn.ShadowPlayCard(getback);
		assertInZone(Zone.DISCARD, getback);

		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(savage));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(band));
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, band});
		assertTrue(scn.IsCharAssignedAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssignedAgainst(gimli, band));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getback = scn.GetShadowCard("getback");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(getback);
		assertInHand(getback);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInHand(getback);
		assertFalse(scn.ShadowPlayAvailable(getback));
	}
}
