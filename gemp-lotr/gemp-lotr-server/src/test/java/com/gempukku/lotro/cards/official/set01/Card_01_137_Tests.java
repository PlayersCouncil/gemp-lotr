package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_137_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reach", "1_137");
					put("reach2", "1_137");

					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3; damage +1 and nothing else
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, strength 7, vitality 3; a regroup ability only ever used on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("elf", "1_53");         // Lorien Elf: [elven] Elf companion, vitality 2; "To play, spot an Elf" is its whole text (cheated in)
					put("bounder", "1_286");    // Bounder: a Hobbit ALLY, vitality 2, home site 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansReachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Reach
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Search</b>.<br><b>Maneuver:</b> Exert an Uruk-hai to make the opponent choose to either exert 2 companions or make the Ring-bearer put on The One Ring until the regroup phase.
		 */

		var scn = GetScenario();

		var reach = scn.GetFreepsCard("reach");

		assertEquals("Saruman's Reach", reach.getBlueprint().getTitle());
		assertNull(reach.getBlueprint().getSubtitle());
		assertFalse(reach.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, reach.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, reach.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, reach.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(reach, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(reach, Timeword.MANEUVER));
		assertEquals(0, reach.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukHaiAndTheFreePeoplesPlayerMayExertTwoCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var raiders = scn.GetShadowCard("raiders");
		var guard = scn.GetShadowCard("guard");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(reach);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, elf);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, guard, servant);
		// Gimli has vitality 3: with 2 wounds he is exhausted and cannot exert.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(elf));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowPlayAvailable(reach));

		scn.ShadowPlayCard(reach);
		// An Uruk-hai of either culture; not an [isengard] minion who is no Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(servant));

		// The opponent is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Exert 2 companions"));
		assertTrue(scn.FreepsChoiceAvailable("Put on The One Ring"));
		scn.FreepsChoose("Exert 2 companions");

		// Two companions who can exert, of any kind and of three cultures and races, the Ring-bearer among them;
		// not Gimli, who is exhausted (a wound could still reach him), an ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, elf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bounder, raiders));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, elf);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(elf));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertFalse(scn.RBWearingOneRing());
		assertInDiscard(reach);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayPutOnTheRingInsteadAndItIsWornUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(reach);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		// The Raiding Party is the one Uruk-hai: it exerts unasked.
		scn.ShadowPlayCard(reach);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.FreepsChoose("Put on The One Ring");
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, raiders}, new PhysicalCardImpl[]{gimli, servant});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.RBWearingOneRing());
		scn.PassSkirmishActions();

		// Still worn in the second skirmish, after one has been resolved.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, servant));
		assertTrue(scn.RBWearingOneRing());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Taken off as the regroup phase begins.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
	}

	@Test
	public void WithOnlyTheRingBearerToExertTheRingGoesOnUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(reach);

		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertFalse(scn.RBWearingOneRing());
		scn.ShadowPlayCard(reach);

		// One companion cannot exert as two, and the ally beside him is no companion: the Free Peoples player
		// is not asked, and the Ring goes on.
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertTrue(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTheRingAlreadyWornTheFreePeoplesPlayerIsNotAskedAndExertsTwoCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var reach2 = scn.GetShadowCard("reach2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(reach, reach2);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The first Reach puts the Ring on.
		assertFalse(scn.RBWearingOneRing());
		scn.ShadowPlayCard(reach);
		scn.FreepsChoose("Put on The One Ring");
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The Ring cannot be put on again, so the second Reach can only make him exert 2 companions: he is not asked
		// which, and chooses the two.
		scn.FreepsPass();
		scn.ShadowPlayCard(reach2);
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertFalse(scn.FreepsChoiceAvailable("Put on The One Ring"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(aragorn, gimli);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.RBWearingOneRing());
		assertInDiscard(reach, reach2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsPlayableInTheManeuverPhaseAndNotInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(reach);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(raiders));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(reach));
		scn.ShadowPass();

		scn.FreepsPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(reach));
		assertEquals(0, scn.GetWoundsOn(raiders));
	}

	@Test
	public void CannotBePlayedWhenTheOnlyUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(reach);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant);
		scn.AddWoundsToChar(raiders, 2);
		assertEquals(2, scn.GetWoundsOn(raiders));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Raiding Party has vitality 3: with 2 wounds it cannot exert.  The Orc beside it could.
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(reach));
	}
}
