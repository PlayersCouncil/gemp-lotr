package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "103_76");
					put("witchking", "8_84");
					put("rider", "12_161");

					put("aragorn", "1_89");
					put("pathfinder", "1_110");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Nelya, Glorified to Conquer
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: Fierce.
		* 	At the start of each skirmish involving a Nazgul, you may exert this minion to make that Nazgul strength +1 for each of your sites on the adventure path (limit +3 unless you can spot another Nazgul).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", card.getBlueprint().getTitle());
		assertEquals("Glorified to Conquer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}




// ======== SITE COUNTING TESTS ========

	@Test
	public void NelyaPumpsNazgulByNumberOfShadowOwnedSitesWithNoLimitWhenAnotherNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(4);  // Sites 2-4 owned by Shadow (4 sites), site 1 owned by Freeps, site 5 about to be shadow
		scn.MoveMinionsToTable(nelya, witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.SkipToAssignments();

		int witchkingBaseStrength = scn.GetStrength(witchking);

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Trigger at start of skirmish
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		// Witch-king auto-selected as only Nazgul in skirmish

		// 2 Nazgul spotted, no limit - should be +4 (Shadow owns sites 2-5)
		// NOT +5 (which would incorrectly count Freeps' site 1)
		assertEquals(witchkingBaseStrength + 4, scn.GetStrength(witchking));
	}

	@Test
	public void NelyaLimitedToPlus3WithOnlyOneNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(5);  // Shadow owns 4 sites
		scn.MoveMinionsToTable(nelya);  // Only 1 Nazgul
		scn.MoveCompanionsToTable(aragorn);
		scn.SkipToAssignments();

		int nelyaBaseStrength = scn.GetStrength(nelya);

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.ShadowAcceptOptionalTrigger();
		// Nelya auto-selected as only Nazgul in skirmish

		// Only 1 Nazgul spotted, so limited to +3 (even though 4 Shadow sites)
		assertEquals(nelyaBaseStrength + 3, scn.GetStrength(nelya));
	}

	@Test
	public void NelyaTriggerNotAvailableWhenExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nelya, witchking);
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(nelya, 2);  // Vitality 3, exhausted
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Can't exert when exhausted
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void NelyaBonusScalesWithSiteCount() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveMinionsToTable(nelya, witchking);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.SkipToAssignments();

		int witchkingBaseStrength = scn.GetStrength(witchking);

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.ShadowAcceptOptionalTrigger();
		// Witch-king auto-selected as only Nazgul in skirmish

		// Only +1 at site 2 (Shadow owns just site 2)
		assertEquals(witchkingBaseStrength + 1, scn.GetStrength(witchking));
	}

	@Test
	public void NelyaGivesNoBonusWithZeroShadowSites() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();

		// Play Pathfinder during Fellowship phase to play site 2 as Freeps' site
		scn.FreepsPlayCard(pathfinder);

		// Now move to site 2 - both site 1 (bid winner) and site 2 (Pathfinder) are Freeps-owned
		scn.FreepsChooseToMove();

		scn.MoveMinionsToTable(nelya, witchking);
		scn.SkipToAssignments();

		int witchkingBaseStrength = scn.GetStrength(witchking);

		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		scn.ShadowAcceptOptionalTrigger();
		// Witch-king auto-selected as only Nazgul in skirmish

		// Shadow owns 0 sites, so +0 bonus
		assertEquals(witchkingBaseStrength, scn.GetStrength(witchking));
	}
}
