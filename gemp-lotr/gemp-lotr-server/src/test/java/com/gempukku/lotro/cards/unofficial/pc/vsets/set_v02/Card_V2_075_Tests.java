package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_075_Tests
{
	// A Map is not played and is in no draw deck: it is part of the deck a player brings to the table, like the
	// Ring-bearer and the Ring.  What it says about the adventure deck is checked when the deck is validated,
	// so these tests validate decks.  PC Movie is the format that uses Maps.
	protected DeckValidationScenario GetScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	// Multipath has PC Movie's rule that an adventure deck is all of one path, and does not use Maps.
	protected DeckValidationScenario GetMultipathScenario() {
		return new DeckValidationScenario(DeckValidationScenario.Multipath);
	}

	@Test
	public void RoadToTheTowersStatsAndKeywordsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: V2
		 * Name: Road to the Towers
		 * Unique: false
		 * Side: none
		 * Culture: none
		 * Twilight Cost: 0
		 * Type: Map
		 * Game Text: Before the game begins, place this Map in your support area.
		 * 	Your adventure deck must only contain cards from the Towers site path.
		 */

		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.TowersSites);

		var card = VirtualTableScenario.FindCard(deck.getMap());

		assertEquals("Road to the Towers", card.getTitle());
		assertNull(card.getSubtitle());
		assertFalse(card.isUnique());
		// A Map is neither a Free Peoples card nor a Shadow card, and has no culture.
		assertNull(card.getSide());
		assertNull(card.getCulture());
		assertEquals(CardType.MAP, card.getCardType());
		assertEquals(0, card.getTwilightCost());
	}

	@Test
	public void TowersSitesPassWithTowersMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.TowersSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// One site of each number, from every set that has Towers sites: sets 4, 5 and 6, V2, and the errata
		// set 54.  A site as the PC errata'd it is a card of its own, and is the one PC Movie plays.
		var sitesOfEverySet = new HashMap<String, String>() {{
			put("site1", "6_115");     // Rocks of Emyn Muil (set 6)
			put("site2", "102_67");    // Eastemnet Hills (V2)
			put("site3", "6_117");     // Meduseld (set 6)
			put("site4", "4_346");     // White Rocks (set 4)
			put("site5", "4_348");     // Deeping Wall (set 4)
			put("site6", "6_118");     // Hornburg Hall (set 6)
			put("site7", "54_355");    // Cavern Entrance, as errata'd (set 54)
			put("site8", "5_119");     // Nan Curunír (set 5)
			put("site9", "102_73");    // Isengard Clefts (V2)
		}};
		deck = dvs.buildDeckWithMap("102_75", sitesOfEverySet);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void FellowshipSitesFailWithTowersMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.FellowshipSites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		// The same nine sites under their own Map, Path of the Fellowship: it is this Map that turns them away.
		deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.FellowshipSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void KingSitesFailWithTowersMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.KingSites);

		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void ShadowsSitesFailWithTowersMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.ShadowsSites);

		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void OneSiteOfAnotherPathAmongTowersSitesFailsWithTowersMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.TowersSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// Each time, one site takes the place of Palantír Chamber, the ninth of the sites that have just passed.
		var sites = new HashMap<>(DeckValidationScenario.TowersSites);
		sites.put("site9", "1_360");     // Emyn Muil, the Fellowship path's site 9
		deck = dvs.buildDeckWithMap("102_75", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		sites.put("site9", "7_360");     // Dagorlad, the King path's site 9
		deck = dvs.buildDeckWithMap("102_75", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		sites.put("site9", "17_146");    // Falls of Rauros, a Shadows site: those have no number
		deck = dvs.buildDeckWithMap("102_75", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void HobbitSitesFailWithTowersMap() {
		var dvs = GetScenario();

		// The nine sites of the Hobbit path's first set.
		var hobbitSites = new HashMap<String, String>() {{
			put("site1", "30_49");     // Bag End
			put("site2", "30_50");     // The Troll Hoard
			put("site3", "30_51");     // Rivendell
			put("site4", "30_52");     // Goblin Town
			put("site5", "30_53");     // Mirkwood
			put("site6", "30_54");     // Esgaroth
			put("site7", "30_55");     // Erebor
			put("site8", "30_56");     // Smaug's Den
			put("site9", "30_57");     // Battle of the Five Armies
		}};
		var deck = dvs.buildDeckWithMap("102_75", hobbitSites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		var sites = new HashMap<>(DeckValidationScenario.TowersSites);
		sites.put("site9", "30_57");     // Battle of the Five Armies, the Hobbit path's site 9
		deck = dvs.buildDeckWithMap("102_75", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void TowersMapIsNotLookedAtInAFormatThatDoesNotUseMaps() {
		var movie = GetScenario();
		var deck = movie.buildDeckWithMap("102_75", DeckValidationScenario.FellowshipSites);
		assertTrue(movie.hasMapError(movie.validate(deck)));

		// The same Map and the same nine sites, in a format with no Maps.
		var multipath = GetMultipathScenario();
		deck = multipath.buildDeckWithMap("102_75", DeckValidationScenario.FellowshipSites);
		assertFalse(multipath.hasMapError(multipath.validate(deck)));
	}
}
