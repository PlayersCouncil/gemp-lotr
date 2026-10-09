package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_027_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sentback1", "59_27");
					put("sentback2", "59_27");
					put("grey", "1_72");       // Gandalf, Friend of the Shirefolk: Wizard, cost 4, strength 6 (+1 for each of four races in the fellowship)
					put("white", "7_36");      // Gandalf, Defender of the West: Wizard, cost 4, strength 9 (-2 without 3 companions of one culture)
					put("radagast", "59_26");  // Radagast, The Brown: the other Wizard companion, cost 4; move limit +1, and the opponent may discard 2 to draw 2 on a move
					put("ent", "6_27");        // Ent Avenger: a [gandalf] companion who is no Wizard, cost 4, strength 6, vitality 4

					put("saruman", "3_69");    // Saruman, Servant of the Eye: an [isengard] Wizard minion, cost 4; may not be assigned to a skirmish
					put("staff", "4_174");     // Saruman's Staff: "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup phase."
					put("runner1", "1_178");   // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SentBackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Sent Back
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Skirmish:</b> Discard this condition to discard each minion skirmishing your Wizard. Place that Wizard in your dead pile.
		 * 	<b>Fellowship</b>: Play a Wizard (even if another copy of that Wizard is in your dead pile).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sentback1");

		assertEquals("Sent Back", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedFromHandForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		scn.MoveCardsToHand(sentback);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(sentback));
		scn.FreepsPlayCard(sentback);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sentback);
		assertNotAttachedTo(sentback, scn.GetRingBearer());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItselfAndEachMinionSkirmishingYourWizardAndPlacesHimInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback1 = scn.GetFreepsCard("sentback1");
		var sentback2 = scn.GetFreepsCard("sentback2");
		var gandalf = scn.GetFreepsCard("grey");
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToSupportArea(sentback1, sentback2);
		scn.MoveCompanionsToTable(gandalf, radagast);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, savage);

		// Not in the assignment phase, the last in which the Free Peoples player acts before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sentback1));
		scn.PassAssignmentActions();

		// Gandalf fights an Orc and an Uruk-hai; another Orc waits for Radagast, the other Wizard.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{radagast, runner2});
		scn.ShadowAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);

		assertInZone(Zone.SUPPORT, sentback1, sentback2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1, runner2, savage);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, radagast);
		assertTrue(scn.FreepsActionAvailable(sentback1));
		assertTrue(scn.FreepsActionAvailable(sentback2));
		scn.FreepsUseCardAction(sentback1);

		// The copy used, and not the other.
		assertInDiscard(sentback1);
		assertInZone(Zone.SUPPORT, sentback2);
		assertInDiscard(runner1, savage);
		assertInZone(Zone.DEAD, gandalf);
		// The Wizard whose skirmish has not begun, and the minion assigned to him, are left alone.
		assertInZone(Zone.FREE_CHARACTERS, radagast);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertTrue(scn.IsCharAssignedAgainst(radagast, runner2));

		// Nobody is left in Gandalf's skirmish: on to the next.
		scn.FreepsResolveSkirmish(radagast);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SarumanSkirmishingYourWizardIsDiscardedAsAMinionAndIsNotTheWizardSentBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var gandalf = scn.GetFreepsCard("grey");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCompanionsToTable(gandalf);

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, runner1);
		scn.AttachCardsTo(saruman, staff);

		// The Staff lets Saruman be assigned to a skirmish this turn.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.ShadowAssignToMinions(gandalf, saruman);
		scn.FreepsResolveSkirmish(gandalf);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman, runner1);
		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);

		// Two Wizards in the skirmish and one of them yours: no choice is put, and Saruman goes as a minion does.
		assertInDiscard(sentback);
		assertInDiscard(saruman, runner1, staff);
		assertInZone(Zone.DEAD, gandalf);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MinionsSkirmishingAGandalfCompanionWhoIsNoWizardAreNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var ent = scn.GetFreepsCard("ent");
		var gandalf = scn.GetFreepsCard("grey");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCompanionsToTable(ent, gandalf);

		var runner1 = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, savage);

		scn.SkipToAssignments();
		// Gandalf himself is assigned to nobody.
		scn.FreepsAssignToMinions(ent, runner1);
		scn.ShadowAssignToMinions(ent, savage);
		scn.FreepsResolveSkirmish(ent);

		// A cost that can be paid is on offer, though it will buy nothing.
		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);

		assertInDiscard(sentback);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1, savage);
		assertInZone(Zone.FREE_CHARACTERS, ent, gandalf);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlacingTheWizardInTheDeadPileKillsHimSoThreatsBecomeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var gandalf = scn.GetFreepsCard("grey");
		var ent = scn.GetFreepsCard("ent");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCompanionsToTable(gandalf, ent);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.AddThreats(2);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, runner1);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(ent));
		scn.FreepsUseCardAction(sentback);

		// A death, not a discard: the threats come off the dead pile as wounds on the companions left.
		assertInZone(Zone.DEAD, gandalf);
		scn.FreepsChooseCard(ent);
		scn.FreepsChooseCard(ent);
		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(ent));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipAbilityPlaysAWizardFromHandAtHisCostAndStaysInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var gandalf = scn.GetFreepsCard("grey");
		var radagast = scn.GetFreepsCard("radagast");
		var ent = scn.GetFreepsCard("ent");
		var saruman = scn.GetFreepsCard("saruman");
		scn.MoveCardsToSupportArea(sentback);
		// Two Wizards; a [gandalf] companion who is none; and a Wizard who is a Shadow card.
		scn.MoveCardsToHand(gandalf, radagast, ent, saruman);

		scn.StartGame();
		// Enough in the pool that Saruman's cost of 4 is not what keeps him out.
		scn.SetTwilight(10);

		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ent, saruman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gandalf);

		// Gandalf costs 4: 10 + 4 = 14.
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sentback);
		assertInHand(radagast, ent, saruman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// It cost the condition nothing, so it is there to use again. Radagast costs 4: 14 + 4 = 18.
		assertTrue(scn.FreepsActionAvailable(sentback));
		scn.FreepsUseCardAction(sentback);
		assertInZone(Zone.FREE_CHARACTERS, radagast);
		assertEquals(18, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sentback);

		// With no Wizard left in hand that could be played there is nothing to offer.
		assertInHand(ent, saruman);
		assertFalse(scn.FreepsActionAvailable(sentback));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityPlaysAWizardWhoseOtherCopyIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var grey = scn.GetFreepsCard("grey");
		var white = scn.GetFreepsCard("white");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToDeadPile(grey);
		scn.MoveCardsToHand(white);

		scn.StartGame();

		// A Gandalf in the dead pile bars every Gandalf from being played in the ordinary way.
		assertFalse(scn.FreepsPlayAvailable(white));
		assertTrue(scn.FreepsActionAvailable(sentback));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(sentback);

		assertInZone(Zone.FREE_CHARACTERS, white);
		assertInZone(Zone.DEAD, grey);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sentback);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACopyInPlayStillBarsTheWizardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var grey = scn.GetFreepsCard("grey");
		var white = scn.GetFreepsCard("white");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCompanionsToTable(grey);
		scn.MoveCardsToHand(white);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, grey);
		assertInHand(white);
		assertFalse(scn.FreepsActionAvailable(sentback));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sentback = scn.GetFreepsCard("sentback1");
		var grey = scn.GetFreepsCard("grey");
		var white = scn.GetFreepsCard("white");
		scn.MoveCardsToSupportArea(sentback);
		scn.MoveCardsToDeadPile(grey);
		scn.MoveCardsToHand(white);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(sentback));

		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(white);
		assertFalse(scn.FreepsActionAvailable(sentback));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
