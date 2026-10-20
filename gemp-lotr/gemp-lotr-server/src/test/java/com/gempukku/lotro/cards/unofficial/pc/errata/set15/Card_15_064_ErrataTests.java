package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_064_ErrataTests
{
	// The Fellowship path. The maneuver phases of these tests are at site 2 (Ettenmoors).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madril", "65_64");

					put("ranger1", "7_116");    // Ranger of Osgiliath: [gondor] ranger companion, cost 2, strength 6; a regroup ability never used here
					put("ranger2", "7_116");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion who is no ranger; Knight is its whole text

					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, site number 2; nothing else below 2 burdens
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site number 4; no text
					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, site number 5, strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
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
		 * Game Text: <b>Ranger. Hunter 1.</b>
		 * 	While you can spot 2 [gondor] rangers, Madril is twilight cost -2.
		 * 	At the start of the Maneuver phase, for each threat you can spot, make a minion's site number +1
		 * 	until the start of the regroup phase.
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
		var ranger1 = scn.GetFreepsCard("ranger1");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		// One [gondor] ranger, a ranger who is not [gondor], and a [gondor] companion who is no ranger.
		scn.MoveCompanionsToTable(ranger1, arwen, knight);
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
	public void CostsNothingOnceASecondGondorRangerIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		// One [gondor] ranger, a ranger who is not [gondor], and a [gondor] companion who is no ranger.
		scn.MoveCompanionsToTable(ranger1, arwen, knight);
		scn.MoveCardsToHand(madril, ranger2);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(ranger2);
		// The second Ranger's own 2.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(madril);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, madril);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDiscountIsTwoAndNoMoreWhereEachCompanionCostsTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger1);
		scn.MoveCardsToHand(madril, ranger2);
		// What East Road (11_236) does to each companion, so that a discount of more than 2 can be told from one of
		// 2: on a bare board both leave Madril at 0.
		scn.ApplyAdHocModifier(new TwilightCostModifier(null, CardType.COMPANION, null, new ConstantEvaluator(2)));

		scn.StartGame();

		// One [gondor] ranger: no discount. 2 + 2 more for each companion.
		assertEquals(4, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(ranger2);
		// The second Ranger's 2 + 2 more.
		assertEquals(4, scn.GetTwilight());
		// 2 + 2 more - 2: a discount of 3 would read 1, and of 1 would read 3.
		assertEquals(2, scn.GetTwilightCost(madril));

		scn.FreepsPlayCard(madril);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, madril);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseEachThreatMakesOneMinionsSiteNumberPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var madril = scn.GetFreepsCard("madril");
		var ranger1 = scn.GetFreepsCard("ranger1");
		scn.MoveCompanionsToTable(madril, ranger1);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage, troop);
		// Three companions, so three threats are within the threat limit.
		scn.AddThreats(3);

		scn.FreepsPass();

		// The start of the Shadow phase is not the start of the maneuver phase.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		scn.ShadowPass();

		// The first threat's +1.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(madril, ranger1, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(nelya);
		assertEquals(3, scn.GetMinionSiteNumber(nelya));

		// The second may go to the same minion again.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nelya);
		assertEquals(4, scn.GetMinionSiteNumber(nelya));

		// And the third to another.
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(4, scn.GetMinionSiteNumber(nelya));    // 2 + 1 + 1
		assertEquals(6, scn.GetMinionSiteNumber(savage));   // 5 + 1
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		// At site 2, a Nazgul of site number 4 is roaming.
		assertTrue(scn.HasKeyword(nelya, Keyword.ROAMING));
		// The threats were spotted, not spent.
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void OneThreatGivesOnePlusOneWhichLastsUntilTheStartOfTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(madril, ranger1, ranger2);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.AddThreats(1);

		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertEquals(5, scn.GetMinionSiteNumber(savage1));
		assertEquals(5, scn.GetMinionSiteNumber(savage2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, savage2));
		scn.FreepsChooseCard(savage1);

		// One threat, one +1: nothing more is asked.
		assertEquals(6, scn.GetMinionSiteNumber(savage1));
		assertEquals(5, scn.GetMinionSiteNumber(savage2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Each Ranger of Osgiliath (6) beats an Uruk Savage (5) without killing it.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{ranger1, savage2},
				new PhysicalCardImpl[]{ranger2, savage1}
		);
		scn.FreepsResolveSkirmish(ranger1);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage2));

		// In the second skirmish, with the first resolved, the +1 still stands, and no later phase has added another.
		scn.FreepsResolveSkirmish(ranger2);
		assertTrue(scn.IsCharSkirmishing(savage1));
		assertEquals(6, scn.GetMinionSiteNumber(savage1));
		assertEquals(5, scn.GetMinionSiteNumber(savage2));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage1);
		assertEquals(5, scn.GetMinionSiteNumber(savage1));
		assertEquals(5, scn.GetMinionSiteNumber(savage2));
	}

	@Test
	public void WithNoThreatNothingIsAskedAndNoSiteNumberChanges() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger1 = scn.GetFreepsCard("ranger1");
		scn.MoveCompanionsToTable(madril, ranger1);

		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, savage);

		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetMinionSiteNumber(nelya));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
