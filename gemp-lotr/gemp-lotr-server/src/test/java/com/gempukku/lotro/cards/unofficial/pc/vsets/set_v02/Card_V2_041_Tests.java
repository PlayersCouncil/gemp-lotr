package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_041_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("peak", "102_41");
					put("runner", "1_178");
					put("troll", "1_165");

					put("aragorn", "1_89");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void ToHighestPeakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: To Highest Peak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Discard X [Moria] minions to add X threats. The Free Peoples player may exert X companions to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("peak");

		assertEquals("To Highest Peak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.hasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToHighestPeakDiscardsMinionsToAddThreats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(peak);
		scn.ShadowMoveCharToTable(runner, troll);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);

		scn.StartGame();
		scn.SkipPassedAllAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, troll.getZone());
		assertTrue(scn.ShadowPlayAvailable(peak));

		scn.ShadowPlayCard(peak);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(runner, troll);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetThreats());
		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(Zone.DISCARD, troll.getZone());

	}

	@Test
	public void ToHighestPeakCanBePreventedByExertingXCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var peak = scn.GetShadowCard("peak");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToHand(peak);
		scn.ShadowMoveCharToTable(runner, troll);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);

		scn.StartGame();
		scn.SkipPassedAllAssignments();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, runner.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, troll.getZone());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(peak));

		scn.ShadowPlayCard(peak);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCards(runner, troll);

		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetThreats());
		assertEquals(Zone.DISCARD, runner.getZone());
		assertEquals(Zone.DISCARD, troll.getZone());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));

	}
}
