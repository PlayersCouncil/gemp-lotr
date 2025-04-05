package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_091_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("cruelty", "53_91");
					put("soldier1", "1_271");
					put("soldier2", "1_271");

					put("sam", "1_310");
					put("freeps1", "2_1");
					put("freeps2", "2_2");
					put("freeps3", "2_3");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void HisCrueltyandMaliceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Title: His Cruelty and Malice
		 * Unique: False
		 * Side: SHADOW
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: condition
		 * Subtype: Support Area
		 * Game Text: Regroup: Discard a [sauron] minion to reveal the top 2 cards of your opponent's draw deck.  If at least 1 is a Free Peoples cruelty, exert a companion (except the Ring-bearer).  Your opponent may discard all revealed Free Peoples cards to prevent this.
		 */

		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetFreepsCard("cruelty");

		assertFalse(cruelty.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, cruelty.getBlueprint().getSide());
		assertEquals(Culture.SAURON, cruelty.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, cruelty.getBlueprint().getCardType());
		assertEquals(1, cruelty.getBlueprint().getTwilightCost());
	}

	@Test
	public void HisCrueltyandMaliceReveals2Cards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.ShadowMoveCardToSupportArea(cruelty);
		scn.ShadowMoveCharToTable(soldier1, soldier2);

		var sam = scn.GetFreepsCard("sam");
		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var shadow1 = scn.GetFreepsCard("cruelty");
		var shadow2 = scn.GetFreepsCard("soldier1");
		var shadow3 = scn.GetFreepsCard("soldier2");

		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardsToTopOfDeck(freeps1, freeps2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup phase
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(cruelty));
		scn.ShadowUseCardAction(cruelty);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(Zone.SHADOW_CHARACTERS, soldier1.getZone());
		scn.ShadowChooseCard(soldier1);
		assertEquals(Zone.DISCARD, soldier1.getZone());
		assertTrue(scn.FreepsDecisionAvailable("revealed"));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
	}

	@Test
	public void HisCrueltyRevealing2ShadowCardsDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.ShadowMoveCardToSupportArea(cruelty);
		scn.ShadowMoveCharToTable(soldier1, soldier2);

		var sam = scn.GetFreepsCard("sam");
		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var shadow1 = scn.GetFreepsCard("cruelty");
		var shadow2 = scn.GetFreepsCard("soldier1");
		var shadow3 = scn.GetFreepsCard("soldier2");

		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardsToTopOfDeck(shadow1,shadow2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup phase
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(cruelty));
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier1);

		var revealedCards = scn.FreepsGetBPChoices();
		assertTrue(revealedCards.contains(shadow1.getBlueprintId()));
		assertTrue(revealedCards.contains(shadow2.getBlueprintId()));
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowAnyDecisionsAvailable());
	}

	@Test
	public void HisCrueltyRevealing1FreepsExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.ShadowMoveCardToSupportArea(cruelty);
		scn.ShadowMoveCharToTable(soldier1, soldier2);

		var sam = scn.GetFreepsCard("sam");
		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var shadow1 = scn.GetFreepsCard("cruelty");
		var shadow2 = scn.GetFreepsCard("soldier1");
		var shadow3 = scn.GetFreepsCard("soldier2");

		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardsToTopOfDeck(freeps1,shadow1);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup phase
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(cruelty));
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier1);

		var revealedCards = scn.FreepsGetBPChoices();
		assertTrue(revealedCards.contains(shadow1.getBlueprintId()));
		assertTrue(revealedCards.contains(freeps1.getBlueprintId()));

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(sam));
	}

	@Test
	public void HisCrueltyRevealing2FreepsExerts() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.ShadowMoveCardToSupportArea(cruelty);
		scn.ShadowMoveCharToTable(soldier1, soldier2);

		var sam = scn.GetFreepsCard("sam");
		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var shadow1 = scn.GetFreepsCard("cruelty");
		var shadow2 = scn.GetFreepsCard("soldier1");
		var shadow3 = scn.GetFreepsCard("soldier2");

		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardsToTopOfDeck(freeps1,freeps2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup phase
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(cruelty));
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier1);

		var revealedCards = scn.FreepsGetBPChoices();
		assertTrue(revealedCards.contains(freeps2.getBlueprintId()));
		assertTrue(revealedCards.contains(freeps1.getBlueprintId()));

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(sam));
	}

	@Test
	public void FreepsDiscarding1RevealedCardPreventsExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.ShadowMoveCardToSupportArea(cruelty);
		scn.ShadowMoveCharToTable(soldier1, soldier2);

		var sam = scn.GetFreepsCard("sam");
		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var shadow1 = scn.GetFreepsCard("cruelty");
		var shadow2 = scn.GetFreepsCard("soldier1");
		var shadow3 = scn.GetFreepsCard("soldier2");

		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardsToTopOfDeck(freeps1,shadow1);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup phase
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(cruelty));
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier1);

		var revealedCards = scn.FreepsGetBPChoices();
		assertTrue(revealedCards.contains(shadow1.getBlueprintId()));
		assertTrue(revealedCards.contains(freeps1.getBlueprintId()));

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(Zone.DECK, freeps1.getZone());
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(Zone.DISCARD, freeps1.getZone());
	}

	@Test
	public void FreepsDiscarding2RevealedCardPreventsExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var cruelty = scn.GetShadowCard("cruelty");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.ShadowMoveCardToSupportArea(cruelty);
		scn.ShadowMoveCharToTable(soldier1, soldier2);

		var sam = scn.GetFreepsCard("sam");
		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var shadow1 = scn.GetFreepsCard("cruelty");
		var shadow2 = scn.GetFreepsCard("soldier1");
		var shadow3 = scn.GetFreepsCard("soldier2");

		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardsToTopOfDeck(freeps1,freeps2);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		//Regroup phase
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.ShadowActionAvailable(cruelty));
		scn.ShadowUseCardAction(cruelty);
		scn.ShadowChooseCard(soldier1);

		var revealedCards = scn.FreepsGetBPChoices();
		assertTrue(revealedCards.contains(freeps2.getBlueprintId()));
		assertTrue(revealedCards.contains(freeps1.getBlueprintId()));

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(Zone.DECK, freeps1.getZone());
		assertEquals(Zone.DECK, freeps2.getZone());
		scn.FreepsChooseYes();
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(Zone.DISCARD, freeps1.getZone());
		assertEquals(Zone.DISCARD, freeps2.getZone());
	}
}
