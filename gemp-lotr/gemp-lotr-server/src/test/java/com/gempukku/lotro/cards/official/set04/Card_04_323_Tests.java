package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_323_Tests
{

	// The Towers path with East Wall of Rohan as its site 1.  Site 2 is Eastfold (Shadow number 2; a Shadow action
	// that needs 3 [isengard] trackers) and site 3 Barrows of Edoras (Shadow number 0; a Fellowship action used only
	// on purpose).  All three are region 1, so no region twilight is added on these moves.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion with a maneuver ability only
				}},
				new HashMap<>()
				{{
					put("site1", "4_323");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EastWallofRohanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: East Wall of Rohan
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 1T
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: When the fellowship moves from East Wall of Rohan, add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("East Wall of Rohan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingFromEastWallOfRohanAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		assertEquals("East Wall of Rohan", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		assertEquals(2, scn.FreepsGetSiteNumber());
		// 2 for East Wall of Rohan + 2 for Eastfold's Shadow number + 2 for Frodo and Aragorn
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSecondPlayersMoveFromEastWallOfRohanAdds2AsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		int before = scn.GetTwilight();

		scn.ShadowPass();

		assertEquals(2, scn.ShadowGetSiteNumber());
		// 2 for East Wall of Rohan + 2 for Eastfold's Shadow number + 2 for his Frodo and Aragorn
		assertEquals(before + 6, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void TheMoveOnFromSite2AddsNothingOfEastWallOfRohan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		// With no minion the turn goes on to the regroup phase at site 2, where the fellowship may move again.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.FreepsGetSiteNumber());
		int before = scn.GetTwilight();

		scn.FreepsChooseToMove();

		assertEquals(3, scn.FreepsGetSiteNumber());
		// 0 for Barrows of Edoras's Shadow number + 2 for Frodo and Aragorn, and nothing more
		assertEquals(before + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
