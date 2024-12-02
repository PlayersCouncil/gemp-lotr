package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_082_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<String, String>()
				{{
					put("grond", "68_82");
					put("goblin", "11_137");

					put("chaff1", "1_89");
					put("chaff2", "1_365");
					put("chaff3", "3_38");
					put("chaff4", "4_109");
					put("chaff5", "4_364");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 18
		* Title: Grond, Forged With Black Steel
		* Unique: True
		* Side: SHADOW
		* Culture: Orc
		* Twilight Cost: 3
		* Type: possession
		* Subtype: Support Area
		* Game Text: To play, spot an [orc] minion.<br>Each time the fellowship moves during the regroup phase, you may discard this possession to make the Free Peoples player shuffle 2 cards from their hand into their draw deck.  Search that deck and discard 2 Free Peoples cards from it.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var grond = scn.GetFreepsCard("grond");

		assertTrue(grond.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, grond.getBlueprint().getSide());
		assertEquals(Culture.ORC, grond.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, grond.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(grond, Keyword.SUPPORT_AREA));
		assertEquals(3, grond.getBlueprint().getTwilightCost());
	}

	@Test
	public void GrondRequiresOrcMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		var goblin = scn.GetShadowCard("goblin");
		scn.ShadowMoveCardToHand(grond, goblin);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(grond));
		scn.ShadowPlayCard(goblin);
		assertTrue(scn.ShadowPlayAvailable(grond));
	}

	@Test
	public void GrondTriggersDuringRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var grond = scn.GetShadowCard("grond");
		scn.ShadowMoveCardToSupportArea(grond);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void GrondTriggerShuffles2CardsAndSearchesFreepsDeckToDiscard2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		var chaff3 = scn.GetFreepsCard("chaff3");
		var chaff4 = scn.GetFreepsCard("chaff4");
		var chaff5 = scn.GetFreepsCard("chaff5");
		var shadow1 = scn.GetFreepsCard("grond");
		var shadow2 = scn.GetFreepsCard("goblin");
		scn.FreepsMoveCardToHand(chaff1, chaff2, shadow1);

		var grond = scn.GetShadowCard("grond");
		scn.ShadowMoveCardToSupportArea(grond);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToMove();

		assertEquals(Zone.SUPPORT, grond.getZone());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(Zone.DISCARD, grond.getZone());

		assertTrue(scn.FreepsDecisionAvailable("choose cards to shuffle"));
		assertEquals("2", Arrays.stream(scn.FreepsGetADParam("max")).findFirst().get());
		assertEquals(Zone.HAND, chaff1.getZone());
		assertEquals(Zone.HAND, shadow1.getZone());
		scn.FreepsChooseCardIDFromSelection(chaff1, shadow1);
		assertEquals(Zone.DECK, chaff1.getZone());
		assertEquals(Zone.DECK, shadow1.getZone());
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.ShadowDecisionAvailable("Choose card"));
		// 5 starting freeps cards - 2 put in hand + 1 shuffled into deck
        assertEquals(4, scn.ShadowGetSelectableCount());
		assertEquals(Zone.DECK, chaff3.getZone());
		assertEquals(Zone.DECK, chaff4.getZone());
		scn.ShadowChooseCardBPFromSelection(chaff3, chaff4);

		assertEquals(Zone.DISCARD, chaff3.getZone());
		assertEquals(Zone.DISCARD, chaff4.getZone());
	}
}
