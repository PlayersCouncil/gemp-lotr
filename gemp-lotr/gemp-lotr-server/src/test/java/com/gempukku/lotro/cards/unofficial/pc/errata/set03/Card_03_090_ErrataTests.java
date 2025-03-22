package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_090_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("hand", "53_90");
					put("aragorn", "1_89");
					put("anduril", "7_79");
					put("pipe", "1_91");
					put("bow", "1_90");

					put("warden", "1_259");
					put("orc", "1_266");

				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void HandofSauronStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 3
		* Title: Hand of Sauron
		* Unique: False
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 0
		* Type: event
		* Subtype: Maneuver
		* Game Text: Exert a unique [sauron] minion to exert a companion (except the Ring-bearer) and
		 * discard a possession attached to that companion.  The Free Peoples player may discard 1
		 * card at random from their hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var hand = scn.GetFreepsCard("hand");

		assertFalse(hand.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hand.getBlueprint().getSide());
		assertEquals(Culture.SAURON, hand.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hand.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(hand, Timeword.MANEUVER));
		assertEquals(0, hand.getBlueprint().getTwilightCost());
	}

	@Test
	public void HandWorksIfNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		var pipe = scn.GetFreepsCard("pipe");
		scn.FreepsMoveCharToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, bow, pipe);
		scn.FreepsMoveCardToHand("warden", "orc");

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToHand(hand);
		scn.ShadowMoveCharToTable(warden, orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(hand));
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(orc));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.ATTACHED, anduril.getZone());
		assertSame(aragorn, anduril.getAttachedTo());
		assertEquals(Zone.ATTACHED, bow.getZone());
		assertSame(aragorn, bow.getAttachedTo());
		assertEquals(Zone.ATTACHED, pipe.getZone());
		assertSame(aragorn, pipe.getAttachedTo());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(orc));
		//aragorn automatically chosen as the only non-RB companion to target
		assertTrue(scn.ShadowDecisionAvailable("Choose a possession attached to that companion to discard"));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(bow);

		//Freeps given option to prevent
		assertTrue(scn.FreepsDecisionAvailable("Discard a card at random from hand to prevent exerting"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.ATTACHED, anduril.getZone());
		assertSame(aragorn, anduril.getAttachedTo());
		assertEquals(Zone.DISCARD, bow.getZone());
		assertEquals(Zone.ATTACHED, pipe.getZone());
		assertSame(aragorn, pipe.getAttachedTo());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
	}

	@Test
	public void HandCanBePreventedByFreepsDiscardingFromHandAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		var pipe = scn.GetFreepsCard("pipe");
		scn.FreepsMoveCharToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril, bow, pipe);
		scn.FreepsMoveCardToHand("warden", "orc");

		var hand = scn.GetShadowCard("hand");
		var warden = scn.GetShadowCard("warden");
		var orc = scn.GetShadowCard("orc");
		scn.ShadowMoveCardToHand(hand);
		scn.ShadowMoveCharToTable(warden, orc);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(hand));
		assertEquals(0, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(orc));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.ATTACHED, anduril.getZone());
		assertSame(aragorn, anduril.getAttachedTo());
		assertEquals(Zone.ATTACHED, bow.getZone());
		assertSame(aragorn, bow.getAttachedTo());
		assertEquals(Zone.ATTACHED, pipe.getZone());
		assertSame(aragorn, pipe.getAttachedTo());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(hand);
		assertEquals(1, scn.GetWoundsOn(warden));
		assertEquals(0, scn.GetWoundsOn(orc));
		//aragorn automatically chosen as the only non-RB companion to target
		assertTrue(scn.ShadowDecisionAvailable("Choose a possession attached to that companion to discard"));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(bow);

		//Freeps given option to prevent
		assertTrue(scn.FreepsDecisionAvailable("Discard a card at random from hand to prevent exerting"));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.ATTACHED, anduril.getZone());
		assertSame(aragorn, anduril.getAttachedTo());
		assertEquals(Zone.ATTACHED, bow.getZone());
		assertSame(aragorn, bow.getAttachedTo());
		assertEquals(Zone.ATTACHED, pipe.getZone());
		assertSame(aragorn, pipe.getAttachedTo());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
	}
}
