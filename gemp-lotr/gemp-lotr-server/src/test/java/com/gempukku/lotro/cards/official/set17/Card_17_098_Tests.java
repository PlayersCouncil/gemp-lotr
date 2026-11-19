package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throne", "17_98");

					put("theoden", "4_292");     // Théoden, Son of Thengel: [rohan] Man companion; a regroup ability used only on purpose
					put("guard1", "7_246");      // Rohirrim Guard: [rohan] Man companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("guard2", "7_246");
					put("merry", "103_85");      // Merry, Master Holbytla: [rohan] Hobbit companion; a Man only with 3 [rohan] companions or Éowyn
					put("herdsman1", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman2", "7_247");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4

					// Sarumans and Grímas of two cultures; none of them has a bar on its own assignment
					put("curunir", "17_115");    // Saruman, Curunír: [uruk-hai] Wizard minion
					put("coldly", "17_48");      // Saruman, Coldly Still: [men] Wizard minion
					put("wormtongue", "4_154");  // Gríma, Wormtongue: [isengard] Man minion
					put("worm", "18_67");        // Gríma, Witless Worm: [men] Man minion

					put("runner", "1_178");      // Goblin Runner: strength 5, vitality 1
					put("party1", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and no other text
					put("party2", "1_158");
					put("assassin", "2_93");     // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, not worn, answers no wound
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void ThroneoftheGoldenHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Throne of the Golden Hall
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot Théoden (or 2 [rohan] companions).
		 * 	Saruman and Gríma cannot be assigned to skirmishes.
		 * 	<b>Response:</b> If a [rohan] Man is about to take a wound, exert another [rohan] Man and add (2) to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("throne");

		assertEquals("Throne of the Golden Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTheodenIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var theoden = scn.GetFreepsCard("theoden");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(herdsman1);
		scn.MoveCardsToHand(throne, theoden);

		scn.StartGame();

		// A [rohan] Man ally and a [gondor] Man companion: no Théoden, and no [rohan] companion at all.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(throne));

		// Théoden is then the only [rohan] companion.
		scn.FreepsPlayCard(theoden);
		assertTrue(scn.FreepsPlayAvailable(throne));
		scn.FreepsPlayCard(throne);

		assertInZone(Zone.SUPPORT, throne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithTwoRohanCompanionsAndNoTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var guard1 = scn.GetFreepsCard("guard1");
		var merry = scn.GetFreepsCard("merry");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(guard1, aragorn);
		scn.MoveAlliesToTable(herdsman1);
		scn.MoveCardsToHand(throne, merry);

		scn.StartGame();

		// One [rohan] companion who is no Théoden, a [rohan] ally and a companion of another culture.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(throne));

		// A [rohan] Hobbit companion is the second.
		scn.FreepsPlayCard(merry);
		assertTrue(scn.FreepsPlayAvailable(throne));
		scn.FreepsPlayCard(throne);

		assertInZone(Zone.SUPPORT, throne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SarumanAndGrimaCannotBeAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var curunir = scn.GetShadowCard("curunir");
		var wormtongue = scn.GetShadowCard("wormtongue");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(throne);

		scn.StartGame();
		scn.MoveMinionsToTable(curunir, wormtongue, runner);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(curunir));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(wormtongue));
	}

	@Test
	public void ASarumanAndAGrimaOfAnotherCultureCannotBeAssignedToASkirmishEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var coldly = scn.GetShadowCard("coldly");
		var worm = scn.GetShadowCard("worm");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(throne);

		scn.StartGame();
		scn.MoveMinionsToTable(coldly, worm, runner);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(runner));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(coldly));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(worm));
	}

	@Test
	public void WithoutTheThroneSarumanAndGrimaMayBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var curunir = scn.GetShadowCard("curunir");
		var wormtongue = scn.GetShadowCard("wormtongue");

		scn.StartGame();
		scn.MoveMinionsToTable(curunir, wormtongue);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(curunir));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(wormtongue));
	}

	@Test
	public void WithoutTheThroneASarumanAndAGrimaOfAnotherCultureMayBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coldly = scn.GetShadowCard("coldly");
		var worm = scn.GetShadowCard("worm");

		scn.StartGame();
		scn.MoveMinionsToTable(coldly, worm);
		scn.SkipToAssignments();

		assertTrue(scn.FreepsDecisionAvailable("Assign"));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(coldly));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(worm));
	}

	@Test
	public void ResponseExertsAnotherRohanManAndAdds2ToPreventEachWoundToARohanManCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCompanionsToTable(guard1, guard2, aragorn);
		scn.MoveAlliesToTable(herdsman1);
		// The Guard to be wounded has one vitality left: either wound would kill him
		scn.AddWoundsToChar(guard1, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		assertEquals(2, scn.GetWoundsOn(guard1));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, party1);

		// 6 against 9, damage +1: two wounds, each about to be placed in turn.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(herdsman1));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();

		// Another [rohan] Man: a companion and an ally, not a Man of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, herdsman1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman1);

		assertEquals(1, scn.GetWoundsOn(herdsman1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(guard1));

		// The second wound: offered again.  The Herdsman is exhausted now, and the other Guard exerts.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(twilight + 2 + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(guard1));
		assertInZone(Zone.FREE_CHARACTERS, guard1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheRohanManAboutToBeWoundedIsNotOfferedToExertForHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveAlliesToTable(herdsman1);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, party1);

		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();

		// "Exert another [rohan] Man": the Guard being wounded can exert, and is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, herdsman1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard1));
		scn.FreepsChooseCard(guard2);
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
	}

	@Test
	public void ResponseIsNotOfferedForAWoundToAHobbitOrToAManOfAnotherCultureAndNeitherMayExertForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var herdsman2 = scn.GetFreepsCard("herdsman2");
		var runner = scn.GetShadowCard("runner");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCardsToSupportArea(throne);
		// Two [rohan] companions only: Merry is no Man
		scn.MoveCompanionsToTable(merry, aragorn, guard1);
		scn.MoveAlliesToTable(herdsman1, herdsman2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, party1, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner}, new PhysicalCardImpl[]{aragorn, party1},
				new PhysicalCardImpl[]{guard1, party2});

		// Merry, a [rohan] Hobbit, 3 against 5: his wound is placed with nothing asked.
		assertFalse(scn.IsRace(merry, Race.MAN));
		scn.FreepsResolveSkirmish(merry);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Aragorn, a [gondor] Man, 8 against 9: both wounds placed with nothing asked.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// A [rohan] Man, 6 against 9: offered, and neither Merry nor Aragorn may exert for it.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(herdsman1, herdsman2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		scn.FreepsChooseCard(herdsman1);
		assertEquals(1, scn.GetWoundsOn(herdsman1));
		assertEquals(0, scn.GetWoundsOn(guard1));

		// Merry's own trigger answers the Herdsman's exertion; then the second wound is offered again, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponsePreventsAWoundToARohanManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var guard1 = scn.GetFreepsCard("guard1");
		var herdsman1 = scn.GetFreepsCard("herdsman1");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCompanionsToTable(guard1);
		scn.MoveAlliesToTable(herdsman1);
		// The Herdsman has one vitality left: the wound would kill him, and he cannot exert
		scn.AddWoundsToChar(herdsman1, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);
		assertEquals(1, scn.GetWoundsOn(herdsman1));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(assassin));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));

		// The Herdsman is about to take the wound; the Guard is the one other [rohan] Man, and exerts.
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(herdsman1));
		assertInZone(Zone.SUPPORT, herdsman1);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhenTheOnlyOtherRohanManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		scn.MoveCardsToSupportArea(throne);
		scn.MoveCompanionsToTable(guard1, guard2, aragorn);
		scn.AddWoundsToChar(guard2, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(party1);
		assertEquals(1, scn.GetVitality(guard2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard1, party1);

		// Aragorn could exert, but is no [rohan] Man; the Guard being wounded is no other [rohan] Man.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(throne));
		assertEquals(2, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
