package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_260_Tests
{

	// The card's own block, with Trollshaw Forest laid as site 1: its text is in force in the first fellowship phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability never used here
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion; a fellowship ability never used here
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a [shire] Hobbit companion; his text is about [shire] tales
					// Farmer Maggot, Chaser of Rascals: a Hobbit ally, who may bear what a Hobbit may; his ability is not used
					put("maggot", "1_288");

					put("rangersword", "1_112"); // Ranger's Sword: a [gondor] possession, "Bearer must be Aragorn"
					put("phial", "10_13");      // Phial of Galadriel, Star-glass: an [elven] artifact of cost 0, "Bearer must be a Hobbit"
					put("hobbitsword1", "1_299"); // Hobbit Sword: a [shire] possession, "Bearer must be a Hobbit", and no other text
					put("hobbitsword2", "1_299");
					put("hobbitsword3", "1_299");
					// Three more Hobbit Swords that stay in the draw deck, so that the Rule of 4 and not an empty deck ends the draws
					put("filler1", "1_299");
					put("filler2", "1_299");
					put("filler3", "1_299");
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] condition, "Bearer must be a [gondor] companion"
					put("map", "1_318");        // Thrór's Map: a [shire] possession that plays to the support area; its ability is not used
				}},
				new HashMap<>()
				{{
					put("site1", "11_260");
					put("site2", "13_185");     // Abandoned Mine Shaft: a burden when left in the regroup phase outside region 1; never left here
					// Never laid in these tests.
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TrollshawForestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Trollshaw Forest
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Left
		 * Game Text: <b>Forest</b>. Each time you play an artifact or possession on your companion, you may draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("site1");

		assertEquals("Trollshaw Forest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void EachPossessionOrArtifactPlayedOnHisCompanionOffersADrawThatMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var phial = scn.GetFreepsCard("phial");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(rangersword, phial);

		var forest = scn.GetFreepsSite("site1");
		scn.StartGame(forest);

		assertEquals(forest, scn.GetCurrentSite());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());

		// A possession on Aragorn: the draw is offered, and taken.
		scn.FreepsPlayCard(rangersword);
		assertAttachedTo(rangersword, aragorn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forest));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(forest));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Each time: an artifact on Frodo, a Hobbit and the Ring-bearer, offers it again.  Declined, nothing is drawn.
		scn.FreepsPlayCard(phial);
		assertAttachedTo(phial, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forest));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoDrawIsOfferedForAConditionOnACompanionAPossessionOnAnAllyOrInTheSupportAreaOrATransfer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword1 = scn.GetFreepsCard("hobbitsword1");
		var hobbitsword2 = scn.GetFreepsCard("hobbitsword2");
		var saga = scn.GetFreepsCard("saga");
		var map = scn.GetFreepsCard("map");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(frodo, hobbitsword1);
		scn.MoveCardsToHand(hobbitsword2, saga, map);

		var forest = scn.GetFreepsSite("site1");
		scn.StartGame(forest);

		assertEquals(forest, scn.GetCurrentSite());
		int deck = scn.GetFreepsDeckCount();

		// A condition on a companion.
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forest));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A possession on an ally, who is no companion.
		scn.FreepsPlayCard(hobbitsword2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(maggot, sam));
		scn.FreepsChooseCard(maggot);
		assertAttachedTo(hobbitsword2, maggot);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forest));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A possession played to the support area, on nobody.
		scn.FreepsPlayCard(map);
		assertInZone(Zone.SUPPORT, map);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forest));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A possession transferred from one companion to another is not played (CR 4.2 "transfer").
		assertAttachedTo(hobbitsword1, frodo);
		assertTrue(scn.FreepsTransferAvailable(hobbitsword1));
		// Sam is the only one it can go to (Maggot bears a hand weapon already): the transfer is made unasked.
		scn.FreepsTransferCard(hobbitsword1);
		assertAttachedTo(hobbitsword1, sam);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forest));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheOtherPlayersTurnAtTheFirstPlayersForestHisPossessionOnHisCompanionOffersHimTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetShadowCard("aragorn");
		var rangersword = scn.GetShadowCard("rangersword");
		var saga = scn.GetShadowCard("saga");
		var map = scn.GetShadowCard("map");
		scn.MoveCompanionsToTable(aragorn);

		// The first player's Forest is site 1; his fellowship moves on to site 2, and the second player's
		// fellowship begins its first turn at the Forest.
		var forest = scn.GetFreepsSite("site1");
		scn.StartGame(forest);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.SkipToOtherPlayersTurn();
		// The turns before drew his deck into his hand: hand and deck are set out now.
		scn.SetShadowHand(rangersword);
		scn.SetShadowDrawDeck(saga, map);
		scn.ShadowRefreshDecision();

		assertEquals(forest, scn.GetSite(1));
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		int deck = scn.GetShadowDeckCount();
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(rangersword);
		assertAttachedTo(rangersword, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(forest));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forest));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheFifthPlayOfAFellowshipPhaseOffersNoDrawOnceTheRuleOfFourIsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var rangersword = scn.GetFreepsCard("rangersword");
		var phial = scn.GetFreepsCard("phial");
		var hobbitsword1 = scn.GetFreepsCard("hobbitsword1");
		var hobbitsword2 = scn.GetFreepsCard("hobbitsword2");
		var hobbitsword3 = scn.GetFreepsCard("hobbitsword3");
		scn.MoveCompanionsToTable(aragorn, sam, pippin);
		scn.MoveCardsToHand(rangersword, phial, hobbitsword1, hobbitsword2, hobbitsword3);

		var forest = scn.GetFreepsSite("site1");
		scn.StartGame(forest);

		assertEquals(forest, scn.GetCurrentSite());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(5, scn.GetFreepsHandCount());

		// Four plays, four draws: each is offered and taken, and puts back the card it played.
		scn.FreepsPlayCard(rangersword);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forest));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsPlayCard(phial);
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forest));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsPlayCard(hobbitsword1);
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forest));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsPlayCard(hobbitsword2);
		scn.FreepsChooseCard(sam);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(forest));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsHandCount());

		// The fifth card played in the fellowship phase is the Rule of 4's: Pippin is the only Hobbit left that
		// can bear a hand weapon, and the draw, which would do nothing, is not offered.
		scn.FreepsPlayCard(hobbitsword3);
		assertAttachedTo(hobbitsword3, pippin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(forest));
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
