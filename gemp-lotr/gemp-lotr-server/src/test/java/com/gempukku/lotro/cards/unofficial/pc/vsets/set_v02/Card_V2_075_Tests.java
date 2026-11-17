package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
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

	// A PC Movie table.  Player 1's deck brings this Map and player 2's brings Journey of the King, so that it
	// can be told whose Map lies where.  Both decks hold the Towers sites: a game does not hold a deck to
	// its Map, deck validation does.
	protected VirtualTableScenario GetTableScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a companion in each draw deck, so that the game stops to
					// ask for a starting fellowship before any hand is drawn.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie,
				VirtualTableScenario.TowersMap,
				VirtualTableScenario.KingMap
		);
	}

	// The same two decks, Maps and all, at a Multipath table.
	protected VirtualTableScenario GetMultipathTableScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				VirtualTableScenario.TowersMap,
				VirtualTableScenario.KingMap
		);
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
	public void BeforeTheGameBeginsEachPlayersMapIsInItsOwnersSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTableScenario();

		var map = scn.GetFreepsMap();
		var theirMap = scn.GetShadowMap();

		// The players have bid and been seated, and no more: no fellowship has been chosen and no hand drawn.
		// Each player's Map is already in that player's support area.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals("102_75", map.getBlueprintId());
		assertEquals(scn.P1(), map.getOwner());
		assertInZone(Zone.SUPPORT, map);
		assertEquals("102_76", theirMap.getBlueprintId());
		assertEquals(scn.P2(), theirMap.getOwner());
		assertInZone(Zone.SUPPORT, theirMap);

		// And there they stay once the game is under way.
		scn.StartGame();
		assertInZone(Zone.SUPPORT, map, theirMap);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TowersMapIsNotInTheGameInAFormatThatDoesNotUseMaps() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathTableScenario();

		// The decks that put a Map in each support area at a PC Movie table: here neither player has one.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertNull(scn.GetFreepsMap());
		assertNull(scn.GetShadowMap());
	}

	@Test
	public void TowersSitesPassWithTowersMap() {
		var dvs = GetScenario();

		// Nothing at all is said against the deck: not by the Map, and not by the format on any other ground.
		var deck = dvs.buildDeckWithMap("102_75", DeckValidationScenario.TowersSites);
		assertEquals(List.of(), dvs.validate(deck));

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
		assertEquals(List.of(), dvs.validate(deck));
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
		// The Map names the stray site, and none of the eight that are of its own path.
		assertEquals(List.of("Emyn Muil"), dvs.getMapErrorSites(dvs.validate(deck)));

		sites.put("site9", "7_360");     // Dagorlad, the King path's site 9
		deck = dvs.buildDeckWithMap("102_75", sites);
		assertEquals(List.of("Dagorlad"), dvs.getMapErrorSites(dvs.validate(deck)));

		sites.put("site9", "17_146");    // Falls of Rauros, a Shadows site: those have no number
		deck = dvs.buildDeckWithMap("102_75", sites);
		assertEquals(List.of("Falls of Rauros"), dvs.getMapErrorSites(dvs.validate(deck)));
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
