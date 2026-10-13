package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "2_22");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength +1 for each race spotted in the fellowship
					put("ent", "6_27");         // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose

					put("sleep", "1_84");       // Sleep, Caradhras: [gandalf] spell event, twilight 3
					put("bird", "17_23");       // Scintillating Bird: [gandalf] spell condition, twilight 1
					put("walkers", "1_79");     // The Nine Walkers: [gandalf] condition that is no spell, twilight 1
					put("chill", "1_134");      // Saruman's Chill: [isengard] spell condition, twilight 1; to play, exert an [isengard] minion
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, vitality 3, damage +1 and nothing else

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gandalf, Bearer of Obligation 13_33 may be the Ring-bearer: a table for each side of the format rule on
	// cancelling a skirmish the Ring-bearer is in.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "2_22");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				"13_33",
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "2_22");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				"13_33",
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Gandalf's Staff
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: +1
		 * Game Text: Bearer must be Gandalf.
		 * The twilight cost of each [gandalf] spell is -1.
		 * <b>Skirmish:</b> Exert Gandalf twice to cancel a skirmish involving him.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CanBePlayedOnlyOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(ent);
		scn.MoveCardsToHand(staff);

		scn.StartGame();

		// A [gandalf] companion who is not Gandalf, and the Ring-bearer, cannot bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(staff));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(staff));

		scn.FreepsPlayCard(staff);
		assertAttachedTo(staff, gandalf);
		assertNotAttachedTo(staff, ent);
		assertNotAttachedTo(staff, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachGandalfSpellCostsOneLessAndNoOtherCardDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var bird = scn.GetFreepsCard("bird");
		var walkers = scn.GetFreepsCard("walkers");
		var chill = scn.GetShadowCard("chill");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(sleep, bird, walkers);
		scn.MoveCardsToShadowHand("chill");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		assertEquals(0, scn.GetTwilight());
		// A spell event: 3 - 1.
		scn.FreepsPlayCard(sleep);
		assertEquals(2, scn.GetTwilight());
		// A spell condition: 1 - 1.
		scn.FreepsPlayCard(bird);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, bird);
		// A [gandalf] condition that is no spell pays its printed 1.
		scn.FreepsPlayCard(walkers);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		int before = scn.GetTwilight();
		// A spell of another culture pays its printed 1.
		scn.ShadowPlayCard(chill);
		// It plays on a site: sites 1 and 2 are both in play.
		scn.ShadowChooseCard(scn.GetCurrentSite());
		assertEquals(before - 1, scn.GetTwilight());
		assertAttachedTo(chill, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionExertsGandalfTwiceToCancelHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 4 + 1 from the staff: with 2 wounds he can still exert twice.
		scn.AddWoundsToChar(gandalf, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner));
		assertTrue(scn.FreepsActionAvailable(staff));
		scn.FreepsUseCardAction(staff);

		assertEquals(4, scn.GetWoundsOn(gandalf));
		// The skirmish is over with no winner: Gandalf (6, +1 for the Hobbit Ring-bearer) would have killed the Runner.
		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertFalse(scn.IsCharSkirmishing(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertAttachedTo(staff, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Vitality 4 + 1 from the staff: with 3 wounds he has 2 left and can exert only once.
		scn.AddWoundsToChar(gandalf, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(staff));
		assertEquals(3, scn.GetWoundsOn(gandalf));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishGandalfExertsAndThatSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));
		assertFalse(scn.IsCharSkirmishing(gandalf));
		// The exertions can be paid, so it is offered; the skirmish it would cancel must involve Gandalf.
		assertTrue(scn.FreepsActionAvailable(staff));
		scn.FreepsUseCardAction(staff);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner));

		scn.PassSkirmishActions();
		// Aragorn (8) beats the Runner (5, vitality 1), which is killed.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsASkirmishOfAGandalfWhoIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsActionAvailable(staff));
		scn.FreepsUseCardAction(staff);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		// Cancelled: Gandalf (7, +1 from the Ring) would have killed the Runner.
		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathASkirmishOfAGandalfWhoIsTheRingBearerIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(gandalf, staff);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// The exertions can be paid, so it is offered; the skirmish is the Ring-bearer's and is not cancelled.
		assertTrue(scn.FreepsActionAvailable(staff));
		scn.FreepsUseCardAction(staff);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner));

		scn.PassSkirmishActions();
		// Gandalf (7, +1 from the Ring) beats the Runner (5, vitality 1), which is killed.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
