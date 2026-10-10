package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.game.DeckValidationContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * An adventure deck whose sites "may be from any block" (the deck building override skipSiteBlockValidation, which
 * Race Text 92_19 sets) is let off the format's rule of block and nothing else.  The format's X-list and its
 * errata are its word on one card, not on which blocks it plays: a site the format has X-listed is refused with
 * the override as without it, and so is the first printing of a site the format plays as errata'd.
 *
 * Ketura's ruling on Q-0185 (2026-10-09), A being "the text lifts the block rule and nothing else": "I think we
 * can safely ignore the X-list issue, as nobody is going to run RTMD on anything but a common format, and none of
 * those have banned sites.  So I think the errata question is the only one that can crop up, as we do run PC-Movie
 * RTMD.  I think A is preferred."
 *
 * What the override does lift, shown here as the control of each test: a site need not be of the format's block,
 * nor of the path the deck's Map names, nor from a set the format plays.  What it never lifted (nine sites, one of
 * each number where sites have numbers, Movie sites and Shadows sites not in one deck) is not gone over again.
 *
 * These tests hand the format the override itself (anyBlock in each test: sites of any block, as a deck building
 * override hands it to the format).  That a card sets it is that card's test (Card_92_019_Tests).
 *
 * The mixed deck of four of the tests is the Fellowship sites with a Towers site at 3 (Barrows of Edoras) and a
 * King site at 6 (Minas Tirith Seventh Circle): one site of each number, from all three Movie blocks.  Site 2 is
 * Bree Gate, which every format here takes as it is (the rig's Ettenmoors is X-listed in Multipath and errata'd in
 * PC Movie).  Site 9 is left as the rig has it, Emyn Muil 1_360: X-listed in Multipath, taken as it is by the
 * other two.
 *
 * The objections are read with getErrorLines: every line of every objection, one to an entry and in alphabetical
 * order (the sites' objections come as one entry of several lines, in whatever order the deck holds its sites).
 */
public class SitesFromAnyBlockTests
{
	// Pre-Shadows Multipath: any one site path of the Movie block.  It has eight sites X-listed.
	protected DeckValidationScenario GetMultipathScenario() {
		return new DeckValidationScenario(DeckValidationScenario.Multipath);
	}

	// PC Movie: any one site path of the Movie block, named by the deck's Map.  It plays sets 1 to 10 with the
	// errata of sets 51 to 60, seven sites among them (Ettenmoors and Cavern Entrance are two).
	protected DeckValidationScenario GetPCMovieScenario() {
		return new DeckValidationScenario(DeckValidationScenario.PCMovie);
	}

	// Fellowship block: the Fellowship site path and sets 1 to 3 only, with no X-list and no errata.
	protected DeckValidationScenario GetFellowshipScenario() {
		return new DeckValidationScenario(DeckValidationScenario.Fellowship);
	}

	@Test
	public void ASiteTheFormatHasXListedIsRefusedInADeckOfOneBlock() {
		var dvs = GetMultipathScenario();
		var anyBlock = new DeckValidationContext().setSkipSiteBlockValidation(true);

		// Nine Fellowship sites that Multipath takes as they are
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "1_327");
		sites.put("site9", "1_361");
		var deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of(), dvs.validate(deck));
		assertEquals(List.of(), dvs.validate(deck, anyBlock));

		// The same nine with Emyn Muil 1_360 at 9, which Multipath has X-listed: refused in the same words with
		// the override as without it
		sites.put("site9", "1_360");
		deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of("Card is X-listed: Emyn Muil"), dvs.getErrorLines(dvs.validate(deck)));
		assertEquals(List.of("Card is X-listed: Emyn Muil"), dvs.getErrorLines(dvs.validate(deck, anyBlock)));
	}

	@Test
	public void ASiteTheFormatHasXListedIsRefusedInADeckOnlyTheOverrideAllows() {
		var dvs = GetMultipathScenario();
		var anyBlock = new DeckValidationContext().setSkipSiteBlockValidation(true);

		// Sites of all three Movie blocks, with Slopes of Amon Hen 1_361 at 9, which Multipath takes as it is:
		// refused for their blocks without the override, and with it nothing at all is said against the deck
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "1_327");    // Bree Gate
		sites.put("site3", "4_337");    // Barrows of Edoras: a Towers site
		sites.put("site6", "7_350");    // Minas Tirith Seventh Circle: a King site
		sites.put("site9", "1_361");
		var deck = dvs.buildDeckWithSites(sites);
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));
		assertEquals(List.of(), dvs.validate(deck, anyBlock));

		// Golden Hall 4_338 in place of Barrows of Edoras at 3: a Towers site Multipath has X-listed.  The X-list
		// is the one thing said against the deck.
		sites.put("site3", "4_338");
		deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of("Card is X-listed: Golden Hall"), dvs.getErrorLines(dvs.validate(deck, anyBlock)));

		// Two X-listed sites are each named
		sites.put("site9", "1_360");
		deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of("Card is X-listed: Emyn Muil", "Card is X-listed: Golden Hall"),
				dvs.getErrorLines(dvs.validate(deck, anyBlock)));
	}

	@Test
	public void TheFirstPrintingOfASiteTheFormatPlaysAsErratadIsRefused() {
		var dvs = GetPCMovieScenario();
		var anyBlock = new DeckValidationContext().setSkipSiteBlockValidation(true);

		// Sites of all three Movie blocks under the Fellowship Map, with Ettenmoors at 2 as PC Movie plays it
		// (the errata, 51_331): refused for its blocks and by its Map without the override, and with it nothing
		// at all is said against the deck
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "1_327");    // Bree Gate
		sites.put("site3", "4_337");    // Barrows of Edoras: a Towers site
		sites.put("site6", "7_350");    // Minas Tirith Seventh Circle: a King site
		sites.put("site2", "51_331");
		var deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertTrue(dvs.hasSiteBlockError(dvs.validate(deck)));
		assertTrue(dvs.hasMapError(dvs.validate(deck)));
		assertEquals(List.of(), dvs.validate(deck, anyBlock));

		// The same deck with Ettenmoors as first printed (1_331): the printing is the one thing said against it
		sites.put("site2", "1_331");
		deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertEquals(List.of("Non-errata version of an errata'd card: Ettenmoors"),
				dvs.getErrorLines(dvs.validate(deck, anyBlock)));
	}

	@Test
	public void TheFirstPrintingIsRefusedForASiteOffTheMapsPathToo() {
		var dvs = GetPCMovieScenario();
		var anyBlock = new DeckValidationContext().setSkipSiteBlockValidation(true);

		// Sites of all three Movie blocks under the Fellowship Map, with a second Towers site at 7: Cavern
		// Entrance as PC Movie plays it (the errata, 54_355)
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "1_327");    // Bree Gate
		sites.put("site3", "4_337");    // Barrows of Edoras: a Towers site
		sites.put("site6", "7_350");    // Minas Tirith Seventh Circle: a King site
		sites.put("site7", "54_355");
		var deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertEquals(List.of(), dvs.validate(deck, anyBlock));

		// and as first printed (4_355)
		sites.put("site7", "4_355");
		deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertEquals(List.of("Non-errata version of an errata'd card: Cavern Entrance"),
				dvs.getErrorLines(dvs.validate(deck, anyBlock)));
	}

	@Test
	public void ASiteFromASetTheFormatDoesNotPlayIsStillLetIn() {
		var dvs = GetFellowshipScenario();
		var anyBlock = new DeckValidationContext().setSkipSiteBlockValidation(true);

		// Fellowship block plays sets 1 to 3: a Towers site and a King site are of neither its block nor its
		// sets.  Without the override each is refused; with it nothing is said against either.
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", "1_327");    // Bree Gate
		sites.put("site3", "4_337");    // Barrows of Edoras: a Towers site
		sites.put("site6", "7_350");    // Minas Tirith Seventh Circle: a King site
		var deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of("Site does not use supported block: Barrows of Edoras",
				"Site does not use supported block: Minas Tirith Seventh Circle"), dvs.getErrorLines(dvs.validate(deck)));
		assertEquals(List.of(), dvs.validate(deck, anyBlock));

		// A format with no errata for a site has nothing against its first printing: Ettenmoors 1_331
		sites.put("site2", "1_331");
		deck = dvs.buildDeckWithSites(sites);
		assertEquals(List.of(), dvs.validate(deck, anyBlock));
	}
}
