package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_031_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a second companion so the fellowship survives the path
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_31", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_31"
		);
	}

	// A format that only permits Fellowship-block sites, so the pool would wrongly be Fellowship-only here if
	// it were still filtered by the game's format legality instead of by block.
	protected VirtualTableScenario GetRestrictiveFormatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"fotr1_block", "94_31", null, null
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89"); // Aragorn, Ranger of the North
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
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MovingFromSite8AsksForNoSiteAndAddsOnlyTheMovesOwnTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		scn.SkipToSite(8);

		assertEquals(8, scn.GetCurrentSiteNumber());
		int twilightBeforeMove = scn.GetTwilight();
		scn.FreepsPass();

		// The fellowship's own site 9 (Emyn Muil 1_360) was laid from an adventure deck, and nobody was asked
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("1_360", scn.GetCurrentSite().getBlueprintId());
		assertFalse(scn.ShadowDecisionAvailable("site from outside the game"));
		// The normal move twilight alone: Emyn Muil's 9 plus 1 per companion (Frodo and Aragorn), and no (4)
		assertEquals(twilightBeforeMove + 9 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
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

		// Slopes of Amon Hen 1_361, Shadow number 9: a site 9 outside both adventure decks, with no
		// triggers to interfere (its text makes [isengard] minions cheaper, and none is played)
		assertTrue(scn.ShadowDecisionAvailable("site from outside the game"));
		assertFalse(scn.FreepsDecisionAvailable("site from outside the game"));
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_361"));
		// "Chooses", with no "may": exactly one
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCardBPFromSelection("1_361");

		assertEquals(10, scn.GetCurrentSiteNumber());
		assertEquals("1_361", scn.GetCurrentSite().getBlueprintId());
		assertEquals("1_361", scn.GetSite(10).getBlueprintId());
		// The normal move twilight -- the site's cost of 9 plus 1 per companion (Frodo and Aragorn) -- plus the (4).
		assertEquals(twilightBeforeMove + 9 + 2 + 4, scn.GetTwilight());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
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
		scn.ShadowChooseCardBPFromSelection("1_361"); // Slopes of Amon Hen, a site 9 with no text to interfere

		// At site 10 with its skirmish phases still to come, the game goes on
		assertEquals(10, scn.GetCurrentSiteNumber());
		assertFalse(scn.GameIsFinished());

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
		assertTrue(scn.ShadowHasBPChoiceAvailable("1_361"));  // Slopes of Amon Hen (Fellowship)
		assertTrue(scn.ShadowHasBPChoiceAvailable("4_360"));  // Fortress of Orthanc (Towers)
		assertTrue(scn.ShadowHasBPChoiceAvailable("7_360"));  // Dagorlad (King)
		assertTrue(scn.ShadowHasBPChoiceAvailable("32_49"));  // Northern Slopes (Hobbit)
		// A Shadows-block site (Rath Dinen 100_1) has no printed site number, so it can never match "site 9".
		assertFalse(scn.ShadowHasBPChoiceAvailable("100_1"));
		// Nor is a site 8 that lies outside the game (Brown Lands 1_357) a site 9.
		assertFalse(scn.ShadowHasBPChoiceAvailable("1_357"));
		// The fellowship's own site 9 (already in the game) is not offered as a choice to replace itself.
		assertFalse(scn.ShadowHasBPChoiceAvailable(VirtualTableScenario.FellowshipSites.get("site9")));

		scn.ShadowChooseCardBPFromSelection("1_361");

		assertEquals("1_361", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
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

	@Test
	public void PlayerTwoAsOwnerDoesNotWinAtSite9AndPlayerOnePicksHisSite10() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.StartGame();
		// Player 2 has to stand on site 9 before player 1, who holds no modifier, wins there: he moves
		// twice on his sixth turn (6 -> 7 -> 8), and on his seventh is a site ahead.
		scn.SkipToSite(6);
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		scn.SkipToPhaseInverted(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowChooseToMove();
		scn.SkipToOtherPlayersTurn(); // player 1's seventh turn: 7 -> 8
		scn.SkipToOtherPlayersTurn(); // player 2's seventh turn, at site 8

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.ShadowPass();
		scn.SkipToPhaseInverted(Phase.REGROUP);

		// The owner's fellowship has survived site 9, and the game goes on
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.GameIsFinished());

		scn.PassCurrentPhaseActions();
		int twilightBeforeMove = scn.GetTwilight();
		scn.ShadowChooseToMove();

		// Player 1 is the Shadow player of this turn: the choice is his
		assertTrue(scn.FreepsDecisionAvailable("site from outside the game"));
		assertFalse(scn.ShadowDecisionAvailable("site from outside the game"));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Fortress of Orthanc: a Towers site 9, Shadow number 9 (more only with over 4 companions)
		scn.FreepsChooseCardBPFromSelection("4_360");

		assertEquals(10, scn.GetCurrentSiteNumber());
		assertEquals("4_360", scn.GetCurrentSite().getBlueprintId());
		// The site's cost of 9 plus 1 for player 2's only companion (Frodo), plus the (4)
		assertEquals(twilightBeforeMove + 9 + 1 + 4, scn.GetTwilight());
		assertFalse(scn.GameIsFinished());

		scn.SkipToPhaseInverted(Phase.REGROUP);

		assertEquals(10, scn.GetCurrentSiteNumber());
		assertTrue(scn.GameIsFinished());
	}
}
