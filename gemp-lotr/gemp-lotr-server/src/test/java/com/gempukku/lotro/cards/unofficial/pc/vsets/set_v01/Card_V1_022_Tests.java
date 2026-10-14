package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_022_Tests
{
	// An Aragorn who does not himself have the Aragorn signet, so that "Aragorn" and "a companion with the
	// Aragorn signet" are different characters; he is also the one Aragorn who is not a [gondor] card.  The
	// Ring-bearer is Frodo, Son of Drogo, with the Frodo signet.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("memorial", "101_22");

					// Aragorn, Defender of Rohan: a [rohan] Man with no signet, cost 4, strength 8, vitality 4.
					// His text gives [rohan] companions hunter while the Ring-bearer is assigned to a skirmish,
					// and Frodo never is.
					put("aragorn", "17_93");
					// Merry, From O'er the Brandywine: a [shire] Hobbit with the Aragorn signet, strength 3,
					// vitality 4.  His text needs a weapon, and he never has one.
					put("merry", "1_303");
					// Arwen, Daughter of Elrond: an [elven] Elf with the Aragorn signet, cost 2, vitality 3.
					// Her text needs a Nazgul.
					put("arwen", "1_30");

					// Goblin Patrol Troop: strength 13, no text.
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An Aragorn who has the Aragorn signet himself.
	protected VirtualTableScenario GetSignetAragornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("memorial", "101_22");

					// Aragorn, Wingfoot: Aragorn signet, vitality 4.  His text answers the fellowship moving, and
					// the tests that use him end in the fellowship phase.
					put("aragorn", "4_364");
					// Merry, From O'er the Brandywine: a [shire] Hobbit with the Aragorn signet, vitality 4.
					put("merry", "1_303");
					// Boromir, Lord of Gondor: Aragorn signet, vitality 3.  His text is about being overwhelmed.
					put("boromir", "1_96");
					// Gimli, Son of Gloin: Gandalf signet.  A skirmish ability, never used here.
					put("gimli", "1_13");
					// Dwarf Guard: a companion with no signet at all.
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GilraensMemorialStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Gilraen's Memorial
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, exert Aragorn.
		 * 	When Aragorn is killed, discard this artifact.
		 * 	Fellowship: Exert a companion with the Aragorn signet to remove a burden.  Then exert Aragorn or
		 * 	discard this artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("memorial");

		assertEquals("Gilraen's Memorial", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAragornIsInPlayAndThenExertsHimToBePlayedForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		// A companion with the Aragorn signet who could exert, and is not Aragorn.
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(memorial, aragorn);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(memorial));

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.FreepsPlayAvailable(memorial));
		assertEquals(4, scn.GetTwilight());     // Aragorn's cost
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsPlayCard(memorial);
		assertInZone(Zone.SUPPORT, memorial);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, merry);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.  Merry, with the Aragorn signet, can.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(memorial, arwen);

		scn.StartGame();

		assertTrue(scn.IsExhausted(aragorn));
		assertFalse(scn.IsExhausted(merry));
		// Arwen, also in hand, can be played; the artifact cannot.
		assertTrue(scn.FreepsPlayAvailable(arwen));
		assertFalse(scn.FreepsPlayAvailable(memorial));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsDiscardedWhenAragornIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(memorial);
		// Vitality 4 with 3 wounds: the next wound kills him.
		scn.AddWoundsToChar(aragorn, 3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.SUPPORT, memorial);

		// Aragorn's 8 loses to the Troop's 13 without being overwhelmed, and he takes one wound.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);

		// His death leaves a rule and the artifact's trigger waiting together, and the Free Peoples player says
		// which is carried out first.  Neither is his to decline.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsResolveRuleFirst();
		assertInDiscard(memorial);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StaysInPlayWhenAnotherCompanionWithTheAragornSignetIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(memorial);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, troop);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.SUPPORT, memorial);

		// Merry's 3 is overwhelmed by the Troop's 13, and he is killed.
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, merry);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.SUPPORT, memorial);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipActionExertsACompanionWithTheAragornSignetToRemoveABurdenThenExertsAragornOrDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, merry, arwen);
		scn.MoveCardsToSupportArea(memorial);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(memorial));

		scn.FreepsUseCardAction(memorial);
		// Merry and Arwen have the Aragorn signet.  This Aragorn has no signet and Frodo has his own.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetBurdens());

		// Then exert Aragorn or discard this artifact: here, exert Aragorn.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Exert Aragorn");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.SUPPORT, memorial);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second use, taking the other alternative.
		assertTrue(scn.FreepsActionAvailable(memorial));
		scn.FreepsUseCardAction(memorial);
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseOption("Discard");
		assertInDiscard(memorial);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyACompanionWithTheAragornSignetWhoCanExertIsOfferedAndAragornMayPayBothExertions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSignetAragornScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, merry, boromir, gimli, guard);
		scn.MoveCardsToSupportArea(memorial);
		// Vitality 3 with 2 wounds: Boromir has the signet and cannot exert.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		scn.AddBurdens(1);

		assertTrue(scn.FreepsActionAvailable(memorial));
		scn.FreepsUseCardAction(memorial);

		// Aragorn (a [gondor] Man) and Merry (a [shire] Hobbit) have the signet and can exert.  Boromir has it
		// and is exhausted; Gimli has the Gandalf signet, Frodo the Frodo signet, the Dwarf Guard none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, gimli, guard, frodo));

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetBurdens());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsChooseOption("Exert Aragorn");
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SUPPORT, memorial);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedOnceNoCompanionWithTheAragornSignetCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(memorial);
		// Vitality 4 with 2 wounds: Merry can exert once more.
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		scn.AddBurdens(3);

		assertTrue(scn.FreepsActionAvailable(memorial));

		// Merry is the only companion with the signet, so he is chosen without asking.
		scn.FreepsUseCardAction(memorial);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetBurdens());
		scn.FreepsChooseOption("Exert Aragorn");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, memorial);

		// Merry is exhausted.  Aragorn (no signet) and Frodo (Frodo signet) could still exert, and there are
		// burdens left to remove.
		assertTrue(scn.IsExhausted(merry));
		assertFalse(scn.IsExhausted(aragorn));
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(memorial));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(memorial);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(troop);

		assertTrue(scn.FreepsActionAvailable(memorial));

		// Nothing but the phase has changed: Merry can still exert and the burden is still there.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(memorial));
	}

	@Test
	public void DiscardsItselfWithoutAChoiceWhenAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(memorial);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.AddBurdens(1);

		assertTrue(scn.FreepsActionAvailable(memorial));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInZone(Zone.SUPPORT, memorial);

		// Merry is the only companion with the signet, so he is chosen without asking.
		scn.FreepsUseCardAction(memorial);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());

		// Only one of the two alternatives can be carried out, so there is nothing to choose.
		assertInDiscard(memorial);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsItselfWithoutAChoiceWhenPayingTheCostHasExhaustedAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSignetAragornScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Aragorn is the only companion with the Aragorn signet.
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(memorial);
		// Vitality 4 with 2 wounds: Aragorn can exert once, and not twice.
		scn.AddWoundsToChar(aragorn, 2);

		scn.StartGame();
		scn.AddBurdens(1);

		assertTrue(scn.FreepsActionAvailable(memorial));
		assertInZone(Zone.SUPPORT, memorial);

		scn.FreepsUseCardAction(memorial);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());

		assertInDiscard(memorial);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveTheCostIsStillPaidAndAragornStillExertsOrTheArtifactIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memorial = scn.GetFreepsCard("memorial");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveCardsToSupportArea(memorial);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(memorial));

		scn.FreepsUseCardAction(memorial);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Exert Aragorn");
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, memorial);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
