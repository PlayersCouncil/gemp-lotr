package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_02_043_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("aragorn", "1_89");

					put("sword", "52_43");
					put("uruk", "1_154");
					put("captain", "2_46");
					put("lurtz", "1_127");
					put("chaff1", "1_155");
					put("chaff2", "1_156");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void LurtzsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: Lurtz's Sword
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Isengard
		* Twilight Cost: 1
		* Type: possession
		* Subtype: Hand Weapon
		* Strength: 2
		* Game Text: Bearer must be a unique Uruk-hai.
		* 	If bearer is Lurtz, he is damage +1.
		* 	Each time bearer wins a skirmish, the Free Peoples player must wound a companion or discard the top 3 cards of their draw deck.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sword.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void LurtzsSwordPlaysOnUniqueUruk() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var lurtz = scn.GetShadowCard("lurtz");
		var captain = scn.GetShadowCard("captain");
		scn.ShadowMoveCharToTable(uruk, lurtz, captain);
		var sword = scn.GetShadowCard("sword");
		scn.ShadowMoveCardToHand(sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(sword));
		assertEquals(Zone.HAND, sword.getZone());
		scn.ShadowPlayCard(sword);
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(captain);
		assertEquals(Zone.ATTACHED, sword.getZone());
		assertEquals(captain, sword.getAttachedTo());
	}

	@Test
	public void LurtzsSwordSkirmishWinCanCauseWound() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);

		var captain = scn.GetShadowCard("captain");
		scn.ShadowMoveCharToTable(captain);
		var sword = scn.GetShadowCard("sword");
		scn.ShadowAttachCardsTo(captain, sword);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveActionOrder("skirmish");

		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(Zone.FREE_CHARACTERS, aragorn.getZone());
		scn.FreepsChooseMultipleChoiceOption("wound");
		// both aragorn and frodo are valid targets, even tho aragorn is exhausted
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(aragorn);
		assertEquals(Zone.DEAD, aragorn.getZone());
	}

	@Test
	public void LurtzsSwordSkirmishWinCanCauseTopDeckDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(aragorn);

		var captain = scn.GetShadowCard("captain");
		scn.ShadowMoveCharToTable(captain);
		var sword = scn.GetShadowCard("sword");
		scn.ShadowAttachCardsTo(captain, sword);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveActionOrder("skirmish");

		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseMultipleChoiceOption("discard");
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
	}

	@Test
	public void LurtzsSwordGrantsDamageBonusToLurtz() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lurtz = scn.GetShadowCard("lurtz");
		scn.ShadowMoveCharToTable(lurtz);
		var sword = scn.GetShadowCard("sword");
		scn.ShadowMoveCardToHand(sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(sword));
		assertTrue(scn.hasKeyword(lurtz, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		scn.ShadowPlayCard(sword);
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
	}

	@Test
	public void LurtzsSwordDoesNotGrantDamageBonusToNonLurtzUruk() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		scn.ShadowMoveCharToTable(captain);
		var sword = scn.GetShadowCard("sword");
		scn.ShadowMoveCardToHand(sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(sword));
		assertTrue(scn.hasKeyword(captain, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));
		scn.ShadowPlayCard(sword);
		assertEquals(1, scn.GetKeywordCount(captain, Keyword.DAMAGE));
	}
}
