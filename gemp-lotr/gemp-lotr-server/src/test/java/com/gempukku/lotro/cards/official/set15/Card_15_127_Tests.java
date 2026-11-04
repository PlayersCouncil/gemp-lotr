package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trophy", "15_127");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant is its whole text
					put("guard2", "5_83");
					put("scout", "5_90");       // Rohirrim Scout: [rohan] Man companion, valiant; a regroup ability used only on purpose
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, not valiant; a skirmish ability only while mounted
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, home 4T; a fellowship heal used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit, valiant; a Man only beside 3 [rohan] companions or Eowyn
					put("lorien", "54_76");     // Lorien Guardian: [elven] Elf companion, valiant; a regroup ability used only on purpose
					put("faramir", "18_48");    // Faramir, Captain of Ithilien: [gondor] Man companion, hunter 2; a maneuver heal used only on purpose
					put("elfguard", "17_7");    // Elven Guardian: [elven] Elf companion, hunter 1; +2 only against a wounded minion
					put("uruk", "15_171");      // Tracking Uruk: [uruk-hai] Uruk-hai, hunter 1 is its whole text
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1, no hunter
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimTrophyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Grim Trophy
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] Man strength +1 for each hunter you can spot (or if you can spot more valiant Men than hunters, make a [rohan] Man strength +1 for each valiant Man you can spot).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("trophy");

		assertEquals("Grim Trophy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesARohanManAnywhereStrengthPlus1ForEachHunterUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trophy = scn.GetFreepsCard("trophy");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		var frodo = scn.GetRingBearer();
		var uruk = scn.GetShadowCard("uruk");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard, merry, faramir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(trophy);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next door: not playable in the Free Peoples player's assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(trophy));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner1}, new PhysicalCardImpl[]{faramir, runner2});
		// The Tracking Uruk is left unassigned: a hunter in no skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		assertEquals(4, scn.GetStrength(guma));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.FreepsPlayAvailable(trophy));
		scn.FreepsPlayCard(trophy);
		// The Household Guard in this skirmish and Guma in none; not Merry, a [rohan] Hobbit who is no Man beside one
		// other [rohan] companion, nor Faramir, a Man of [gondor], nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, faramir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		// Two hunters, Faramir and the Uruk, and one valiant Man, the Guard (Merry is valiant but no Man): the hunter
		// count, 4 + 2. The parenthesis, one valiant Man, would have made it 5.
		assertEquals(6, scn.GetStrength(guma));
		assertEquals(6, scn.GetStrength(guard));
		assertInDiscard(trophy);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(faramir);
		// The next skirmish is a phase of its own: the bonus is gone.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(guma));
	}

	@Test
	public void WithThreeHuntersAndTwoValiantMenTheRohanManIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trophy = scn.GetFreepsCard("trophy");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var faramir = scn.GetFreepsCard("faramir");
		var elfguard = scn.GetFreepsCard("elfguard");
		var uruk = scn.GetShadowCard("uruk");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(guard, scout, faramir, elfguard);
		scn.MoveCardsToHand(trophy);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPlayCard(trophy);
		scn.FreepsChooseCard(guard);
		// Three hunters (Faramir, the Elven Guardian, the Uruk) against two valiant Men: 6 + 3.
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithMoreValiantMenThanHuntersTheRohanManIsStrengthPlus1ForEachValiantManUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trophy = scn.GetFreepsCard("trophy");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var lorien = scn.GetFreepsCard("lorien");
		var uruk = scn.GetShadowCard("uruk");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(rider, guard, scout, lorien);
		scn.MoveCardsToHand(trophy);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, runner1}, new PhysicalCardImpl[]{guard, runner2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		assertEquals(5, scn.GetStrength(rider));
		scn.FreepsPlayCard(trophy);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, guard, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorien));
		scn.FreepsChooseCard(rider);
		// Two valiant Men (the Guard and the Scout) against one hunter (the Uruk): 5 + 2. The Lorien Guardian is valiant
		// and no Man (counted, 5 + 3), the Rider a Man and not valiant; the hunter count would have made it 6.
		assertEquals(7, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 7 against 5: the Rider wins. In the Guard's skirmish, the next, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(rider));
	}

	@Test
	public void WithThreeValiantMenAndTwoHuntersTheRohanManIsStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trophy = scn.GetFreepsCard("trophy");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var scout = scn.GetFreepsCard("scout");
		var faramir = scn.GetFreepsCard("faramir");
		var uruk = scn.GetShadowCard("uruk");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(rider, guard, guard2, scout, faramir);
		scn.MoveCardsToHand(trophy);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, runner1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(rider);

		assertEquals(5, scn.GetStrength(rider));
		scn.FreepsPlayCard(trophy);
		scn.FreepsChooseCard(rider);
		// Three valiant Men against two hunters (Faramir, the Uruk): 5 + 3.
		assertEquals(8, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var trophy = scn.GetFreepsCard("trophy");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		var uruk = scn.GetShadowCard("uruk");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry, faramir);
		scn.MoveCardsToHand(trophy);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, runner1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(faramir);

		int faramirStrength = scn.GetStrength(faramir);
		assertEquals(3, scn.GetStrength(merry));
		// Merry, the one [rohan] companion, is no Man: nothing to make stronger, and the event is played all the same.
		assertTrue(scn.FreepsPlayAvailable(trophy));
		scn.FreepsPlayCard(trophy);
		assertInDiscard(trophy);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(faramirStrength, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
