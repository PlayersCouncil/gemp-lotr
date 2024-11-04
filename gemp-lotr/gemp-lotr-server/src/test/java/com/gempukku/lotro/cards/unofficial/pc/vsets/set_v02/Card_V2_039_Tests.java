package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_039_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("dungeon", "102_39");
					put("balrog1", "6_76");
					put("balrog2", "19_18");

					//Dwarf guards for threat limit
					put("fodder1", "1_7");
					put("fodder2", "1_7");
					put("fodder3", "1_7");
					put("fodder4", "1_7");
					put("fodder5", "1_7");
					put("fodder6", "1_7");
					put("fodder7", "1_7");
					put("fodder8", "1_7");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void FromDeepestDungeonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: From Deepest Dungeon
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time the fellowship moves, you may reveal The Balrog from your hand to add a threat.
		* 	Shadow: Remove X threats.  Play The Balrog from your discard pile; its twilight cost is -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dungeon");

		assertEquals("From Deepest Dungeon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FromDeepestDungeonRevealsBalrogFromHandOnMoveToAddThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToHand(balrog1);
		scn.ShadowMoveCardToDiscard("balrog2"); // So we don't draw it later

		scn.FreepsMoveCharToTable("fodder1");

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.AcknowledgeReveal();
		assertEquals(1, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();
		scn.AcknowledgeReveal();
		assertEquals(2, scn.GetThreats());
	}

	@Test
	public void FromDeepestDungeonMoveRevealIsOptional() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToHand(balrog1);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetThreats());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
	}

	@Test
	public void FromDeepestDungeonRevealsNothingIfNoBalrogInHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToDiscard("balrog1", "balrog2");

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());


	}


	@Test
	public void FromDeepestDungeonShadowCanDiscount0ByRemoving0Threats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToDiscard(balrog1);

		scn.FreepsMoveCharToTable("fodder1", "fodder2", "fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8");

		scn.StartGame();

		scn.SetTwilight(39);
		scn.AddThreats(9);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(9, scn.GetThreats());
		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, balrog1.getZone());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("0");

		assertEquals(Zone.SHADOW_CHARACTERS, balrog1.getZone());
		//Balrog costs 14 + 2 for roaming, with a -0 discount = 16
		assertEquals(34, scn.GetTwilight());
		assertEquals(9, scn.GetThreats());
	}

	@Test
	public void FromDeepestDungeonShadowCanDiscount1ByRemoving1Threat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToDiscard(balrog1);

		scn.FreepsMoveCharToTable("fodder1", "fodder2", "fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8");

		scn.StartGame();

		scn.SetTwilight(39);
		scn.AddThreats(9);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(9, scn.GetThreats());
		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, balrog1.getZone());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("1");

		assertEquals(Zone.SHADOW_CHARACTERS, balrog1.getZone());
		//Balrog costs 14 + 2 for roaming, with a -1 discount = 15
		assertEquals(35, scn.GetTwilight());
		assertEquals(8, scn.GetThreats());
	}

	@Test
	public void FromDeepestDungeonShadowCanDiscount9ByRemoving9Threats() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToDiscard(balrog1);

		scn.FreepsMoveCharToTable("fodder1", "fodder2", "fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8");

		scn.StartGame();

		scn.SetTwilight(39);
		scn.AddThreats(9);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(9, scn.GetThreats());
		assertEquals(50, scn.GetTwilight());
		assertEquals(Zone.DISCARD, balrog1.getZone());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("9");

		assertEquals(Zone.SHADOW_CHARACTERS, balrog1.getZone());
		//Balrog costs 14 + 2 for roaming, with a -9 discount = 7
		assertEquals(43, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
	}

	@Test
	public void FromDeepestDungeonShadowCanActivateIfDiscountLessThanTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToDiscard(balrog1);

		scn.FreepsMoveCharToTable("fodder1", "fodder2", "fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8");

		scn.StartGame();

		scn.AddThreats(9);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(9, scn.GetThreats());
		assertEquals(11, scn.GetTwilight());
		assertEquals(Zone.DISCARD, balrog1.getZone());
		assertTrue(scn.ShadowActionAvailable(dungeon));

		scn.ShadowUseCardAction(dungeon);
		assertTrue(scn.ShadowDecisionAvailable("remove"));
		scn.ShadowChoose("5");

		assertEquals(Zone.SHADOW_CHARACTERS, balrog1.getZone());
		//Balrog costs 14 + 2 for roaming, with a -5 discount = 11
		assertEquals(0, scn.GetTwilight());
		assertEquals(4, scn.GetThreats());
	}

	@Test
	public void FromDeepestDungeonShadowCannotActivateIfMaxDiscountMoreThanTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var dungeon = scn.GetShadowCard("dungeon");
		var balrog1 = scn.GetShadowCard("balrog1");
		scn.ShadowMoveCardToSupportArea(dungeon);
		scn.ShadowMoveCardToDiscard(balrog1);

		scn.FreepsMoveCharToTable("fodder1", "fodder2", "fodder3", "fodder4", "fodder5", "fodder6", "fodder7", "fodder8");

		scn.StartGame();

		scn.AddThreats(4);

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(4, scn.GetThreats());
		assertEquals(11, scn.GetTwilight());
		assertEquals(Zone.DISCARD, balrog1.getZone());
		//Balrog costs 14 + 2 for roaming, with a -4 discount = 12, more twilight than is currently in the pool.
		assertFalse(scn.ShadowActionAvailable(dungeon));
	}
}
