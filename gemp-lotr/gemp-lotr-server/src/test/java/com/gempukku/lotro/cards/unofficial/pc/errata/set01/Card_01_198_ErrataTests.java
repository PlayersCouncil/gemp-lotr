package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_198_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("gimli", "1_13");
					put("arwen", "1_30");

					put("ttmm", "51_198");
					put("runner", "1_178");
					put("scout", "1_191");

					put("chaff1", "1_192");
					put("chaff2", "1_193");
					put("chaff3", "1_194");
					put("chaff4", "1_195");
					put("chaff5", "1_196");
					put("chaff6", "1_197");
					put("chaff7", "1_198");
					put("chaff8", "1_199");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ThroughtheMistyMountainsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Through the Misty Mountains
		* Unique: False
		* Side: SHADOW
		* Culture: Moria
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: <b>Search.</b> To play, exert a [moria] minion.   <br>Each time the fellowship moves to any site 4, 5, or 6 and contains a Dwarf or Elf, draw a ttmm.  The Free Peoples player may discard 1 ttmm at random from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var ttmm = scn.GetFreepsCard("ttmm");

		assertFalse(ttmm.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ttmm.getBlueprint().getSide());
		assertEquals(Culture.MORIA, ttmm.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, ttmm.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(ttmm, Keyword.SEARCH));
		assertTrue(scn.hasKeyword(ttmm, Keyword.SUPPORT_AREA));
		assertEquals(1, ttmm.getBlueprint().getTwilightCost());
	}

	@Test
	public void TTMMExertsAMoriaMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		var scout = scn.GetShadowCard("scout");
		var ttmm = scn.GetShadowCard("ttmm");
		scn.ShadowMoveCharToTable(runner);
		scn.ShadowMoveCardToHand(scout, ttmm);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(ttmm));
		scn.ShadowPlayCard(scout);
		assertTrue(scn.ShadowPlayAvailable(ttmm));
		assertEquals(0, scn.GetWoundsOn(scout));
		scn.ShadowPlayCard(ttmm);
		assertEquals(1, scn.GetWoundsOn(scout));
	}

	@Test
	public void TTMMDrawsACardAt4and5and6IfADwarfIsInFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCharToTable(gimli);
		//Ensure that Freeps has no cards in hand ever to prevent TTMM
		scn.FreepsMoveCardToDiscard("arwen", "ttmm", "runner", "scout", "chaff1", "chaff2",
				"chaff3", "chaff4", "chaff5", "chaff6", "chaff7", "chaff8");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.ShadowMoveCardToSupportArea(ttmm);

		scn.StartGame();

		scn.SkipToSite(2);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 3
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(4);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 4
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipToSite(5);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 5
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipToSite(6);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 6
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipToSite(7);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 7
		//did not draw:
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
	}

	@Test
	public void TTMMDrawsACardAt4and5and6IfAElfIsInFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(arwen);
		//Ensure that Freeps has no cards in hand ever to prevent TTMM
		scn.FreepsMoveCardToDiscard("gimli", "ttmm", "runner", "scout", "chaff1", "chaff2",
				"chaff3", "chaff4", "chaff5", "chaff6", "chaff7", "chaff8");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.ShadowMoveCardToSupportArea(ttmm);

		scn.StartGame();

		scn.SkipToSite(2);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 3
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(4);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 4
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipToSite(5);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 5
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipToSite(6);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 6
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(9, scn.GetShadowHandCount());

		scn.SkipToSite(7);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 7
		//did not draw:
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
	}

	@Test
	public void TTMMDrawsNoCardsIfNoDwarfOrElfIsInFellowship() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		//Ensure that Freeps has no cards in hand ever to prevent TTMM
		scn.FreepsMoveCardToDiscard("arwen", "gimli", "ttmm", "runner", "scout", "chaff1", "chaff2",
				"chaff3", "chaff4", "chaff5", "chaff6", "chaff7", "chaff8");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.ShadowMoveCardToSupportArea(ttmm);

		scn.StartGame();

		scn.SkipToSite(2);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 3
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(4);

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 4
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(5);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 5
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(6);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 6
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());

		scn.SkipToSite(7);

		assertEquals(8, scn.GetShadowHandCount());
		scn.FreepsPassCurrentPhaseAction(); // move to 7
		//did not draw
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
	}

	@Test
	public void TTMMFreepsCanDiscardRandomCardFromHandToPreventDraw() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		//Ensure that Freeps has no cards in hand ever to prevent TTMM
		scn.FreepsMoveCharToTable("arwen", "gimli");

		var ttmm = scn.GetShadowCard("ttmm");
		scn.ShadowMoveCardToSupportArea(ttmm);

		scn.StartGame();

		scn.SkipToSite(3);
		scn.FreepsPassCurrentPhaseAction(); // move to 4

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		assertTrue(scn.FreepsDecisionAvailable("discard"));
		scn.FreepsChooseYes();
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(7, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
	}
}
