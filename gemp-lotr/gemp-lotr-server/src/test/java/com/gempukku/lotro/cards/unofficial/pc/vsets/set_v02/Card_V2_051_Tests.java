package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.HasInitiativeModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_051_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("wrath", "102_51");
					put("sam", "7_327");
					put("eowyn", "5_122");
					put("javelin", "7_248");
					put("deor", "7_222");
					put("bounder", "1_286");
					put("castout", "12_108");

					put("unspoiled", "7_327");
					put("lord", "4_219");
					put("cantea", "1_230");
					put("terror", "1_226");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NowforWrathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Now for Wrath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [rohan] item or [rohan] ally is discarded or killed (except during the fellowship phase), add a [rohan] token here.
		* 	Each time a minion is exerted by a Free Peoples card, you may remove 2 [rohan] tokens here and exert a valiant companion to wound that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("Now for Wrath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NowforWrathDoesNotReactToFellowshipPhasePossessionDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var javelin = scn.GetFreepsCard("javelin");
		var sam = scn.GetFreepsCard("sam");
		scn.FreepsMoveCardToHand(javelin);
		scn.FreepsMoveCharToTable(sam);
		scn.FreepsMoveCardToSupportArea(wrath);

		//cheating to get around Sam's initiative requirement
		scn.ApplyAdHocModifier(new HasInitiativeModifier(null, null, Side.FREE_PEOPLE));

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertEquals(Zone.HAND, javelin.getZone());

		scn.FreepsUseCardAction(sam);
		assertEquals(Zone.DISCARD, javelin.getZone());
		assertEquals(0, scn.GetCultureTokensOn(wrath));
	}

	@Test
	public void NowforWrathReactsToManeuverPhaseRohanAllyKill() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var deor = scn.GetFreepsCard("deor");
		var bounder = scn.GetFreepsCard("bounder");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.FreepsMoveCharToTable(eowyn);
		scn.FreepsMoveCardToSupportArea(wrath, deor, bounder);

		scn.AddWoundsToChar(deor, 1);
		scn.AddWoundsToChar(bounder, 1);

		var terror = scn.GetShadowCard("terror");
		scn.ShadowMoveCharToTable("cantea");
		scn.ShadowMoveCardToHand(terror);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertEquals(Zone.SUPPORT, deor.getZone());
		assertEquals(Zone.SUPPORT, bounder.getZone());

		scn.ShadowPlayCard(terror);
		scn.FreepsResolveRuleFirst();
		assertEquals(Zone.DEAD, deor.getZone());
		assertEquals(Zone.DEAD, bounder.getZone());
		//Token from Deor, but not from Bounder
		assertEquals(1, scn.GetCultureTokensOn(wrath));
	}

	@Test
	public void NowforWrathReactsToArcheryPhasePossessionDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var javelin = scn.GetFreepsCard("javelin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.FreepsMoveCharToTable(eowyn);
		scn.AttachCardsTo(eowyn, javelin);
		scn.FreepsMoveCardToSupportArea(wrath);

		scn.ShadowMoveCharToTable("cantea");

		scn.StartGame();

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertEquals(Zone.ATTACHED, javelin.getZone());

		scn.FreepsUseCardAction(javelin);
		assertEquals(Zone.DISCARD, javelin.getZone());
		assertEquals(1, scn.GetCultureTokensOn(wrath));
	}

	@Test
	public void NowforWrathReactsToSkirmishPhaseShadowPossessionDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var javelin = scn.GetFreepsCard("javelin");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.FreepsMoveCharToTable(eowyn);
		scn.AttachCardsTo(eowyn, javelin);
		scn.FreepsMoveCardToSupportArea(wrath);

		var cantea = scn.GetShadowCard("cantea");
		scn.ShadowMoveCharToTable(cantea);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eowyn, cantea);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertEquals(Zone.ATTACHED, javelin.getZone());

		scn.ShadowUseCardAction(cantea);
		assertEquals(Zone.DISCARD, javelin.getZone());
		assertEquals(1, scn.GetCultureTokensOn(wrath));
	}

	@Test
	public void NowforWrathReactsToReconciledPossessionDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var javelin = scn.GetFreepsCard("javelin");
		scn.FreepsMoveCardToSupportArea(wrath);
		scn.FreepsMoveCardToHand(javelin);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.FreepsChooseCard(javelin);
		assertEquals(Zone.DISCARD, javelin.getZone());
		assertEquals(1, scn.GetCultureTokensOn(wrath));
	}

	@Test
	public void NowforWrathReactsToReconciledAllyDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var deor = scn.GetFreepsCard("deor");
		scn.FreepsMoveCardToSupportArea(wrath);
		scn.FreepsMoveCardToHand(deor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.FreepsChooseCard(deor);
		assertEquals(Zone.DISCARD, deor.getZone());
		assertEquals(1, scn.GetCultureTokensOn(wrath));
	}

	@Test
	public void NowforWrathDoesNotRespondToMinionExertedByFreepsCardIfLessThan2Tokens() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var eowyn = scn.GetFreepsCard("eowyn");
		var castout = scn.GetFreepsCard("castout");

		scn.FreepsMoveCharToTable(eowyn);
		scn.FreepsMoveCardToSupportArea(wrath, castout);
		scn.AddTokensToCard(wrath, 1);

		var cantea = scn.GetShadowCard("cantea");
		scn.ShadowMoveCharToTable(cantea);
		scn.AddWoundsToChar(cantea, 1);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("Exert a Minion"));
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.hasKeyword(eowyn, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetWoundsOn(cantea));

		scn.FreepsUseCardAction(castout);
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action or pass"));
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(cantea));
	}

	@Test
	public void NowforWrathDoesNotRespondToMinionExertedByFreepsCardIfHas2TokensButNoValiantComp() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var deor = scn.GetFreepsCard("deor");
		var castout = scn.GetFreepsCard("castout");

		scn.FreepsMoveCardToSupportArea(wrath, castout, deor);
		scn.AddTokensToCard(wrath, 2);

		var cantea = scn.GetShadowCard("cantea");
		scn.ShadowMoveCharToTable(cantea);
		scn.AddWoundsToChar(cantea, 1);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("Exert a Minion"));
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		assertFalse(scn.hasKeyword(deor, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(1, scn.GetWoundsOn(cantea));

		scn.FreepsUseCardAction(castout);
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action or pass"));
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(2, scn.GetWoundsOn(cantea));
	}

	@Test
	public void NowforWrathRemoves2TokenAndExertsValiantCompToWoundMinionExertedByFreepsCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var eowyn = scn.GetFreepsCard("eowyn");
		var castout = scn.GetFreepsCard("castout");

		scn.FreepsMoveCharToTable(eowyn);
		scn.FreepsMoveCardToSupportArea(wrath, castout);
		scn.AddTokensToCard(wrath, 3);

		var cantea = scn.GetShadowCard("cantea");
		scn.ShadowMoveCharToTable(cantea);
		scn.AddWoundsToChar(cantea, 1);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("Exert a Minion"));
		assertEquals(3, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.hasKeyword(eowyn, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetWoundsOn(cantea));
		assertEquals(Zone.SHADOW_CHARACTERS, cantea.getZone());

		scn.FreepsUseCardAction(castout);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(Zone.DISCARD, cantea.getZone());

		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action or pass"));

	}


}
