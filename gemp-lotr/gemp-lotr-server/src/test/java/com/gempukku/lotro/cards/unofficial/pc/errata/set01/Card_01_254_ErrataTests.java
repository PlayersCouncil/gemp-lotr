package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_254_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("mordor", "51_254");
					put("wraith", "6_99");

					put("greenleaf", "1_50");
					put("arwen", "3_7");
					put("bow", "1_41");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void MordorEnragedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Mordor Enraged
		* Side: Free Peoples
		* Culture: Sauron
		* Twilight Cost: 1
		* Type: condition
		* Subtype: 
		* Game Text: To play, exert a [sauron] minion. Plays on an archer companion.
		* 	Each time the Free Peoples player uses an archery special ability, bearer must exert.
		*/

		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		PhysicalCardImpl mordor = scn.GetFreepsCard("mordor");

		assertFalse(mordor.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, mordor.getBlueprint().getSide());
		assertEquals(Culture.SAURON, mordor.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, mordor.getBlueprint().getCardType());
		assertFalse(scn.hasKeyword(mordor, Keyword.SUPPORT_AREA));
		assertEquals(1, mordor.getBlueprint().getTwilightCost());
	}

	@Test
	public void MordorRequiresASauronMinionExertion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var wraith = scn.GetShadowCard("wraith");
		scn.ShadowMoveCardToHand(mordor);
		scn.ShadowMoveCharToTable(wraith);

		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.FreepsMoveCharToTable(greenleaf);

		scn.StartGame();
		scn.AddWoundsToChar(wraith, 1);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetWoundsOn(wraith));
		assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
		assertFalse(scn.ShadowPlayAvailable(mordor));
	}

	@Test
	public void MordorRequiresAFreepsArcher() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var wraith = scn.GetShadowCard("wraith");
		scn.ShadowMoveCardToHand(mordor);
		scn.ShadowMoveCharToTable(wraith);

		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.FreepsMoveCardToHand(greenleaf);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(wraith));
		assertEquals(Zone.HAND, greenleaf.getZone());
		assertFalse(scn.ShadowPlayAvailable(mordor));
	}

	@Test
	public void MordorPlaysOnAFreepsArcher() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var wraith = scn.GetShadowCard("wraith");
		scn.ShadowMoveCardToHand(mordor);
		scn.ShadowMoveCharToTable(wraith);

		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.FreepsMoveCharToTable(greenleaf);

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetWoundsOn(wraith));
		assertEquals(Zone.FREE_CHARACTERS, greenleaf.getZone());
		assertTrue(scn.ShadowPlayAvailable(mordor));
		scn.ShadowPlayCard(mordor);
		assertEquals(1, scn.GetWoundsOn(wraith));
		assertEquals(Zone.ATTACHED, mordor.getZone());
		assertEquals(greenleaf, mordor.getAttachedTo());
	}

	@Test
	public void MordorExertsBearerEachTimeFreepsUsesArcheryAbility() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		GenericCardTestHelper scn = GetScenario();

		var mordor = scn.GetShadowCard("mordor");
		var wraith = scn.GetShadowCard("wraith");
		scn.ShadowMoveCardToHand(mordor);
		scn.ShadowMoveCharToTable(wraith);

		var greenleaf = scn.GetFreepsCard("greenleaf");
		var arwen = scn.GetFreepsCard("arwen");
		scn.FreepsMoveCharToTable(greenleaf, arwen);
		scn.FreepsAttachCardsTo(arwen, "bow");

		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(mordor);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(arwen);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(greenleaf));
		scn.FreepsUseCardAction(greenleaf);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(greenleaf));
	}
}
