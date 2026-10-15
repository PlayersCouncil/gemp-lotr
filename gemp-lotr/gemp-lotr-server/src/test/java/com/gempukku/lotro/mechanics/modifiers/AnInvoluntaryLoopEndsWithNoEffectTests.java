package com.gempukku.lotro.mechanics.modifiers;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Two standing texts whose conditions read each other.
 *
 * Comprehensive Rules 4.2, "loops": "When an infinite loop occurs that includes no voluntary actions, the loop ends
 * immediately with no effect.  Example: Frodo, Weary From the Journey (which gets strength +2 while skirmishing a
 * minion of strength 8 or less) is skirmishing Black Land Observer or Wandering Hillman (which get strength +3
 * while you can spot a companion of strength 10 or more).  Neither the minions nor Frodo would get the strength
 * bonuses."
 *
 * The loop goes round only where each bonus undoes the other: Frodo at 8 or 9 before his bonus is 10 or 11 with
 * it, which gives the minion its +3, which takes Frodo's +2 away, which takes the minion's +3 away.  Then neither
 * applies (F-0536).  Where one of the two answers the same either way, there is no loop and the texts apply as
 * they read: Frodo at 7 never reaches 10 and has his +2 against a minion of 8, and Frodo at 10 or 11 makes the
 * minion 11 whatever he gets, and so gets nothing.
 *
 * Frodo 15_144 is strength 3, and The Great Ring 19_1 adds nothing while it is not worn.  He is brought to the
 * strength before his bonus by what he bears: Hobbit Sword 15_145 (+2, no other text), Celebring 18_7 (+2; his
 * text is about being played), Rohan Stable Master 15_134 (+1; "considered to be mounted"), The Gaffer 13_151
 * (+1; his heal answers a transfer) and Hobbiton Brewer 15_146 (+2; his response, when Frodo is about to take a
 * wound, is declined).  Followers and the sword are laid on him, so nothing of their Aid or play is carried out.
 */
public class AnInvoluntaryLoopEndsWithNoEffectTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "15_145");
					put("celebring", "18_7");
					put("stablemaster", "15_134");
					put("gaffer", "13_151");
					put("brewer", "15_146");

					// Black Land Observer: strength 8, vitality 2.  "When you play this minion, you may spot an
					// [orc] condition to add a threat.  While you can spot a companion of strength 10 or more, this
					// minion is strength +3."
					put("observer", "15_101");
					// Wandering Hillman: strength 8, vitality 3.  "To play, spot a [men] Man.  While you can spot a
					// companion of strength 10 or more, this minion is strength +3.  Maneuver: Remove 2 burdens or
					// 2 threats to control a site."  His maneuver ability is not used.
					put("hillman", "15_94");
				}},
				VirtualTableScenario.FellowshipSites,
				"15_144",
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void FrodoAt8BeforeHisBonusAgainstBlackLandObserverGetsNoBonusAndNeitherDoesTheObserver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("stablemaster"));
		var observer = scn.GetShadowCard("observer");

		scn.StartGame();
		scn.MoveMinionsToTable(observer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, observer);
		scn.FreepsResolveSkirmish(frodo);

		// With his +2 he would be 10, which gives the Observer +3, which takes the +2 away: a loop, so neither.
		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(observer));

		// 8 against 8: the minion wins a tie.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(observer));
	}

	@Test
	public void FrodoAt9BeforeHisBonusAgainstBlackLandObserverGetsNoBonusAndWinsAgainstTheObserverAt8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("stablemaster"),
				scn.GetFreepsCard("gaffer"));
		var observer = scn.GetShadowCard("observer");

		scn.StartGame();
		scn.MoveMinionsToTable(observer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, observer);
		scn.FreepsResolveSkirmish(frodo);

		// Asked minion first: the answer does not hang on which card is asked about first.
		assertEquals(8, scn.GetStrength(observer));
		assertEquals(9, scn.GetStrength(frodo));

		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(observer));
	}

	@Test
	public void FrodoAt8BeforeHisBonusAgainstWanderingHillmanGetsNoBonusAndNeitherDoesTheHillman() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("stablemaster"));
		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, hillman);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(8, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(hillman));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(hillman));
	}

	@Test
	public void FrodoAt9BeforeHisBonusAgainstWanderingHillmanGetsNoBonusAndWinsAgainstTheHillmanAt8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("stablemaster"),
				scn.GetFreepsCard("gaffer"));
		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, hillman);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(9, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(hillman));

		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(hillman));
	}

	@Test
	public void FrodoAt7BeforeHisBonusNeverReaches10SoHeHasHisBonusAgainstTheObserverAt8() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"));
		var observer = scn.GetShadowCard("observer");

		scn.StartGame();
		scn.MoveMinionsToTable(observer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, observer);
		scn.FreepsResolveSkirmish(frodo);

		// 7 + 2 is 9: no companion of strength 10, so the Observer stays 8 and Frodo keeps his +2.  No loop.
		assertEquals(9, scn.GetStrength(frodo));
		assertEquals(8, scn.GetStrength(observer));

		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(observer));
	}

	@Test
	public void FrodoAt10BeforeHisBonusGivesTheObserverItsBonusAndGetsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("brewer"),
				scn.GetFreepsCard("stablemaster"));
		var observer = scn.GetShadowCard("observer");

		scn.StartGame();
		scn.MoveMinionsToTable(observer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, observer);
		scn.FreepsResolveSkirmish(frodo);

		// Frodo is 10 with or without his +2, so the Observer is 11, and Frodo has no minion of 8 or less.  No loop.
		assertEquals(10, scn.GetStrength(frodo));
		assertEquals(11, scn.GetStrength(observer));

		scn.PassSkirmishActions();
		// Hobbiton Brewer's response to the wound, declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(observer));
	}

	@Test
	public void FrodoAt11BeforeHisBonusGivesTheObserverItsBonusAndGetsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("brewer"),
				scn.GetFreepsCard("stablemaster"), scn.GetFreepsCard("gaffer"));
		var observer = scn.GetShadowCard("observer");

		scn.StartGame();
		scn.MoveMinionsToTable(observer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, observer);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(11, scn.GetStrength(frodo));
		assertEquals(11, scn.GetStrength(observer));

		scn.PassSkirmishActions();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(observer));
	}

	@Test
	public void FrodoAt10BeforeHisBonusGivesTheHillmanItsBonusAndGetsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("sword"), scn.GetFreepsCard("celebring"), scn.GetFreepsCard("brewer"),
				scn.GetFreepsCard("stablemaster"));
		var hillman = scn.GetShadowCard("hillman");

		scn.StartGame();
		scn.MoveMinionsToTable(hillman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, hillman);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(10, scn.GetStrength(frodo));
		assertEquals(11, scn.GetStrength(hillman));

		scn.PassSkirmishActions();
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(hillman));
	}
}
