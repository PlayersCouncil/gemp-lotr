package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "10_50");
					put("savage2", "10_50");    // a second Southron Savage
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, strength 5; its (2) is added only when played
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, strength 8, no text
					// Men of Harad: a [raider] condition in the support area, which is no minion; its ability is not used
					put("harad", "4_238");

					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Southron Savage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Southron.
		 * While the total number of other minions and twilight tokens is 3 or fewer, this minion is strength +3 and fierce.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("savage");

		assertEquals("Southron Savage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeTwilightTokensAndNoOtherMinionItIsStrengthElevenAndFierceUntilAPlayAddsAFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(sword);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var harad = scn.GetShadowCard("harad");

		scn.StartGame();
		// Laid in the fellowship phase so that a Free Peoples play can add the fourth token (Q-0228).
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(harad);
		scn.MoveCardsToHand(runner);
		scn.SetTwilight(3);

		// Three tokens, and itself, which is not an "other" minion; a Shadow condition in play and a minion in
		// hand, neither of which counts.
		assertInZone(Zone.SUPPORT, harad);
		assertInHand(runner);
		assertEquals(3, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		// 3 + 1 for the sword
		assertEquals(4, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OtherMinionsOfAnyCultureAndRaceAreCountedWithTheTwilightTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, brigand);
		scn.SetTwilight(1);

		// An Orc and a Man of two other cultures, and 1 token: 3.
		assertEquals(1, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		// The other minions are not made stronger or fierce.
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.FIERCE));
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));

		// The same two minions and 2 tokens: 4.
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoSavagesEachCountTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SetTwilight(2);

		// For each, one other minion and 2 tokens: 3.
		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(11, scn.GetStrength(savage2));
		assertTrue(scn.HasKeyword(savage2, Keyword.FIERCE));

		// One other minion and 3 tokens: 4.
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(8, scn.GetStrength(savage2));
		assertFalse(scn.HasKeyword(savage2, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
