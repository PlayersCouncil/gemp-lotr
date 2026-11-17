package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_353_Tests
{

	// Anduin Confluence is site 7 of the Fellowship sites.  No minion is laid, so each move goes from the Shadow
	// phase straight to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally, home 6; a fellowship ability only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, home 3; a fellowship ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, home 3; a fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion, who is no ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinConfluenceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Anduin Confluence
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. When the fellowship moves to Anduin Confluence, discard every ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Anduin Confluence", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void MovingToAnduinConfluenceDiscardsEveryAllyOfTheFellowshipAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var legolas = scn.GetFreepsCard("legolas");
		// The Shadow player's own ally: his Free Peoples cards are not active on this turn, and so not affected.
		var shadowGrimir = scn.GetShadowCard("grimir");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(celeborn, bilbo, shadowGrimir);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, celeborn, bilbo, shadowGrimir);

		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, celeborn, bilbo);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.SUPPORT, shadowGrimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingOnFromAnduinConfluenceDiscardsNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");

		scn.StartGame();
		scn.SkipToSite(7);
		// An ally who came into play at Anduin Confluence, after the move there.
		scn.MoveAlliesToTable(celeborn);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, celeborn);

		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
