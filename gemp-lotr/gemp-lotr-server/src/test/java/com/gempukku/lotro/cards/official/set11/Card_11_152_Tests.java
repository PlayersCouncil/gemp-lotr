package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_152_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "11_152");
					put("soldier2", "11_152");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, strength 4; a Fellowship ability only
					// Merry, Master Holbytla: a [rohan] Hobbit companion, strength 3, a Man only while 3 [rohan] companions
					// (or Eowyn) can be spotted; his trigger and his ability are used only on purpose, and nobody exerts here
					put("merry", "103_85");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RiddermarkSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Riddermark Soldier
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: When you play this companion, each [rohan] companion is strength +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Riddermark Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WhenPlayedEachRohanCompanionIsStrengthPlus2AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var soldier = scn.GetFreepsCard("soldier");
		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(guma));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPlayCard(soldier);

		// The Soldier himself and Merry, a [rohan] companion who is no Man (two [rohan] companions only); not Guma,
		// a [rohan] Man who is no companion, nor Frodo, a companion of another culture.  Nothing is asked.
		assertInZone(Zone.FREE_CHARACTERS, soldier);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusLastsThroughTheSkirmishesAndIsGoneAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();

		assertEquals(6, scn.GetStrength(guard));
		scn.FreepsPlayCard(soldier);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(guard));

		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{soldier, runner2});
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		// The Guard's skirmish is over; in the Soldier's, the bonus still holds on both.
		scn.FreepsResolveSkirmish(soldier);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(guard));

		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void ARohanCompanionPlayedAfterTheSoldierIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(soldier, guard);

		scn.StartGame();

		scn.FreepsPlayCard(soldier);
		assertEquals(7, scn.GetStrength(soldier));

		// CR 4.2 "event", read for a trigger of a card in play: the bonus reaches the [rohan] companions there as it
		// is carried out.
		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherRohanCompanionsPlaySetsNothingOffAndASecondCopyStrengthensOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");
		var soldier2 = scn.GetFreepsCard("soldier2");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(soldier);
		scn.MoveCardsToHand(guard, soldier2);

		scn.StartGame();

		// Cheated into play, the Soldier's own trigger has not been used.
		assertEquals(5, scn.GetStrength(soldier));

		scn.FreepsPlayCard(guard);
		assertEquals(5, scn.GetStrength(soldier));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second copy's play is answered by its own trigger alone: +2 once on each, not +4.
		scn.FreepsPlayCard(soldier2);
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(soldier2));
		assertEquals(8, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedIntoTheStartingFellowshipItsTriggerStrengthensIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));

		scn.FreepsChooseCard(soldier);

		// CR 4.2 "starting fellowship": "When you play" text is used on a starting companion.
		assertInZone(Zone.FREE_CHARACTERS, soldier);
		assertEquals(7, scn.GetStrength(soldier));
		// The second Soldier, at cost 2, still fits within the 4 a starting fellowship may cost: player 1 chooses on.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
	}
}
