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

public class Card_15_064_Tests
{

	// The Fellowship path.  The maneuver phases of these tests are at site 2 (Ettenmoors).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madril", "15_64");

					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] ranger, not unique, cost 2, strength 6; a regroup ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] ranger, unique, cost 4; a maneuver ability never used here
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion who is no ranger; Knight is its whole text

					// The Misadventure of Mr. Underhill: [isengard] support-area condition.  "The twilight cost of
					// each companion played to a site which is not a sanctuary is +2."
					put("misadventure", "1_129");

					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, site number 2, strength 10; nothing else below 2 burdens
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site number 4; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site number 5, strength 5, vitality 3; Damage +1 is its whole text

					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, site number 3; her text needs a [wraith] condition on a companion
					// Ulaire Nertea, Messenger of Dol Guldur: Nazgul, cost 4, site number 3.  Her "when you play"
					// offer needs 5 companions.
					put("nertea", "1_234");
					// A Shadow Fell Over Them: [wraith] support-area condition.  "Assignment: Spot a Nazgul and
					// remove (4) to play a Nazgul from your discard pile."  Its other text answers a Nazgul's win.
					put("fell", "15_182");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MadrilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Madril, Defender of Osgiliath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>.
		 *   While you can spot 2 [gondor] rangers, Madril is twilight cost -2.
		 *   At the start of the maneuver phase, each minion is site number +1 for each threat you can spot until
		 *   the start of the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("madril");

		assertEquals("Madril", card.getBlueprint().getTitle());
		assertEquals("Defender of Osgiliath", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void IsPlayedForTwoWhileOnlyOneGondorRangerCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		// One [gondor] ranger, a ranger who is not [gondor], and a [gondor] companion who is no ranger.
		scn.MoveCompanionsToTable(ranger, arwen, knight);
		scn.MoveCardsToHand(madril);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(madril));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(madril);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, madril);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MadrilCosts2LessWith2GondorRangers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		// One [gondor] ranger, a ranger who is not [gondor], and a [gondor] companion who is no ranger.
		scn.MoveCompanionsToTable(ranger, arwen, knight);
		scn.MoveCardsToHand(madril, aragorn);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(madril));

		// The second [gondor] ranger: unique and of another title, where the first is neither.
		scn.FreepsPlayCard(aragorn);
		// Aragorn's own 4.
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(madril);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, madril);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDiscountIsTwoAndNoMoreWhereEachCompanionCostsTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(madril, aragorn);
		// On a bare board a discount of more than 2 leaves Madril at 0 as a discount of 2 does.  The Shadow
		// player's Misadventure makes each companion played at Bag End, which is no sanctuary, cost 2 more.
		scn.MoveCardsToSupportArea(scn.GetShadowCard("misadventure"));

		scn.StartGame();

		// One [gondor] ranger: no discount.  2 + 2 more for each companion.
		assertEquals(4, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(aragorn);
		// Aragorn's 4 + 2 more.
		assertEquals(6, scn.GetTwilight());
		// 2 + 2 more - 2: a discount of 3 would read 1, and of 1 would read 3.
		assertEquals(2, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(madril);
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, madril);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverTriggerMakesEachMinionSiteNumberPlusOnePerThreatUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(madril, ranger);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage, troop);
		// Three companions, so two threats are within the threat limit.
		scn.AddThreats(2);

		scn.FreepsPass();

		// The start of the Shadow phase is not the start of the maneuver phase.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		scn.ShadowPass();

		// Two threats: every minion is +2, and nobody is asked anything.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(4, scn.GetMinionSiteNumber(nelya));    // 2 + 2
		assertEquals(7, scn.GetMinionSiteNumber(savage));   // 5 + 2
		assertEquals(6, scn.GetMinionSiteNumber(troop));    // 4 + 2
		// At site 2, a Nazgul of site number 4 is roaming.
		assertTrue(scn.HasKeyword(nelya, Keyword.ROAMING));
		// The threats were spotted, not spent.
		assertEquals(2, scn.GetThreats());

		// Madril, 5 and hunter 1, beats the Savage (5) without killing it; the Ranger (6) loses to Nelya (10).
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{madril, savage},
				new PhysicalCardImpl[]{ranger, nelya}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(madril);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// In the second skirmish, with the first resolved, the +2 still stands, and no later phase has added more.
		scn.FreepsResolveSkirmish(ranger);
		assertTrue(scn.IsCharSkirmishing(nelya));
		assertEquals(4, scn.GetMinionSiteNumber(nelya));
		assertEquals(7, scn.GetMinionSiteNumber(savage));
		assertEquals(6, scn.GetMinionSiteNumber(troop));
		scn.PassSkirmishActions();

		// The first decision of the regroup phase: the minions are still there, at their printed numbers.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
	}

	@Test
	public void OneThreatIsPlusOneAndAThreatAddedAfterTheStartOfTheManeuverPhaseAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(madril, ranger);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);
		scn.AddThreats(1);

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetMinionSiteNumber(nelya));    // 2 + 1
		assertEquals(6, scn.GetMinionSiteNumber(savage));   // 5 + 1

		// The threats were counted at the start of the maneuver phase: a second one, laid in that phase, adds nothing.
		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(3, scn.GetMinionSiteNumber(nelya));
		assertEquals(6, scn.GetMinionSiteNumber(savage));

		// Nor does the start of the next phase count them again.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetMinionSiteNumber(nelya));
		assertEquals(6, scn.GetMinionSiteNumber(savage));
	}

	@Test
	public void ManeuverAbilityDoesNothingWithZeroThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(madril, ranger);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);

		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMinionPlayedAfterTheStartOfTheManeuverPhaseHasItsPrintedSiteNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(madril, ranger);

		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var fell = scn.GetShadowCard("fell");
		scn.MoveCardsToSupportArea(fell);
		scn.MoveCardsToDiscard(nertea);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(1);
		// 4 for the condition's ability, and Nertea's 4 + 2 for roaming at site 2.
		scn.SetTwilight(10);

		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(4, scn.GetMinionSiteNumber(lemenya));  // 3 + 1

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(fell));
		scn.ShadowUseCardAction(fell);

		// Nertea was not among the minions when the maneuver phase started.
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(3, scn.GetMinionSiteNumber(nertea));
		assertEquals(4, scn.GetMinionSiteNumber(lemenya));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
