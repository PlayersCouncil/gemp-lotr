package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_040_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("fire", "102_40");
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
	public void ThroughFireandWaterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Through Fire and Water
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a unique [moria] minion wins a skirmish, you may remove a threat to exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fire");

		assertEquals("Through Fire and Water", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ThroughFireAndWaterRemovesThreatToExertCompanionWhenUniqueMoriaMinionWins() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToSupportArea(fire);
		scn.ShadowMoveCharToTable(runner, troll);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);

		scn.StartGame();
		scn.AddThreats(2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.PassSkirmishActions();

		//Runner isn't unique
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troll);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void ThroughFireAndWaterNeedsAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var runner = scn.GetShadowCard("runner");
		var troll = scn.GetShadowCard("troll");
		scn.ShadowMoveCardToSupportArea(fire);
		scn.ShadowMoveCharToTable(runner, troll);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.PassSkirmishActions();

		//Runner isn't unique
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troll);
		scn.PassFierceSkirmishActions();

		//No threats
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
	}
}
