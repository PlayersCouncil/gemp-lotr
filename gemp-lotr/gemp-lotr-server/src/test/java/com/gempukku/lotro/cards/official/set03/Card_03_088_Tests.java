package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("road", "3_88");

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers only a weapon played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1 and no other text
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl; weakens bearers of [wraith] conditions
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: [raider] Nazgûl; its text needs 3 burdens and Easterlings
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, no Nazgûl; its text reads only the archery total
					put("fear", "1_278");       // Strength Born of Fear: a [sauron] condition; a skirmish ability only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text

					// Four cards for the Free Peoples player's draw deck, in a known order.
					put("gimli", "1_13");
					put("arwen", "1_30");
					put("legolas", "1_50");
					put("haldir", "1_48");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetOfftheRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Get Off the Road!
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Search</b>.<br><b>Shadow:</b> Spot a [sauron] minion and a Nazgûl to make the Free Peoples player discard a card from the top of his or her deck for each burden you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("road");

		assertEquals("Get Off the Road!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtThreeBurdensTheTopThreeCardsOfTheFreePeoplesDeckAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var chieftain = scn.GetShadowCard("chieftain");
		var lemenya = scn.GetShadowCard("lemenya");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCardsToHand(road);
		assertInHand(road);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(chieftain, lemenya);
		scn.SetFreepsDrawDeck(gimli, arwen, legolas, haldir);
		assertEquals(3, scn.GetBurdens());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(road));

		scn.ShadowPlayCard(road);

		// One for each of the 3 burdens, from the top down; the fourth is left on top.
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertInDiscard(gimli, arwen, legolas);
		assertEquals(1, scn.GetFreepsDeckCount());
		assertSame(haldir, scn.GetFreepsDrawDeck().get(0));
		// The Shadow player's own deck is untouched.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard + 1, scn.GetShadowDiscardCount());
		assertInDiscard(road);
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtOneBurdenOnlyTheTopCardIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var sentry = scn.GetShadowCard("sentry");
		var khamul = scn.GetShadowCard("khamul");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCardsToHand(road);
		assertInHand(road);

		scn.StartGame();
		scn.AddBurdens(1);
		// A [sauron] minion who is no Orc, and a Nazgûl who is not [wraith].
		scn.MoveMinionsToTable(sentry, khamul);
		scn.SetFreepsDrawDeck(gimli, arwen, legolas, haldir);
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(road));
		scn.ShadowPlayCard(road);

		assertEquals(1, scn.GetFreepsDiscardCount());
		assertInDiscard(gimli);
		assertEquals(3, scn.GetFreepsDeckCount());
		assertSame(arwen, scn.GetFreepsDrawDeck().get(0));
		assertInDiscard(road);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotPlayableWithoutANazgulOrWithoutASauronMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var chieftain = scn.GetShadowCard("chieftain");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");
		var fear = scn.GetShadowCard("fear");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(road);
		assertInHand(road);

		scn.StartGame();
		scn.AddBurdens(1);
		// A [sauron] minion, and a [wraith] Orc where a Nazgûl should be.
		scn.MoveMinionsToTable(chieftain, creeper);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(road));

		// A Nazgûl as well.
		scn.MoveMinionsToTable(lemenya);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(road));

		// The [sauron] minion gone: a [sauron] card that is no minion, and a minion of another culture.
		scn.MoveCardsToDiscard(chieftain);
		scn.MoveCardsToSupportArea(fear);
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, fear);
		assertFalse(scn.ShadowPlayAvailable(road));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var road = scn.GetShadowCard("road");
		var chieftain = scn.GetShadowCard("chieftain");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(road);
		assertInHand(road);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(chieftain, lemenya);
		scn.SetFreepsDrawDeck();
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowPlayAvailable(road));
		scn.ShadowPlayCard(road);

		// Nothing to discard from; nothing is taken from the Shadow player's deck in its place.
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertInDiscard(road);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
