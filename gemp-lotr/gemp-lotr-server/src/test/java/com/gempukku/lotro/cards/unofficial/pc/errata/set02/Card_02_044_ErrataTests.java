package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_044_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("business", "52_44");
					put("uruk", "1_151");

					put("mirror", "1_55");
					put("curse1", "1_36");
					put("curse2", "1_36");
					put("galadriel", "6_18");
					put("chaff1", "6_19");
					put("chaff2", "6_20");
					put("chaff3", "6_21");
					put("chaff4", "6_22");
					put("chaff5", "6_23");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NoBusinessofOursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: No Business of Ours
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Isengard
		* Twilight Cost: 3
		* Type: condition
		* Subtype: Support Area
		* Game Text: While you can spot an [isengard] minion, this card is twilight cost -3.
		* 	The Free Peoples player may not look at or reveal cards in any Shadow player’s hand.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetFreepsCard("business");

		assertFalse(business.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, business.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, business.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, business.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(business, Keyword.SUPPORT_AREA));
		assertEquals(3, business.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoBusinessSpotsNoMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.ShadowMoveCardToHand(business);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(10);

		assertTrue(scn.ShadowPlayAvailable(business));
	}

	@Test
	public void NoBusinessDiscountsIfIsengardMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCardToHand(business);
		scn.ShadowMoveCharToTable(uruk);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(10);
		scn.ShadowPlayCard(business);
		assertEquals(10, scn.GetTwilight());
	}

	@Test
	public void FreepsCannotRevealCardsInShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.ShadowMoveCardToHand(business);

		var galadriel = scn.GetFreepsCard("galadriel");
		var curse1 = scn.GetFreepsCard("curse1");
		var curse2 = scn.GetFreepsCard("curse2");
		scn.FreepsMoveCardToHand(curse1, curse2);
		scn.FreepsMoveCardToSupportArea(galadriel);

		scn.StartGame();
		scn.ShadowDrawCards(8);

		//First check that curse works with No Business in hand
		scn.FreepsPlayCard(curse1);
		assertTrue(scn.FreepsDecisionAvailable("Hand"));
		scn.ShadowMoveCardToSupportArea(business); //slide in No Business between actions
		scn.FreepsDismissRevealedCards();

		//Curse's effect should now be blocked
		assertTrue(scn.FreepsPlayAvailable(curse2));
		scn.FreepsPlayCard(curse2);
		assertFalse(scn.FreepsDecisionAvailable("Hand"));
		assertTrue(scn.FreepsDecisionAvailable("Play Fellowship action"));
	}

	@Test
	public void FreepsCannotLookAtCardsInShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var business = scn.GetShadowCard("business");
		scn.ShadowMoveCardToHand(business);
		scn.ShadowMoveCharToTable("uruk");

		var galadriel = scn.GetFreepsCard("galadriel");
		var mirror = scn.GetFreepsCard("mirror");
		scn.FreepsMoveCardToSupportArea(mirror, galadriel);

		scn.StartGame();
		scn.ShadowDrawCards(8);
		scn.SkipToPhase(Phase.MANEUVER);

		//First check that the mirror works with No Business in hand
		scn.FreepsUseCardAction(mirror);
		assertTrue(scn.FreepsDecisionAvailable("Opponent's hand"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(2, scn.GetFreepsCardChoiceCount());
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseAnyCard();

		scn.ShadowMoveCardToSupportArea(business); //slide in No Business between actions
		scn.ShadowPassCurrentPhaseAction();

		//Mirror's effect should now be blocked
		assertTrue(scn.FreepsActionAvailable(mirror));
		scn.FreepsUseCardAction(mirror);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Play Maneuver action"));
	}
}
