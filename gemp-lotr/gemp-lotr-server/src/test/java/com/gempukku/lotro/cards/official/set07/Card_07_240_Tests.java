package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_240_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "7_240");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally; a fellowship ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: the [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Long Spear
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [rohan] Man.
		 * You may add a threat to play this possession any time you could play a skirmish event.
		*/

		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");

		assertEquals("Long Spear", spear.getBlueprint().getTitle());
		assertNull(spear.getBlueprint().getSubtitle());
		assertFalse(spear.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, spear.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, spear.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, spear.getBlueprint().getCardType());
		assertTrue(spear.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, spear.getBlueprint().getPossessionClasses().size());
		assertEquals(1, spear.getBlueprint().getTwilightCost());
		assertEquals(2, spear.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndAddsNoThreatOutsideASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, knight, merry);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(spear);

		scn.StartGame();

		assertInHand(spear);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(spear));
		scn.FreepsPlayCard(spear);

		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, the [rohan] Hobbit (only two
		// [rohan] companions in play, so no Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertAttachedTo(spear, guma);
		// The threat is the price of a play at skirmish time only.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotPlayableInTheManeuverPhaseAndPlayedInASkirmishForAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(spear);
		assertInHand(spear);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Frodo and the Guard: a threat could be added.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(spear));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInHand(spear);
		assertTrue(scn.FreepsPlayAvailable(spear));
		scn.FreepsPlayCard(spear);

		// The Guard is the only [rohan] Man, so the Spear goes on him unasked.
		assertAttachedTo(spear, guard);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInASkirmishWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(spear);
		assertInHand(spear);

		scn.StartGame();
		// Two companions, Frodo and the Guard: two threats are as many as can be.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(2, scn.GetThreats());
		assertInHand(spear);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(spear));
	}
}
