package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruviel", "1_67");

					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("orophin", "1_56");       // Orophin, Lorien Bowman: another Elf ally of home 6; an archery ability only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
					put("gaffer", "1_291");       // The Gaffer: in the draw deck, the card Bag End's own text plays
					put("savage", "1_151");       // Uruk Savage: a card for the Shadow player's hand
					put("runner", "1_178");       // Goblin Runner: a card for the Shadow player's hand
				}},
				// Site 6 is Galadriel's Glade: "Sanctuary. Fellowship: Exert an Elf to look at an opponent's hand."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetWoodsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruviel", "1_67");

					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("orophin", "1_56");       // Orophin, Lorien Bowman: another Elf ally of home 6, strength 3; an archery ability only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
					put("nelya", "1_233");        // Ulaire Nelya, Lieutenant of Morgul: "Shadow: Exert Ulaire Nelya and spot an opponent's site to replace it with your site of the same number."
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site6", "1_352");   // Lothlorien Woods: "Forest. Sanctuary. Each ally whose home is site 6 is strength +3."
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetValleyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruviel", "1_67");

					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("sam", "1_311");          // Sam, Son of Hamfast: a second Hobbit companion, vitality 4; a fellowship ability used only on purpose
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site6", "2_120");   // Valley of the Silverlode: "River. Sanctuary. When the fellowship moves to Valley of the Silverlode, each Hobbit companion may heal."
					put("site7", "1_354");   // Anduin Wilderland: skips the archery phase, and leaves allies alone (Anduin Confluence discards them)
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruviel", "1_67");

					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and ranger; strength against a Nazgul only
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup event, cost 0; spot a ranger to play the fellowship's next site
					put("bow", "1_41");           // Elven Bow: a possession Arwen may bear, cost 1
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site6", "4_354");   // Hornburg Armory: "Sanctuary. Fellowship: Play a possession to draw a card."
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UruvielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruviel, Maid of Lórien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: While you can spot your site 6, Uruviel has the game text of that site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruviel");

		assertEquals("Uruviel", card.getBlueprint().getTitle());
		assertEquals("Maid of Lórien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void IsPlayedFromHandForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		scn.MoveCardsToHand(uruviel);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(uruviel));

		scn.FreepsPlayCard(uruviel);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, uruviel);
		assertEquals(5, scn.GetStrength(uruviel));
		assertEquals(2, scn.GetVitality(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoTextOfASiteUntilHerOwnersSite6IsOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var orophin = scn.GetFreepsCard("orophin");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var shadowSavage = scn.GetShadowCard("savage");
		var shadowRunner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(uruviel, orophin);

		var bagEnd = scn.GetFreepsSite(1);
		var glade = scn.GetFreepsSite(6);

		scn.StartGame();

		// Her owner's site 1 is on the path, and its text is on offer ("Fellowship: Exert a Hobbit to play The
		// Gaffer from your draw deck"): it is not her owner's site 6, and Uruviel has none of it.
		assertSame(bagEnd, scn.GetCurrentSite());
		assertTrue(scn.FreepsActionAvailable(bagEnd));
		assertFalse(scn.FreepsActionAvailable(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(5);
		scn.SetFreepsHand(pathfinder);
		scn.SetShadowHand(shadowSavage, shadowRunner);
		scn.FreepsRefreshDecision();

		// No site 6 is on the path yet; her owner's is in the adventure deck. There are Elves to exert.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertNull(scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, glade);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsActionAvailable(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Pathfinder plays the fellowship's next site from its player's own adventure deck.
		scn.FreepsPlayCard(pathfinder);
		assertSame(glade, scn.GetSite(6));
		assertEquals(5, scn.GetCurrentSiteNumber());

		// Galadriel's Glade: "Fellowship: Exert an Elf to look at an opponent's hand." Uruviel has it; Orophin,
		// another Elf ally of home 6, has not; and the site itself offers nothing, the fellowship being at site 5.
		assertTrue(scn.FreepsActionAvailable(uruviel));
		assertFalse(scn.FreepsActionAvailable(orophin));
		assertFalse(scn.FreepsActionAvailable(glade));

		scn.FreepsUseCardAction(uruviel);
		// Any Elf may pay, Uruviel herself among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, uruviel, orophin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(uruviel));
		assertEquals(0, scn.GetWoundsOn(orophin));

		// The Shadow player's hand is shown to the Free Peoples player.
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowSavage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(shadowRunner, false));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoTextOfTheOpponentsSite6ThoughTheFellowshipIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(uruviel);

		var opponentsGlade = scn.GetShadowSite(6);

		scn.StartGame();
		scn.SkipToSite(6);

		// The fellowship moved here, so the site is the Shadow player's copy. Its own text is on offer, with
		// Elves to exert; Uruviel, whose owner's site 6 is not on the path, has no text.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertSame(opponentsGlade, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(opponentsGlade));
		assertFalse(scn.FreepsActionAvailable(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GainsTheTextWhenTheOpponentsSite6IsReplacedByHerOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(uruviel);

		var glade = scn.GetFreepsSite(6);
		var opponentsGlade = scn.GetShadowSite(6);

		scn.StartGame();
		scn.SkipToSite(5);
		// The opponent's site 6 already lies ahead of the fellowship.
		scn.MoveCardToAdventurePath(opponentsGlade);
		scn.SetFreepsHand(pathfinder);
		scn.FreepsRefreshDecision();

		assertSame(opponentsGlade, scn.GetSite(6));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsActionAvailable(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Pathfinder: "(replacing opponent's site if necessary)".
		scn.FreepsPlayCard(pathfinder);
		assertSame(glade, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, opponentsGlade);
		assertTrue(scn.FreepsActionAvailable(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HerCopyOfLothlorienWoodsStacksWithTheWoodsUntilTheOpponentsWoodsReplaceHerOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoodsScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var orophin = scn.GetFreepsCard("orophin");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(uruviel, orophin);

		var nelya = scn.GetShadowCard("nelya");
		var bagEnd = scn.GetFreepsSite(1);
		var woods = scn.GetFreepsSite(6);
		var opponentsWoods = scn.GetShadowSite(6);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(pathfinder);
		scn.FreepsRefreshDecision();

		// With no site 6 on the path the two allies of home 6 are at their printed strength.
		assertNull(scn.GetSite(6));
		assertEquals(5, scn.GetStrength(uruviel));
		assertEquals(3, scn.GetStrength(orophin));

		// Her owner's Lothlorien Woods lies ahead. The fellowship is at site 5, so the site's own text does not
		// apply; Uruviel's copy of it does, to her and to Orophin: +3.
		scn.FreepsPlayCard(pathfinder);
		assertSame(woods, scn.GetSite(6));
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(8, scn.GetStrength(uruviel));
		assertEquals(6, scn.GetStrength(orophin));

		scn.MoveMinionsToTable(nelya);
		scn.FreepsPass();

		// The fellowship is at its own player's Woods: +3 for the site and +3 for Uruviel's copy.
		assertSame(woods, scn.GetCurrentSite());
		assertEquals(11, scn.GetStrength(uruviel));
		assertEquals(9, scn.GetStrength(orophin));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		// Nelya may spot either of the Free Peoples player's sites on the path.
		scn.ShadowUseCardAction(nelya);
		assertTrue(scn.ShadowHasCardChoiceAvailable(woods, bagEnd));
		scn.ShadowChooseCard(woods);

		// The fellowship now stands on the Shadow player's Lothlorien Woods: the site's text still holds, and
		// Uruviel has lost her copy of it (issue #1054: the copied +3 used to stay on her).
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(opponentsWoods, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, woods);
		assertEquals(8, scn.GetStrength(uruviel));
		assertEquals(6, scn.GetStrength(orophin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HasNoTextOfHerOwnersSite6OfTheTowerBlock() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(uruviel);

		var armory = scn.GetFreepsSite(6);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(pathfinder, bow);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(pathfinder);

		// Her owner's Hornburg Armory lies on the path as site 6 of the Tower block ("Fellowship: Play a
		// possession to draw a card", and a possession that can be played is in hand). "Your site 6" on a card
		// of the Fellowship block is site 6 of that block: Uruviel has no text.
		assertSame(armory, scn.GetSite(6));
		assertEquals(SitesBlock.TWO_TOWERS, armory.getBlueprint().getSiteBlock());
		assertEquals(6, armory.getBlueprint().getSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(bow));
		assertFalse(scn.FreepsActionAvailable(uruviel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingToHerOwnersValleyOfTheSilverlodeOffersTheHealOnceForTheSiteAndOnceForHerCopyAndMovingOnOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetValleyScenario();

		var uruviel = scn.GetFreepsCard("uruviel");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, sam);
		scn.MoveAlliesToTable(uruviel);

		var valley = scn.GetFreepsSite(6);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(pathfinder);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(pathfinder);
		assertSame(valley, scn.GetSite(6));

		// Vitality 4 each: three wounds leave something for a second heal, and for a third that must not come.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.AddWoundsToChar(arwen, 1);
		scn.FreepsPass();

		// The site makes its offer, and Uruviel, who has its text, her own.
		assertSame(valley, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(valley));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel));

		scn.FreepsChooseOptionalTrigger(valley);
		// Each Hobbit companion may heal; Arwen is wounded and no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen));
		scn.FreepsChooseCards(frodo, sam);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(uruviel));

		scn.FreepsChooseOptionalTrigger(uruviel);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCards(frodo, sam);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on in the same turn. Her owner's Valley still lies on the path, so she still has
		// its text; but this move is not to Valley of the Silverlode, and the wounded Hobbits are offered nothing.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertSame(valley, scn.GetSite(6));
		assertInZone(Zone.SUPPORT, uruviel);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
