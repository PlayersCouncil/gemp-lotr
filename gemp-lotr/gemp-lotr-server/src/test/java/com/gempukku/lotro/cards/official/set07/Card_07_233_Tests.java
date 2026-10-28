package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_233_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimbold", "7_233");
					put("rider", "4_286");        // Rider of Rohan: [rohan] Man companion, strength 5; a skirmish ability used only on purpose
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, damage +1 only
					// West Road: King-block site 2, plains; "if the Shadow has initiative, the Shadow number of this site is +3"
					put("westroad", "7_337");
					// Pelennor Plain: King-block site 4, plains; an archery ability for the Shadow player's minion
					put("plain", "7_343");
					// Cards for the hand, played by nobody: a companion, a possession, an event, and a Shadow card
					// (the Rider's alias above gives each player a Rider; the second is "rider2").
					put("rider2", "4_286");
					put("chain", "4_278");        // Heavy Chain: [rohan] possession
					put("defiance", "1_37");      // Defiance: [elven] skirmish event
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] minion, no text
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The default format, multipath, on the King sites with West Road 7_337 (a plains) as site 2, and the
	// Fellowship sites put in each player's adventure deck as well; the test makes the first player's adventure deck
	// King sites and the second player's the Fellowship sites (in multipath all of a player's sites are of one block).
	protected VirtualTableScenario GetKingPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimbold", "7_233");
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text; also the card in hand to discard
					put("bagend", "1_319");       // the Fellowship sites of VirtualTableScenario.FellowshipSites
					put("ettenmoors", "1_331");
					put("rivendell", "1_341");
					put("balinstomb", "1_343");
					put("bridge", "1_349");
					put("glade", "1_351");
					put("confluence", "1_353");
					put("banks", "1_356");
					put("emynmuil", "1_360");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.KingSites);
					put("site2", "7_337");        // West Road: plains; "if the Shadow has initiative, the Shadow number of this site is +3"
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	// The default format, multipath, on the Towers sites: both players' sites are Towers sites, and site 2 is
	// Eastfold 4_331, a plains.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grimbold", "7_233");
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text; also the card in hand to discard
				}},
				VirtualTableScenario.TowersSites,
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
					put("grimbold", "7_233");
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text; also the card in hand to discard
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void GrimboldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Grimbold, Marshal of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.
		 * 	<b>Skirmish:</b> Replace the fellowship's site with your plains site of the same number to discard a card from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grimbold");

		assertEquals("Grimbold", card.getBlueprint().getTitle());
		assertEquals("Marshal of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityReplacesTheShadowPlayersSiteWithHisPlainsOfTheSameNumberToDiscardACardFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var rider = scn.GetFreepsCard("rider");
		var westRoad = scn.GetFreepsCard("westroad");
		var rider2 = scn.GetFreepsCard("rider2");
		var chain = scn.GetFreepsCard("chain");
		var defiance = scn.GetFreepsCard("defiance");
		var handTroop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(grimbold, rider);
		scn.MoveCardsToHand(rider2, chain, defiance, handTroop);

		var savage = scn.GetShadowCard("savage");
		var shadowHandCard = scn.GetShadowCard("chain");
		var kingsTent = scn.GetShadowSite("site2");
		scn.MoveCardsToHand(shadowHandCard);

		scn.StartGame();
		assertInHand(rider2, chain, defiance, handTroop, shadowHandCard);
		// His site 2 is West Road, a plains; the Shadow player's is King's Tent, which is none.
		// The card map puts West Road and Pelennor Plain in both adventure decks: the Shadow player's is made the
		// nine King sites again.
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), kingsTent, scn.GetShadowSite("site3"), scn.GetShadowSite("site4"),
				scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"), scn.GetShadowSite("site8"),
				scn.GetShadowSite("site9"));
		scn.SetFreepsAdventureDeck(westRoad, scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		// The move lays the Shadow player's site 2.
		scn.FreepsPass();
		assertSame(kingsTent, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, westRoad);
		assertTrue(scn.HasKeyword(westRoad, Keyword.PLAINS));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		// Grimbold is in no skirmish: the Rider's is the one fought.
		scn.FreepsAssignToMinions(rider, savage);
		scn.FreepsResolveSkirmish(rider);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(grimbold));
		scn.FreepsUseCardAction(grimbold);

		// The cost: West Road takes site 2, and King's Tent goes back to its owner's adventure deck.
		assertSame(westRoad, scn.GetCurrentSite());
		assertSame(westRoad, scn.GetSite(2));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, kingsTent);

		// Any card of his own hand, whatever its type or side; not the Shadow player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider2, chain, defiance, handTroop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shadowHandCard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(chain);

		assertInDiscard(chain);
		assertInHand(rider2, defiance, handTroop, shadowHandCard);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyHandTheSiteIsStillReplacedAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var rider = scn.GetFreepsCard("rider");
		var westRoad = scn.GetFreepsCard("westroad");
		scn.MoveCompanionsToTable(grimbold, rider);

		var savage = scn.GetShadowCard("savage");
		var kingsTent = scn.GetShadowSite("site2");

		scn.StartGame();
		// The card map puts West Road and Pelennor Plain in both adventure decks: the Shadow player's is made the
		// nine King sites again.
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), kingsTent, scn.GetShadowSite("site3"), scn.GetShadowSite("site4"),
				scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"), scn.GetShadowSite("site8"),
				scn.GetShadowSite("site9"));
		scn.SetFreepsAdventureDeck(westRoad, scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.FreepsPass();
		assertSame(kingsTent, scn.GetCurrentSite());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, savage);
		scn.FreepsResolveSkirmish(rider);

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(grimbold));
		scn.FreepsUseCardAction(grimbold);

		assertSame(westRoad, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, kingsTent);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhereHisOnlyPlainsSitesAreOfOtherNumbersThanTheFellowshipsSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var rider = scn.GetFreepsCard("rider");
		var westRoad = scn.GetFreepsCard("westroad");
		var plain = scn.GetFreepsCard("plain");
		var freepsDimholt = scn.GetFreepsSite("site3");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(grimbold, rider);
		scn.MoveCardsToHand(chain);

		var savage = scn.GetShadowCard("savage");
		var kingsTent = scn.GetShadowSite("site2");
		var shadowDimholt = scn.GetShadowSite("site3");

		scn.StartGame();
		assertInHand(chain);
		// The card map puts West Road and Pelennor Plain in both adventure decks: the Shadow player's is made the
		// nine King sites again.
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), kingsTent, scn.GetShadowSite("site3"), scn.GetShadowSite("site4"),
				scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"), scn.GetShadowSite("site8"),
				scn.GetShadowSite("site9"));
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site2"), freepsDimholt, scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SkipToSite(2);
		assertSame(kingsTent, scn.GetSite(2));
		// His deck: West Road (a plains site 2, the number of a site on the path that the fellowship has left),
		// his own Dimholt (site 3, no plains) and Pelennor Plain (a plains site 4).
		scn.SetFreepsAdventureDeck(westRoad, freepsDimholt, plain, scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, savage);
		scn.FreepsResolveSkirmish(rider);

		assertSame(shadowDimholt, scn.GetCurrentSite());
		assertSame(kingsTent, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, westRoad, freepsDimholt, plain);
		assertTrue(scn.HasKeyword(westRoad, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(plain, Keyword.PLAINS));
		assertFalse(scn.HasKeyword(freepsDimholt, Keyword.PLAINS));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimbold));
		assertInHand(chain);
	}

	@Test
	public void NotOfferedWhereOnlyTheShadowPlayersDeckHoldsAPlainsSiteOfThatNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var rider = scn.GetFreepsCard("rider");
		var chain = scn.GetFreepsCard("chain");
		var shadowWestRoad = scn.GetShadowCard("westroad");
		scn.MoveCompanionsToTable(grimbold, rider);
		scn.MoveCardsToHand(chain);

		var savage = scn.GetShadowCard("savage");
		var kingsTent = scn.GetShadowSite("site2");

		scn.StartGame();
		assertInHand(chain);
		// The card map puts West Road and Pelennor Plain in both adventure decks: the Shadow player's is made the
		// nine King sites again, and the Free Peoples player's the nine King sites without them.
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), kingsTent, scn.GetShadowSite("site3"), scn.GetShadowSite("site4"),
				scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"), scn.GetShadowSite("site8"),
				scn.GetShadowSite("site9"));
		scn.SetFreepsAdventureDeck(scn.GetFreepsSite("site2"), scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"),
				scn.GetFreepsSite("site5"), scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"),
				scn.GetFreepsSite("site9"));
		scn.FreepsPass();
		assertSame(kingsTent, scn.GetCurrentSite());
		// His own West Road, a plains site 2, now lies in the Shadow player's deck, and none in his own.
		scn.SetShadowAdventureDeck(scn.GetShadowSite("site1"), shadowWestRoad, scn.GetShadowSite("site3"), scn.GetShadowSite("site4"),
				scn.GetShadowSite("site5"), scn.GetShadowSite("site6"), scn.GetShadowSite("site7"), scn.GetShadowSite("site8"),
				scn.GetShadowSite("site9"));
		assertInZone(Zone.ADVENTURE_DECK, shadowWestRoad);
		assertTrue(scn.HasKeyword(shadowWestRoad, Keyword.PLAINS));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, savage);
		scn.FreepsResolveSkirmish(rider);

		assertSame(kingsTent, scn.GetCurrentSite());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimbold));
		assertInZone(Zone.ADVENTURE_DECK, shadowWestRoad);
		assertInHand(chain);
	}

	@Test
	public void InTheMultipathFormatHeReplacesAFellowshipSiteWithHisKingPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingPathScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var inHand = scn.GetFreepsCard("troop");
		var westRoad = scn.GetFreepsSite("site2");
		scn.MoveCompanionsToTable(grimbold);
		scn.MoveCardsToHand(inHand);

		var troop = scn.GetShadowCard("troop");
		var ettenmoors = scn.GetShadowCard("ettenmoors");

		// Each adventure deck holds two site 1s: the first player starts on his King site 1.
		scn.StartGame(scn.GetFreepsSite("site1"));
		assertInHand(inHand);
		scn.SetFreepsAdventureDeck(westRoad, scn.GetFreepsSite("site3"), scn.GetFreepsSite("site4"), scn.GetFreepsSite("site5"),
				scn.GetFreepsSite("site6"), scn.GetFreepsSite("site7"), scn.GetFreepsSite("site8"), scn.GetFreepsSite("site9"));
		scn.SetShadowAdventureDeck(scn.GetShadowCard("bagend"), ettenmoors, scn.GetShadowCard("rivendell"),
				scn.GetShadowCard("balinstomb"), scn.GetShadowCard("bridge"), scn.GetShadowCard("glade"),
				scn.GetShadowCard("confluence"), scn.GetShadowCard("banks"), scn.GetShadowCard("emynmuil"));
		// The move lays the Shadow player's Fellowship-block site 2.
		scn.FreepsPass();
		assertSame(ettenmoors, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, westRoad);

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbold, troop);
		scn.FreepsResolveSkirmish(grimbold);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(grimbold));
		scn.FreepsUseCardAction(grimbold);

		// His King-block plains site 2 takes the place of the Fellowship-block site the fellowship is at (Q-0327),
		// and a card is discarded from his hand.
		assertSame(westRoad, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, ettenmoors);
		assertInDiscard(inHand);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhereHisPlainsSiteOfThatNumberIsATowersSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var inHand = scn.GetFreepsCard("troop");
		var freepsSite2 = scn.GetFreepsSite("site2");
		scn.MoveCompanionsToTable(grimbold);
		scn.MoveCardsToHand(inHand);

		var troop = scn.GetShadowCard("troop");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame();
		assertInHand(inHand);
		// The move lays the Shadow player's site 2; the first player's Eastfold, a plains site 2 of the Towers
		// block, is in his adventure deck.
		scn.FreepsPass();
		assertSame(shadowSite2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertTrue(scn.HasKeyword(freepsSite2, Keyword.PLAINS));

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbold, troop);
		scn.FreepsResolveSkirmish(grimbold);

		// There is no King-block plains site to lay, and the ability is not offered (Q-0327).
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimbold));
		assertInHand(inHand);
	}

	@Test
	public void NotOfferedOnAShadowsBlockPath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var grimbold = scn.GetFreepsCard("grimbold");
		var inHand = scn.GetFreepsCard("troop");
		var freepsSite1 = scn.GetFreepsSite("site1");
		scn.MoveCompanionsToTable(grimbold);
		scn.MoveCardsToHand(inHand);

		var troop = scn.GetShadowCard("troop");
		var shadowSite2 = scn.GetShadowSite("site2");

		scn.StartGame(freepsSite1);
		assertInHand(inHand);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertSame(shadowSite2, scn.GetCurrentSite());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(grimbold, troop);
		scn.FreepsResolveSkirmish(grimbold);

		// His adventure deck holds Shadows-block sites, none of them a King-block site: there is no site to lay,
		// and the ability is not offered (Q-0327).
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(grimbold));
		assertInHand(inHand);
	}
}
