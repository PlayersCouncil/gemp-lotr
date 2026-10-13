package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("erestor", "3_14");       // Erestor, Chief Advisor to Elrond: another Elf ally of home 3; a response against [sauron] wounds only
					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion, cost 2; an archery ability only
					put("lorien", "1_53");        // Lorien Elf: Elf companion, cost 1; to play, spot an Elf
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
					put("gaffer", "1_291");       // The Gaffer: in the draw deck, the card Bag End's own text plays
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBedroomScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion, cost 2; an archery ability only
					put("sam", "1_311");          // Sam, Son of Hamfast: Hobbit companion, cost 2; a fellowship ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: left in the draw deck, the card that is drawn
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_339");   // Frodo's Bedroom: "Sanctuary. Fellowship: Play a Hobbit to draw a card."
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFordScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
					put("nelya", "1_233");        // Ulaire Nelya, Lieutenant of Morgul: "Shadow: Exert Ulaire Nelya and spot an opponent's site to replace it with your site of the same number."
					put("witchking", "1_237");    // The Witch-king, Lord of Angmar: Nazgul, cost 8; a card in the Shadow hand whose cost is read
					put("lemenya", "1_232");      // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, cost 4, site 3; her text needs a [wraith] condition on a companion
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_338");   // Ford of Bruinen: "River. Sanctuary. The twilight cost of the first Nazgul played to Ford of Bruinen each turn is -5."
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetCourtyardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_337");   // Council Courtyard: "Sanctuary. When the fellowship moves from Council Courtyard, remove (2)."
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHouseOfElrondScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "3_119");   // House of Elrond: "Sanctuary. When the fellowship moves to House of Elrond, the Free Peoples player may spot 2 Elves to remove a burden."
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("silinde", "1_60");
					
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion, cost 2; an archery ability only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SilindeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Silinde, Elf of Mirkwood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: While you can spot your site 3, Silinde has the game text of that site.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("silinde");

		assertEquals("Silinde", card.getBlueprint().getTitle());
		assertEquals("Elf of Mirkwood", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void IsPlayedFromHandForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCardsToHand(silinde);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(silinde));

		scn.FreepsPlayCard(silinde);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, silinde);
		assertEquals(5, scn.GetStrength(silinde));
		assertEquals(2, scn.GetVitality(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoTextOfASiteUntilHisOwnersSite3IsOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var erestor = scn.GetFreepsCard("erestor");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var legolas = scn.GetFreepsCard("legolas");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde, erestor);

		var bagEnd = scn.GetFreepsSite(1);
		var valley = scn.GetFreepsSite(3);

		scn.StartGame();

		// His owner's site 1 is on the path, and its text is on offer ("Fellowship: Exert a Hobbit to play The
		// Gaffer from your draw deck"): it is not his owner's site 3, and Silinde has none of it.
		assertSame(bagEnd, scn.GetCurrentSite());
		assertTrue(scn.FreepsActionAvailable(bagEnd));
		assertFalse(scn.FreepsActionAvailable(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder, legolas, lorien);
		scn.FreepsRefreshDecision();

		// No site 3 is on the path yet; his owner's is in the adventure deck. An Elf is in hand to be played.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, valley);
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(silinde));

		// Pathfinder plays the fellowship's next site from its player's own adventure deck.
		scn.FreepsPlayCard(pathfinder);
		assertSame(valley, scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Rivendell Valley: "Fellowship: Play an Elf to draw a card." Silinde has it; Erestor, another Elf
		// ally of home 3, has not; and the site itself offers nothing, the fellowship being at site 2.
		assertTrue(scn.FreepsActionAvailable(silinde));
		assertFalse(scn.FreepsActionAvailable(erestor));
		assertFalse(scn.FreepsActionAvailable(valley));

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(silinde);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, lorien));
		scn.FreepsChooseCard(legolas);

		// Legolas is played for his 2, and one card is drawn: the hand is the Lorien Elf and the card drawn.
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, lorien);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoTextOfTheOpponentsSite3ThoughTheFellowshipIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var opponentsValley = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(legolas);
		scn.FreepsRefreshDecision();

		// The fellowship moved here, so the site is the Shadow player's copy. Its own text is on offer, with
		// an Elf in hand to play; Silinde, whose owner's site 3 is not on the path, has no text.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(opponentsValley, scn.GetCurrentSite());
		assertTrue(scn.FreepsActionAvailable(opponentsValley));
		assertFalse(scn.FreepsActionAvailable(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GainsTheTextWhenTheOpponentsSite3IsReplacedByHisOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var valley = scn.GetFreepsSite(3);
		var opponentsValley = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		// The opponent's site 3 already lies ahead of the fellowship.
		scn.MoveCardToAdventurePath(opponentsValley);
		scn.SetFreepsHand(pathfinder, legolas);
		scn.FreepsRefreshDecision();

		assertSame(opponentsValley, scn.GetSite(3));
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(silinde));

		// Pathfinder: "(replacing opponent's site if necessary)".
		scn.FreepsPlayCard(pathfinder);
		assertSame(valley, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, opponentsValley);
		assertTrue(scn.FreepsActionAvailable(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HisCopyOfFordOfBruinenStacksWithTheFordUntilTheOpponentsFordReplacesHisOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFordScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");
		var bagEnd = scn.GetFreepsSite(1);
		var ford = scn.GetFreepsSite(3);
		var opponentsFord = scn.GetShadowSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		scn.SetShadowHand(witchking);
		scn.FreepsRefreshDecision();

		// With no site 3 on the path the Witch-king costs his printed 8.
		assertEquals(8, scn.GetTwilightCost(witchking));

		scn.FreepsPlayCard(pathfinder);
		assertSame(ford, scn.GetSite(3));
		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		// The fellowship is at its own player's Ford of Bruinen. The Ford makes the first Nazgul played to it
		// this turn cost 5 less, and Silinde, who has that text, 5 less again: 8 - 5 - 5, which reads 0.
		assertSame(ford, scn.GetCurrentSite());
		assertEquals(0, scn.GetTwilightCost(witchking));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		// Nelya may spot either of the Free Peoples player's sites on the path, and not the Shadow player's own.
		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(ford, bagEnd));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scn.GetSite(2)));
		scn.ShadowChooseCard(ford);

		// The fellowship now stands on the Shadow player's Ford of Bruinen: the site's text still holds, and
		// Silinde has lost his copy of it. 8 - 5.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(opponentsFord, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, ford);
		assertEquals(3, scn.GetTwilightCost(witchking));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HasTheTextOfWhicheverSite3IsHisOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBedroomScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var bedroom = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder, legolas);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(pathfinder);
		assertSame(bedroom, scn.GetSite(3));

		// Frodo's Bedroom: "Fellowship: Play a Hobbit to draw a card." With an Elf alone in hand the price
		// cannot be paid, which Rivendell Valley's text would take.
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertFalse(scn.FreepsActionAvailable(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToHand(sam);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(silinde));

		int twilight = scn.GetTwilight();
		var gimli = scn.GetFreepsCard("gimli");
		assertInZone(Zone.DECK, gimli);
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());

		// Sam is the one Hobbit in hand: he is played, for his 2, and a card is drawn.
		scn.FreepsUseCardAction(silinde);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertInZone(Zone.HAND, legolas, gimli);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoTextOfHisOwnersSite3OfTheTowerBlock() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var barrows = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder, legolas);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(pathfinder);

		// His owner's Barrows of Edoras lies on the path as site 3 of the Tower block ("Fellowship: Place your
		// hand beneath your draw deck and draw 4 cards", and a card is in hand to place). "Your site 3" on a
		// card of the Fellowship block is site 3 of that block: Silinde has no text.
		assertSame(barrows, scn.GetSite(3));
		assertEquals(SitesBlock.TWO_TOWERS, barrows.getBlueprint().getSiteBlock());
		assertEquals(3, barrows.getBlueprint().getSiteNumber());
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HisCopyOfFordOfBruinenDiscountsNoNazgulPlayedToAnotherSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFordScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var lemenya = scn.GetShadowCard("lemenya");
		var ford = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(pathfinder);
		assertSame(ford, scn.GetSite(3));

		// A turn later the fellowship starts at its own player's Ford of Bruinen and moves on to site 4.
		scn.SkipToSite(3);
		assertSame(ford, scn.GetCurrentSite());
		scn.SetShadowHand(lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 3 for the site.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(15, scn.GetTwilight());
		// Lemenya is the first Nazgul played this turn, but not to Ford of Bruinen: the Ford lies behind, and
		// Silinde's copy of its text names the Ford as the Ford's own does. She costs her printed 4 (her site
		// number is 3: no roaming at site 4).
		assertEquals(4, scn.GetTwilightCost(lemenya));

		scn.ShadowPlayCard(lemenya);
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ANazgulPlayedAtSite2IsNotTheFirstPlayedToFordOfBruinenForHisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFordScenario();

		// Silinde is the second player's here: only a player who moves second can find his own site 3 two sites
		// ahead of his fellowship, laid when the first player moved twice.
		var silinde = scn.GetShadowCard("silinde");
		var lemenya = scn.GetFreepsCard("lemenya");
		var witchking = scn.GetFreepsCard("witchking");
		var ford = scn.GetShadowSite(3);

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertSame(ford, scn.GetCurrentSite());

		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(lemenya, witchking);
		scn.SetTwilight(10);
		scn.ShadowPass();

		// Silinde's owner is at site 2 with his own Ford of Bruinen a site ahead, so Silinde has its text. Lemenya
		// is not played to the Ford: she costs her printed 4, and 2 more to roam (her site number is 3).
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(ford, scn.GetSite(3));
		assertInZone(Zone.SUPPORT, silinde);
		assertEquals(4, scn.GetTwilightCost(lemenya));
		assertEquals(8, scn.GetTwilightCost(witchking));
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(lemenya);
		assertEquals(pool - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		// The fellowship moves again in the same turn, to the Ford. The Witch-king is the first Nazgul played to
		// Ford of Bruinen this turn, for the site and for Silinde's copy of its text alike: 8 - 5 - 5, which reads 0.
		scn.SkipToMovementDecision();
		scn.ShadowChooseToMove();
		assertSame(ford, scn.GetCurrentSite());
		assertInZone(Zone.HAND, witchking);
		assertEquals(0, scn.GetTwilightCost(witchking));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void MovingFromHisOwnersCouncilCourtyardRemovesTwoForTheSiteAndTwoForHisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCourtyardScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var courtyard = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(pathfinder);
		assertSame(courtyard, scn.GetSite(3));

		scn.SkipToSite(3);
		assertSame(courtyard, scn.GetCurrentSite());
		scn.SetTwilight(6);
		scn.FreepsPass();

		// The site's text and Silinde's copy of it answer the same move: the Free Peoples player orders them.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Council Courtyard");

		// 6 set - 2 for the site - 2 for Silinde's copy + 2 companions + 3 for site 4.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToHisOwnersHouseOfElrondOffersTheBurdenOnceForTheSiteAndOnceForHisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHouseOfElrondScenario();

		var silinde = scn.GetFreepsCard("silinde");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		var house = scn.GetFreepsSite(3);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(pathfinder);
		assertSame(house, scn.GetSite(3));

		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsPass();

		// Arwen and Silinde are the 2 Elves. The site makes its offer, and Silinde, who has its text, his own.
		assertSame(house, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(house));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(silinde));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
