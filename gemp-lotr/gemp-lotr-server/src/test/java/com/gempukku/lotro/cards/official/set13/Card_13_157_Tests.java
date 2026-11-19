package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_157_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("businessman", "13_157");
					put("businessman2", "13_157");
					// [shire] cards that give themselves [shire] tokens
					put("faith", "13_147");     // Faith in Friendship: condition, one token as it is played; its regroup trigger is never reached
					put("kept", "6_111");       // Kept Safe: condition, two tokens as it is played; its Response needs an unbound Hobbit about to be wounded
					put("ale", "18_108");       // Golden Perch Ale: support-area POSSESSION, a token for each [shire] companion's win
					put("fury", "12_132");      // Sudden Fury: [shire] condition, a token for each [shire] companion's loss; here it holds none
					// Token holders of other cultures
					put("path", "8_42");        // A Path Appointed: [gondor] condition that adds [gondor] tokens to itself; its abilities are never used
					put("threatening", "13_103"); // Always Threatening: the Shadow player's [orc] condition that holds [orc] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestfarthingBusinessmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Westfarthing Businessman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: This companion is strength +1 for each [shire] card that has a culture token on it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("businessman");

		assertEquals("Westfarthing Businessman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void IsStrengthPlusOneForEachShireConditionPlayedWithTokensAndNotForEachToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var businessman = scn.GetFreepsCard("businessman");
		var faith = scn.GetFreepsCard("faith");
		var kept = scn.GetFreepsCard("kept");
		scn.MoveCompanionsToTable(businessman);
		scn.MoveCardsToHand(faith, kept);

		scn.StartGame();
		assertInZone(Zone.HAND, faith, kept);
		assertEquals(3, scn.GetStrength(businessman));

		// Faith in Friendship takes one [shire] token as it is played.
		scn.FreepsPlayCard(faith);
		assertEquals(1, scn.GetCultureTokensOn(faith));
		assertEquals(4, scn.GetStrength(businessman));

		// Kept Safe takes two: it is one more card, not two.
		scn.FreepsPlayCard(kept);
		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertEquals(5, scn.GetStrength(businessman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsShireCardsWithATokenOfEachTypeAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var businessman = scn.GetFreepsCard("businessman");
		var businessman2 = scn.GetFreepsCard("businessman2");
		var frodo = scn.GetRingBearer();
		var kept = scn.GetFreepsCard("kept");
		var ale = scn.GetFreepsCard("ale");
		var fury = scn.GetFreepsCard("fury");
		var path = scn.GetFreepsCard("path");
		var threatening = scn.GetShadowCard("threatening");
		scn.MoveCompanionsToTable(businessman, businessman2);
		scn.MoveCardsToSupportArea(kept, ale, fury, path);
		scn.MoveCardsToSupportArea(threatening);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(kept));
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(threatening));
		// [shire] cards with no token count for nothing.
		assertEquals(3, scn.GetStrength(businessman));
		// Frodo 3, +1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));

		scn.AddTokensToCard(ale, 1);
		assertEquals(4, scn.GetStrength(businessman));

		// A [shire] condition with two tokens is one card more; A Path Appointed's [gondor] token and Always
		// Threatening's [orc] tokens are on cards of other cultures, and Sudden Fury still has none.
		scn.AddTokensToCard(kept, 2);
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(threatening, 2);
		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(2, scn.GetCultureTokensOn(kept));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(5, scn.GetStrength(businessman));
		// Each copy gives the bonus to itself alone: the second reads 5 as well, not 7.
		assertEquals(5, scn.GetStrength(businessman2));
		// The bonus is this companion's alone.
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
