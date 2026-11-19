package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_266_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_266");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, cost 2, valiant and nothing else
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: a [rohan] Man ALLY, cost 1; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, cost 4; a maneuver ability used only on purpose
					put("royal", "102_54");     // Royal Guard: [rohan] ally, vitality 3; a skirmish ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] ally of another title, vitality 2; a regroup ability only
					put("theoden", "4_292");    // Théoden, Son of Thengel: a [rohan] companion who is no ally, vitality 2; a regroup ability only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a [shire] ally, vitality 3; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EomerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Éomer, Sister-son of Théoden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: While you can spot a [rohan] Man, Éomer's twilight cost is -1.
		 * At the start of each turn, you may heal a [rohan] ally.
		*/

		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");

		assertEquals("Éomer", eomer.getBlueprint().getTitle());
		assertEquals("Sister-son of Théoden", eomer.getBlueprint().getSubtitle());
		assertTrue(eomer.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eomer.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eomer.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eomer.getBlueprint().getCardType());
		assertEquals(Race.MAN, eomer.getBlueprint().getRace());
		assertEquals(3, eomer.getBlueprint().getTwilightCost());
		assertEquals(7, eomer.getBlueprint().getStrength());
		assertEquals(3, eomer.getBlueprint().getVitality());
		assertEquals(6, eomer.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, eomer.getBlueprint().getSignet());
	}

	@Test
	public void WithARohanManCompanionToSpotEomerIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(eomer);

		// The text names Éomer himself, so it applies to him as he is played (CR 4.2 "modifier").
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var aldor = scn.GetFreepsCard("aldor");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(eomer, aldor);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(aldor);

		assertInZone(Zone.SUPPORT, aldor);
		assertEquals(2, scn.GetTwilightCost(eomer));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(eomer);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheTurnHeMayHealARohanAllyAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var royal = scn.GetFreepsCard("royal");
		var herdsman = scn.GetFreepsCard("herdsman");
		var theoden = scn.GetFreepsCard("theoden");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(eomer, theoden);
		scn.MoveAlliesToTable(royal, herdsman, bilbo);
		scn.AddWoundsToChar(royal, 2);
		scn.AddWoundsToChar(herdsman, 1);
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(bilbo, 1);
		assertEquals(2, scn.GetWoundsOn(royal));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsAcceptOptionalTrigger();

		// A [rohan] ally: not a [rohan] companion, nor an ally of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(royal, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden, bilbo, eomer));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(royal);

		// One wound healed of two.
		assertEquals(1, scn.GetWoundsOn(royal));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedTheHealIsNotMadeAndItIsOfferedAgainAtHisOwnersNextTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var royal = scn.GetFreepsCard("royal");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveAlliesToTable(royal);
		scn.AddWoundsToChar(royal, 2);
		assertEquals(2, scn.GetWoundsOn(royal));

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(royal));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's turn: Éomer is not active, and nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eomer));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetWoundsOn(royal));

		// His owner's next turn: offered again, and taken.
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eomer));
		scn.FreepsAcceptOptionalTrigger();

		// The one [rohan] ally is healed unasked.
		assertEquals(1, scn.GetWoundsOn(royal));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoWoundedRohanAllyTheHealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var royal = scn.GetFreepsCard("royal");
		var theoden = scn.GetFreepsCard("theoden");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCompanionsToTable(eomer, theoden);
		scn.MoveAlliesToTable(royal, bilbo);
		// Wounded: a [rohan] companion and an ally of another culture; the [rohan] ally is not.
		scn.AddWoundsToChar(theoden, 1);
		scn.AddWoundsToChar(bilbo, 1);
		assertEquals(0, scn.GetWoundsOn(royal));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(bilbo));

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertEquals(1, scn.GetWoundsOn(bilbo));
	}
}
