package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stone", "18_50");
					put("boromir", "1_96");
					put("ravager", "4_15");
					put("morc", "7_193");
					put("uruk", "4_187");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheFaithfulStoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: The Faithful Stone
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale.</b> Each time a non-[wraith] minion is played, you may spot a Man to put a [gondor] token here.<br><b>Maneuver:</b> Remove 3 [gondor] tokens from here to spot a minion. That minion cannot be assigned to a skirmish until the regroup phase.  Any Shadow player may remove (2) to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stone");

		assertEquals("The Faithful Stone", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheFaithfulStoneAddsTokenWhenNonRingwraithMinionPlayedIfManCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionToTable(boromir);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetCultureTokensOn(stone));
		scn.ShadowPlayCard(uruk);
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stone));
	}

	@Test
	public void TheFaithfulStoneAddsTokenWhenNonRingwraithMinionPlayedIfShadowManCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToSupportArea(stone);

		var uruk = scn.GetShadowCard("uruk");
		var dunlender = scn.GetShadowCard("ravager");
		scn.MoveCardsToHand(uruk);
		scn.MoveMinionsToTable(dunlender);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetCultureTokensOn(stone));
		scn.ShadowPlayCard(uruk);
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stone));
	}

	@Test
	public void TheFaithfulStoneAddsNoTokenIfManCannotBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToSupportArea(stone);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(uruk);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetCultureTokensOn(stone));
		scn.ShadowPlayCard(uruk);
		assertEquals(0, scn.GetCultureTokensOn(stone));
	}

	@Test
	public void TheFaithfulStoneAddsNoTokenWhenRingwraithMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToSupportArea(stone);

		var morc = scn.GetShadowCard("morc");
		scn.MoveCardsToHand(morc);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetCultureTokensOn(stone));
		scn.ShadowPlayCard(morc);
		assertEquals(0, scn.GetCultureTokensOn(stone));
	}

	@Test
	public void TheFaithfulStoneManueverActionCannotBeUsedIfLessThan3Tokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCardsToSupportArea(stone);
		scn.MoveCompanionToTable(boromir);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.AddTokensToCard(stone, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(stone));
	}

	@Test
	public void TheFaithfulStoneManueverActionRemoves3TokensToPreventAssigningTargetMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToSupportArea(stone);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.AddTokensToCard(stone, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.SetTwilight(0);
		assertTrue(scn.FreepsActionAvailable(stone));
		assertEquals(3, scn.GetCultureTokensOn(stone));
		scn.FreepsUseCardAction(stone);
		assertEquals(0, scn.GetCultureTokensOn(stone));

		// Maneuver actions
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		//Both pass Archery actions
		scn.PassCurrentPhaseActions();
		//Both pass Assignment actions
		scn.PassCurrentPhaseActions();
		//Both pass Fierce Assignment actions
		scn.PassCurrentPhaseActions();

		//We are now straight to the Regroup phase as there are no assignable minions
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void TheFaithfulStoneManueverActionCanBePreventedByShadowRemoving2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var stone = scn.GetFreepsCard("stone");
		scn.MoveCardsToSupportArea(stone);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.AddTokensToCard(stone, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.SetTwilight(2);
		assertTrue(scn.FreepsActionAvailable(stone));
		assertEquals(3, scn.GetCultureTokensOn(stone));
		scn.FreepsUseCardAction(stone);
		assertEquals(0, scn.GetCultureTokensOn(stone));

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Would you like to remove 2 twilight to prevent"));
		scn.ShadowChooseYes();
		assertEquals(0, scn.GetTwilight());

		scn.SkipToAssignments();
		assertTrue(scn.FreepsDecisionAvailable("assign"));
	}
}
