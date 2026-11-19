package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_161_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wariness", "1_161");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard (the culture without the race); each companion
					// is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");

					// Hobbit Stealth: [shire] Skirmish event, Stealth; "At sites 1 to 5, cancel a skirmish involving a Hobbit"
					put("stealth", "1_298");
					// Depart Silently: [gandalf] Maneuver event, Stealth; "At sites 1 to 5, spot Gandalf to prevent Hobbits
					// from being assigned to skirmishes until the regroup phase"
					put("depart", "3_31");
					// Halfling Deftness: [shire] Skirmish event with no Stealth; "Exert a Hobbit to make him strength +3"
					put("deftness", "1_293");
					// Nice Imitation: [shire] condition, Stealth, plays to the support area; a response to a Hobbit's
					// wound, which nothing here sets off
					put("imitation", "2_106");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, strength 3, vitality 4; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Depart Silently's spot; a strength modifier only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarinessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Wariness
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Search</b>.
		 * <b>Response:</b> If a stealth event is played, exert or discard your Uruk-hai to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wariness");

		assertEquals("Wariness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsOneUrukHaiToCancelAStealthEventAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wariness = scn.GetShadowCard("wariness");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(wariness, stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, worker, saruman);
		assertInHand(wariness, stealth);

		// Site 2.  Merry against the Savage; the Guard, the Worker and Saruman against Aragorn.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage}, new PhysicalCardImpl[]{aragorn, worker});
		scn.ShadowAssignToMinions(aragorn, guard, saruman);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsPlayCard(stealth);
		assertTrue(scn.ShadowPlayAvailable(wariness));

		scn.ShadowPlayCard(wariness);
		// Both halves of the cost can be paid, and the Shadow player is asked which.
		assertEquals(List.of("Exert your Uruk-hai", "Discard your Uruk-hai"), scn.ShadowGetMultipleChoices());
		scn.ShadowChooseOption("Exert");

		// An Uruk-hai of [isengard] (in this skirmish) and one of [sauron] (in another); neither the [isengard]
		// Orc nor the [uruk-hai] Wizard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		// One Uruk-hai exerts, and no other minion (CR 4.2: "This card exerts or discards one of your Uruk-hai").
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInDiscard(stealth, wariness);
		// Hobbit Stealth is cancelled: Merry's skirmish is still being fought.
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPass();
		// 5 against 3: Merry loses, 1 + damage +1.
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DiscardsOneUrukHaiToCancelAStealthEventOfAnotherCultureAndPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wariness = scn.GetShadowCard("wariness");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var depart = scn.GetFreepsCard("depart");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, gandalf);
		scn.MoveCardsToHand(wariness, depart);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard);
		assertInHand(wariness, depart);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Site 2: Depart Silently would keep every Hobbit from being assigned until the regroup phase.
		scn.FreepsPlayCard(depart);
		assertTrue(scn.ShadowPlayAvailable(wariness));

		scn.ShadowPlayCard(wariness);
		assertTrue(scn.ShadowChoiceAvailable("Exert"));
		assertTrue(scn.ShadowChoiceAvailable("Discard"));
		scn.ShadowChooseOption("Discard");
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		scn.ShadowChooseCard(guard);

		assertInDiscard(guard, depart, wariness);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The event is cancelled: Merry can be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		assertTrue(scn.IsCharAssignedAgainst(merry, savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void WithItsOnlyUrukHaiExhaustedItIsDiscardedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wariness = scn.GetShadowCard("wariness");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(wariness, stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		// Vitality 3 with 2 wounds: exhausted, so it cannot exert, and the Worker beside it is no Uruk-hai.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInHand(wariness, stealth);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, worker);
		scn.ShadowAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsPlayCard(stealth);
		assertTrue(scn.ShadowPlayAvailable(wariness));

		scn.ShadowPlayCard(wariness);
		// Only the discard can be paid in full: it is paid with no question.
		assertInDiscard(savage, stealth, wariness);
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.IsCharSkirmishingAgainst(merry, worker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedForAnEventThatIsNoStealthEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wariness = scn.GetShadowCard("wariness");
		var savage = scn.GetShadowCard("savage");
		var deftness = scn.GetFreepsCard("deftness");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(wariness, deftness);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInHand(wariness, deftness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));

		// A [shire] skirmish event with no Stealth: carried out at once, and the Shadow player is asked for
		// skirmish actions with Wariness not among them.
		scn.FreepsPlayCard(deftness);
		// Frodo is a Hobbit too: Merry is chosen to exert.
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(6, scn.GetStrength(merry));
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wariness));
		assertInHand(wariness);
		assertEquals(0, scn.GetWoundsOn(savage));
	}

	@Test
	public void NotOfferedForAStealthCardThatIsNoEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wariness = scn.GetShadowCard("wariness");
		var savage = scn.GetShadowCard("savage");
		var imitation = scn.GetFreepsCard("imitation");
		scn.MoveCardsToHand(wariness, imitation);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.FreepsRefreshDecision();
		assertInHand(wariness, imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Stealth condition: it goes to the support area and the Free Peoples player is asked for his next
		// fellowship action; the Shadow player, holding Wariness and an Uruk-hai able to pay, was asked nothing.
		scn.FreepsPlayCard(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(wariness);
		assertEquals(0, scn.GetWoundsOn(savage));
	}

	@Test
	public void NotOfferedWithNoUrukHaiInPlayAndTheStealthEventIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wariness = scn.GetShadowCard("wariness");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(wariness, stealth);

		scn.StartGame();
		// An [isengard] Orc and an [uruk-hai] Wizard, each able to exert: neither is an Uruk-hai.
		scn.MoveMinionsToTable(worker, saruman);
		assertInHand(wariness, stealth);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, worker);
		scn.ShadowAssignToMinions(merry, saruman);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));

		// Hobbit Stealth cancels Merry's skirmish: nobody is wounded and the turn goes on to the regroup phase.
		scn.FreepsPlayCard(stealth);
		assertInDiscard(stealth);
		assertInHand(wariness);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
