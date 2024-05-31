package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_126_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("hunt", "51_126");
					put("uruk", "1_154");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void HuntThemDownStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Hunt Them Down!
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Isengard
		* Twilight Cost: 2
		* Type: event
		* Subtype: 
		* Game Text: <b>Maneuver:</b> Make an Uruk-hai <b>fierce</b> until the regroup phase.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var hunt = scn.GetFreepsCard("hunt");

		assertFalse(hunt.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hunt.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, hunt.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, hunt.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(hunt, Timeword.MANEUVER));
		assertEquals(2, hunt.getBlueprint().getTwilightCost());
	}

	@Test
	public void HuntThemDownAddsFierceDuringManeuver() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hunt = scn.GetShadowCard("hunt");
		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCharToTable(uruk);
		scn.ShadowMoveCardToHand(hunt);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(hunt));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(uruk, Keyword.FIERCE));
	}
}
