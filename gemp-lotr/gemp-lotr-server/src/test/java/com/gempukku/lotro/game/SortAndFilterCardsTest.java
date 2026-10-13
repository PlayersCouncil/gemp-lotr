package com.gempukku.lotro.game;

import com.gempukku.lotro.framework.LoadedLibraries;
import com.gempukku.lotro.framework.DeckValidationScenario;
import com.gempukku.lotro.framework.VirtualTableScenario;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * {@link SortAndFilterCards} and the sites it lists for a format, with and without {@code siteOverride:true}.
 *
 * The deck builder sends {@code siteOverride:true} beside the format when the card type asked for is Site and the
 * player has, in the Race to Mount Doom league chosen, a meta-site that lets his adventure deck hold sites of any
 * block (the deck building override skipSiteBlockValidation; Race Text 92_19).  The format filter then lists the
 * sites deck validation takes for that player: a site of any block and any set, still held to the format's X-list
 * and to its errata (Q-0185: "sites from any block" lifts the block rule and nothing else; F-0342).  Everything
 * else is as it is without the override: cards that are not sites, and a block or a set the player picks to narrow
 * the list.
 *
 * Each test filters a handful of cards named below, by the filter strings the client builds.
 *
 * "Any block" is the Fellowship, Towers, King and Shadows blocks, the ones the text's parenthesis names: a site of
 * the Hobbit block is listed with the override only in a format that lists it without (Q-0219: B; the last test).
 *
 * Not gone into: printings from errata or V sets the format does not play.  Under the override deck validation
 * takes them and so the list offers them (Q-0219, where B left them as they are), and no list read here holds
 * one.  That a block the player picks still narrows his sites is Q-0218.
 */
public class SortAndFilterCardsTest extends LoadedLibraries {
	// Fellowship sites
	private static final String BreeGate = "1_327";                // site 2
	private static final String Ettenmoors = "1_331";              // site 2; X-listed in Multipath, errata'd in the PC formats
	private static final String EttenmoorsErrata = "51_331";
	private static final String EmynMuil = "1_360";                // site 9; X-listed in Multipath
	private static final String SlopesOfAmonHen = "1_361";         // site 9; errata'd in the PC formats
	private static final String BagEndStudy = "101_57";            // site 1, of the PC set V1
	// Towers sites
	private static final String BarrowsOfEdoras = "4_337";         // site 3
	private static final String GoldenHall = "4_338";              // site 3; X-listed in Multipath
	// King site
	private static final String SeventhCircle = "7_350";           // site 6
	// Shadows sites, which have no number
	private static final String AnduinBanks = "11_227";            // errata'd in PC Expanded
	private static final String AnduinBanksErrata = "61_227";
	private static final String FangornGlade = "11_239";
	private static final String CourtyardParapet = "13_188";       // X-listed in Standard, errata'd in PC Expanded
	// Hobbit sites, of sets no format here plays but The Hobbit Draft Game and Anything Goes
	private static final String TrollHoard = "30_50";              // site 2
	private static final String DolGuldur = "32_46";               // site 5

	// Cards that are not sites
	private static final String Aragorn = "1_89";                  // a companion of set 1
	private static final String Elrond = "1_40";                   // an ally of set 1; X-listed in Movie, errata'd in the PC formats
	private static final String ElrondErrata = "51_40";
	private static final String AragornHeir = "4_109";             // a companion of set 4
	private static final String ArgumentReady = "12_1";            // an event of set 12
	private static final String RulingRing = "1_2";                // The One Ring
	private static final String RaceText = "92_19";                // the meta-site itself: a card of no format

	private static final List<String> DecipherSites = List.of(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen,
			BarrowsOfEdoras, GoldenHall, SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet);
	private static final List<String> NonSites = List.of(Aragorn, Elrond, ElrondErrata, AragornHeir, ArgumentReady,
			RulingRing, RaceText);

