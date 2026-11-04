package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.PhysicalCardImpl;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;

import static org.junit.Assert.*;

public class Card_15_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("range", "15_23");
					put("elf", "1_53");          // Lorien Elf: an Elf companion with no keyword, strength 4 (cheated in past "To play, spot an Elf")
					put("celeborn", "1_34");     // Celeborn, Lord of Lorien: an Elf ally who is no archer
					put("legolas", "1_51");      // Legolas, Prince of Mirkwood: an archer Elf companion; +3 only against a Nazgul
					put("orophin", "1_56");      // Orophin, Lorien Bowman: an archer Elf ally; an Archery ability used only on purpose
					put("bowman", "2_60");       // Goblin Bowman: an archer minion with no other text
					put("runner1", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PointBlankRangeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Point Blank Range
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +2 (or +4 if you can spot 3 archers).
		*/

		var scn = GetScenario();

		var range = scn.GetFreepsCard("range");

		assertEquals("Point Blank Range", range.getBlueprint().getTitle());
		assertNull(range.getBlueprint().getSubtitle());
		assertFalse(range.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, range.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, range.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, range.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(range, Timeword.SKIRMISH));
		assertEquals(1, range.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlusTwoWithTwoArchersUntilTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var range = scn.GetFreepsCard("range");
		var frodo = scn.GetRingBearer();
		var elf = scn.GetFreepsCard("elf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(elf);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(range);
		assertInHand(range);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var bowman = scn.GetShadowCard("bowman");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner1}, new PhysicalCardImpl[]{elf, runner2});
		// The archers come down once the archery phase is past ("Archers get in the way"): two of them, Legolas
		// unassigned and the Bowman with no skirmish.
		scn.MoveCompanionsToTable(legolas);
		scn.MoveMinionsToTable(bowman);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(elf));
		assertTrue(scn.FreepsPlayAvailable(range));
		scn.FreepsPlayCard(range);

		// "An Elf": the Elf companion of a later skirmish, the unassigned one and the ally; Frodo is no Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf, legolas, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(elf);

		// 4 + 2: two archers, one short of the 4.
		assertInDiscard(range);
		assertEquals(6, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo's skirmish resolves; in the Elf's own skirmish the bonus is gone.
		scn.PassSkirmishActions();
		// Frodo loses to the Runner: The Ruling Ring's offer on his wound is declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(elf));
	}

	@Test
	public void WithThreeArchersOfEachKindTheElfIsStrengthPlusFourUntilTheEndOfTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var range = scn.GetFreepsCard("range");
		var frodo = scn.GetRingBearer();
		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(range);
		assertInHand(range);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var bowman = scn.GetShadowCard("bowman");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, runner1}, new PhysicalCardImpl[]{elf, runner2});
		// Three archers: a companion, an ally and a minion; each is needed for the 3.
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(orophin);
		scn.MoveMinionsToTable(bowman);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(4, scn.GetStrength(elf));
		scn.FreepsPlayCard(range);
		scn.FreepsChooseCard(elf);

		// 4 + 4
		assertInDiscard(range);
		assertEquals(8, scn.GetStrength(elf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Frodo loses to the Runner: The Ruling Ring's offer on his wound is declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsResolveSkirmish(elf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(elf));
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var range = scn.GetFreepsCard("range");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(range);
		assertInHand(range);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(range));
	}
}
