package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_92_003_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the ranger Pathfinder spots
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0: spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_3", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the ranger Pathfinder spots
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0: spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_3"
		);
	}

	// The second player holds the modifier and the first holds Race Text 92_24 ("Your Nazgul are non-unique")
	protected VirtualTableScenario GetShadowScenarioAgainstRaceText92_24() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the ranger Pathfinder spots
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0: spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_24", "92_3"
		);
	}

	// The control: the same table with no modifier
	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: the ranger Pathfinder spots
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0: spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, null
		);
	}

	@Test
	public void GameStartsWhenTheOpponentHoldsAShadowConditionedModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// #1023 setup: the reported game had the first player holding 92_24 (Your Nazgul are non-unique,
		// conditioned on OwnerIsShadow) while the second player held this modifier.  The crash itself turned out
		// to be a pre-game decision problem (see PregameDecisionTests); this pins down that the modifiers
		// themselves start up cleanly in that arrangement.
		var scn = GetShadowScenarioAgainstRaceText92_24();

		scn.StartGame();

		var site1 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GameStartsWhenTheSecondPlayerHoldsTheModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// #1023 setup: the player holding this modifier bid lower and was not going first.
		var scn = GetShadowScenario();

		scn.StartGame();

		var site1 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AllSitesUndergroundStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_3
		 * Type: MetaSite
		 * Intensity: -1
		 * Game Text: All sites gain underground.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_3", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-1, card.getBlueprint().getIntensity());
	}

	@Test
	public void CurrentSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_3: "All sites gain underground."
		// Site 1 (Bag End) does not natively have underground.

		var scn = GetFreepsScenario();

		scn.StartGame();

		var site1 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
		// "All sites", and nothing else: the Ring-bearer is a card in play that is no site
		assertFalse(scn.HasKeyword(scn.GetRingBearer(), Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutTheModNeitherSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// The control: Bag End and Ettenmoors print no underground, and without the modifier they have none.

		var scn = GetNoModScenario();

		scn.StartGame();

		var site1 = scn.GetCurrentSite();
		assertFalse(scn.HasKeyword(site1, Keyword.UNDERGROUND));

		scn.FreepsPass(); // move to site 2

		var site2 = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site1, Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(site2, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PastSiteRetainsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// After moving past site 1, it should still have underground.

		var scn = GetFreepsScenario();

		scn.StartGame();
		scn.FreepsPass(); // move to site 2

		var site1 = scn.GetSite(1);
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));

		var site2 = scn.GetCurrentSite();
		assertTrue(scn.HasKeyword(site2, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedButNotMovedToSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_3: Pathfinder plays the next site from the adventure deck before moving.
		// That site should have underground even before the fellowship moves there.

		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();

		// We're at site 1. Site 2 is in the adventure deck, not yet in play.
		assertNull(scn.GetSite(2));

		// Play Pathfinder — plays site 2 from adventure deck
		scn.FreepsPlayCard(pathfinder);

		// Site 2 should now be in play but fellowship hasn't moved yet
		var site2 = scn.GetSite(2);
		assertNotNull(site2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(site2, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ControlledSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		// 92_3: A site controlled by the shadow player should also have underground.

		var scn = GetFreepsScenario();

		scn.StartGame();

		// Need to get past site 1 with both players so it can be controlled.
		scn.SkipToSite(3);

		scn.ShadowTakeControlOfSite();

		// Site 1 should now be controlled and still have underground
		var site1 = scn.GetSite(1);
		assertNotNull(site1);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.HasKeyword(site1, Keyword.UNDERGROUND));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
