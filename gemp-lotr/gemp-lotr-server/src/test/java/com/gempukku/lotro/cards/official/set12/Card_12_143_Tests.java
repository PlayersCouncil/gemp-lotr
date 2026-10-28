package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quelling", "12_143");

					// Intimidating Uruk: [uruk-hai] Uruk-hai, strength 10, damage +1; its text answers only its play at a
					// battleground, and it is cheated in.
					put("intimidating", "11_189");
					// Tyrannical Uruk: [uruk-hai] Uruk-hai, strength 14, damage +1, toil 1 (a play cost only).
					put("tyrannical", "11_204");
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard, strength 8.  Each companion is resistance
					// -1 (nothing here reads it); a skirmish ability, never used.
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9; damage +1 and no other text.
					put("guard", "10_81");
					put("guard2", "10_81");
					// Dark Alliance: an [uruk-hai] condition in the support area; a Response to a Free Peoples special
					// ability, and none is used.
					put("alliance", "12_141");

					// Aragorn, Ranger of the North: strength 8; a maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: strength 6; a skirmish ability, never used.
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuellingForceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Quelling Force
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [uruk-hai] minion strength +1 for each other [uruk-hai] minion you can spot.
		*/

		var scn = GetScenario();

		var quelling = scn.GetFreepsCard("quelling");

		assertEquals("Quelling Force", quelling.getBlueprint().getTitle());
		assertNull(quelling.getBlueprint().getSubtitle());
		assertFalse(quelling.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, quelling.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, quelling.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, quelling.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(quelling, Timeword.SKIRMISH));
		assertEquals(1, quelling.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnUrukHaiMinionStrongerForEachOtherUrukHaiMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var quelling = scn.GetShadowCard("quelling");
		var intimidating = scn.GetShadowCard("intimidating");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(quelling);
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(intimidating, tyrannical, saruman, guard, guard2);
		assertInHand(quelling);
		assertInZone(Zone.SUPPORT, alliance);

		// The Shadow player's assignment actions, the phase before the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(quelling));
		scn.ShadowPass();

		// Tyrannical Uruk, Saruman and the second Guard are left out of every skirmish.  Two [sauron] Uruk-hai, so that a
		// count by race (3) is not a count by culture (2).
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, intimidating}, new PhysicalCardImpl[]{scn.GetRingBearer(), guard});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(intimidating));
		assertTrue(scn.ShadowPlayAvailable(quelling));
		scn.ShadowPlayCard(quelling);

		// Any [uruk-hai] minion, in the skirmish or not, Uruk-hai or Wizard; not the [sauron] Uruk-hai, nor the [uruk-hai]
		// condition, which is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(intimidating, tyrannical, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, guard2, alliance));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(intimidating);

		// 10 + 2: Tyrannical Uruk and Saruman.  Not the minion itself, not the [sauron] Uruk-hai, not the [uruk-hai]
		// condition.  Every one that can be spotted is counted, with no number asked.
		assertEquals(12, scn.GetStrength(intimidating));
		assertEquals(14, scn.GetStrength(tyrannical));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(guard));
		assertInZone(Zone.DISCARD, quelling);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AWizardWhoseSkirmishIsStillToComeGainsForOneOtherAndHasItNoLongerInHisOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var quelling = scn.GetShadowCard("quelling");
		var intimidating = scn.GetShadowCard("intimidating");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(quelling);

		scn.StartGame();
		scn.MoveMinionsToTable(intimidating, saruman);
		assertInHand(quelling);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, intimidating}, new PhysicalCardImpl[]{gimli, saruman});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(saruman));
		scn.ShadowPlayCard(quelling);
		assertTrue(scn.ShadowHasCardChoiceAvailable(intimidating, saruman));
		scn.ShadowChooseCard(saruman);

		// 8 + 1: Intimidating Uruk is the one other.
		assertEquals(9, scn.GetStrength(saruman));
		assertEquals(10, scn.GetStrength(intimidating));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// The bonus lasts the skirmish it was given in; each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionOnTheTableItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var quelling = scn.GetShadowCard("quelling");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(quelling);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertInHand(quelling);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The only minion is a [sauron] Uruk-hai: nothing to choose, and the event is played all the same.
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.ShadowPlayAvailable(quelling));
		scn.ShadowPlayCard(quelling);

		assertInZone(Zone.DISCARD, quelling);
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
