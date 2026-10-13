package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_332_Tests
{
	// The King block path with Rohirrim Road as site 1, where every test starts.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, vitality 3; stronger only against a Nazgul
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, vitality 3; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, vitality 4; a maneuver ability only

					// Cards whose cost is read in hand.
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] Shadow condition, cost 1, support area
					put("nazgulsword", "1_218"); // Nazgul Sword: [wraith] Shadow possession, cost 1
					put("runner", "1_178");     // Goblin Runner: [moria] minion, cost 1
					put("bred", "1_121");       // Bred for Battle: [isengard] Shadow event, cost 0
					put("songs", "1_61");       // Songs of the Blessed Realm: [elven] Free Peoples condition, cost 1
					put("hobbitsword", "1_299"); // Hobbit Sword: [shire] Free Peoples possession, cost 1, on a Hobbit
					put("ringofair", "9_23");   // Vilya, Ring of Air: [elven] Free Peoples artifact, cost 0
					put("healing", "3_22");     // Master of Healing: [elven] Free Peoples event, cost 1
					put("lorienelf", "1_53");   // Lorien Elf: [elven] companion, cost 1

					put("catapult", "8_32");    // Catapult: [gondor] support possession; Maneuver: discard 2 cards from hand
												// to reveal the Shadow player's top card; he discards a Shadow card of that cost
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, cost 1, support area
					put("strength", "1_278");   // Strength Born of Fear: [sauron] Shadow condition, cost 1, support area
					put("coming", "1_196");     // They Are Coming: [moria] Shadow condition, cost 3, support area
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text, so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					put("site1", "7_332");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rohirrim Road
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1K
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert an Elf to make the twilight cost of each condition and possession +2
		 * until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Rohirrim Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ExertingAnElfMakesEachConditionAndPossessionCost2More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var golradir = scn.GetFreepsCard("golradir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var armory = scn.GetShadowCard("armory");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		var runner = scn.GetShadowCard("runner");
		var bred = scn.GetShadowCard("bred");
		var freepsArmory = scn.GetFreepsCard("armory");
		var songs = scn.GetFreepsCard("songs");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var ringofair = scn.GetFreepsCard("ringofair");
		var healing = scn.GetFreepsCard("healing");
		var lorienelf = scn.GetFreepsCard("lorienelf");

		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(armory, nazgulsword, runner, bred, freepsArmory, songs, hobbitsword, ringofair, healing, lorienelf);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		assertEquals(1, scn.GetTwilightCost(armory));
		assertEquals(1, scn.GetTwilightCost(nazgulsword));
		assertEquals(1, scn.GetTwilightCost(freepsArmory));
		assertEquals(1, scn.GetTwilightCost(songs));
		assertEquals(1, scn.GetTwilightCost(hobbitsword));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(0, scn.GetTwilightCost(bred));
		assertEquals(0, scn.GetTwilightCost(ringofair));
		assertEquals(1, scn.GetTwilightCost(healing));
		assertEquals(1, scn.GetTwilightCost(lorienelf));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsActionAvailable(road));

		scn.FreepsUseCardAction(road);
		// An Elf companion or an Elf ally; not a Man, nor the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, golradir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(golradir);
		assertEquals(1, scn.GetWoundsOn(golradir));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Conditions and possessions of either side, whoever holds them, cost 2 more.
		assertEquals(3, scn.GetTwilightCost(armory));
		assertEquals(3, scn.GetTwilightCost(nazgulsword));
		assertEquals(3, scn.GetTwilightCost(freepsArmory));
		assertEquals(3, scn.GetTwilightCost(songs));
		assertEquals(3, scn.GetTwilightCost(hobbitsword));
		// Not a minion, a companion, an event of either side, nor an artifact.
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(0, scn.GetTwilightCost(bred));
		assertEquals(0, scn.GetTwilightCost(ringofair));
		assertEquals(1, scn.GetTwilightCost(healing));
		assertEquals(1, scn.GetTwilightCost(lorienelf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachPlayerPays2MoreToPlayAPossessionOrACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var frodo = scn.GetRingBearer();
		var armory = scn.GetShadowCard("armory");

		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(hobbitsword, armory);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		scn.FreepsUseCardAction(road);
		assertEquals(1, scn.GetWoundsOn(arwen));

		// The Free Peoples player's possession: 1 printed + 2.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(hobbitsword);
		assertAttachedTo(hobbitsword, frodo);
		assertEquals(3, scn.GetTwilight());

		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Shadow player's condition, at the next site the same turn: 1 printed + 2.
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(armory));
		scn.ShadowPlayCard(armory);
		assertEquals(twilight - 3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCostIsStill2MoreAfterASecondMoveInTheRegroupPhaseAndBackOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var songs = scn.GetFreepsCard("songs");
		var armory = scn.GetShadowCard("armory");

		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(songs, armory);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		assertEquals(1, scn.GetTwilightCost(songs));
		assertEquals(1, scn.GetTwilightCost(armory));
		scn.FreepsUseCardAction(road);
		assertEquals(3, scn.GetTwilightCost(songs));
		assertEquals(3, scn.GetTwilightCost(armory));

		// The last phase of the turn: still 2 more.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetTwilightCost(songs));
		assertEquals(3, scn.GetTwilightCost(armory));

		// The fellowship moves again before the turn is over: the regroup phase ends, the turn does not, and the
		// next Shadow phase is still this turn's.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(songs));
		assertEquals(3, scn.GetTwilightCost(armory));

		// The turn over, each is back at its printed 1.
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetTwilightCost(songs));
		assertEquals(1, scn.GetTwilightCost(armory));
	}

	@Test
	public void NotOfferedWhenTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(arwen, aragorn);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(arwen, 2);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		// A fresh Man and the Ring-bearer cannot pay.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(road));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARevealedConditionIsNotComingIntoPlaySoItsCostIsNotModified() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var catapult = scn.GetFreepsCard("catapult");
		var armory = scn.GetShadowCard("armory");
		var pinned = scn.GetShadowCard("pinned");
		var strength = scn.GetShadowCard("strength");
		var coming = scn.GetShadowCard("coming");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(arwen);
		// Catapult needs 2 [gondor] knights to be played; it is put in play directly.
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToSupportArea(pinned, strength, coming);
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("bred"));

		scn.StartGame();

		var road = scn.GetCurrentSite();
		scn.FreepsUseCardAction(road);
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Uruk-hai Armory, printed cost 1, is the card to be revealed; in hand it would cost 3.
		scn.MoveCardsToTopOfDeck(armory);

		assertTrue(scn.FreepsActionAvailable(catapult));
		scn.FreepsUseCardAction(catapult);
		scn.DismissRevealedCards();

		// The Shadow cards in play that cost 1 are offered, the one that costs 3 is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pinned, strength));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(coming));
		scn.ShadowChooseCard(pinned);
		assertInDiscard(pinned);
		assertInZone(Zone.SUPPORT, strength, coming);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
