package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deft", "2_99");

					// Frodo, Son of Drogo 1_290 is the Ring-bearer, a Hobbit companion.
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F; his skirmish ability is used only on purpose
					put("goldberry", "9_51");   // Goldberry, River-daughter: a [shire] ally who is no Hobbit, home 2F; her texts need a river
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; his abilities are used only on purpose

					// Cards that are no sites, read in hand
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession, cost 1
					put("runner", "1_178");     // Goblin Runner: a minion, cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeftinTheirMovementsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Deft in Their Movements
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Stealth</b>.
		 * 	<b>Regroup:</b> Spot 2 Hobbits to make each site's Shadow number -2 until the end of the turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deft");

		assertEquals("Deft in Their Movements", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAHobbitCompanionAndAHobbitAllyMakesEachSiteTwoLessAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deft = scn.GetFreepsCard("deft");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(3);
		// The regroup phase at Balin's Tomb, site 4
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(deft, sword, runner);
		scn.FreepsRefreshDecision();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site4 = scn.GetSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(2, scn.GetTwilightCost(site2));
		assertEquals(3, scn.GetTwilightCost(site4));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(deft));

		scn.FreepsPlayCard(deft);
		assertInDiscard(deft);
		assertEquals(0, scn.GetTwilightCost(site1));
		assertEquals(0, scn.GetTwilightCost(site2));
		assertEquals(1, scn.GetTwilightCost(site4));
		// A possession and a minion in hand are no sites.
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedSpottingOneHobbitBesideAShireAllyWhoIsNoHobbitAndCanOnceTheOpponentsHobbitIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deft = scn.GetFreepsCard("deft");
		var goldberry = scn.GetFreepsCard("goldberry");
		var ted = scn.GetShadowCard("ted");
		scn.MoveAlliesToTable(goldberry);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(deft);
		scn.FreepsRefreshDecision();

		// Frodo is the one Hobbit; Goldberry is [shire] and no Hobbit.
		assertInPlay(goldberry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(deft));

		// The Shadow player's Hobbit minion is a Hobbit too: no "your" in the text.
		scn.MoveMinionsToTable(ted);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(deft));

		scn.FreepsPlayCard(deft);
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(4)));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void EverySiteInPlayStaysTwoLessThroughTheSecondMoveAndIsBackOnTheNextTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deft = scn.GetFreepsCard("deft");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(deft);
		scn.FreepsRefreshDecision();

		var site4 = scn.GetSite(4);
		assertEquals(3, scn.GetTwilightCost(site4));

		scn.FreepsPlayCard(deft);
		assertEquals(1, scn.GetTwilightCost(site4));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// Still in force in the Shadow phase after the second move (an `until: end(regroup)` is over by now).
		var site5 = scn.GetSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(site4));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetTwilightCost(site4));
		assertEquals(6, scn.GetTwilightCost(site5));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Ignore("F-0555: the definition lays a standing ModifyCost over `site` until the end of the turn, so a site laid after the event is played is -2 too; CR 4.2 \"event\" gives this very card as its example (\"affects only sites that are in play when it is played\"). Site 5, laid by the second move, reads 4 and the move adds 6; it should read 6 and add 8.")
	@Test
	public void ASiteLaidByTheSecondMoveKeepsItsPrintedShadowNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deft = scn.GetFreepsCard("deft");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToHand(deft);
		scn.FreepsRefreshDecision();

		var site4 = scn.GetSite(4);
		assertEquals(3, scn.GetTwilightCost(site4));
		// Site 5 is not on the path yet: it is laid as the fellowship moves on.
		assertNull(scn.GetSite(5));

		scn.FreepsPlayCard(deft);
		assertEquals(1, scn.GetTwilightCost(site4));
		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsChooseToMove();
		// The Bridge of Khazad-dûm, Shadow number 6, was not in play when the event was played: 6 + 2 companions
		var site5 = scn.GetSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilightCost(site5));
		assertEquals(8, scn.GetTwilight());
		// The site that was in play is still two less.
		assertEquals(1, scn.GetTwilightCost(site4));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