	// The sites Decipher printed and the cards that are not sites, with any more the test names
	private static List<String> Pool(String... more) {
		var pool = new ArrayList<>(DecipherSites);
		pool.addAll(NonSites);
		pool.addAll(List.of(more));
		return pool;
	}

	// The ids the filter lets through, in the filter's own order
	private static List<String> Filter(String filter, List<String> pool) {
		var items = pool.stream().map(BasicCardItem::new).toList();
		return new SortAndFilterCards().process(filter, items, _cardLibrary, _formatLibrary).stream()
				.map(BasicCardItem::getBlueprintId).toList();
	}

	// The same, in alphabetical order of id: for a reading of which cards are listed, whatever their order
	private static List<String> Listed(String filter, List<String> pool) {
		return Filter(filter, pool).stream().sorted().toList();
	}

	private static List<String> Sorted(String... ids) {
		return List.of(ids).stream().sorted().toList();
	}

	@Test
	public void FellowshipBlock_ListsItsOwnSitesAndWithTheOverrideSitesOfEveryBlock() {
		var pool = Pool();

		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen),
				Listed("format:fotr_block cardType:SITE", pool));

		// Towers, King and Shadows sites, none of them from a set Fellowship block plays
		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen, BarrowsOfEdoras, GoldenHall,
				SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:fotr_block cardType:SITE siteOverride:true", pool));

		// The override is about sites: a companion of set 4 and an event of set 12 are no more Fellowship block
		// cards with it than without
		assertEquals(Sorted(Aragorn, Elrond, RulingRing), Listed("format:fotr_block cardType:-SITE", pool));
		assertEquals(Sorted(Aragorn, Elrond, RulingRing),
				Listed("format:fotr_block cardType:-SITE siteOverride:true", pool));
	}

	@Test
	public void MovieBlock_ListsKingSitesAndWithTheOverrideSitesOfEveryBlock() {
		var pool = Pool();

		// Movie block plays sets 1 to 10 on the King site path
		assertEquals(Sorted(SeventhCircle), Listed("format:movie cardType:SITE", pool));

		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen, BarrowsOfEdoras, GoldenHall,
				SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:movie cardType:SITE siteOverride:true", pool));

		// Elrond 1_40 is X-listed in Movie block, and stays out
		assertEquals(Sorted(Aragorn, AragornHeir, RulingRing), Listed("format:movie cardType:-SITE", pool));
		assertEquals(Sorted(Aragorn, AragornHeir, RulingRing),
				Listed("format:movie cardType:-SITE siteOverride:true", pool));
	}

	@Test
	public void Expanded_ListsShadowsSitesAndWithTheOverrideSitesOfEveryBlock() {
		var pool = Pool();

		assertEquals(Sorted(AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:expanded cardType:SITE", pool));

		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen, BarrowsOfEdoras, GoldenHall,
				SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:expanded cardType:SITE siteOverride:true", pool));

		assertEquals(Sorted(Aragorn, Elrond, AragornHeir, ArgumentReady, RulingRing),
				Listed("format:expanded cardType:-SITE", pool));
		assertEquals(Sorted(Aragorn, Elrond, AragornHeir, ArgumentReady, RulingRing),
				Listed("format:expanded cardType:-SITE siteOverride:true", pool));
	}

	@Test
	public void PCMovie_WithTheOverrideAddsShadowsSitesAndStillHoldsBackTheFirstPrintingOfAnErratadSite() {
		var pool = Pool(EttenmoorsErrata, BagEndStudy);

		// PC Movie plays any one Movie site path, with Ettenmoors and Slopes of Amon Hen as errata'd
		assertEquals(Sorted(BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy, BarrowsOfEdoras, GoldenHall,
				SeventhCircle),
				Listed("format:pc_movie cardType:SITE", pool));

		// With the override the Shadows sites are listed too.  Ettenmoors 1_331 and Slopes of Amon Hen 1_361 are
		// not: the errata is the format's word on that card, whatever block the player may take sites from.
		var listed = Listed("format:pc_movie cardType:SITE siteOverride:true", pool);
		assertEquals(Sorted(BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy, BarrowsOfEdoras, GoldenHall,
				SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet), listed);
		assertFalse(listed.contains(Ettenmoors));
		assertFalse(listed.contains(SlopesOfAmonHen));

		assertEquals(Sorted(Aragorn, ElrondErrata, AragornHeir, RulingRing),
				Listed("format:pc_movie cardType:-SITE", pool));
		assertEquals(Sorted(Aragorn, ElrondErrata, AragornHeir, RulingRing),
				Listed("format:pc_movie cardType:-SITE siteOverride:true", pool));
	}

	@Test
	public void PCExpanded_WithTheOverrideAddsMovieSitesAndStillHoldsBackFirstPrintings() {
		var pool = Pool(EttenmoorsErrata, AnduinBanksErrata, BagEndStudy);

		// PC Expanded plays Shadows sites, with Anduin Banks and Courtyard Parapet as errata'd
		assertEquals(Sorted(AnduinBanksErrata, FangornGlade), Listed("format:pc_expanded cardType:SITE", pool));

		// With the override the Movie sites are listed too, and of each errata'd site only the errata
		assertEquals(Sorted(AnduinBanksErrata, FangornGlade, BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy,
				BarrowsOfEdoras, GoldenHall, SeventhCircle),
				Listed("format:pc_expanded cardType:SITE siteOverride:true", pool));
	}

	@Test
	public void Multipath_ASiteTheFormatHasXListedIsNotListedWithTheOverrideEither() {
		var pool = Pool();

		// Pre-Shadows Multipath has Ettenmoors, Emyn Muil and Golden Hall X-listed
		assertEquals(Sorted(BreeGate, SlopesOfAmonHen, BarrowsOfEdoras, SeventhCircle),
				Listed("format:multipath cardType:SITE", pool));

		// With the override the Shadows sites are listed, and the X-listed sites still are not
		assertEquals(Sorted(BreeGate, SlopesOfAmonHen, BarrowsOfEdoras, SeventhCircle, AnduinBanks, FangornGlade,
				CourtyardParapet),
				Listed("format:multipath cardType:SITE siteOverride:true", pool));
	}

	@Test
	public void ABlockThePlayerPicksStillNarrowsTheSitesWithTheOverride() {
		var pool = Pool(EttenmoorsErrata, BagEndStudy);

		// PC Movie with its Fellowship block picked: that block's sites, with the override as without
		assertEquals(Sorted(BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy),
				Listed("format:pc_movie block:pc_fotr_block cardType:SITE", pool));
		assertEquals(Sorted(BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy),
				Listed("format:pc_movie block:pc_fotr_block cardType:SITE siteOverride:true", pool));

		// and its Towers block
		assertEquals(Sorted(BarrowsOfEdoras, GoldenHall),
				Listed("format:pc_movie block:pc_ttt_block cardType:SITE siteOverride:true", pool));

		// Expanded has Fellowship block among its blocks.  Without the override no Fellowship site is an Expanded
		// site, so picking that block lists none; with it, picking that block lists the Fellowship sites alone.
		assertEquals(List.of(), Listed("format:expanded block:fotr_block cardType:SITE", pool));
		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen),
				Listed("format:expanded block:fotr_block cardType:SITE siteOverride:true", pool));

		// Fellowship block is a format of one block, which the deck builder picks by itself: its own sites are
		// what is listed until the player sets the block to Any
		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen),
				Listed("format:fotr_block block:fotr_block cardType:SITE siteOverride:true", pool));
	}

	@Test
	public void ASetThePlayerPicksStillNarrowsTheSitesWithTheOverride() {
		var pool = Pool(EttenmoorsErrata, BagEndStudy);

		// Set 11 is not a PC Movie set: no site of it is listed without the override, and with it the sites of
		// set 11 and no others
		assertEquals(List.of(), Listed("format:pc_movie set:11 cardType:SITE", pool));
		assertEquals(Sorted(AnduinBanks, FangornGlade),
				Listed("format:pc_movie set:11 cardType:SITE siteOverride:true", pool));

		assertEquals(Sorted(BarrowsOfEdoras, GoldenHall),
				Listed("format:pc_movie set:4 cardType:SITE siteOverride:true", pool));
	}

	@Test
	public void WithNoFormatInTheFilterTheOverrideChangesNothing() {
		var pool = Pool();

		assertEquals(Sorted(DecipherSites.toArray(new String[0])), Listed("cardType:SITE", pool));
		assertEquals(Sorted(DecipherSites.toArray(new String[0])), Listed("cardType:SITE siteOverride:true", pool));

		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen), Listed("set:1 cardType:SITE", pool));
		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen),
				Listed("set:1 cardType:SITE siteOverride:true", pool));
	}

	@Test
	public void SiteOverrideFalseIsNoOverride() {
		var pool = Pool();

		assertEquals(Sorted(SeventhCircle), Listed("format:movie cardType:SITE siteOverride:false", pool));
	}

	@Test
	public void WithSeveralFormatsInTheFilterASiteIsListedIfAnyOfThemTakesIt() {
		var pool = Pool();

		assertEquals(Sorted(SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:movie,expanded cardType:SITE", pool));

		// Multipath has Emyn Muil X-listed and Movie block has not: listed, since one of the two takes it
		assertEquals(Sorted(BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen, BarrowsOfEdoras, GoldenHall,
				SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:multipath,movie cardType:SITE siteOverride:true", pool));
	}

	@Test
	public void WithNoCardTypeAskedForTheOverrideListsEverySiteAmongTheFormatsOtherCards() {
		var pool = Pool();

		assertEquals(Sorted(Aragorn, Elrond, RulingRing, BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen),
				Listed("format:fotr_block", pool));
		assertEquals(Sorted(Aragorn, Elrond, RulingRing, BreeGate, Ettenmoors, EmynMuil, SlopesOfAmonHen,
				BarrowsOfEdoras, GoldenHall, SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:fotr_block siteOverride:true", pool));
	}

	@Test
	public void TheSitesBoxOfTheDeckBuilderListsThemBySiteNumberThenName() {
		var pool = Pool(EttenmoorsErrata, BagEndStudy);

		// What the deck builder sends when the sites box is clicked with a PC Movie league chosen
		assertEquals(List.of(BagEndStudy, BreeGate, EttenmoorsErrata, BarrowsOfEdoras, GoldenHall, SeventhCircle,
				EmynMuil),
				Filter("format:pc_movie cardType:SITE sort:siteNumber,name", pool));

		// and for a player whose sites may be of any block: Shadows sites have no number and come first
		assertEquals(List.of(AnduinBanks, CourtyardParapet, FangornGlade, BagEndStudy, BreeGate, EttenmoorsErrata,
				BarrowsOfEdoras, GoldenHall, SeventhCircle, EmynMuil),
				Filter("format:pc_movie cardType:SITE sort:siteNumber,name siteOverride:true", pool));
	}

	@Test
	public void WhatTheListOffersWithTheOverrideIsWhatDeckValidationTakesWithRaceText92_19()
			throws CardNotFoundException {
		var bagEnd = "1_319";
		var balinsTomb = "1_343";
		var bridge = "1_349";
		var confluence = "1_353";
		var anduinBanksOfSet1 = "1_356";
		var pool = Pool(EttenmoorsErrata, bagEnd, balinsTomb, bridge, confluence, anduinBanksOfSet1);

		var listed = Listed("format:pc_movie cardType:SITE siteOverride:true", pool);

		// Nine sites of the three Movie blocks, one of each number, every one of them on the list
		var sites = new HashMap<String, String>();
		sites.put("site1", bagEnd);
		sites.put("site2", EttenmoorsErrata);
		sites.put("site3", BarrowsOfEdoras);
		sites.put("site4", balinsTomb);
		sites.put("site5", bridge);
		sites.put("site6", SeventhCircle);
		sites.put("site7", confluence);
		sites.put("site8", anduinBanksOfSet1);
		sites.put("site9", EmynMuil);
		assertTrue(listed.containsAll(sites.values()));

		// The deck they make is refused as PC Movie stands and has nothing said against it for the player who
		// has Race Text 92_19
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var overrides = VirtualTableScenario.FindCard(RaceText).getDeckBuildingOverrides();
		var deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertNotEquals(List.of(), dvs.validate(deck));
		assertEquals(List.of(), dvs.validate(deck, overrides));

		// Ettenmoors as first printed is not on the list, and the same deck with it at 2 is refused
		assertFalse(listed.contains(Ettenmoors));
		sites.put("site2", Ettenmoors);
		deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertNotEquals(List.of(), dvs.validate(deck, overrides));
	}

	@Test
	public void WithTheOverrideAHobbitSiteIsListedOnlyWhereItIsListedWithout() throws CardNotFoundException {
		var pool = Pool(EttenmoorsErrata, BagEndStudy, TrollHoard, DolGuldur);

		// PC Movie does not play the Hobbit sets.  With the override its list gains the Shadows sites, as it does
		// with no Hobbit site to choose from, and neither Hobbit site.
		assertEquals(Sorted(BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy, BarrowsOfEdoras, GoldenHall,
				SeventhCircle),
				Listed("format:pc_movie cardType:SITE", pool));
		assertEquals(Sorted(BreeGate, EttenmoorsErrata, EmynMuil, BagEndStudy, BarrowsOfEdoras, GoldenHall,
				SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:pc_movie cardType:SITE siteOverride:true", pool));

		// The same in a format of one block, and in a Shadows format
		assertEquals(Sorted(BreeGate, Ettenmoors, EttenmoorsErrata, EmynMuil, SlopesOfAmonHen, BagEndStudy,
				BarrowsOfEdoras, GoldenHall, SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:fotr_block cardType:SITE siteOverride:true", pool));
		assertEquals(Sorted(BreeGate, Ettenmoors, EttenmoorsErrata, EmynMuil, SlopesOfAmonHen, BagEndStudy,
				BarrowsOfEdoras, GoldenHall, SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:expanded cardType:SITE siteOverride:true", pool));

		// The Hobbit Draft Game lists the Hobbit sites as its own, and with the override the sites of the four
		// blocks beside them
		assertEquals(Sorted(TrollHoard, DolGuldur), Listed("format:hobbit cardType:SITE", pool));
		assertEquals(Sorted(TrollHoard, DolGuldur, BreeGate, Ettenmoors, EttenmoorsErrata, EmynMuil, SlopesOfAmonHen,
				BagEndStudy, BarrowsOfEdoras, GoldenHall, SeventhCircle, AnduinBanks, FangornGlade, CourtyardParapet),
				Listed("format:hobbit cardType:SITE siteOverride:true", pool));

		// A set the player picks: the Hobbit sites are not PC Movie's with the override either
		assertEquals(List.of(), Listed("format:pc_movie set:32 cardType:SITE siteOverride:true", pool));

		// What the list leaves out, deck validation refuses for the player who has Race Text 92_19: eight
		// Fellowship sites that PC Movie takes, which validate for him, and then Dol Guldur at 5
		var sites = new HashMap<>(DeckValidationScenario.FellowshipSites);
		sites.put("site2", BreeGate);
		var dvs = new DeckValidationScenario(DeckValidationScenario.PCMovie);
		var overrides = VirtualTableScenario.FindCard(RaceText).getDeckBuildingOverrides();
		var deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertEquals(List.of(), dvs.validate(deck, overrides));

		sites.put("site5", DolGuldur);
		deck = dvs.buildDeckWithMap(DeckValidationScenario.FellowshipMap, sites);
		assertEquals(List.of("Site does not use supported block: Dol Guldur"),
				dvs.getErrorLines(dvs.validate(deck, overrides)));
	}
}
