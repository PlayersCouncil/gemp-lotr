package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_048_Tests
{

	// The Hobbit table, with Secret Door as site 7 in place of Erebor 30_55.  Site 6 is Esgaroth 30_54 (Shadow
	// number 3; a start-of-regroup offer the site skipper declines).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Thrór's Key: unique [dwarven] possession on a [dwarven] companion; "each time the fellowship
					// moves from site 6 or 7, remove (2)", and a regroup ability at site 6 or lower, not used
					put("key", "32_11");
					// Thrór's Map: unique [dwarven] possession on a [dwarven] companion, of another title; an ability
					// used only on purpose
					put("map", "32_12");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability, not used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site7", "32_48");
				}},
				null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SecretDoorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Secret Door
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 7
		 * Shadow Number: 8
		 * Direction: none
		 * Game Text: <b>Mountain</b>. While you can spot Thrór's Key, the Shadow number of Secret Door is -3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Secret Door", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void WithThrorsKeyTheMoveToSecretDoorAddsThreeLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var key = scn.GetFreepsCard("key");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, key);
		assertAttachedTo(key, balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetTwilight(0);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());
		var esgaroth = scn.GetCurrentSite();
		assertEquals(3, scn.GetTwilightCost(esgaroth));
		scn.FreepsPass();

		var door = scn.GetCurrentSite();
		assertEquals("32_48", door.getBlueprintId());
		assertEquals(5, scn.GetTwilightCost(door));
		// The Key's "moves from site 6" comes first and finds nothing to remove; then 8 - 3 for Secret Door
		// + 2 companions (Bilbo and Balin).
		assertEquals(7, scn.GetTwilight());
		// Esgaroth, behind the fellowship, keeps its own 3.
		assertEquals(3, scn.GetTwilightCost(esgaroth));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnotherDwarvenPossessionTheMoveToSecretDoorAddsTheFullEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var map = scn.GetFreepsCard("map");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, map);
		assertAttachedTo(map, balin);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		var door = scn.GetCurrentSite();
		assertEquals("32_48", door.getBlueprintId());
		assertEquals(8, scn.GetTwilightCost(door));
		// 8 for Secret Door + 2 companions
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThrorsKeyPlayedAtSecretDoorLowersItsShadowNumberByThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var key = scn.GetFreepsCard("key");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(key);
		assertInHand(key);

		scn.StartGame();
		scn.SkipToSite(7);

		var door = scn.GetCurrentSite();
		assertEquals("32_48", door.getBlueprintId());
		// In hand, the Key is not spotted.
		assertInHand(key);
		assertEquals(8, scn.GetTwilightCost(door));

		assertTrue(scn.FreepsPlayAvailable(key));
		scn.FreepsPlayCard(key);

		assertAttachedTo(key, balin);
		assertEquals(5, scn.GetTwilightCost(door));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromSecretDoorItsNumberIsEightAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var key = scn.GetFreepsCard("key");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, key);
		assertAttachedTo(key, balin);

		scn.StartGame();
		scn.SkipToSite(7);

		var door = scn.GetCurrentSite();
		assertEquals("32_48", door.getBlueprintId());
		assertEquals(5, scn.GetTwilightCost(door));

		scn.SkipToSite(8);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertAttachedTo(key, balin);
		assertEquals(8, scn.GetTwilightCost(door));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
