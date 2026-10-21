package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");

					put("troll", "1_165");      // Cave Troll of Moria: unique [moria] Troll, strength 15, damage +1 and fierce
					put("commander", "1_186");  // Guard Commander: unique [moria] Orc, strength 7; a skirmish ability only used on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc who is not unique, strength 5
					put("enquea", "1_231");     // Úlairë Enquëa: unique [wraith] Nazgûl, strength 11; a maneuver ability only used on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship sites, with two more Fellowship sites put in each player's adventure deck; each test makes the
	// adventure decks it reads nine sites of one block, one of each number.
	protected VirtualTableScenario GetPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
					put("marshes", "1_332");    // Midgewater Marshes: Fellowship-block site 2, marsh; "each time a Hobbit moves to" it, which no test does
					put("chamber", "1_344");    // Dwarrowdelf Chamber: Fellowship-block site 4, underground; a move to it, which no test makes

					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, the minion that makes a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The default format, multipath, on the Towers sites, with Fellowship sites put in each player's adventure deck
	// as well (those of VirtualTableScenario.FellowshipSites, with Midgewater Marshes 1_332, a marsh, as site 2);
	// the test makes the first player's adventure deck Towers sites and the second player's the Fellowship sites (in
	// multipath all of a player's sites are of one block).
	protected VirtualTableScenario GetTowersPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
					put("bagend", "1_319");       // neither marsh nor underground
					put("marshes", "1_332");      // Midgewater Marshes: marsh; "each time a Hobbit moves to" it, which no test does
					put("rivendell", "1_341");
					put("balinstomb", "1_343");
					put("bridge", "1_349");
					put("glade", "1_351");
					put("confluence", "1_353");
					put("banks", "1_356");
					put("emynmuil", "1_360");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	// The default format, multipath, on the King sites: both players' sites are King sites.  Site 3 is The Dimholt
	// 8_117, underground.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	// A Shadows-block path, whose sites have no number until they are played.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dark", "2_55");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void DarkPlacesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Dark Places
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Plays to your support area.<br>Each unique [moria] minion is strength +1.<br><b>Shadow:</b> Spot an opponent's site to replace it with your marsh or underground site of the same number.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dark");

		assertEquals("Dark Places", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachUniqueMoriaMinionIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dark = scn.GetShadowCard("dark");
		var troll = scn.GetShadowCard("troll");
		var commander = scn.GetShadowCard("commander");
		var runner = scn.GetShadowCard("runner");
		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(troll, commander, runner, enquea);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(commander));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(11, scn.GetStrength(enquea));

		scn.MoveCardsToSupportArea(dark);

		// A unique [moria] Troll and a unique [moria] Orc.
		assertEquals(16, scn.GetStrength(troll));
		assertEquals(8, scn.GetStrength(commander));
		// Goblin Runner is no unique minion, and Enquëa no [moria] minion.
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(11, scn.GetStrength(enquea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityReplacesTheOpponentsSiteWithHisMarshOfTheSameNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPathScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite(1);
		var freepsSite2 = scn.GetFreepsSite(2);
		var marshes = scn.GetShadowCard("marshes");
		var balinsTomb = scn.GetShadowSite(4);
		var bridge = scn.GetShadowSite(5);

		scn.StartGame();
		// The Shadow player's site 2 is Midgewater Marshes; his sites 4 and 5 are underground.
		scn.SetShadowAdventureDeck(scn.GetShadowSite(1), marshes, scn.GetShadowSite(3), balinsTomb, bridge,
				scn.GetShadowSite(6), scn.GetShadowSite(7), scn.GetShadowSite(8), scn.GetShadowSite(9));
		// The Free Peoples player's own site 2 lies next, as where he played it himself, and the fellowship moves
		// onto it.
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite(3), scn.GetFreepsSite(4), scn.GetFreepsSite(5),
				scn.GetFreepsSite(6), scn.GetFreepsSite(7), scn.GetFreepsSite(8), scn.GetFreepsSite(9));
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, marshes, balinsTomb, bridge);
		assertTrue(scn.HasKeyword(balinsTomb, Keyword.UNDERGROUND));
		assertTrue(scn.HasKeyword(bridge, Keyword.UNDERGROUND));
		assertTrue(scn.ShadowActionAvailable(dark));

		// Both sites are the opponent's; only site 2 is one he has a marsh or underground site of the same number
		// for (his site 1, Bag End, is neither), and it is chosen unasked.
		scn.ShadowUseCardAction(dark);

		assertSame(marshes, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, marshes);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		// His underground sites of other numbers stay where they were.
		assertInZone(Zone.ADVENTURE_DECK, balinsTomb, bridge);
		assertSame(freepsSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// No opponent's site is left that he has such a site for.
		assertFalse(scn.ShadowActionAvailable(dark));
	}

	@Test
	public void ShadowAbilityReplacesTheOpponentsSiteWithHisUndergroundSiteOfTheSameNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPathScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite(1);
		var freepsSite4 = scn.GetFreepsSite(4);
		var chamber = scn.GetShadowCard("chamber");

		scn.StartGame();
		// Each adventure deck the nine sites of VirtualTableScenario.FellowshipSites, before the moves lay sites.
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite(2), scn.GetFreepsSite(3), scn.GetFreepsSite(4), scn.GetFreepsSite(5),
				scn.GetFreepsSite(6), scn.GetFreepsSite(7), scn.GetFreepsSite(8), scn.GetFreepsSite(9));
		scn.SetShadowAdventureDeck(scn.GetShadowSite(1), scn.GetShadowSite(2), scn.GetShadowSite(3), scn.GetShadowSite(4),
				scn.GetShadowSite(5), scn.GetShadowSite(6), scn.GetShadowSite(7), scn.GetShadowSite(8), scn.GetShadowSite(9));
		scn.SkipToSite(3);
		// The Shadow player's site 4 is Dwarrowdelf Chamber, underground; his sites 2 and 3 lie on the path.
		scn.SetShadowAdventureDeck(scn.GetShadowSite(1), chamber, scn.GetShadowSite(5), scn.GetShadowSite(6),
				scn.GetShadowSite(7), scn.GetShadowSite(8), scn.GetShadowSite(9));
		// The Free Peoples player's own site 4, Balin's Tomb, lies next, and the fellowship moves onto it.
		scn.MoveCardToAdventurePath(freepsSite4);
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, chamber);
		assertTrue(scn.ShadowActionAvailable(dark));

		scn.ShadowUseCardAction(dark);

		assertSame(chamber, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_PATH, chamber);
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4);
		assertSame(freepsSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityOffersEachOpponentsSiteWithAMarshOrUndergroundSiteOfItsNumberAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPathScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite(1);
		var freepsSite2 = scn.GetFreepsSite(2);
		var freepsSite4 = scn.GetFreepsSite(4);
		var shadowSite3 = scn.GetShadowSite(3);
		var marshes = scn.GetShadowCard("marshes");
		var chamber = scn.GetShadowCard("chamber");

		scn.StartGame();
		// The Shadow player's site 2 is Midgewater Marshes and his site 4 Dwarrowdelf Chamber, a marsh and an
		// underground site; his sites 1 and 3 are neither.
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite(3), freepsSite4, scn.GetFreepsSite(5), scn.GetFreepsSite(6),
				scn.GetFreepsSite(7), scn.GetFreepsSite(8), scn.GetFreepsSite(9));
		scn.SetShadowAdventureDeck(scn.GetShadowSite(1), marshes, shadowSite3, chamber, scn.GetShadowSite(5),
				scn.GetShadowSite(6), scn.GetShadowSite(7), scn.GetShadowSite(8), scn.GetShadowSite(9));
		// The Free Peoples player lays his own site 2, as where he played it himself, and the move to site 3 lays the
		// Shadow player's own; his own site 4 lies next, and the fellowship moves onto it.
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(freepsSite4);
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite2, scn.GetSite(2));
		assertSame(shadowSite3, scn.GetSite(3));
		assertSame(freepsSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, marshes, chamber);
		assertTrue(scn.ShadowActionAvailable(dark));
		scn.ShadowUseCardAction(dark);

		// Two of the opponent's sites have a marsh or an underground site of his of the same number; not his site 1,
		// which has the number of a site of his that is neither, nor the site 3 of the Shadow player's own.
		assertTrue(scn.ShadowHasCardChoiceAvailable(freepsSite2, freepsSite4));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsSite1, shadowSite3));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(freepsSite4);

		assertSame(chamber, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite4, marshes);
		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite2, scn.GetSite(2));
		assertSame(shadowSite3, scn.GetSite(3));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// His marsh still has the opponent's site 2 to replace.
		assertTrue(scn.ShadowActionAvailable(dark));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhereHisSiteOfThatNumberIsNeitherMarshNorUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPathScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);
		var balinsTomb = scn.GetShadowSite(4);

		scn.StartGame();
		// His adventure deck is the nine sites of VirtualTableScenario.FellowshipSites.
		scn.SetShadowAdventureDeck(scn.GetShadowSite(1), shadowSite2, scn.GetShadowSite(3), balinsTomb,
				scn.GetShadowSite(5), scn.GetShadowSite(6), scn.GetShadowSite(7), scn.GetShadowSite(8), scn.GetShadowSite(9));
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite(3), scn.GetFreepsSite(4), scn.GetFreepsSite(5),
				scn.GetFreepsSite(6), scn.GetFreepsSite(7), scn.GetFreepsSite(8), scn.GetFreepsSite(9));
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();

		// The opponent's sites 1 and 2 are there to spot. His own site 2 is Ettenmoors, plains, and his site 1 Bag
		// End; his underground sites are of other numbers.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2, balinsTomb);
		assertTrue(scn.HasKeyword(shadowSite2, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(balinsTomb, Keyword.UNDERGROUND));
		assertFalse(scn.ShadowActionAvailable(dark));
		assertSame(freepsSite2, scn.GetSite(2));
	}

	@Test
	public void ShadowAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPathScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite2 = scn.GetFreepsSite(2);
		var marshes = scn.GetShadowCard("marshes");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SetShadowAdventureDeck(scn.GetShadowSite(1), marshes, scn.GetShadowSite(3), scn.GetShadowSite(4),
				scn.GetShadowSite(5), scn.GetShadowSite(6), scn.GetShadowSite(7), scn.GetShadowSite(8), scn.GetShadowSite(9));
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite(3), scn.GetFreepsSite(4), scn.GetFreepsSite(5),
				scn.GetFreepsSite(6), scn.GetFreepsSite(7), scn.GetFreepsSite(8), scn.GetFreepsSite(9));
		scn.MoveCardsToSupportArea(dark);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dark));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dark));
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, marshes);
	}

	@Test
	public void InTheMultipathFormatItReplacesATowersSiteWithHisFellowshipMarsh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersPathScenario();

		var dark = scn.GetShadowCard("dark");
		var towersSite1 = scn.GetFreepsSite("site1");
		var towersSite2 = scn.GetFreepsSite("site2");
		var bagEnd = scn.GetShadowCard("bagend");
		var marshes = scn.GetShadowCard("marshes");

		// Each adventure deck holds two site 1s: the first player starts on his Towers site 1.
		scn.StartGame(towersSite1);
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(bagEnd, marshes, scn.GetShadowCard("rivendell"), scn.GetShadowCard("balinstomb"),
				scn.GetShadowCard("bridge"), scn.GetShadowCard("glade"), scn.GetShadowCard("confluence"),
				scn.GetShadowCard("banks"), scn.GetShadowCard("emynmuil"));
		// The first player lays his own site 2 himself (as a card that plays the next site would).
		scn.MoveCardToAdventurePath(towersSite2);
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertSame(towersSite1, scn.GetSite(1));
		assertSame(towersSite2, scn.GetCurrentSite());
		assertTrue(scn.ShadowActionAvailable(dark));

		// Both sites are the opponent's; only site 2 is one he has a Fellowship-block marsh or underground site of
		// the same number for (his site 1, Bag End, is neither), and it is chosen unasked.
		scn.ShadowUseCardAction(dark);

		// His Fellowship-block marsh takes the place of the Towers-block site the fellowship is at (Q-0327).
		assertSame(marshes, scn.GetSite(2));
		assertSame(marshes, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, towersSite2, bagEnd);
		assertSame(towersSite1, scn.GetSite(1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhereHisUndergroundSiteOfThatNumberIsAKingSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite(1);
		var freepsSite3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);

		scn.StartGame();
		scn.MoveCardsToSupportArea(dark);
		// The move lays the Shadow player's own site 2.
		scn.FreepsPass();
		// The first player's site 3, The Dimholt, lies ahead, as where he played it himself.
		scn.MoveCardToAdventurePath(freepsSite3);
		scn.ShadowRefreshDecision();

		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(freepsSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertTrue(scn.HasKeyword(shadowSite3, Keyword.UNDERGROUND));

		// Two of the opponent's sites are there to spot, and the Shadow player has an underground site 3, but of
		// the King block: there is no Fellowship-block site to lay, and the ability is not offered (Q-0327).
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dark));
	}

	@Test
	public void NotOfferedOnAShadowsBlockPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var dark = scn.GetShadowCard("dark");
		var freepsSite1 = scn.GetFreepsSite("site1");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame(freepsSite1);
		scn.MoveCardsToSupportArea(dark);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);

		assertSame(freepsSite1, scn.GetSite(1));
		assertSame(shadowSite2, scn.GetSite(2));

		// The opponent's site 1 is there to spot, but the Shadow player's adventure deck holds Shadows-block sites,
		// none of them a Fellowship-block site: there is no site to lay, and the ability is not offered (Q-0327).
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dark));
	}
}
