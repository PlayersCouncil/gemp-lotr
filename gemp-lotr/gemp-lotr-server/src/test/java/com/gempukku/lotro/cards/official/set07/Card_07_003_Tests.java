package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tested", "7_3");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F, strength 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					// [dwarven] conditions.
					put("lord", "1_21");        // Lord of Moria: support area, each Dwarf is damage +1 (no strength)
					put("rock", "4_50");        // Here Is Good Rock: support area; its skirmish ability stacks a Free Peoples card from hand here
					put("liking", "5_9");       // More to My Liking: support area; its maneuver ability stacks the top card of the draw deck here, whatever it is
					// A condition of another culture that play stacks cards on; with no Aragorn-signet companion skirmishing it does nothing.
					put("end", "101_23");       // I Would Have Gone With You to the End: [gondor]
					put("toby", "1_305");       // Old Toby: a Free Peoples card to stack

					put("runner", "1_178");     // Goblin Runner: Orc, strength 5, vitality 1; its text acts only as it is played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BattleTestedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Battle Tested
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2 (+4 if you spot a card stacked on a [dwarven] condition).
		*/

		var scn = GetScenario();

		var tested = scn.GetFreepsCard("tested");

		assertEquals("Battle Tested", tested.getBlueprint().getTitle());
		assertNull(tested.getBlueprint().getSubtitle());
		assertFalse(tested.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, tested.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, tested.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, tested.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(tested, Timeword.SKIRMISH));
		assertEquals(0, tested.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNoCardStackedOnADwarvenConditionOfHisOwnADwarfIsStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tested = scn.GetFreepsCard("tested");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var end = scn.GetFreepsCard("end");
		var toby = scn.GetFreepsCard("toby");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		// A [dwarven] condition with nothing on it, and a card stacked on a [gondor] condition.
		scn.MoveCardsToSupportArea(lord, end);
		scn.StackCardsOn(end, toby);
		scn.MoveCardsToHand(tested);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		// The other player's own [dwarven] condition, with a card stacked on it: not active on this turn.
		var theirRock = scn.GetShadowCard("rock");
		var theirToby = scn.GetShadowCard("toby");
		scn.MoveCardsToSupportArea(theirRock);
		scn.StackCardsOn(theirRock, theirToby);

		assertInHand(tested);
		assertEquals(0, scn.GetStackedCards(lord).size());
		assertEquals(1, scn.GetStackedCards(end).size());
		assertEquals(1, scn.GetStackedCards(theirRock).size());

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// No requirement and no cost: not playable in the phase next door.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(tested));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.FreepsPlayAvailable(tested));

		scn.FreepsPlayCard(tested);
		// A Dwarf in the skirmish and a Dwarf ally in none are offered; companions who are no Dwarf are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// 6 + 2: none of the three stacks is on an active [dwarven] condition of his.
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(tested);

		// Still +2 at the next decision of the skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));

		// Gimli beats the Runner; in the next skirmish, a phase of its own, he is 6 again.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAFreePeoplesCardStackedOnADwarvenConditionTheDwarfIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tested = scn.GetFreepsCard("tested");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rock = scn.GetFreepsCard("rock");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(rock);
		scn.StackCardsOn(rock, toby);
		scn.MoveCardsToHand(tested);
		assertInHand(tested);
		assertEquals(1, scn.GetStackedCards(rock).size());

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(tested);
		// Gimli is the only Dwarf, so he is chosen with nothing asked.  6 + 4: the parenthesis takes the place of the +2.
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(tested);
		assertInZone(Zone.SUPPORT, rock);
		assertEquals(1, scn.GetStackedCards(rock).size());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAShadowCardStackedOnADwarvenConditionADwarfAllyInNoSkirmishIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tested = scn.GetFreepsCard("tested");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var liking = scn.GetFreepsCard("liking");
		// His own Goblin Runner, as More to My Liking stacks the top card of the draw deck whatever it is.
		var ownRunner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(liking);
		scn.StackCardsOn(liking, ownRunner);
		scn.MoveCardsToHand(tested);
		assertInHand(tested);
		assertEquals(1, scn.GetStackedCards(liking).size());

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));

		scn.FreepsPlayCard(tested);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		scn.FreepsChooseCard(grimir);
		// 3 + 4: a Dwarf in no skirmish may be chosen (the text names none), and a Shadow card stacked counts.
		assertEquals(7, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(gimli));
		assertInDiscard(tested);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
