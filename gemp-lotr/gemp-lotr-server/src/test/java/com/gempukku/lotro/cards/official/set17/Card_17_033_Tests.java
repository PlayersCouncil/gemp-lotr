package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "17_33");
					put("ranger2", "17_33");
					put("madril", "17_30");     // Madril, Loyal Lieutenant: unbound [gondor] Man companion, cost 3; his texts read only [gondor] conditions and his own ability
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1, site 4: roaming at site 2
					put("blackrider", "12_161"); // Black Rider: [wraith] Nazgul, site 3: roaming at site 2; fierce, strength 8 +1 for each companion
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgul, strength 10, site 2: not roaming at site 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The only companions in the deck are these, so that the starting fellowship offers no one else.
	protected VirtualTableScenario GetStartingFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "17_33");
					put("madril", "17_30");     // Madril, Loyal Lieutenant: unbound [gondor] MAN companion, cost 3
					put("dead", "8_45");        // Sleepless Dead: unbound [gondor] WRAITH companion, cost 3; to play, add a threat
					put("knight", "5_35");      // Gondorian Knight: unbound [gondor] Man companion, cost 2; Knight and no other text
					put("ithilien", "4_130");   // Ranger of Ithilien: RING-BOUND [gondor] Man companion, cost 3 (2 in a starting fellowship)
					put("eliterider", "4_265"); // Elite Rider: unbound [rohan] Man companion, cost 3 (2 in a starting fellowship)
					put("merry", "1_302");      // Merry, Friend to Sam: a companion of cost 1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangeroftheWhiteTreeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Ranger of the White Tree
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: Ranger. Hunter 1.
		 * 	When this is in your starting fellowship and you can spot an unbound [gondor] companion with cost 3 or more, his twilight cost is -2.
		 * 	Maneuver: Exert this companion and spot a roaming minion to make him defender +1 until the start of the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger1");

		assertEquals("Ranger of the White Tree", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void InTheStartingFellowshipBesideMadrilHeCostsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var madril = scn.GetFreepsCard("madril");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");

		// The game has not been started: player 1 is choosing his starting fellowship.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(madril);

		// Madril's 3 of the 4 a starting fellowship may cost.  The Ranger at 3 would not fit; at 3 - 2 he does.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger1, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight));
		scn.FreepsChooseCard(ranger1);

		// 3 + 1 = 4: not even Merry fits, and player 2 is asked.  Had the Ranger cost 0, Merry would still be offered.
		assertInZone(Zone.FREE_CHARACTERS, madril, ranger1);
		assertInZone(Zone.DECK, merry);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void InTheStartingFellowshipBesideAGondorWraithOfCostThreeHeCostsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var dead = scn.GetFreepsCard("dead");
		var merry = scn.GetFreepsCard("merry");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(dead);

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger1, merry));
		scn.FreepsChooseCard(ranger1);

		assertInZone(Zone.FREE_CHARACTERS, dead, ranger1);
		assertInZone(Zone.DECK, merry);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void InTheStartingFellowshipBesideAGondorCompanionOfCostTwoHeCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var knight = scn.GetFreepsCard("knight");
		var merry = scn.GetFreepsCard("merry");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(knight);

		// 2 + 3 is over the 4; at 2 + 1 he would fit.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger1));
		assertInZone(Zone.DECK, ranger1);
	}

	@Test
	public void InTheStartingFellowshipBesideARingBoundGondorCompanionHeCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ithilien = scn.GetFreepsCard("ithilien");
		var merry = scn.GetFreepsCard("merry");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(ithilien);

		// The Ranger of Ithilien prints 3 and costs 3 - 1 here.  2 + 3 is over the 4; at 2 + 1 he would fit.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger1));
		assertInZone(Zone.DECK, ranger1);
	}

	@Test
	public void InTheStartingFellowshipBesideAnUnboundCompanionOfAnotherCultureHeCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var eliterider = scn.GetFreepsCard("eliterider");
		var merry = scn.GetFreepsCard("merry");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(eliterider);

		// The Elite Rider prints 3 and costs 3 - 1 here.  2 + 3 is over the 4; at 2 + 1 he would fit.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger1));
		assertInZone(Zone.DECK, ranger1);
	}

	@Test
	public void PlayedFromHandAfterTheStartingFellowshipHeCostsThreeBesideMadril() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var madril = scn.GetFreepsCard("madril");
		scn.MoveCompanionsToTable(madril);
		scn.MoveCardsToHand(ranger1);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, madril);
		assertInHand(ranger1);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(ranger1);

		assertInZone(Zone.FREE_CHARACTERS, ranger1);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsHimToMakeHimDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger1, ranger2);

		var runner = scn.GetShadowCard("runner");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		// At site 2 the Runner (site 4) is roaming and Nelya (site 2) is not.
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertEquals(0, scn.GetKeywordCount(ranger1, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(ranger1));

		scn.FreepsUseCardAction(ranger1);

		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetKeywordCount(ranger1, Keyword.DEFENDER));
		// The other copy neither pays nor gains.
		assertEquals(0, scn.GetWoundsOn(ranger2));
		assertEquals(0, scn.GetKeywordCount(ranger2, Keyword.DEFENDER));

		scn.SkipToAssignments();
		assertEquals(1, scn.GetKeywordCount(ranger1, Keyword.DEFENDER));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{ranger1, runner},
				new PhysicalCardImpl[]{ranger2, nelya});

		scn.FreepsResolveSkirmish(ranger2);
		scn.PassSkirmishActions();
		// 6 + 1 (hunter) against Nelya's 10: the other Ranger loses and lives.
		assertEquals(1, scn.GetWoundsOn(ranger2));

		scn.FreepsResolveSkirmish(ranger1);
		// In a skirmish after one already resolved, still defender +1.
		assertEquals(1, scn.GetKeywordCount(ranger1, Keyword.DEFENDER));
		scn.PassSkirmishActions();

		// 6 + 1 against 5: the Runner dies, and at the regroup phase the bonus is gone.
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(ranger1, Keyword.DEFENDER));
	}

	@Test
	public void ManeuverAbilityIsOfferedBesideARoamingNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		scn.MoveCompanionsToTable(ranger1);

		var blackrider = scn.GetShadowCard("blackrider");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(blackrider, nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		// At site 2 the Black Rider (site 3) is roaming.
		assertTrue(scn.HasKeyword(blackrider, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertTrue(scn.FreepsActionAvailable(ranger1));

		scn.FreepsUseCardAction(ranger1);

		assertEquals(1, scn.GetWoundsOn(ranger1));
		assertEquals(1, scn.GetKeywordCount(ranger1, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoRoamingMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		scn.MoveCompanionsToTable(ranger1);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(ranger1));
		assertFalse(scn.FreepsActionAvailable(ranger1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger1, ranger2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(ranger1, 2);
		assertEquals(2, scn.GetWoundsOn(ranger1));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertFalse(scn.FreepsActionAvailable(ranger1));
		// The other copy, able to exert, is offered on the same board.
		assertTrue(scn.FreepsActionAvailable(ranger2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
