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
					put("card", "7_233");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
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
		 * Game Text: <b>Valiant</b>.<br><b>Skirmish:</b> Replace the fellowship's site with your plains site of the same number to discard a card from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

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

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GrimboldTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
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
