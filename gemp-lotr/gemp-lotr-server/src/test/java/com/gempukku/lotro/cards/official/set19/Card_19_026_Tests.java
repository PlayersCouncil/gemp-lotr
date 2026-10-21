package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_026_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "19_26");
					put("rider", "7_225");      // Elite Rider: [rohan] Man companion, cost 3; his own discount only in a starting fellowship
					put("defender", "17_93");   // Aragorn, Defender of Rohan: [rohan] Man companion, cost 4; hunter only while the Ring-bearer is assigned
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2, valiant and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, cost 4; a maneuver ability never used
					// For Death and Glory: [rohan] support-area condition, cost 3, no requirement to play; its text acts only
					// while the Ring-bearer is assigned, and its skirmish ability is never used
					put("glory", "17_97");
					// Rohirrim Soldier: [rohan] Man companion, printed cost 3, and "while you can spot a [rohan] Man, this
					// companion's twilight cost is -1": beside Éowyn he costs 2 to play
					put("soldier", "15_138");

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Éowyn, Lady of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Valiant</b>.
		 * 	<b>Fellowship:</b> Play a [rohan] companion with a twilight cost of 3 or more; his or her twilight cost is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Lady of the Mark", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void FellowshipAbilityPlaysARohanCompanionOfCostThreeOrMoreFromHandForOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var defender = scn.GetFreepsCard("defender");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glory = scn.GetFreepsCard("glory");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(rider, defender, guard, aragorn, glory);

		scn.StartGame();

		assertInHand(rider, defender, guard, aragorn, glory);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);

		// [rohan] companions of cost 3 and 4. Not the Household Guard (cost 2, one short), not Aragorn, Ranger of
		// the North ([gondor]), not For Death and Glory ([rohan], cost 3, no companion).
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, defender));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn, glory));

		scn.FreepsChooseCard(rider);

		assertInZone(Zone.FREE_CHARACTERS, rider);
		assertEquals(2, scn.GetTwilight());     // 3 - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The -1 was for that play alone: Aragorn, Defender of Rohan played from hand costs his whole 4.
		scn.FreepsPlayCard(defender);
		assertInZone(Zone.FREE_CHARACTERS, defender);
		assertEquals(6, scn.GetTwilight());     // 2 + 4
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithNoRohanCompanionOfCostThreeOrMoreInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glory = scn.GetFreepsCard("glory");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(guard, aragorn, glory);
		// The one that would do lies in the discard pile, not in hand.
		scn.MoveCardsToDiscard(rider);

		scn.StartGame();

		assertInHand(guard, aragorn, glory);
		assertInDiscard(rider);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eowyn));
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(rider);

		scn.StartGame();

		assertInHand(rider);
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(rider);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(eowyn));
	}

	@Test
	public void ACompanionPrintedAtThreeWhoseOwnTextLowersHisCostIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var soldier = scn.GetFreepsCard("soldier");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();

		// Printed 3; with Éowyn, a [rohan] Man, to spot, he costs 2 to play.
		assertInHand(soldier);
		assertEquals(3, soldier.getBlueprint().getTwilightCost());
		assertEquals(2, scn.GetTwilightCost(soldier));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);

		assertInZone(Zone.FREE_CHARACTERS, soldier);
		assertEquals(1, scn.GetTwilight());     // 3 - 1 by his own text - 1 by Éowyn's
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
