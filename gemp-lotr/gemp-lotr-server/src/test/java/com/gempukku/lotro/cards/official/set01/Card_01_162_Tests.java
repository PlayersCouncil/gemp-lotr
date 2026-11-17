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

public class Card_01_162_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("worry", "1_162");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
					// White Hand Destroyer: unique [uruk-hai] Uruk-hai, strength 10, vitality 3, printed fierce; hunter only
					// for each site its owner controls, and he controls none
					put("destroyer", "17_124");
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard (the culture without the race); each companion
					// is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, strength 3, vitality 4
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WorryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Worry
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert an Uruk-hai. Plays to your support area.
		 * Each time a companion or ally loses a skirmish involving an Uruk-hai, the opponent must choose to either exert the Ring-bearer or add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("worry");

		assertEquals("Worry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayAnUrukHaiOfEitherCultureExertsAndItGoesToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worry = scn.GetShadowCard("worry");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, worker, saruman);
		scn.SetTwilight(10);
		assertInHand(worry);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowPlayAvailable(worry));

		scn.ShadowPlayCard(worry);
		// An Uruk-hai of [isengard] and one of [sauron]; neither the [isengard] Orc nor the [uruk-hai] Wizard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker, saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertInZone(Zone.SUPPORT, worry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worry = scn.GetShadowCard("worry");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToHand(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		// Vitality 3 with 2 wounds: exhausted.  The Worker beside it can exert, but is no Uruk-hai.
		scn.AddWoundsToChar(savage, 2);
		scn.SetTwilight(10);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertInHand(worry);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(worry));
		assertInHand(worry);
	}

	@Test
	public void WhenACompanionLosesToAnUrukHaiTheFreePeoplesPlayerMayExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var worry = scn.GetShadowCard("worry");
		var guard = scn.GetShadowCard("guard");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, worker, saruman);
		assertInZone(Zone.SUPPORT, worry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{gimli, saruman},
				new PhysicalCardImpl[]{sam, worker});

		// Gimli loses to the [uruk-hai] Wizard, 8 against 6: no Uruk-hai, nothing asked.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Sam loses to the [isengard] Orc, 5 against 3: nothing asked.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Aragorn loses to the [sauron] Uruk-hai, 9 against 8 (1 + damage +1): the Free Peoples player chooses.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(List.of("Exert the Ring-bearer", "Add a burden"), scn.FreepsGetMultipleChoices());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChooseOption("Exert the Ring-bearer");
		// The Ring-bearer exerts; Sam, Ring-bound but not the Ring-bearer, does not; no burden.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddABurdenInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var worry = scn.GetShadowCard("worry");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertInZone(Zone.SUPPORT, worry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));

		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheRingBearerExhaustedABurdenIsAddedWithNothingAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var worry = scn.GetShadowCard("worry");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		// Frodo, vitality 4, with 3 wounds: exhausted.  Sam beside him could exert.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, worry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.FreepsGetBurdens());
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyWhoLosesToAnUrukHaiSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var worry = scn.GetShadowCard("worry");
		var savage = scn.GetShadowCard("savage");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.SUPPORT, worry);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// 5 against 4: the ally loses to an [isengard] Uruk-hai, and 1 + damage +1 kills him.
		assertInZone(Zone.DEAD, thrarin);
		assertTrue(scn.FreepsChoiceAvailable("Exert the Ring-bearer"));
		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));

		scn.FreepsChooseOption("Exert the Ring-bearer");
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsAskedWhenAnUrukHaiLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var worry = scn.GetShadowCard("worry");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInZone(Zone.SUPPORT, worry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 5 against 8: the Uruk-hai loses and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachLossToAFierceUrukHaiPutsTheChoiceAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var worry = scn.GetShadowCard("worry");
		var destroyer = scn.GetShadowCard("destroyer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(worry);

		scn.StartGame();
		scn.MoveMinionsToTable(destroyer);
		assertInZone(Zone.SUPPORT, worry);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// 10 against 8: Aragorn loses to an [uruk-hai] Uruk-hai; a burden is chosen.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Add a burden");
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fierce skirmish: Aragorn loses again, and is asked again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("Exert the Ring-bearer");
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
