package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_030_Tests
{

	// Northern Signal-fire, Flame of Nardol (103_30) Tests

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nardol", "103_30");
					put("beacon1", "103_35");
					put("beacon2", "103_35");

					put("aragorn", "1_89");

					// Valid discard targets (Gondor/Rohan conditions or possessions)
					put("brego", "13_63");           // Gondor Possession (attached)
					put("arrowslits", "5_80");       // Rohan support Condition
					put("stronghold", "102_48");     // Rohan support Possession
					put("citadel", "5_32");          // Gondor support Condition

					// Invalid discard targets
					put("sapling", "9_35");          // Gondor Artifact - not condition/possession
					put("lordofmoria", "1_21");      // Dwarven Condition - not Gondor/Rohan
					put("sting", "1_313");           // Shire Possession - not Gondor/Rohan

					put("runner", "1_178");          // Generic minion for archery phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Nardol
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		* 	Archery: Hinder this beacon and discard any number of [Gondor] or [Rohan] conditions or possessions to make the fellowship archery total +1 for each card discarded.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nardol");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Nardol", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



//
// Extra Cost tests - requires hindering 2 beacons
//

	@Test
	public void NardolCannotPlayWithoutTwoBeaconsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(nardol);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(nardol));
	}

	@Test
	public void NardolCanPlayByHinderingTwoBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(nardol);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(nardol));

		scn.FreepsPlayCard(nardol);
		scn.FreepsChooseCards(beacon1, beacon2);

		assertInZone(Zone.SUPPORT, nardol);
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
	}

//
// Archery ability tests - hinder self and discard to boost archery total
//


	@Test
	public void NardolCanDiscardZeroCardsAndHindersSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var citadel = scn.GetFreepsCard("citadel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(nardol, citadel);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		int baseArchery = scn.GetFreepsArcheryTotal();

		scn.FreepsUseCardAction(nardol);
		// Choose to discard nothing
		scn.FreepsChoose("");

		// Archery total unchanged (discarded 0 cards)
		assertEquals(baseArchery, scn.GetFreepsArcheryTotal());
		// Citadel still in play
		assertInZone(Zone.SUPPORT, citadel);
		assertTrue(scn.IsHindered(nardol));
	}

	@Test
	public void NardolDiscardingOneGondorPossessionGivesPlus1Archery() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var brego = scn.GetFreepsCard("brego"); // Gondor Possession
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brego);
		scn.MoveCardsToSupportArea(nardol);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		int baseArchery = scn.GetFreepsArcheryTotal();

		scn.FreepsUseCardAction(nardol);
		scn.FreepsChooseCard(brego);

		assertEquals(baseArchery + 1, scn.GetFreepsArcheryTotal());
		assertInZone(Zone.DISCARD, brego);
	}

	@Test
	public void NardolDiscardingOneRohanConditionGivesPlus1Archery() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nardol = scn.GetFreepsCard("nardol");
		var arrowslits = scn.GetFreepsCard("arrowslits"); // Rohan Condition
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(nardol, arrowslits);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		int baseArchery = scn.GetFreepsArcheryTotal();

		scn.FreepsUseCardAction(nardol);
		scn.FreepsChooseCard(arrowslits);

		assertEquals(baseArchery + 1, scn.GetFreepsArcheryTotal());
		assertInZone(Zone.DISCARD, arrowslits);
	}

	@Test
	public void NardolDiscardingMultipleCardsGivesCorrespondingArcheryBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var nardol = scn.GetFreepsCard("nardol");
		var citadel = scn.GetFreepsCard("citadel");       // Gondor Condition
		var arrowslits = scn.GetFreepsCard("arrowslits"); // Rohan Condition
		var stronghold = scn.GetFreepsCard("stronghold"); // Rohan Possession
		var sapling = scn.GetFreepsCard("sapling"); // Gondor Artifact - not valid
		var lordofmoria = scn.GetFreepsCard("lordofmoria"); // Dwarven Condition - not valid
		var sting = scn.GetFreepsCard("sting");     // Shire Possession - not valid
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToSupportArea(nardol, citadel, arrowslits, stronghold, sapling, lordofmoria);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);

		int baseArchery = scn.GetFreepsArcheryTotal();

		scn.FreepsUseCardAction(nardol);
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoicesAvailable(citadel, arrowslits, stronghold));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(sapling, lordofmoria, sting));
		scn.FreepsChooseCards(citadel, arrowslits, stronghold);

		// +3 for discarding 3 cards
		assertEquals(baseArchery + 3, scn.GetFreepsArcheryTotal());
		assertInZone(Zone.DISCARD, citadel);
		assertInZone(Zone.DISCARD, arrowslits);
		assertInZone(Zone.DISCARD, stronghold);
	}

}
