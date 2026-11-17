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

public class Card_15_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "15_121");
					put("eomer", "15_123");      // Éomer, Horsemaster: [rohan] Man companion, hunter 2; a skirmish ability used only on purpose
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, no hunter, no text but valiant
					put("merry", "103_85");      // Merry, Master Holbytla: [rohan] Hobbit companion; a skirmish ability used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("herdsman", "7_247");    // Rohirrim Herdsman: a [rohan] Man ally; a regroup ability used only on purpose
					put("troop1", "1_177");      // Goblin Patrol Troop: a minion with no text and no hunter, strength 13
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrilliantLightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Brilliant Light
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] companion strength +2 (or strength +3 and <b>damage +1</b> if he or she is a hunter companion).
		*/

		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");

		assertEquals("Brilliant Light", light.getBlueprint().getTitle());
		assertNull(light.getBlueprint().getSubtitle());
		assertFalse(light.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, light.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, light.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, light.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(light, Timeword.SKIRMISH));
		assertEquals(1, light.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARohanCompanionWhoIsNoHunterIsStrengthPlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(eomer, guard, merry, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(light);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop1},
				new PhysicalCardImpl[]{eomer, troop2});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(light));

		scn.FreepsPlayCard(light);
		// Any [rohan] companion: the one skirmishing, one assigned to a later skirmish, and a Hobbit in none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, eomer, merry));
		// A companion of another culture, and a [rohan] ally.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(guard);
		// The Guard is no hunter, though a hunter companion is on the table: +2 and no damage bonus.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertInDiscard(light);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Strength 13 against 8: the Guard loses and takes a wound; the bonus goes with his skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AHunterCompanionIsStrengthPlus3AndDamagePlus1UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eomer, guard);
		scn.MoveCardsToHand(light);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eomer, troop1},
				new PhysicalCardImpl[]{guard, troop2});
		scn.FreepsResolveSkirmish(eomer);

		// 7 printed + 2 for his hunter 2 against a minion that is no hunter.
		assertEquals(9, scn.GetStrength(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(light));

		scn.FreepsPlayCard(light);
		scn.FreepsChooseCard(eomer);
		// +3 in place of +2, not as well as it.
		assertEquals(12, scn.GetStrength(eomer));
		assertEquals(1, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Strength 13 against 12: Éomer loses and takes a wound; both bonuses go with his skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(1, scn.GetWoundsOn(eomer));
		// Out of a skirmish his hunter adds nothing: 7 printed.
		assertEquals(7, scn.GetStrength(eomer));
		assertEquals(0, scn.GetKeywordCount(eomer, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(light);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(light);
		assertFalse(scn.FreepsPlayAvailable(light));
	}
}
