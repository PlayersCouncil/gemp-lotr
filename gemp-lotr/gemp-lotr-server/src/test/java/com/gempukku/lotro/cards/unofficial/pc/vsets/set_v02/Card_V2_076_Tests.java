package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_076_Tests
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
	public void JourneyOfTheKingStatsAndKeywordsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: V2
		 * Name: Journey of the King
		 * Unique: false
		 * Side: none
		 * Culture: none
		 * Twilight Cost: 0
		 * Type: Map
		 * Game Text: Before the game begins, place this Map in your support area.
		 * 	Your adventure deck must only contain cards from the King site path.
		 */

		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.KingSites);

		var card = VirtualTableScenario.FindCard(deck.getMap());

		assertEquals("Journey of the King", card.getTitle());
		assertNull(card.getSubtitle());
		assertFalse(card.isUnique());
		// A Map is neither a Free Peoples card nor a Shadow card, and has no culture.
		assertNull(card.getSide());
		assertNull(card.getCulture());
		assertEquals(CardType.MAP, card.getCardType());
		assertEquals(0, card.getTwilightCost());
	}

	@Test
	public void KingSitesPassWithKingMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.KingSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// One site of each number, from every set that has King sites: sets 7, 8 and 10, V3, and the errata
		// set 57.  A site as the PC errata'd it is a card of its own, and is the one PC Movie plays.
		var sitesOfEverySet = new HashMap<String, String>() {{
			put("site1", "57_332");    // Rohirrim Road, as errata'd (set 57)
			put("site2", "7_335");     // King's Tent (set 7)
			put("site3", "10_117");    // Base of Mindolluin (set 10)
			put("site4", "8_118");     // City of the Dead (set 8)
			put("site5", "10_119");    // Steward's Tomb (set 10)
			put("site6", "7_350");     // Minas Tirith Seventh Circle (set 7)
			put("site7", "8_120");     // Osgiliath Channel (set 8)
			put("site8", "103_122");   // Desolation of Ash (V3)
			put("site9", "7_360");     // Dagorlad (set 7)
		}};
		deck = dvs.buildDeckWithMap("102_76", sitesOfEverySet);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void FellowshipSitesFailWithKingMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.FellowshipSites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		// The same nine sites under their own Map, Path of the Fellowship: it is this Map that turns them away.
		deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.FellowshipSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void TowersSitesFailWithKingMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.TowersSites);

		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void ShadowsSitesFailWithKingMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.ShadowsSites);

		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void OneSiteOfAnotherPathAmongKingSitesFailsWithKingMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.KingSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// Each time, one site takes the place of Dagorlad, the ninth of the sites that have just passed.
		var sites = new HashMap<>(DeckValidationScenario.KingSites);
		sites.put("site9", "1_360");     // Emyn Muil, the Fellowship path's site 9
		deck = dvs.buildDeckWithMap("102_76", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		sites.put("site9", "4_363");     // Palantír Chamber, the Towers path's site 9
		deck = dvs.buildDeckWithMap("102_76", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		sites.put("site9", "17_146");    // Falls of Rauros, a Shadows site: those have no number
		deck = dvs.buildDeckWithMap("102_76", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void HobbitSitesFailWithKingMap() {
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
		var deck = dvs.buildDeckWithMap("102_76", hobbitSites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		var sites = new HashMap<>(DeckValidationScenario.KingSites);
		sites.put("site9", "30_57");     // Battle of the Five Armies, the Hobbit path's site 9
		deck = dvs.buildDeckWithMap("102_76", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void KingMapIsNotLookedAtInAFormatThatDoesNotUseMaps() {
		var movie = GetScenario();
		var deck = movie.buildDeckWithMap("102_76", DeckValidationScenario.FellowshipSites);
		assertTrue(movie.hasMapError(movie.validate(deck)));

		// The same Map and the same nine sites, in a format with no Maps.
		var multipath = GetMultipathScenario();
		deck = multipath.buildDeckWithMap("102_76", DeckValidationScenario.FellowshipSites);
		assertFalse(multipath.hasMapError(multipath.validate(deck)));
	}
}
