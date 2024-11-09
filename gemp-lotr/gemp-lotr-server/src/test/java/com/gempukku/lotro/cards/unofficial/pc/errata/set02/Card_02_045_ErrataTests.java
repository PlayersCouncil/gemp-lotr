package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_045_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("attention", "52_45");

					put("urukarcher", "1_131");
					put("goblinarcher", "1_176");
					put("sauron", "9_48");
					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.IsildursBaneRing
		);
	}

	@Test
	public void TooMuchAttentionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: Too Much Attention
		* Unique: False
		* Side: SHADOW
		* Culture: Isengard
		* Twilight Cost: 1
		* Type: event
		* Subtype: Response
		* Game Text: If the Ring-bearer puts on The One Ring, spot an [isengard] minion to take a minion into hand
		 * from your draw deck.  The Free Peoples player may discard the top 6 cards of their
		 * draw deck to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var attention = scn.GetFreepsCard("attention");

		assertFalse(attention.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, attention.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, attention.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, attention.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(attention, Timeword.RESPONSE));
		assertEquals(1, attention.getBlueprint().getTwilightCost());
	}

	@Test
	public void PuttingOnRingWithNoIsengardMinionDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var attention = scn.GetShadowCard("attention");
		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.ShadowMoveCardToHand(attention);
		scn.ShadowMoveCharToTable(goblinarcher);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable()); // IB One Ring
		scn.FreepsAcceptOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable()); // Should have no response from Shadow
	}

	@Test
	public void PuttingOnRingWithIsengardMinionCanTakeMinionIntoHandFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var attention = scn.GetShadowCard("attention");
		var urukarcher = scn.GetShadowCard("urukarcher");
		var goblinarcher = scn.GetShadowCard("goblinarcher");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var sauron = scn.GetShadowCard("sauron");
		scn.ShadowMoveCardToHand(attention);
		scn.ShadowMoveCharToTable(urukarcher);
		scn.ShadowMoveCardToDiscard(runner1, goblinarcher);
		scn.ShadowMoveCardsToTopOfDeck(runner2, sauron);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger(); // IB One Ring

		assertEquals(Zone.HAND,  attention.getZone());
		assertEquals(Zone.DECK,  sauron.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(sauron);
		scn.ShadowDismissRevealedCards();
		//Decline the prevention
		scn.FreepsChooseNo();
		assertEquals(Zone.DISCARD,  attention.getZone());
		assertEquals(Zone.HAND,  sauron.getZone());
	}

	@Test
	public void MinionFromDrawDeckCanBePreventedByFreepsDiscardingTop5Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		var attention = scn.GetShadowCard("attention");
		var urukarcher = scn.GetShadowCard("urukarcher");
		var goblinarcher = scn.GetShadowCard("goblinarcher");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var sauron = scn.GetShadowCard("sauron");
		scn.ShadowMoveCardToHand(attention);
		scn.ShadowMoveCharToTable(urukarcher);
		scn.ShadowMoveCardToDiscard(runner1, goblinarcher);
		scn.ShadowMoveCardsToTopOfDeck(runner2, sauron);

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.PassCurrentPhaseActions();
		scn.FreepsAcceptOptionalTrigger(); // IB One Ring

		assertEquals(Zone.HAND,  attention.getZone());
		assertEquals(Zone.DECK,  sauron.getZone());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetShadowCardChoiceCount());
		scn.ShadowChooseCardBPFromSelection(sauron);

		//Accept the prevention
		assertEquals(7, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.ShadowDismissRevealedCards();
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(6, scn.GetFreepsDiscardCount());
		assertEquals(Zone.DISCARD,  attention.getZone());
		assertEquals(Zone.DECK,  sauron.getZone());
	}

}
