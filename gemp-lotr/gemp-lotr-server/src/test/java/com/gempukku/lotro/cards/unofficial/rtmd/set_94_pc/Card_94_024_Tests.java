package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInPlay;
import static com.gempukku.lotro.framework.Assertions.assertNotInZone;

import static org.junit.Assert.*;

public class Card_94_024_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");    // Gimli, Son of Gloin: [dwarven] companion, twilight 2
					put("merry", "1_302");   // Merry, Friend to Sam: [shire] companion, twilight 1, no play requirement
					put("bounder", "1_286"); // Bounder: [shire] Hobbit ally, twilight 1; a skirmish ability only
					put("sword", "1_299");   // Hobbit Sword: [shire] possession, twilight 1, on a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_24", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");    // Gimli, Son of Gloin: [dwarven] companion, twilight 2
					put("merry", "1_302");   // Merry, Friend to Sam: [shire] companion, twilight 1, no play requirement
					put("bounder", "1_286"); // Bounder: [shire] Hobbit ally, twilight 1; a skirmish ability only
					put("sword", "1_299");   // Hobbit Sword: [shire] possession, twilight 1, on a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_24"
		);
	}

	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");    // Gimli, Son of Gloin: [dwarven] companion, twilight 2
					put("merry", "1_302");   // Merry, Friend to Sam: [shire] companion, twilight 1, no play requirement
					put("bounder", "1_286"); // Bounder: [shire] Hobbit ally, twilight 1; a skirmish ability only
					put("sword", "1_299");   // Hobbit Sword: [shire] possession, twilight 1, on a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_24
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: Your companions are twilight cost +X, where X is the region number (including
		 * during your starting fellowship).
		 */

		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");

		assertEquals("Race Text 94_24", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(4, mod.getBlueprint().getIntensity());
	}

	@Test
	public void CompanionCostScalesWithRegionNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		// In hand, where the question is what they would cost to play.  A companion in play costs the number
		// printed on him whatever the region (CR 4.2 "twilight cost").
		scn.MoveCardsToHand(gimli, merry);
		scn.StartGame();

		// Region 1 (sites 1-3): +1
		assertEquals(3, scn.GetTwilightCost(gimli));
		assertEquals(2, scn.GetTwilightCost(merry));

		// And that is what is paid for him: 2 printed + 1 for the region
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(gimli);
		assertEquals(3, scn.GetTwilight());

		// Site 3 is the last of region 1: still +1
		scn.SkipToSite(3);
		scn.MoveCardsToHand(gimli, merry);
		assertEquals(3, scn.GetTwilightCost(gimli));
		assertEquals(2, scn.GetTwilightCost(merry));

		// Region 2 (sites 4-6): +2
		scn.SkipToSite(4);
		scn.MoveCardsToHand(gimli, merry);
		assertEquals(4, scn.GetTwilightCost(gimli));
		assertEquals(3, scn.GetTwilightCost(merry));

		// Site 6 is the last of region 2: still +2
		scn.SkipToSite(6);
		scn.MoveCardsToHand(gimli, merry);
		assertEquals(4, scn.GetTwilightCost(gimli));
		assertEquals(3, scn.GetTwilightCost(merry));

		// Region 3 (sites 7-9): +3
		scn.SkipToSite(7);
		scn.MoveCardsToHand(gimli, merry);
		assertEquals(5, scn.GetTwilightCost(gimli));
		assertEquals(4, scn.GetTwilightCost(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyAndAPossessionCostWhatIsPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCardsToHand(gimli, bounder, sword);
		scn.StartGame();

		// Region 1: the companion is +1. The ally and the possession are the mod owner's own cards as well,
		// and neither is a companion: 1 each, as printed.
		assertEquals(3, scn.GetTwilightCost(gimli));
		assertEquals(1, scn.GetTwilightCost(bounder));
		assertEquals(1, scn.GetTwilightCost(sword));

		// Frodo is the only Hobbit yet, so the sword goes to him unasked
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertEquals(1, scn.GetTwilight());

		scn.FreepsPlayCard(bounder);
		assertInPlay(bounder);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayerTwosCompanionsCostMoreOnHisOwnTurnWhenHeOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var gimli = scn.GetShadowCard("gimli");

		scn.MoveCardsToHand(gimli);
		scn.StartGame();
		scn.SkipToOtherPlayersTurn();

		// Player 2's fellowship is at site 1, in region 1: 2 printed + 1
		assertEquals(3, scn.GetTwilightCost(gimli));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(gimli));

		scn.ShadowPlayCard(gimli);
		assertInPlay(gimli);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void SurchargeAppliesDuringStartingFellowshipSelection() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		// With the mod: Gimli costs 3, Merry costs 2 (both +1 for region 1). Budget is 4.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, merry));

		scn.FreepsChooseCard(gimli);

		// 3 (Gimli) + 2 (Merry) = 5 > 4, and there is nothing else in the pool to pick:
		// the decision auto-completes once nothing affordable remains.
		assertFalse(scn.FreepsDecisionAvailable("Starting fellowship"));

		scn.ShadowDeclineChoosing();
		scn.StartGame(true, false);

		assertInPlay(gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SurchargeAppliesDuringPlayerTwosStartingFellowshipWhenPlayerTwoOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var freepsGimli = scn.GetFreepsCard("gimli");
		var freepsMerry = scn.GetFreepsCard("merry");
		var gimli = scn.GetShadowCard("gimli");
		var merry = scn.GetShadowCard("merry");

		// Player 1 has no mod: Gimli costs 2, Merry costs 1. 2 + 1 = 3 <= 4: both fit.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(freepsGimli);
		assertTrue(scn.FreepsHasCardChoiceAvailable(freepsMerry));
		scn.FreepsChooseCard(freepsMerry);

		// Player 2 owns the mod: Gimli costs 3, Merry costs 2 (both +1 for region 1). Budget is 4.
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, merry));

		scn.ShadowChooseCard(gimli);

		// 3 (Gimli) + 2 (Merry) = 5 > 4: nothing he can afford remains, and his choice is over.
		assertFalse(scn.ShadowDecisionAvailable("Starting fellowship"));

		scn.StartGame(true, false);

		assertInPlay(freepsGimli, freepsMerry, gimli);
		assertNotInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModTheSameTwoCompanionsFitTheBudget() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoModScenario();
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		// Without the mod: Gimli costs 2, Merry costs 1. 2 + 1 = 3 <= 4: both fit.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsChooseCard(gimli);

		assertTrue(scn.FreepsHasCardChoiceAvailable(merry));
		scn.FreepsChooseCard(merry);

		scn.ShadowDeclineChoosing();
		scn.StartGame(true, false);

		assertInPlay(gimli, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OpponentsCompanionsAreUnaffectedWhenShadowOwnsMod() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var gimli = scn.GetFreepsCard("gimli");

		// In hand: in play he would cost the 2 printed on him whoever owned the mod.
		scn.MoveCardsToHand(gimli);
		scn.StartGame();

		// "your,companion" does not match Freeps's companion when Shadow owns the mod
		assertEquals(2, scn.GetTwilightCost(gimli));
	}
}
