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

	// A PC Movie table.  Player 1's deck brings this Map and player 2's brings Path of the Fellowship, so that it
	// can be told whose Map lies where.  Both decks hold the King sites: a game does not hold a deck to
	// its Map, deck validation does.
	protected VirtualTableScenario GetTableScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a companion in each draw deck, so that the game stops to
					// ask for a starting fellowship before any hand is drawn.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie,
				VirtualTableScenario.KingMap,
				VirtualTableScenario.FellowshipMap
		);
	}

	// The same two decks, Maps and all, at a Multipath table.
	protected VirtualTableScenario GetMultipathTableScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath,
				VirtualTableScenario.KingMap,
				VirtualTableScenario.FellowshipMap
		);
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
	public void BeforeTheGameBeginsEachPlayersMapIsInItsOwnersSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTableScenario();

		var map = scn.GetFreepsMap();
		var theirMap = scn.GetShadowMap();

		// The players have bid and been seated, and no more: no fellowship has been chosen and no hand drawn.
		// Each player's Map is already in that player's support area.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals("102_76", map.getBlueprintId());
		assertEquals(scn.P1(), map.getOwner());
		assertInZone(Zone.SUPPORT, map);
		assertEquals("102_74", theirMap.getBlueprintId());
		assertEquals(scn.P2(), theirMap.getOwner());
		assertInZone(Zone.SUPPORT, theirMap);

		// And there they stay once the game is under way.
		scn.StartGame();
		assertInZone(Zone.SUPPORT, map, theirMap);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void KingMapIsNotInTheGameInAFormatThatDoesNotUseMaps() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathTableScenario();

		// The decks that put a Map in each support area at a PC Movie table: here neither player has one.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertNull(scn.GetFreepsMap());
		assertNull(scn.GetShadowMap());
	}

	@Test
	public void KingSitesPassWithKingMap() {
		var dvs = GetScenario();

		// Nothing at all is said against the deck: not by the Map, and not by the format on any other ground.
		var deck = dvs.buildDeckWithMap("102_76", DeckValidationScenario.KingSites);
		assertEquals(List.of(), dvs.validate(deck));

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
		assertEquals(List.of(), dvs.validate(deck));
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
		// The Map names the stray site, and none of the eight that are of its own path.
		assertEquals(List.of("Emyn Muil"), dvs.getMapErrorSites(dvs.validate(deck)));

		sites.put("site9", "4_363");     // Palantír Chamber, the Towers path's site 9
		deck = dvs.buildDeckWithMap("102_76", sites);
		assertEquals(List.of("Palantír Chamber"), dvs.getMapErrorSites(dvs.validate(deck)));

		sites.put("site9", "17_146");    // Falls of Rauros, a Shadows site: those have no number
		deck = dvs.buildDeckWithMap("102_76", sites);
		assertEquals(List.of("Falls of Rauros"), dvs.getMapErrorSites(dvs.validate(deck)));
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
