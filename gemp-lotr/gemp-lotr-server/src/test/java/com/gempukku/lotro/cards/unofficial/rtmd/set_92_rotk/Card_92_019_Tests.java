package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class Card_92_019_Tests
{
	// The text is a rule of deck building and nothing in play, so these tests validate decks: each in the format
	// named, first as a player without the modifier brings it and then with what the modifier itself tells deck
	// validation (its deck building overrides, which a league hands over for the player who has it).

	// Pre-Shadows Multipath: any one site path of the Movie block
	protected DeckValidationScenario GetMultipathScenario() {
		return new DeckValidationScenario(DeckValidationScenario.Multipath);
	}

	// Fellowship block: the Fellowship site path only
	protected DeckValidationScenario GetFellowshipScenario() {
		return new DeckValidationScenario(DeckValidationScenario.Fellowship);
	}

	// PC Movie: any one site path of the Movie block, named by the deck's Map
	protected DeckValidationScenario GetPCMovieScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	// Expanded: Shadows sites only
	protected DeckValidationScenario GetExpandedScenario() {
		return new DeckValidationScenario(DeckValidationScenario.Shadows);
	}

	// A table whose player 1 has the modifier, for the card itself
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_19", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_19
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Your adventure deck may contain sites from any block. (You may either mix any Movie block
		 * 	sites together, or use any Shadow sites, but not both.)
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_19", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	// ===== Movie Block (multipath) =====

	@Test
	public void MovieBlock_CanMixMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetMultipathScenario();
		// The Fellowship sites with Bree Gate at 2, a Towers site at 3 (Barrows of Edoras) and a King site at 6
		// (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks
		var mixedMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedMovieSites.put("site2", "1_327");
		mixedMovieSites.put("site3", "4_337");
		mixedMovieSites.put("site6", "7_350");
		var deck = dvs.buildDeckWithSites(mixedMovieSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Mixed Movie sites should pass with context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void MovieBlock_CanUseShadowsSitesWithContext() throws CardNotFoundException {
		var dvs = GetMultipathScenario();
		// Nine Shadows sites with no more than three of one Shadow number, as an adventure deck of them must be
		// (the rig's own nine have six of Shadow number 0)
		var shadowsSites = new HashMap<>(DeckValidationScenario.ShadowsSites);
		shadowsSites.put("site6", "11_240");    // Flats of Rohan, Shadow number 2
		shadowsSites.put("site7", "11_231");    // Caras Galadhon, Shadow number 2
		shadowsSites.put("site8", "11_250");    // North Undeep, Shadow number 3
		var deck = dvs.buildDeckWithSites(shadowsSites);

		// A player without the modifier is refused this deck.  Multipath makes no objection of block to nine
		// Shadows sites (they are all of one block): what it refuses is their sets, so the whole list is read.
		assertNotEquals(List.of(), dvs.validate(deck));

		// With it nothing at all is said against the deck
		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Shadows sites should pass with context in Movie Block", dvs.hasSiteBlockError(errors));
		assertEquals(List.of(), errors);
	}

	@Test
	public void MovieBlock_CannotMixShadowsAndMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetMultipathScenario();
		// The Fellowship sites with Bree Gate at 2 and a Shadows site, Fangorn Glade, in place of site 5
		var mixedShadowsMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedShadowsMovieSites.put("site2", "1_327");
		mixedShadowsMovieSites.put("site5", "11_239");
		var deck = dvs.buildDeckWithSites(mixedShadowsMovieSites);

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertTrue("Mixed Shadows+Movie sites should fail even with context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void MovieBlock_ASiteTheFormatHasXListedIsStillRefusedWithContext() throws CardNotFoundException {
		var dvs = GetMultipathScenario();

		// Nine Fellowship sites that Multipath takes as they are: Bree Gate at 2 (the rig's Ettenmoors is
		// X-listed) and Slopes of Amon Hen at 9
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "1_327");
		sites.put("site9", "1_361");
		var deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of(), dvs.validate(deck));

		// The same nine with Emyn Muil 1_360 at 9, which Multipath has X-listed.  All nine are of one block, so
		// the block is not what the format objects to.
		sites.put("site9", "1_360");
		deck = dvs.buildDeckWithSites(sites);
		assertNotEquals(List.of(), dvs.validate(deck));
		assertFalse(dvs.hasSiteBlockError(dvs.validate(deck)));

		// "Sites from any block" says nothing of the X-list: the deck is refused with the modifier as without it.
		assertNotEquals(List.of(), dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides()));
	}

	// ===== Fellowship Block =====

	@Test
	public void FellowshipBlock_CanMixMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetFellowshipScenario();
		// The Fellowship sites with Bree Gate at 2, a Towers site at 3 (Barrows of Edoras) and a King site at 6
		// (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks
		var mixedMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedMovieSites.put("site2", "1_327");
		mixedMovieSites.put("site3", "4_337");
		mixedMovieSites.put("site6", "7_350");
		var deck = dvs.buildDeckWithSites(mixedMovieSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Mixed Movie sites should pass with context in Fellowship Block", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void FellowshipBlock_CanUseShadowsSitesWithContext() throws CardNotFoundException {
		var dvs = GetFellowshipScenario();
		var deck = dvs.buildDeckWithSites(DeckValidationScenario.ShadowsSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Shadows sites should pass with context in Fellowship Block", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void FellowshipBlock_CannotMixShadowsAndMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetFellowshipScenario();
		// The Fellowship sites with Bree Gate at 2 and a Shadows site, Fangorn Glade, in place of site 5
		var mixedShadowsMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedShadowsMovieSites.put("site2", "1_327");
		mixedShadowsMovieSites.put("site5", "11_239");
		var deck = dvs.buildDeckWithSites(mixedShadowsMovieSites);

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertTrue("Mixed Shadows+Movie sites should fail even with context in Fellowship Block", dvs.hasSiteBlockError(errors));
	}

	// ===== PC-Movie (with maps) =====

	@Test
	public void PCMovie_WithFellowshipMap_CanMixMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();
		// The Fellowship sites with Bree Gate at 2, a Towers site at 3 (Barrows of Edoras) and a King site at 6
		// (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks
		var mixedMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedMovieSites.put("site2", "1_327");
		mixedMovieSites.put("site3", "4_337");
		mixedMovieSites.put("site6", "7_350");
		var deck = dvs.buildDeckWithMap("102_74", mixedMovieSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));
		// and by his Map, which names one path of the three
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Mixed Movie sites should pass with Fellowship map + context", dvs.hasSiteBlockError(errors));
		// The Map's own objection to sites off its path is gone with it too
		assertFalse(dvs.hasMapError(errors));
	}

	@Test
	public void PCMovie_WithTowersMap_CanMixMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();
		// The Fellowship sites with Bree Gate at 2, a Towers site at 3 (Barrows of Edoras) and a King site at 6
		// (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks
		var mixedMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedMovieSites.put("site2", "1_327");
		mixedMovieSites.put("site3", "4_337");
		mixedMovieSites.put("site6", "7_350");
		var deck = dvs.buildDeckWithMap("102_75", mixedMovieSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));
		// and by his Map, which names one path of the three
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Mixed Movie sites should pass with Towers map + context", dvs.hasSiteBlockError(errors));
		// The Map's own objection to sites off its path is gone with it too
		assertFalse(dvs.hasMapError(errors));
	}

	@Test
	public void PCMovie_WithKingMap_CanMixMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();
		// The Fellowship sites with Bree Gate at 2, a Towers site at 3 (Barrows of Edoras) and a King site at 6
		// (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks
		var mixedMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedMovieSites.put("site2", "1_327");
		mixedMovieSites.put("site3", "4_337");
		mixedMovieSites.put("site6", "7_350");
		var deck = dvs.buildDeckWithMap("102_76", mixedMovieSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));
		// and by his Map, which names one path of the three
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Mixed Movie sites should pass with King map + context", dvs.hasSiteBlockError(errors));
		// The Map's own objection to sites off its path is gone with it too
		assertFalse(dvs.hasMapError(errors));
	}

	@Test
	public void PCMovie_WithFellowshipMap_CanUseShadowsSitesWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();
		// Nine Shadows sites with no more than three of one Shadow number, as an adventure deck of them must be
		// (the rig's own nine have six of Shadow number 0)
		var shadowsSites = new HashMap<>(DeckValidationScenario.ShadowsSites);
		shadowsSites.put("site6", "11_240");    // Flats of Rohan, Shadow number 2
		shadowsSites.put("site7", "11_231");    // Caras Galadhon, Shadow number 2
		shadowsSites.put("site8", "11_250");    // North Undeep, Shadow number 3
		var deck = dvs.buildDeckWithMap("102_74", shadowsSites);

		// A player without the modifier is refused this deck by his Map.  The format itself makes no objection of
		// block to nine Shadows sites (they are all of one block), so the whole list is read below.
		assertTrue(dvs.hasMapError(dvs.validate(deck)));

		// With it nothing at all is said against the deck, by the Map or by the format
		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Shadows sites should pass with Fellowship map + context", dvs.hasSiteBlockError(errors));
		assertEquals(List.of(), errors);
	}

	@Test
	public void PCMovie_WithFellowshipMap_CannotMixShadowsAndMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();
		// The Fellowship sites with Bree Gate at 2 and a Shadows site, Fangorn Glade, in place of site 5
		var mixedShadowsMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedShadowsMovieSites.put("site2", "1_327");
		mixedShadowsMovieSites.put("site5", "11_239");
		var deck = dvs.buildDeckWithMap("102_74", mixedShadowsMovieSites);

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertTrue("Mixed Shadows+Movie sites should fail even with map + context", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void PCMovie_TheFirstPrintingOfAnErratadSiteIsStillRefusedWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();

		// Nine Fellowship sites under the Fellowship Map, with Ettenmoors at 2 as PC Movie plays it: the errata,
		// 51_331.  A legal deck, with the modifier or without.
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "51_331");
		var deck = dvs.buildDeckWithMap("102_74", sites);
		assertEquals(List.of(), dvs.validate(deck));
		assertEquals(List.of(), dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides()));

		// The same nine with Ettenmoors as first printed, 1_331.  All nine are of one block and of the Map's
		// path, so neither is what the format objects to.
		sites.put("site2", "1_331");
		deck = dvs.buildDeckWithMap("102_74", sites);
		assertNotEquals(List.of(), dvs.validate(deck));
		assertFalse(dvs.hasSiteBlockError(dvs.validate(deck)));
		assertFalse(dvs.hasMapError(dvs.validate(deck)));

		// "Sites from any block" says nothing of errata: the deck is refused with the modifier as without it.
		assertNotEquals(List.of(), dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides()));
	}

	@Test
	public void PCMovie_WithFellowshipMap_AMixOfMovieSitesIsHeldToTheErrataWithContext() throws CardNotFoundException {
		var dvs = GetPCMovieScenario();

		// Ettenmoors at 2 as PC Movie plays it (the errata, 51_331), a Towers site at 3 (Barrows of Edoras) and a
		// King site at 6 (Minas Tirith Seventh Circle): the deck this modifier is for.  A player without it is
		// refused the deck for its sites; with it nothing at all is said against the deck.
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "51_331");
		sites.put("site3", "4_337");
		sites.put("site6", "7_350");
		var deck = dvs.buildDeckWithMap("102_74", sites);
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));
		assertEquals(List.of(), dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides()));

		// The same mix with Ettenmoors as first printed, 1_331: refused with the modifier, and not for its blocks
		// or by its Map, which the modifier has lifted
		sites.put("site2", "1_331");
		deck = dvs.buildDeckWithMap("102_74", sites);
		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertNotEquals(List.of(), errors);
		assertFalse(dvs.hasSiteBlockError(errors));
		assertFalse(dvs.hasMapError(errors));
	}

	// ===== Expanded =====

	@Test
	public void Expanded_CanMixMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetExpandedScenario();
		// The Fellowship sites with Bree Gate at 2, a Towers site at 3 (Barrows of Edoras) and a King site at 6
		// (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks
		var mixedMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedMovieSites.put("site2", "1_327");
		mixedMovieSites.put("site3", "4_337");
		mixedMovieSites.put("site6", "7_350");
		var deck = dvs.buildDeckWithSites(mixedMovieSites);

		// A player without the modifier is refused this deck for its sites
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertFalse("Mixed Movie sites should pass with context in Expanded", dvs.hasSiteBlockError(errors));
	}

	@Test
	public void Expanded_CannotMixShadowsAndMovieSitesWithContext() throws CardNotFoundException {
		var dvs = GetExpandedScenario();
		// The Fellowship sites with Bree Gate at 2 and a Shadows site, Fangorn Glade, in place of site 5
		var mixedShadowsMovieSites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		mixedShadowsMovieSites.put("site2", "1_327");
		mixedShadowsMovieSites.put("site5", "11_239");
		var deck = dvs.buildDeckWithSites(mixedShadowsMovieSites);

		var errors = dvs.validate(deck, VirtualTableScenario.FindCard("92_19").getDeckBuildingOverrides());
		assertTrue("Mixed Shadows+Movie sites should fail even with context in Expanded", dvs.hasSiteBlockError(errors));
	}
}
