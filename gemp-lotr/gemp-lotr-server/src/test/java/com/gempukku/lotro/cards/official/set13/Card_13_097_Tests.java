package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cutthroat1", "13_97");
					put("cutthroat2", "13_97");
					// Howdah: [men] support-area condition that gives itself [men] tokens as it is played (one for each
					// [men] archer spotted, none here); its start-of-archery trigger is never reached
					put("howdah1", "13_94");
					put("howdah2", "13_94");
					// Fleet of Corsair Ships: [men] POSSESSION in the support area; its abilities are never used
					put("fleet", "18_66");
					// Footman of Dunland: [men] Man minion; makes the current site plains
					put("footman", "11_82");
					// Pavise: [men] possession borne by a [men] minion, so in no support area
					put("pavise", "11_94");
					// Ships of Great Draught: [raider] support-area possession that holds [raider] tokens
					put("ships", "8_65");
					// Strength Born of Fear: [sauron] support-area CONDITION, a Shadow one; its skirmish ability is never used
					put("fear", "1_278");
					// Lord of Moria: [dwarven] Free Peoples condition, not unique ("Each Dwarf is damage +1": no Dwarf here)
					put("lom1", "1_21");
					put("lom2", "1_21");
					// A Path Appointed: [gondor] Free Peoples condition of another title; its abilities are never used
					put("path", "8_42");
					// Longbottom Leaf: a Free Peoples POSSESSION in the support area
					put("leaf", "1_300");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PirateCutthroatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Pirate Cutthroat
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 12
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: To play, spot a [men] card in your support area.
		 * 	<b>Shadow:</b> Exert this minion and spot a Free Peoples condition to reinforce a [men] token for each
		 * 	card you spot that has the same card title as that condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cutthroat1");

		assertEquals("Pirate Cutthroat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CanBePlayedOnlyOnceAMenConditionIsInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cutthroat = scn.GetShadowCard("cutthroat1");
		var howdah = scn.GetShadowCard("howdah1");
		var footman = scn.GetShadowCard("footman");
		var pavise = scn.GetShadowCard("pavise");
		var ships = scn.GetShadowCard("ships");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(cutthroat, howdah);
		// A [raider] possession and a [sauron] condition in the support area: support cards of other cultures.
		scn.MoveCardsToSupportArea(ships, fear);

		scn.StartGame();
		// A [men] minion bearing a [men] possession: [men] cards in play, but in no support area.
		scn.MoveMinionsToTable(footman);
		scn.AttachCardsTo(footman, pavise);
		assertAttachedTo(pavise, footman);
		assertInZone(Zone.SUPPORT, ships, fear);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(cutthroat));

		scn.ShadowPlayCard(howdah);
		assertInZone(Zone.SUPPORT, howdah);
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertTrue(scn.ShadowPlayAvailable(cutthroat));
		scn.ShadowPlayCard(cutthroat);
		assertInZone(Zone.SHADOW_CHARACTERS, cutthroat);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMenPossessionInTheSupportAreaAlsoLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cutthroat = scn.GetShadowCard("cutthroat1");
		var fleet = scn.GetShadowCard("fleet");
		scn.MoveCardsToHand(cutthroat);
		scn.MoveCardsToSupportArea(fleet);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fleet);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(cutthroat));
		scn.ShadowPlayCard(cutthroat);
		assertInZone(Zone.SHADOW_CHARACTERS, cutthroat);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsThisMinionAndSpotsOnlyAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lom1 = scn.GetFreepsCard("lom1");
		var lom2 = scn.GetFreepsCard("lom2");
		var path = scn.GetFreepsCard("path");
		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToSupportArea(lom1, lom2, path, leaf);

		var cutthroat1 = scn.GetShadowCard("cutthroat1");
		var cutthroat2 = scn.GetShadowCard("cutthroat2");
		var howdah1 = scn.GetShadowCard("howdah1");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(howdah1, fear);

		scn.StartGame();
		scn.AddTokensToCard(howdah1, 1);
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertInZone(Zone.SUPPORT, lom1, lom2, path, leaf, fear);
		scn.MoveMinionsToTable(cutthroat1, cutthroat2);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cutthroat1));
		assertEquals(0, scn.GetWoundsOn(cutthroat2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(cutthroat1));

		scn.ShadowUseCardAction(cutthroat1);
		// The minion whose ability it is exerts; the other copy does not.
		assertEquals(1, scn.GetWoundsOn(cutthroat1));
		assertEquals(0, scn.GetWoundsOn(cutthroat2));
		// Free Peoples conditions of two titles; not a Free Peoples possession, nor a Shadow condition (two of them).
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lom1, lom2, path));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(leaf, fear, howdah1));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowDecisionAvailable("spot"));
	}

	@Test
	public void ReinforcesAMenTokenForEachCardOfThatTitleThePlayerSpots() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lom1 = scn.GetFreepsCard("lom1");
		var lom2 = scn.GetFreepsCard("lom2");
		var path = scn.GetFreepsCard("path");
		scn.MoveCardsToSupportArea(lom1, lom2, path);

		var cutthroat1 = scn.GetShadowCard("cutthroat1");
		var cutthroat2 = scn.GetShadowCard("cutthroat2");
		var howdah1 = scn.GetShadowCard("howdah1");
		var howdah2 = scn.GetShadowCard("howdah2");
		var ships = scn.GetShadowCard("ships");
		var fleet = scn.GetShadowCard("fleet");
		// The Fleet is a [men] card with no [men] token; the Ships hold a [raider] token.
		scn.MoveCardsToSupportArea(howdah1, howdah2, ships, fleet);

		scn.StartGame();
		scn.AddTokensToCard(howdah1, 1);
		scn.AddTokensToCard(howdah2, 1);
		scn.AddTokensToCard(ships, 1);
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertEquals(1, scn.GetCultureTokensOn(howdah2));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(fleet));
		assertInZone(Zone.SUPPORT, lom1, lom2, path);
		scn.MoveMinionsToTable(cutthroat1, cutthroat2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowUseCardAction(cutthroat1);
		scn.ShadowChooseCard(lom1);
		// Both Lords of Moria have that title, the one spotted included; A Path Appointed has not.
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// Two reinforcements, one at a time: only the cards holding a [men] token are offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(howdah1, howdah2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships, fleet));
		scn.ShadowChooseCard(howdah1);
		assertEquals(2, scn.GetCultureTokensOn(howdah1));
		assertTrue(scn.ShadowHasCardChoiceAvailable(howdah1, howdah2));
		scn.ShadowChooseCard(howdah1);
		assertEquals(3, scn.GetCultureTokensOn(howdah1));
		assertEquals(1, scn.GetCultureTokensOn(howdah2));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(fleet));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The other copy spots the same title and names fewer than there are: one reinforcement.
		scn.ShadowUseCardAction(cutthroat2);
		scn.ShadowChooseCard(lom2);
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		scn.ShadowChooseCard(howdah2);
		assertEquals(3, scn.GetCultureTokensOn(howdah1));
		assertEquals(2, scn.GetCultureTokensOn(howdah2));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMenTokenInPlayTheShadowAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		scn.MoveCardsToSupportArea(path);

		var cutthroat1 = scn.GetShadowCard("cutthroat1");
		var howdah1 = scn.GetShadowCard("howdah1");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToSupportArea(howdah1, ships);

		scn.StartGame();
		scn.AddTokensToCard(ships, 1);
		assertEquals(0, scn.GetCultureTokensOn(howdah1));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.SUPPORT, path);
		scn.MoveMinionsToTable(cutthroat1);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cutthroat1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		// An ability with a cost is offered whatever its effect can do.
		assertTrue(scn.ShadowActionAvailable(cutthroat1));
		scn.ShadowUseCardAction(cutthroat1);
		assertEquals(1, scn.GetWoundsOn(cutthroat1));
		// The one Free Peoples condition is spotted unasked; one card has its title.
		assertEquals(Integer.valueOf(1), scn.ShadowGetChoiceDefault());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");
		assertEquals(0, scn.GetCultureTokensOn(howdah1));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoFreePeoplesConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		scn.MoveCardsToSupportArea(leaf);

		var cutthroat1 = scn.GetShadowCard("cutthroat1");
		var howdah1 = scn.GetShadowCard("howdah1");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToSupportArea(howdah1, fear);

		scn.StartGame();
		scn.AddTokensToCard(howdah1, 1);
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		// A Free Peoples possession and a Shadow condition are in play; no Free Peoples condition is.
		assertInZone(Zone.SUPPORT, leaf, fear);
		scn.MoveMinionsToTable(cutthroat1);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cutthroat1));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cutthroat1));
	}

	@Test
	public void ShadowAbilityIsNotOfferedOnceThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var path = scn.GetFreepsCard("path");
		scn.MoveCardsToSupportArea(path);

		var cutthroat1 = scn.GetShadowCard("cutthroat1");
		var cutthroat2 = scn.GetShadowCard("cutthroat2");
		var howdah1 = scn.GetShadowCard("howdah1");
		scn.MoveCardsToSupportArea(howdah1);

		scn.StartGame();
		scn.AddTokensToCard(howdah1, 1);
		assertEquals(1, scn.GetCultureTokensOn(howdah1));
		assertInZone(Zone.SUPPORT, path);
		scn.MoveMinionsToTable(cutthroat1, cutthroat2);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(cutthroat1, 1);
		assertEquals(1, scn.GetWoundsOn(cutthroat1));
		assertEquals(0, scn.GetWoundsOn(cutthroat2));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(cutthroat1));
		assertTrue(scn.ShadowActionAvailable(cutthroat2));
	}
}
