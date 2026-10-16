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

public class Card_04_265_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider1", "4_265");
					put("rider2", "4_265");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text

					// A wound to a minion in the middle of a skirmish, from the Free Peoples side.
					put("numenor", "5_37");     // Men of Numenor: Skirmish event, cost 0: wound a minion bearing a [gondor] fortification
					put("wall", "5_33");        // City Wall: a [gondor] fortification; its bearer is strength -3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The only companions in the deck are the two Riders, Haethen and Merry, so that the starting fellowship offers no
	// one else.
	protected VirtualTableScenario GetStartingFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider1", "4_265");
					put("rider2", "4_265");
					put("haethen", "12_116");   // Haethen, Veteran Fighter: a [rohan] Man companion of cost 3; his texts read only skirmishes
					put("merry", "1_302");      // Merry, Friend to Sam: a companion of cost 1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EliteRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Elite Rider
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: When this companion is in your starting fellowship, his twilight cost is -1.
		 * 	While skirmishing a wounded minion, this companion is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rider1");

		assertEquals("Elite Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void InTheStartingFellowshipEachEliteRiderCostsTwoAndAnotherRohanManCostsHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartingFellowshipScenario();

		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var haethen = scn.GetFreepsCard("haethen");
		var merry = scn.GetFreepsCard("merry");

		// The game has not been started: player 1 is choosing his starting fellowship.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider1, rider2, haethen, merry));

		scn.FreepsChooseCard(rider1);
		// 3 - 1 of the 4 a starting fellowship may cost.  The second Rider's own -1 is his alone: he costs 2, not 1,
		// and still fits.  Haethen, a [rohan] Man at his printed 3, does not.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider2, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(haethen));

		scn.FreepsChooseCard(rider2);
		// 2 + 2 = 4: not even Merry fits, and player 2 is asked.  Had the second Rider cost 1, Merry would still be
		// offered.
		assertInZone(Zone.FREE_CHARACTERS, rider1, rider2);
		assertInZone(Zone.DECK, haethen, merry);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void PlayedFromHandInTheFellowshipPhaseHeCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider1 = scn.GetFreepsCard("rider1");
		scn.MoveCardsToHand(rider1);

		scn.StartGame();
		assertInHand(rider1);
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetTwilightCost(rider1));

		scn.FreepsPlayCard(rider1);
		assertInZone(Zone.FREE_CHARACTERS, rider1);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HeIsStrengthPlus2OnlyWhileTheMinionHeIsSkirmishingIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(rider1, rider2);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);
		// The Savage is unwounded and bears the fortification Men of Numenor needs.  The Troop has two wounds, and
		// the first Rider one.
		scn.AttachCardsTo(savage, wall);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(rider1, 1);
		assertAttachedTo(wall, savage);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(rider1));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{rider1, savage},
				new PhysicalCardImpl[]{rider2, troop});
		assertEquals(6, scn.GetStrength(rider1));
		assertEquals(6, scn.GetStrength(rider2));

		scn.FreepsResolveSkirmish(rider1);
		// Against an unwounded minion nothing, though the Rider himself is wounded.
		assertTrue(scn.IsCharSkirmishingAgainst(rider1, savage));
		assertEquals(6, scn.GetStrength(rider1));
		// The other Rider is assigned to a wounded minion, but that skirmish has not begun.
		assertEquals(6, scn.GetStrength(rider2));

		scn.FreepsPlayCard(numenor);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(8, scn.GetStrength(rider1));
		assertEquals(6, scn.GetStrength(rider2));

		// The Savage, 5 - 3 for the wall, is beaten by more than double.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertEquals(6, scn.GetStrength(rider1));

		scn.FreepsResolveSkirmish(rider2);
		// His own skirmish against the wounded Troop: his own +2, and nothing from the first Rider, who is in none.
		assertTrue(scn.IsCharSkirmishingAgainst(rider2, troop));
		assertEquals(8, scn.GetStrength(rider2));
		assertEquals(6, scn.GetStrength(rider1));

		// 8 against 13: one wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
