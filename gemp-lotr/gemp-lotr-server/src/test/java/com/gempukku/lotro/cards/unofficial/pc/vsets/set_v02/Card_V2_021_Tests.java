package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shot", "102_21");
					put("ram", "5_44");
					put("ladder", "5_57");
					put("fighter", "1_146"); // 3
					put("guard", "1_147"); // 4

					put("merry", "1_302");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreachingShotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Breaching Shot
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Assignment
		 * Game Text: Remove X tokens from machines to assign an Uruk-hai costing X or less to an ally or unbound companion. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shot");

		assertEquals("Breaching Shot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ASSIGNMENT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BreachingShotCanRemove3TokensToAssignAnUrukCosting3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shot = scn.GetShadowCard("shot");
		var ladder = scn.GetShadowCard("ladder");
		var fighter = scn.GetShadowCard("fighter");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveMinionsToTable(fighter, guard);

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(ladder, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(shot));
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertEquals(3, fighter.getBlueprint().getTwilightCost());
		assertEquals(4, guard.getBlueprint().getTwilightCost());
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
		scn.ShadowPlayCard(shot);
		scn.ShadowChoose("3");

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
	}

	@Test
	public void BreachingShotCanRemove3TokensFromVariousMachinesToAssignAnUrukCosting3() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shot = scn.GetShadowCard("shot");
		var ladder = scn.GetShadowCard("ladder");
		var ram = scn.GetShadowCard("ram");
		var fighter = scn.GetShadowCard("fighter");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ladder, ram);
		scn.MoveMinionsToTable(fighter, guard);

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(ladder, 2);
		scn.AddTokensToCard(ram, 2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(shot));
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(ram));
		assertEquals(3, fighter.getBlueprint().getTwilightCost());
		assertEquals(4, guard.getBlueprint().getTwilightCost());
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
		scn.ShadowPlayCard(shot);
		scn.ShadowChoose("3");

		assertEquals(2, scn.ShadowGetCardChoiceCount()); //2 machines with tokens
		scn.ShadowChooseCard(ram);
		scn.ShadowChooseCard(ladder);
		scn.ShadowChooseCard(ram);

		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
	}

	@Test
	public void BreachingShotCanRemove4TokensToAssignAnUrukCosting3Or4() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shot = scn.GetShadowCard("shot");
		var ladder = scn.GetShadowCard("ladder");
		var fighter = scn.GetShadowCard("fighter");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveMinionsToTable(fighter, guard);

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(ladder, 4);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(shot));
		assertEquals(4, scn.GetCultureTokensOn(ladder));
		assertEquals(3, fighter.getBlueprint().getTwilightCost());
		assertEquals(4, guard.getBlueprint().getTwilightCost());
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
		scn.ShadowPlayCard(shot);
		scn.ShadowChoose("4");

		assertEquals(2, scn.ShadowGetCardChoiceCount()); //Both uruks, one costing 3, the other 4
		scn.ShadowChooseCard(guard);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(fighter));
		assertTrue(scn.IsCharAssigned(guard));
	}

	@Test
	public void BreachingShotNotPlayableIfFewerTokensThanTwilightCostOfUruk() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shot = scn.GetShadowCard("shot");
		var ladder = scn.GetShadowCard("ladder");
		var fighter = scn.GetShadowCard("fighter");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveMinionsToTable(fighter, guard);

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(ladder, 2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(shot));
	}

	@Test
	public void BreachingShotDoesNotCrashIfTooFewTokensAreRemovedToAssign() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var shot = scn.GetShadowCard("shot");
		var ladder = scn.GetShadowCard("ladder");
		var fighter = scn.GetShadowCard("fighter");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveMinionsToTable(fighter, guard);

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionToTable(merry);

		scn.StartGame();
		scn.AddTokensToCard(ladder, 3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(shot));
		assertEquals(3, scn.GetCultureTokensOn(ladder));

		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
		scn.ShadowPlayCard(shot);
		scn.ShadowChoose("2");

		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertFalse(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(fighter));
		assertFalse(scn.IsCharAssigned(guard));
		assertTrue(scn.FreepsAnyDecisionsAvailable());
	}
}
