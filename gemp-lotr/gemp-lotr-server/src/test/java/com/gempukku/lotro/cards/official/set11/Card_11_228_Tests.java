package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_228_Tests
{

	// The turn-skippers walk a Fellowship-block path, which they cannot do on a Shadows-block one.  Each adventure
	// deck has Anduin Confluence in place of site 7, and the tests put the Shadow player's copy on the path as the
	// site the fellowship moves to from site 6.  No minion is laid, so each move goes from the Shadow phase
	// straight to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally, home 6; a fellowship ability only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, home 3; a fellowship ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, home 3; a fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion, who is no ally
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "11_228");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinConfluenceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Anduin Confluence
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>River</b>. When the fellowship moves to this site, discard each ally from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site7");

		assertEquals("Anduin Confluence", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site prints no site number.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void MovingToAnduinConfluenceDiscardsEachAllyOfTheFellowshipAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var legolas = scn.GetFreepsCard("legolas");
		// The Shadow player's own ally: his Free Peoples cards are not active on this turn, and so not affected.
		var shadowGrimir = scn.GetShadowCard("grimir");
		var anduin = scn.GetShadowSite("site7");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardToAdventurePath(anduin);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(celeborn, bilbo, shadowGrimir);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertSame(anduin, scn.GetSite(7));
		assertInZone(Zone.SUPPORT, celeborn, bilbo, shadowGrimir);
		assertInZone(Zone.FREE_CHARACTERS, legolas);

		scn.FreepsPass();

		assertSame(anduin, scn.GetCurrentSite());
		assertInZone(Zone.DISCARD, celeborn, bilbo);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.SUPPORT, shadowGrimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingOnFromAnduinConfluenceDiscardsNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var anduin = scn.GetShadowSite("site7");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardToAdventurePath(anduin);
		scn.FreepsPass();
		assertSame(anduin, scn.GetCurrentSite());
		scn.SkipToPhase(Phase.REGROUP);

		// An ally who came into play at Anduin Confluence, after the move there.
		scn.MoveAlliesToTable(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
