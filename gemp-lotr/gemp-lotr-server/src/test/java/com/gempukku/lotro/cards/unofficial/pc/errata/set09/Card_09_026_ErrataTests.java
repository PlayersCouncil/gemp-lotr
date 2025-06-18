package com.gempukku.lotro.cards.unofficial.pc.errata.set09;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.KillEffect;
import com.gempukku.lotro.logic.effects.ReturnCardsToHandEffect;
import com.gempukku.lotro.logic.timing.Action;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_09_026_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "59_26");

					put("chaff1", "1_3");
					put("chaff2", "1_3");
					put("chaff3", "1_3");
					put("chaff4", "1_3");
					put("chaff5", "1_3");
					put("chaff6", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RadagastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 9
		* Title: Radagast, The Brown
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Gandalf
		* Twilight Cost: 4
		* Type: companion
		* Subtype: Wizard
		* Strength: 7
		* Vitality: 4
		* Signet: Gandalf
		* Game Text: The move limit is +1. When Radagast leaves play, this bonus is immediately lost.
		* 	Each time the fellowship moves, each opponent may discard 2 cards from hand to draw 2 cards.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("radagast");

		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void RadagastMakesTheMoveLimitPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();
		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(radagast);
		assertEquals(3, scn.GetMoveLimit());
	}

	@Test
	public void RadagastMoveLimitBonusLostIfRadagastIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo, VirtualTableScenario.P1);
				action.appendEffect(new KillEffect(radagast, (PhysicalCard) null, KillEffect.Cause.CARD_EFFECT));
				return Collections.singletonList(action);
			}
		});

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(radagast);
		assertEquals(3, scn.GetMoveLimit());

		assertEquals(Zone.FREE_CHARACTERS, radagast.getZone());
		scn.FreepsUseCardAction(frodo);
		assertEquals(Zone.DEAD, radagast.getZone());
		scn.FreepsChoose("0");
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void RadagastMoveLimitBonusLostIfRadagastIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo, VirtualTableScenario.P1);
				action.appendEffect(new DiscardCardsFromPlayEffect(scn.P1, frodo, Filters.name("Radagast")));
				return Collections.singletonList(action);
			}
		});

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(radagast);
		assertEquals(3, scn.GetMoveLimit());

		assertEquals(Zone.FREE_CHARACTERS, radagast.getZone());
		scn.FreepsUseCardAction(frodo);
		assertEquals(Zone.DISCARD, radagast.getZone());
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void RadagastMoveLimitBonusLostIfRadagastIsReturnedToHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCardsToHand(radagast);

		scn.StartGame();
		scn.ApplyAdHocAction(new AbstractActionProxy() {
			@Override
			public List<? extends Action> getPhaseActions(String playerId, LotroGame game) {
				ActivateCardAction action = new ActivateCardAction(frodo, VirtualTableScenario.P1);
				action.appendEffect(new ReturnCardsToHandEffect(frodo, Filters.name("Radagast")));
				return Collections.singletonList(action);
			}
		});

		assertEquals(2, scn.GetMoveLimit());
		scn.FreepsPlayCard(radagast);
		assertEquals(3, scn.GetMoveLimit());

		assertEquals(Zone.FREE_CHARACTERS, radagast.getZone());
		scn.FreepsUseCardAction(frodo);
		assertEquals(Zone.HAND, radagast.getZone());
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void MovingDuringFellowshipPhaseTriggersShadowDiscardToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard 2 cards to draw 2 cards?"));
		scn.ShadowChooseYes();

		assertEquals(Zone.HAND, chaff1.getZone());
		assertEquals(Zone.HAND, chaff2.getZone());
		assertEquals(Zone.HAND, chaff3.getZone());
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		scn.ShadowChooseCards(chaff1, chaff2);
		assertEquals(Zone.DISCARD, chaff1.getZone());
		assertEquals(Zone.DISCARD, chaff2.getZone());
		assertEquals(Zone.HAND, chaff3.getZone());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
	}

	@Test
	public void MovingDuringRegroupPhaseTriggersShadowDiscardToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var radagast = scn.GetFreepsCard("radagast");
		scn.MoveCompanionsToTable(radagast);

		var chaff1 = scn.GetShadowCard("chaff1");
		var chaff2 = scn.GetShadowCard("chaff2");
		var chaff3 = scn.GetShadowCard("chaff3");
		var chaff4 = scn.GetShadowCard("chaff4");
		var chaff5 = scn.GetShadowCard("chaff5");
		var chaff6 = scn.GetShadowCard("chaff6");
		var evilradagast = scn.GetShadowCard("radagast");
		scn.MoveCardsToHand(chaff1, chaff2, chaff3);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseNo(); //ignore the fellowship phase movement

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.MoveCardsToBottomOfDeck(chaff4, chaff5, chaff6, evilradagast);
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard 2 cards to draw 2 cards?"));
		scn.ShadowChooseYes();

		assertEquals(Zone.HAND, chaff1.getZone());
		assertEquals(Zone.HAND, chaff2.getZone());
		assertEquals(Zone.HAND, chaff3.getZone());
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDeckCount());
		scn.ShadowChooseCards(chaff1, chaff2);
		assertEquals(Zone.DISCARD, chaff1.getZone());
		assertEquals(Zone.DISCARD, chaff2.getZone());
		assertEquals(Zone.HAND, chaff3.getZone());
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDeckCount());
	}

}
