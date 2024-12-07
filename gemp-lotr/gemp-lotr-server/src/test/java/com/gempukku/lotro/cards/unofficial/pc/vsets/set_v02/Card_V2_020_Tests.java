package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_020_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("fanatic", "102_20");
					put("savage", "1_151");
					put("machine", "5_60");

					put("treebeard", "10_18");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void BerserkFanaticStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Berserk Fanatic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Damage +1. 
		* 	This minion is strength +1 for each wound on each character in its skirmish.
		* 	When this minion dies in a skirmish, you may remove 2 [isengard] tokens from a machine to wound a character it was skirmishing. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fanatic");

		assertEquals("Berserk Fanatic", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.hasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void BerserkFanaticIsStrenghtPlus1ForEachWoundOnEachCharacterInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var savage = scn.GetShadowCard("savage");
		var machine = scn.GetShadowCard("machine");
		scn.ShadowMoveCharToTable(fanatic, savage);
		scn.ShadowMoveCardToSupportArea(machine);

		var frodo = scn.GetRingBearer();

		scn.StartGame();

		scn.addWounds(frodo, 1);
		scn.addWounds(fanatic, 1);
		scn.addWounds(savage, 1);

		//not in skirmish, should be base
		assertEquals(8, scn.getStrength(fanatic));

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, fanatic, savage);
		scn.FreepsResolveSkirmish(frodo);

		//Base strength of 8, +1 strength for the single wound on each of itself, the savage, and frodo
		assertEquals(11, scn.getStrength(fanatic));
	}

	@Test
	public void BerserkFanaticDyingInSkirmishCanRemove2TokensFromMachineToWoundSkirmisher() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic");
		var machine = scn.GetShadowCard("machine");
		scn.ShadowMoveCharToTable(fanatic);
		scn.ShadowMoveCardToSupportArea(machine);

		var treebeard = scn.GetFreepsCard("treebeard");
		scn.FreepsMoveCharToTable(treebeard);

		scn.StartGame();

		scn.addWounds(fanatic, 2);
		scn.AddTokensToCard(machine, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(treebeard, fanatic);
		scn.FreepsResolveSkirmish(treebeard);

		assertEquals(1, scn.GetVitality(fanatic));
		assertEquals(0, scn.GetWoundsOn(treebeard));
		assertEquals(2, scn.GetCultureTokensOn(machine));
		assertEquals(Zone.SHADOW_CHARACTERS, fanatic.getZone());

		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(treebeard));
		assertEquals(0, scn.GetCultureTokensOn(machine));
		assertEquals(Zone.DISCARD, fanatic.getZone());
	}
}
