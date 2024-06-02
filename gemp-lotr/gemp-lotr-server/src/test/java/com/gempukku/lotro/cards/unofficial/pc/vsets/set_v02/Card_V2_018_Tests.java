package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_018_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("card", "102_18");
					put("boromir", "1_97");
					put("faramir", "4_117");
					put("twk", "1_237");
					// put other cards in here as needed for the test case
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void DenethorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Denethor, Formidable Father
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Site Number: 3K
		 * Game Text: While you can spot the same number of threats as companions, each [gondor] companion is strength -2.
		* 	At the start of the maneuver phase, you may add 2 threats to make a [gondor] companion defender +1 and strength +2 until the regroup phase (or add 1 threat if that companion is Boromir).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Denethor", card.getBlueprint().getTitle());
		assertEquals("Formidable Father", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.KING, 3)));
	}

	@Test
	public void gondorCompanionsAreDiminishedWhenThreatsAreFull() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");

		scn.FreepsMoveCardToHand(card, boromir, faramir);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(boromir);
		scn.FreepsPlayCard(faramir);

		// Their starting strengths should be 7 with zero threats
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(7, scn.GetStrength(faramir));


		// Their starting strengths should be 7 with one threat
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(7, scn.GetStrength(faramir));

		// Their starting strengths should be 7 with two threats
		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(7, scn.GetStrength(faramir));

		// Their starting strengths should be 5 with three threats
		scn.AddThreats(1);
		assertEquals(3, scn.GetThreats());
		assertEquals(5, scn.GetStrength(boromir));
		assertEquals(5, scn.GetStrength(faramir));
	}

	@Test
	public void startOfManeuverTriggerCostTwoThreatsToBoostFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var twk = scn.GetShadowCard("twk");

		scn.FreepsMoveCardToHand(card, boromir, faramir, twk);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(boromir);
		scn.FreepsPlayCard(faramir);

		// Their starting strengths should be 7 with zero threats
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(faramir));
		assertFalse(scn.HasKeyword(faramir, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowMoveCardToHand(twk);

		scn.ShadowPlayCard(twk);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		scn.FreepsChooseCard(faramir);

		assertEquals(2, scn.GetThreats());
		assertEquals(9, scn.GetStrength(faramir));
		assertTrue(scn.HasKeyword(faramir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
	}

	@Test
	public void startOfManeuverTriggerCostOneThreatToBoostBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var boromir = scn.GetFreepsCard("boromir");
		var faramir = scn.GetFreepsCard("faramir");
		var twk = scn.GetShadowCard("twk");

		scn.FreepsMoveCardToHand(card, boromir, faramir, twk);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());

		scn.FreepsPlayCard(boromir);
		scn.FreepsPlayCard(faramir);

		// Their starting strengths should be 7 with zero threats
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(boromir));
		assertFalse(scn.HasKeyword(boromir, Keyword.DEFENDER));

		scn.SkipToPhase(Phase.SHADOW);
		scn.ShadowMoveCardToHand(twk);

		scn.ShadowPlayCard(twk);

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.HasKeyword(boromir, Keyword.DEFENDER));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
	}
}
