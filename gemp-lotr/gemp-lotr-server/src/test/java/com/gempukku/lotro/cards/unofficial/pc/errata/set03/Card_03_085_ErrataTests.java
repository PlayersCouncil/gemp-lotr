package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_085_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tgat", "53_85");
					put("twigul", "101_40");
					put("lord", "4_219");

					put("sam", "1_311");
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TooGreatandTerribleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Too Great and Terrible
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [ringwraith] minion. Bearer must
		* 	be a companion or ally (except the Ring-bearer). Limit 1 per bearer.
		* 	Each time bearer is exerted by a Free Peoples card, exert bearer unless the Free Peoples player
		 * 	discards a card from hand of bearer's culture.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tgat");

		assertEquals("Too Great and Terrible", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TooGreatandTerribleExertsARingwraithMinionToPlayOnCompOrAllyExceptRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var tgat = scn.GetShadowCard("tgat");
		var twigul = scn.GetShadowCard("twigul");
		var lord = scn.GetShadowCard("lord");
		scn.MoveCardsToHand(tgat);
		scn.MoveMinionsToTable(twigul, lord);

		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionToTable(sam);
		scn.MoveCardsToSupportArea(bounder);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(twigul));
		assertEquals(0, scn.GetWoundsOn(lord));
		assertEquals(Zone.HAND, tgat.getZone());
		assertTrue(scn.ShadowPlayAvailable(tgat));

		scn.ShadowPlayCard(tgat);

		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoicesAvailable(sam, bounder));

		scn.ShadowChooseCard(bounder);
		assertEquals(Zone.ATTACHED, tgat.getZone());
		assertEquals(bounder, tgat.getAttachedTo());
		assertEquals(1, scn.GetWoundsOn(twigul));
		assertEquals(0, scn.GetWoundsOn(lord));
	}

	@Test
	public void TooGreatandTerribleExertsBearerEachTimeTheyExertFromFreepsCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionToTable(sam);
		scn.MoveCardsToHand(bounder);

		var tgat = scn.GetShadowCard("tgat");
		scn.AttachCardsTo(sam, tgat);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsUseCardAction(sam);

		assertTrue(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		scn.FreepsChooseNo();
		assertEquals(2, scn.GetWoundsOn(sam));
	}

	@Test
	public void TooGreatandTerribleDoesNotExertBearerIfDoneByShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionToTable(sam);
		scn.MoveCardsToHand(bounder);

		var tgat = scn.GetShadowCard("tgat");
		var lord = scn.GetShadowCard("lord");
		scn.AttachCardsTo(sam, tgat);
		scn.MoveMinionsToTable(lord);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowUseCardAction(lord);

		assertEquals(1, scn.GetWoundsOn(sam));

		assertFalse(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
	}

	@Test
	public void TooGreatandTerribleLetsFreepsBlockExertionIfCardDiscardedFromHandOfSameCulture() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionToTable(sam);
		scn.MoveCardsToHand(bounder);

		var tgat = scn.GetShadowCard("tgat");
		scn.AttachCardsTo(sam, tgat);

		scn.StartGame(false);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(Zone.HAND, bounder.getZone());
		scn.FreepsUseCardAction(sam);

		assertTrue(scn.FreepsDecisionAvailable("Discard a card from hand sharing a culture with"));
		scn.FreepsChooseYes();
		assertEquals(Zone.DISCARD, bounder.getZone());
		assertEquals(1, scn.GetWoundsOn(sam));
	}
}
