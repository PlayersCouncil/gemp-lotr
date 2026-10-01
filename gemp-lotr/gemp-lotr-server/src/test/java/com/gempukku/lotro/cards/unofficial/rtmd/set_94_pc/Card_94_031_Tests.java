package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_031_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: a second companion so the fellowship survives the path
	}};

	// Slopes of Amon Hen, twilight cost 9: a site 9 outside both adventure decks, with no triggers to interfere
	private static final String EXTRA_SITE = "1_361";
	private static final int EXTRA_SITE_TWILIGHT = 9;

	// One site 9 from each of the other three numbered blocks, to prove the offered pool spans all of them.
	private static final String TOWERS_SITE = "4_360"; // Fortress of Orthanc (Towers)
	private static final String KING_SITE = "7_360"; // Dagorlad (King)
	private static final String HOBBIT_SITE = "32_49"; // Northern Slopes (Hobbit)
	// A Shadows-block site (no printed site number at all) that must never be offered.
	private static final String SHADOWS_SITE = "100_1"; // Rath Dínen

	// A format that only permits Fellowship-block sites, so the pool would wrongly be Fellowship-only here if
	// it were still filtered by the game's format legality instead of by block.
	private static final String FELLOWSHIP_ONLY_FORMAT = "fotr1_block";

	protected VirtualTableScenario GetRestrictiveFormatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				FELLOWSHIP_ONLY_FORMAT, "94_31", null, null
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_31", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_31"
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_31
		 * Type: MetaSite
		 * Intensity: 6
		 * Game Text: Your fellowship cannot win the game until surviving site 10. When moving from site 9, the Shadow
		 * player chooses any site 9 from outside the game to play as the next site and adds (4).
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_31", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(6, mod.getBlueprint().getIntensity());
	}

	@Test
	public void GameDoesNotEndAtSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(9);

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.GameIsFinished());
	}

	@Test
	public void WithoutTheModGameEndsAtSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(9);

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void ShadowPicksSite10FromOutsideTheGameAndMoveAdds4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(9);

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		int twilightBeforeMove = scn.GetTwilight();
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowDecisionAvailable("site from outside the game"));
		assertTrue(scn.ShadowHasBPChoiceAvailable(EXTRA_SITE));

		scn.ShadowChooseCardBPFromSelection(EXTRA_SITE);

		assertEquals(10, scn.GetCurrentSiteNumber());
		assertEquals(EXTRA_SITE, scn.GetCurrentSite().getBlueprintId());
		// The normal move twilight -- the site's cost plus 1 per companion (Frodo and Aragorn) -- plus the (4).
		assertEquals(twilightBeforeMove + EXTRA_SITE_TWILIGHT + 2 + 4, scn.GetTwilight());
		assertFalse(scn.GameIsFinished());
	}

	@Test
	public void GameEndsAfterSurvivingSite10() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(9);

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(EXTRA_SITE);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(10, scn.GetCurrentSiteNumber());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void SitePoolSpansAllFourNumberedBlocksRegardlessOfFormatAndExcludesShadows() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRestrictiveFormatScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(9);

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowDecisionAvailable("site from outside the game"));
		// Fellowship, Towers, King and Hobbit site 9s are all offered even though the table's format
		// (Fellowship Block - Set 1) would only permit Fellowship-block sites in a real deck.
		assertTrue(scn.ShadowHasBPChoiceAvailable(EXTRA_SITE));
		assertTrue(scn.ShadowHasBPChoiceAvailable(TOWERS_SITE));
		assertTrue(scn.ShadowHasBPChoiceAvailable(KING_SITE));
		assertTrue(scn.ShadowHasBPChoiceAvailable(HOBBIT_SITE));
		// A Shadows-block site has no printed site number, so it can never match "site 9".
		assertFalse(scn.ShadowHasBPChoiceAvailable(SHADOWS_SITE));
		// The fellowship's own site 9 (already in the game) is not offered as a choice to replace itself.
		assertFalse(scn.ShadowHasBPChoiceAvailable(VirtualTableScenario.FellowshipSites.get("site9")));

		scn.ShadowChooseCardBPFromSelection(EXTRA_SITE);

		assertEquals(EXTRA_SITE, scn.GetCurrentSite().getBlueprintId());
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsStillWinsAtSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(9);

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertTrue(scn.GameIsFinished());
	}
}
