package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_074_Tests
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
	public void PathOfTheFellowshipStatsAndKeywordsAreCorrect() throws CardNotFoundException {
		/**
		 * Set: V2
		 * Name: Path of the Fellowship
		 * Unique: false
		 * Side: none
		 * Culture: none
		 * Twilight Cost: 0
		 * Type: Map
		 * Game Text: Before the game begins, place this Map in your support area.
		 * 	Your adventure deck must only contain cards from the Fellowship site path.
		 */

		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.FellowshipSites);

		var card = VirtualTableScenario.FindCard(deck.getMap());

		assertEquals("Path of the Fellowship", card.getTitle());
		assertNull(card.getSubtitle());
		assertFalse(card.isUnique());
		// A Map is neither a Free Peoples card nor a Shadow card, and has no culture.
		assertNull(card.getSide());
		assertNull(card.getCulture());
		assertEquals(CardType.MAP, card.getCardType());
		assertEquals(0, card.getTwilightCost());
	}

	@Test
	public void FellowshipSitesPassWithFellowshipMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.FellowshipSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// One site of each number, from every set that has Fellowship sites: sets 1, 2 and 3, V1, and the errata
		// sets 51 and 53.  A site as the PC errata'd it is a card of its own, and is the one PC Movie plays.
		var sitesOfEverySet = new HashMap<String, String>() {{
			put("site1", "2_115");     // Hobbiton Party Field (set 2)
			put("site2", "51_331");    // Ettenmoors, as errata'd (set 51)
			put("site3", "3_119");     // House of Elrond (set 3)
			put("site4", "1_343");     // Balin's Tomb (set 1)
			put("site5", "1_349");     // The Bridge of Khazad-dum (set 1)
			put("site6", "2_120");     // Valley of the Silverlode (set 2)
			put("site7", "3_118");     // The Great River (set 3)
			put("site8", "101_63");    // Nen Hithoel (V1)
			put("site9", "53_120");    // Wastes of Emyn Muil, as errata'd (set 53)
		}};
		deck = dvs.buildDeckWithMap("102_74", sitesOfEverySet);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void TowersSitesFailWithFellowshipMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.TowersSites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		// The same nine sites under their own Map, Road to the Towers: it is this Map that turns them away.
		deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.TowersSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void KingSitesFailWithFellowshipMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.KingSites);

		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void ShadowsSitesFailWithFellowshipMap() {
		var dvs = GetScenario();
		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.ShadowsSites);

		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void OneSiteOfAnotherPathAmongFellowshipSitesFailsWithFellowshipMap() {
		var dvs = GetScenario();

		var deck = dvs.buildDeckWithMap("102_74", DeckValidationScenario.FellowshipSites);
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// Each time, one site takes the place of Emyn Muil, the ninth of the sites that have just passed.
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site9", "4_363");     // Palantír Chamber, the Towers path's site 9
		deck = dvs.buildDeckWithMap("102_74", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		sites.put("site9", "7_360");     // Dagorlad, the King path's site 9
		deck = dvs.buildDeckWithMap("102_74", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		sites.put("site9", "17_146");    // Falls of Rauros, a Shadows site: those have no number
		deck = dvs.buildDeckWithMap("102_74", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void HobbitSitesFailWithFellowshipMap() {
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
		var deck = dvs.buildDeckWithMap("102_74", hobbitSites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site9", "30_57");     // Battle of the Five Armies, the Hobbit path's site 9
		deck = dvs.buildDeckWithMap("102_74", sites);
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
	}

	@Test
	public void FellowshipMapIsNotLookedAtInAFormatThatDoesNotUseMaps() {
		var movie = GetScenario();
		var deck = movie.buildDeckWithMap("102_74", DeckValidationScenario.TowersSites);
		assertTrue(movie.hasMapError(movie.validate(deck)));

		// The same Map and the same nine sites, in a format with no Maps.
		var multipath = GetMultipathScenario();
		deck = multipath.buildDeckWithMap("102_74", DeckValidationScenario.TowersSites);
		assertFalse(multipath.hasMapError(multipath.validate(deck)));
	}
}
