package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_120_Tests
{

	// The Towers path with Caverns of Isengard as its site 9, reached by SkipToSite(8) and the fellowship-phase move.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					// Gollum, Dark as Darkness: strength 5, vitality 4.  "Regroup: Exert Gollum twice to wound the
					// Ring-bearer."  His other abilities are used only on purpose.
					put("gollum", "9_28");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "5_120");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same table with the usual Towers site 9, Palantír Chamber 4_363 (a Shadow action that needs 2 burdens).
	protected VirtualTableScenario GetPalantirChamberScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight", "5_35");
					put("brigand", "4_10");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CavernsofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Caverns of Isengard
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 9T
		 * Shadow Number: 9
		 * Direction: Right
		 * Game Text: <b>Underground</b>. The Free Peoples player wins the game only if the Ring-bearer survives until Shadow players reconcile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Caverns of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void ARingBearerWhoSurvivesTheSkirmishesWinsOnlyOnceTheRegroupPhaseIsOverAndTheShadowPlayerReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("Caverns of Isengard", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, brigand);
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));
		scn.PassCurrentPhaseActions();

		// 8 against 6: the Knight loses and takes his wound; the Ring-bearer has come through every skirmish, and
		// the game goes on into the regroup phase.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.GameIsFinished());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// Both have passed: the Shadow player reconciles, and with that the game is over.
		scn.ShadowPass();
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.ShadowDecisionAvailable("Reconcile"));
		scn.ShadowDeclineReconciliation();
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasWon());
	}

	@Test
	public void ARingBearerKilledInTheRegroupPhaseLosesTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(gollum);
		// Frodo, vitality 4, has one left.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(3, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, gollum);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		// 6 against 5: Gollum loses and takes a wound, and lives.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gollum));
		scn.ShadowUseCardAction(gollum);

		assertEquals(3, scn.GetWoundsOn(gollum));
		assertInZone(Zone.DEAD, frodo);
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasLost());
		assertTrue(scn.ShadowHasWon());
	}

	@Test
	public void AtAnotherSiteNineTheGameIsWonAsTheLastSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPalantirChamberScenario();

		var knight = scn.GetFreepsCard("knight");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("Palantír Chamber", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, brigand);
		scn.FreepsResolveSkirmish(knight);
		assertFalse(scn.GameIsFinished());
		scn.PassCurrentPhaseActions();

		// The same skirmish lost by the Knight; with no Caverns of Isengard there is no regroup phase (CR 4.2
		// "winning the game").
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.FreepsHasWon());
	}
}
