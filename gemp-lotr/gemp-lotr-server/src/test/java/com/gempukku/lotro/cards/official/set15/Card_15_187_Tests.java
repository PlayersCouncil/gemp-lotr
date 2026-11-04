package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_187_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Four archers in the fellowship: a fellowship archery total of 4.
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: Archer; +3 only against a Nazgûl
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: Aragorn is an archer; an archery ability used only on purpose
					put("elf1", "1_53");        // Lórien Elf: an Elf companion with no text in play
					put("elf2", "1_53");
					put("elvenbow1", "1_41");   // Elven Bow: "Bearer is an archer", and nothing else
					put("elvenbow2", "1_41");

					put("marksman", "1_176");   // Goblin Marksman: a Shadow archer, Archer its whole text
				}},
				new HashMap<>()
				{{
					// Anduin River is a Shadows-block site; the tests lay the Shadow player's copy as the next site.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("river", "15_187");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinRiverStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Anduin River
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>River</b>. The fellowship archery total is -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("river");

		assertEquals("Anduin River", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtTheRiverTheFellowshipArcheryTotalIsThreeLessAndTheMinionArcheryTotalIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowSite("river");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), aragorn, elf1, elf2);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("bow"));
		scn.AttachCardsTo(elf1, scn.GetFreepsCard("elvenbow1"));
		scn.AttachCardsTo(elf2, scn.GetFreepsCard("elvenbow2"));
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveCardToAdventurePath(river);
		// A minion laid for its archery total only; it stays in play as the fellowship moves.
		scn.MoveMinionsToTable(marksman);

		// The River lies ahead: its text is not yet in force.
		assertEquals(4, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.FreepsPass();

		assertSame(river, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 4 - 3: a -2 would read 2, a -4 would read 0.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The Marksman's total is not the fellowship's.
		assertEquals(1, scn.GetShadowArcheryTotal());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheRiverItsArcheryTotalIsWholeAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var river = scn.GetShadowSite("river");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("legolas"), aragorn, elf1, elf2);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("bow"));
		scn.AttachCardsTo(elf1, scn.GetFreepsCard("elvenbow1"));
		scn.AttachCardsTo(elf2, scn.GetFreepsCard("elvenbow2"));

		scn.StartGame();
		scn.MoveCardToAdventurePath(river);
		scn.FreepsPass();

		assertSame(river, scn.GetCurrentSite());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// No minion is played: the turn goes on to the regroup phase, and the fellowship moves on to site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(river, scn.GetSite(2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The text of a site binds only while the fellowship is at it.
		assertEquals(4, scn.GetFreepsArcheryTotal());
	}
}
