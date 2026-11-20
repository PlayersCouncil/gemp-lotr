package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "4_130");
					put("ranger2", "4_130");

					// Uruk Savage: [isengard] Uruk-hai, site 5 (roaming at site 3), strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Goblin Runner: [moria] Orc, site 4 (roaming at site 3), strength 5, vitality 1; its trigger is on being played
					put("runner", "1_178");
					// Gollum, Stinker: site 3 (not roaming at site 3), strength 5 (+1 for each burden: none here); his
					// trigger needs him to win
					put("gollum", "5_25");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The only companions in the deck are the two Rangers, Faramir and Merry, so that the starting fellowship offers no
	// one else.
	protected VirtualTableScenario GetStartingFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger1", "4_130");
					put("ranger2", "4_130");
					// Faramir, Son of Denethor: a Ring-bound [gondor] Man ranger of cost 3; his text reads only skirmishes
					put("faramir", "4_117");
					// Merry, Friend to Sam: a companion of cost 1; a skirmish ability used only on purpose
					put("merry", "1_302");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangerofIthilienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ranger of Ithilien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ring-bound</b>. <b>Ranger</b>.
		 * 	When this companion is in your starting fellowship, his twilight cost is -1.
		 * 	While skirmishing a roaming minion, this companion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger1");

		assertEquals("Ranger of Ithilien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void InTheStartingFellowshipEachRangerCostsTwoAndAnotherRingBoundGondorManCostsHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");

		// The game has not been started: player 1 is choosing his starting fellowship.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger1, ranger2, faramir, merry));

		scn.FreepsChooseCard(ranger1);
		// 3 - 1 of the 4 a starting fellowship may cost.  The second Ranger's own -1 is his alone: he costs 2, not 1,
		// and still fits.  Faramir, a Ring-bound [gondor] Man ranger at his printed 3, does not.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger2, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(faramir));

		scn.FreepsChooseCard(ranger2);
		// 2 + 2 = 4: not even Merry fits, nobody is left to offer, and player 1 is asked only to press DONE.  Had
		// the second Ranger cost 1, Merry would still be offered.
		assertInZone(Zone.FREE_CHARACTERS, ranger1, ranger2);
		assertInZone(Zone.DECK, faramir, merry);
		assertTrue(scn.FreepsIsAskedOnlyToConfirm());

		scn.FreepsConfirm();

		// The fellowship is complete, and player 2 is asked.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void PlayedFromHandInTheFellowshipPhaseHeCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		scn.MoveCardsToHand(ranger1);

		scn.StartGame();
		assertInHand(ranger1);
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(ranger1));

		scn.FreepsPlayCard(ranger1);
		assertInZone(Zone.FREE_CHARACTERS, ranger1);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachCopyIsStrengthPlus2OnlyWhileItsOwnSkirmishAgainstARoamingMinionIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(ranger1, ranger2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger1, savage}, new PhysicalCardImpl[]{ranger2, runner});

		// At site 3: an Uruk-hai of site 5 and an Orc of site 4, both roaming.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(runner, Keyword.ROAMING));
		assertEquals(6, scn.GetStrength(ranger1));
		assertEquals(6, scn.GetStrength(ranger2));

		scn.FreepsResolveSkirmish(ranger1);
		assertEquals(8, scn.GetStrength(ranger1));
		// The other copy is assigned to a roaming minion, but its skirmish has not begun.
		assertEquals(6, scn.GetStrength(ranger2));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(ranger2);
		assertEquals(8, scn.GetStrength(ranger2));
		// The first copy's skirmish is over.
		assertEquals(6, scn.GetStrength(ranger1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAMinionThatIsNotRoamingHeIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger1 = scn.GetFreepsCard("ranger1");
		var ranger2 = scn.GetFreepsCard("ranger2");
		var gollum = scn.GetShadowCard("gollum");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger1, ranger2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gollum, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger1, gollum}, new PhysicalCardImpl[]{ranger2, savage});

		// Gollum's site is 3: not roaming at site 3.  A roaming minion is in play, in another skirmish.
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));

		scn.FreepsResolveSkirmish(ranger1);
		assertTrue(scn.IsCharSkirmishingAgainst(ranger1, gollum));
		assertEquals(6, scn.GetStrength(ranger1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
