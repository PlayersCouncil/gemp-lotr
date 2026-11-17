package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_119_Tests
{

	// The Towers path with Valley of Saruman as its site 8.  No minion is laid, so each move goes from the Shadow
	// phase straight on.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: [elven] Elf ally, home 6; a fellowship ability only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally, home 3; a fellowship ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, home 3; a fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion, who is no ally
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site8", "6_119");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValleyofSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Valley of Saruman
		 * Unique: False
		 * Shadow Number: 8
		 * Type: Site
		 * Block: Towers
		 * Direction: Right
		 * Site Number: 8T
		 * Game Text: When the fellowship moves to this site, discard all allies.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Valley of Saruman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingToValleyOfSarumanDiscardsAllAlliesOfTheFellowshipAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var legolas = scn.GetFreepsCard("legolas");
		// The Shadow player's own ally: his Free Peoples cards are not active on this turn, and so not affected.
		var shadowGrimir = scn.GetShadowCard("grimir");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(celeborn, bilbo, shadowGrimir);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, celeborn, bilbo, shadowGrimir);

		scn.FreepsPass();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Valley of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.DISCARD, celeborn, bilbo);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.SUPPORT, shadowGrimir);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingOnFromValleyOfSarumanDiscardsNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var celeborn = scn.GetFreepsCard("celeborn");

		scn.StartGame();
		scn.SkipToSite(8);
		// An ally who came into play at Valley of Saruman, after the move there.
		scn.MoveAlliesToTable(celeborn);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals("Valley of Saruman", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.SUPPORT, celeborn);

		scn.FreepsPass();

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
