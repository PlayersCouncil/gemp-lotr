package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillsman", "17_50");
					put("hillsman2", "17_50");
					put("warrior", "17_60");    // Sunland Warrior: [men] Man; a maneuver ability only
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his texts are not reached from the pile
					put("saruman", "17_48");
					put("poleaxe", "11_95");    // Poleaxe: [men] possession, hand weapon on a [men] minion; a skirmish ability only
					put("poleaxe2", "11_95");
					put("brigand", "4_10");     // Dunlending Brigand: a Man of [dunland], no text
					// Stampeding Savage: [men] Man, laid only in the Free Peoples player's discard pile
					put("savage", "17_53");
					put("runner", "13_90");     // Easterling Runner: [men] Man, to bear the second Poleaxe; a Shadow ability only
					// Ceremonial Armor and Wildman's Oath: [men] possessions in the support area. Each stacks a [men] minion
					// discarded in the MANEUVER phase only, so neither answers the regroup discard here.
					put("armor", "17_41");
					put("oath", "17_66");
					put("hides", "4_19");       // Hides: a [dunland] possession in the support area
					put("companies", "12_58");  // Countless Companies: a [men] condition in the support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampedingHillsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Stampeding Hillsman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Spot a [men] minion in your discard pile and discard this minion to place that minion on your [men] support area possession.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("hillsman");

		assertEquals("Stampeding Hillsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsHimToStackAMenMinionFromThePileOnAMenSupportPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var poleaxe2 = scn.GetShadowCard("poleaxe2");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetFreepsCard("savage");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var hides = scn.GetShadowCard("hides");
		var companies = scn.GetShadowCard("companies");
		scn.MoveCardsToDiscard(warrior, saruman, poleaxe, brigand, savage);
		scn.MoveCardsToSupportArea(armor, oath, hides, companies);
		assertInDiscard(warrior, saruman, poleaxe, brigand, savage);
		assertInZone(Zone.SUPPORT, armor, oath, hides, companies);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that lived through the skirmishes stand here at the start of the regroup phase.
		scn.MoveMinionsToTable(hillsman, hillsman2, runner);
		scn.AttachCardsTo(runner, poleaxe2);
		assertAttachedTo(poleaxe2, runner);
		scn.FreepsRefreshDecision();
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(hillsman));
		scn.ShadowUseCardAction(hillsman);

		// [men] minions of two races in his own pile; not a [men] possession, a [dunland] Man, or the other
		// player's [men] Man in his own pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poleaxe, brigand, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(warrior);

		assertInDiscard(hillsman);
		assertInZone(Zone.SHADOW_CHARACTERS, hillsman2);

		// [men] possessions in his support area; not a [dunland] one there, a [men] condition there, or a [men]
		// possession a minion bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, oath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hides, companies, poleaxe2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armor);

		assertInZone(Zone.STACKED, warrior);
		assertTrue(scn.GetStackedCards(armor).contains(warrior));
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertInDiscard(saruman, poleaxe, brigand, savage, hillsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoMenMinionInHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetFreepsCard("savage");
		var armor = scn.GetShadowCard("armor");
		scn.MoveCardsToDiscard(poleaxe, brigand, savage);
		scn.MoveCardsToSupportArea(armor);
		assertInDiscard(poleaxe, brigand, savage);
		assertInZone(Zone.SUPPORT, armor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(hillsman);
		scn.FreepsRefreshDecision();
		scn.FreepsPass();

		// A [men] possession, a [dunland] Man, and a [men] Man in the other player's pile only.
		assertFalse(scn.ShadowActionAvailable(hillsman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.MoveCardsToDiscard(warrior);
		assertInDiscard(warrior);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(hillsman));
	}

	@Test
	public void WithNoMenSupportAreaPossessionHeIsDiscardedAndTheMinionStaysInThePile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillsman = scn.GetShadowCard("hillsman");
		var warrior = scn.GetShadowCard("warrior");
		var hides = scn.GetShadowCard("hides");
		var companies = scn.GetShadowCard("companies");
		scn.MoveCardsToDiscard(warrior);
		scn.MoveCardsToSupportArea(hides, companies);
		assertInDiscard(warrior);
		assertInZone(Zone.SUPPORT, hides, companies);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(hillsman);
		scn.FreepsRefreshDecision();
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(hillsman));
		scn.ShadowUseCardAction(hillsman);

		// The Warrior is the one candidate, chosen unasked; nothing it could be placed on.
		assertInDiscard(hillsman, warrior);
		assertEquals(0, scn.GetStackedCards(hides).size());
		assertEquals(0, scn.GetStackedCards(companies).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
