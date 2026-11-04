package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_150_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rally", "11_150");
					put("rally2", "11_150");

					put("hguard", "5_83");      // Household Guard: a [rohan] Man companion, resistance 6, valiant and nothing else
					put("rguard", "7_246");     // Rohirrim Guard: a [rohan] Man companion, resistance 6; "Skirmish: Discard a card
					                            //   from hand to exert a minion skirmishing this companion", used only on purpose
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man companion, resistance 6, strength 5; his skirmish
					                            //   ability needs him mounted
					put("soldier", "17_101");   // Soldier of Rohan: a [rohan] Man companion, resistance 5; strength +2 only while
					                            //   the Ring-bearer is assigned
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, resistance 6
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, resistance 6, no Man without
					                            //   Eowyn or 3 [rohan] companions
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally (resistance 0)

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallyCryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Rally Cry
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [rohan] Man who has resistance 4 or more strength +2 for each wound on each minion he or she is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rally");

		assertEquals("Rally Cry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesARohanManOfResistanceFourStrengthPlusTwoForEachWoundOnEachMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var rally2 = scn.GetFreepsCard("rally2");
		var hguard = scn.GetFreepsCard("hguard");
		var rguard = scn.GetFreepsCard("rguard");
		var rider = scn.GetFreepsCard("rider");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(hguard, rguard, rider, soldier, aragorn);
		scn.MoveCardsToHand(rally, rally2);

		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");
		var worker = scn.GetShadowCard("worker");

		assertInHand(rally, rally2);
		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(savage, explorer, worker);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(explorer, 2);
		scn.AddWoundsToChar(worker, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(worker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hguard, savage}, new PhysicalCardImpl[]{rguard, worker});
		scn.ShadowAssignToMinions(hguard, explorer);
		scn.FreepsResolveSkirmish(hguard);

		// Resistance 6 less 2 burdens is exactly 4; the Soldier's 5 less 2 is 3.
		assertEquals(4, scn.GetResistance(hguard));
		assertEquals(4, scn.GetResistance(rguard));
		assertEquals(3, scn.GetResistance(soldier));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetStrength(hguard));
		assertTrue(scn.FreepsPlayAvailable(rally));
		scn.FreepsPlayCard(rally);

		assertEquals(1, scn.FreepsGetChoiceMin());
		// In this skirmish, in another, and in none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, rguard, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, aragorn));
		scn.FreepsChooseCard(hguard);

		assertInDiscard(rally);
		// 6 + 2 x (1 on the Savage + 2 on the Explorer); the Worker's wound, in another skirmish, is not counted.
		assertEquals(12, scn.GetStrength(hguard));
		assertEquals(6, scn.GetStrength(rguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// 12 against 5 + 5: the Guard wins.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bonus lasted that skirmish only.
		scn.FreepsResolveSkirmish(rguard);
		assertEquals(6, scn.GetStrength(hguard));

		// A second count: one wound on the one minion he is skirmishing.
		assertEquals(6, scn.GetStrength(rguard));
		scn.FreepsPlayCard(rally2);
		scn.FreepsChooseCard(rguard);
		assertInDiscard(rally2);
		// 6 + 2 x 1
		assertEquals(8, scn.GetStrength(rguard));
		assertEquals(6, scn.GetStrength(hguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanManInNoSkirmishMayBeChosenAndGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var hguard = scn.GetFreepsCard("hguard");
		var rider = scn.GetFreepsCard("rider");
		scn.MoveCompanionsToTable(hguard, rider);
		scn.MoveCardsToHand(rally);

		var worker = scn.GetShadowCard("worker");

		assertInHand(rally);
		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.AddWoundsToChar(worker, 1);
		assertEquals(1, scn.GetWoundsOn(worker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hguard, worker);
		scn.FreepsResolveSkirmish(hguard);

		assertEquals(6, scn.GetStrength(hguard));
		assertEquals(5, scn.GetStrength(rider));
		scn.FreepsPlayCard(rally);
		assertTrue(scn.FreepsHasCardChoiceAvailable(hguard, rider));
		scn.FreepsChooseCard(rider);

		// The Rider skirmishes no minion: +2 for each of no wounds.
		assertInDiscard(rally);
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(hguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoRohanManOfResistanceFourItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var merry = scn.GetFreepsCard("merry");
		var soldier = scn.GetFreepsCard("soldier");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(merry, soldier, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(rally);

		var savage = scn.GetShadowCard("savage");
		var explorer = scn.GetShadowCard("explorer");
		var worker = scn.GetShadowCard("worker");

		assertInHand(rally);
		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(savage, explorer, worker);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(explorer, 1);
		scn.AddWoundsToChar(worker, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(worker));

		// Each skirmishes a wounded minion: Merry (the [rohan] Hobbit, resistance 4, no Man with two [rohan]
		// companions and no Eowyn), the Soldier (a [rohan] Man of resistance 3) and Aragorn (a [gondor] Man of
		// resistance 4). Any of them taken for the Man would be chosen and gain 2.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, worker}, new PhysicalCardImpl[]{soldier, savage},
				new PhysicalCardImpl[]{aragorn, explorer});
		scn.FreepsResolveSkirmish(merry);

		assertEquals(4, scn.GetResistance(merry));
		assertEquals(3, scn.GetResistance(soldier));
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.FreepsPlayAvailable(rally));
		scn.FreepsPlayCard(rally);

		assertInDiscard(rally);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var hguard = scn.GetFreepsCard("hguard");
		scn.MoveCompanionsToTable(hguard);
		scn.MoveCardsToHand(rally);

		var worker = scn.GetShadowCard("worker");

		assertInHand(rally);
		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.AddWoundsToChar(worker, 1);
		assertEquals(1, scn.GetWoundsOn(worker));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(rally));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(hguard, worker);
		scn.FreepsResolveSkirmish(hguard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(rally));
	}

	@Test
	public void TheBonusIsCountedAsTheEventIsPlayedAndAWoundAfterwardAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rally = scn.GetFreepsCard("rally");
		var rally2 = scn.GetFreepsCard("rally2");
		var rguard = scn.GetFreepsCard("rguard");
		scn.MoveCompanionsToTable(rguard);
		scn.MoveCardsToHand(rally, rally2);

		var worker = scn.GetShadowCard("worker");

		assertInHand(rally, rally2);
		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		scn.AddWoundsToChar(worker, 1);
		assertEquals(1, scn.GetWoundsOn(worker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rguard, worker);
		scn.FreepsResolveSkirmish(rguard);
		assertEquals(6, scn.GetStrength(rguard));
		scn.FreepsPlayCard(rally);
		// 6 + 2 x 1, the Guard being the one candidate
		assertEquals(8, scn.GetStrength(rguard));

		// The Rohirrim Guard's own ability exerts the Worker: a second wound token on it.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(rguard));
		scn.FreepsUseCardAction(rguard);
		assertInDiscard(rally2);
		assertEquals(2, scn.GetWoundsOn(worker));
		assertEquals(8, scn.GetStrength(rguard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
