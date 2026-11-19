package com.gempukku.lotro.virtualtable;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Tests of the rig itself: SetFreepsAdventureDeck and SetShadowAdventureDeck, which make a player's adventure
 * deck exactly the sites given.  Every other site of that deck is set aside in no zone at all; the sites that
 * stay keep their order; a site set aside may be named again and goes back in; the adventure path and the other
 * player's adventure deck are left alone; and the deck that is left is the deck the game lays sites from.  The use
 * is the board on which a player has one site left in his adventure deck, or none, which a game reaches when the
 * sites on the path are his own.
 */
public class AdventureDeckSettingTests
{
	// The Fellowship-block path: site 1 is laid at set-up from the first player's adventure deck, and each later
	// site by its number from the adventure deck of whoever is the Shadow player as the fellowship moves there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a card that is no site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Fails unless the rig refuses the call, saying each of the given things.
	private static void assertRefused(Runnable call, String... said) {
		String refusal = null;
		try {
			call.run();
		}
		catch (AssertionError error) {
			refusal = String.valueOf(error.getMessage());
		}
		assertNotNull("The rig did not refuse the call.", refusal);
		for (String piece : said)
			assertTrue("The refusal does not say '" + piece + "': " + refusal, refusal.contains(piece));
	}

	@Test
	public void TheAdventureDeckIsExactlyTheSitesGivenAndItsOtherSitesAreInNoZone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Set-up has already laid the first player's site 1, which is found on the path by its number.
		var site1 = scn.GetFreepsSite(1);
		var site2 = scn.GetFreepsSite("site2");
		var site3 = scn.GetFreepsSite("site3");
		var site4 = scn.GetFreepsSite("site4");
		var site5 = scn.GetFreepsSite("site5");
		var site6 = scn.GetFreepsSite("site6");
		var site7 = scn.GetFreepsSite("site7");
		var site8 = scn.GetFreepsSite("site8");
		var site9 = scn.GetFreepsSite("site9");
		PhysicalCardImpl[] shadowSites = {
				scn.GetShadowSite("site1"), scn.GetShadowSite("site2"), scn.GetShadowSite("site3"),
				scn.GetShadowSite("site4"), scn.GetShadowSite("site5"), scn.GetShadowSite("site6"),
				scn.GetShadowSite("site7"), scn.GetShadowSite("site8"), scn.GetShadowSite("site9")
		};

		scn.StartGame();

		// The first player's site 1 is on the adventure path and his other eight sites are in his adventure deck.
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertInZone(Zone.ADVENTURE_DECK, site2, site3, site4, site5, site6, site7, site8, site9);
		assertInZone(Zone.ADVENTURE_DECK, shadowSites);

		scn.SetFreepsAdventureDeck(site3, site5);

		// The two named stay; the other six are in no zone; the path and the other player's deck are untouched.
		assertInZone(Zone.ADVENTURE_DECK, site3, site5);
		for (var site : new PhysicalCardImpl[] { site2, site4, site6, site7, site8, site9 })
			assertNull(site.getZone());
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertSame(site1, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, shadowSites);

		// No site named: an empty adventure deck.
		scn.SetFreepsAdventureDeck();
		for (var site : new PhysicalCardImpl[] { site2, site3, site4, site5, site6, site7, site8, site9 })
			assertNull(site.getZone());
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertInZone(Zone.ADVENTURE_DECK, shadowSites);

		// A site that was set aside is still found by the name the table gave it, and goes back in when named.
		assertSame(site4, scn.GetFreepsSite("site4"));
		scn.SetFreepsAdventureDeck(site4);
		assertInZone(Zone.ADVENTURE_DECK, site4);
		for (var site : new PhysicalCardImpl[] { site2, site3, site5, site6, site7, site8, site9 })
			assertNull(site.getZone());

		// Nothing was played and nobody was asked anything new.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheDeckThatIsLeftIsTheDeckTheGameLaysSitesFrom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var site2 = scn.GetShadowSite("site2");
		var site3 = scn.GetShadowSite("site3");

		scn.StartGame();

		// The second player, the Shadow player of this turn, is left with his sites 2 and 3 and nothing else.
		scn.SetShadowAdventureDeck(site3, site2);
		assertInZone(Zone.ADVENTURE_DECK, site2, site3);
		assertNull(scn.GetShadowSite("site4").getZone());
		assertNull(scn.GetShadowSite("site1").getZone());

		// The fellowship moves to site 2, which the game lays from that deck by its number.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(site2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The first player's adventure deck was not touched.
		assertInZone(Zone.ADVENTURE_DECK, scn.GetFreepsSite("site2"), scn.GetFreepsSite("site9"));
	}

	@Test
	public void ACardThatIsNotASiteOfThatPlayersAdventureDeckIsRefusedAndNothingMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var site1 = scn.GetFreepsSite(1);
		var site2 = scn.GetFreepsSite("site2");
		var site3 = scn.GetFreepsSite("site3");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();

		// A card that is no site.
		assertRefused(() -> scn.SetFreepsAdventureDeck(site2, aragorn),
				"Aragorn", "cannot be put in the adventure deck of " + scn.P1(), "an adventure deck holds only its owner's sites");
		// The other player's site.
		assertRefused(() -> scn.SetFreepsAdventureDeck(shadowSite2),
				"a card of " + scn.P2() + "'s", "cannot be put in the adventure deck of " + scn.P1());
		// A site that lies on the adventure path is not taken off it.
		assertRefused(() -> scn.SetFreepsAdventureDeck(site2, site1),
				"Bag End", "ADVENTURE_PATH", "SetAdventureDeck keeps or puts back only sites of the adventure deck itself");
		// A name that is nobody's at the table.
		assertRefused(() -> scn.SetAdventureDeck("Nobody", site2),
				"SetAdventureDeck was asked to set the adventure deck of 'Nobody', who is not a player at this table.");

		// Each refusal came before anything was moved.
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertInZone(Zone.ADVENTURE_DECK, site2, site3, shadowSite2);
		assertInZone(Zone.DECK, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
