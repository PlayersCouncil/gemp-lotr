package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_334_Tests
{

	// The King path, Steps of Edoras its site 1 in place of Edoras Hall.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					// Pippin, Trollslayer: [gondor] Hobbit companion, cost 2; to play, spot a [gondor] companion
					put("pippin", "103_31");
					put("household", "5_83");   // Household Guard: [rohan] Man companion; Valiant is his whole text
					// Gondorian Merchant: the [gondor] ally, a Man; a maneuver ability used only on purpose
					put("merchant", "7_97");
				}},
				new HashMap<>() {{
					put("site1", "7_334");
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
	public void StepsofEdorasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Steps of Edoras
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Each [gondor] companion is <b>defender +1</b> until the end of the turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Steps of Edoras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachGondorCompanionIsDefenderPlus1ThroughTheRestOfTheTurnAfterTheStepsAreLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var household = scn.GetFreepsCard("household");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(boromir, household);
		scn.MoveCardsToSupportArea(merchant);
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		assertEquals("Steps of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// A [gondor] Man; not the [rohan] Man, the [gondor] ally or Frodo.
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(household, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));

		// A [gondor] Hobbit played there in that phase.
		scn.FreepsPlayCard(pippin);
		assertEquals(1, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The move to site 2: the Steps are left, and the turn is not over.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(household, Keyword.DEFENDER));

		// The last decision of the turn: still there.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(household, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
	}

	@Test
	public void TheOtherPlayersGondorCompanionIsDefenderPlus1OnHisOwnTurnThereAndNeitherIsOnceTheirTurnsAreOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		// Player 2's own fellowship.
		var pippin = scn.GetShadowCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		scn.FreepsPass();

		// Player 2's first turn, his fellowship at the Steps.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals("Steps of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
		scn.ShadowPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetKeywordCount(pippin, Keyword.DEFENDER));

		// Player 1's second turn, at site 2: the turn the bonus was for is over.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		// And player 2's second turn, at site 2: his is over too.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DEFENDER));
	}
}
