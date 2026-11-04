package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("edge", "13_44");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5; his texts are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] minion of another title, strength 8
					put("ranger", "4_130");     // Ranger of Ithilien: Ring-bound [gondor] Man companion, cost 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion, cost 4
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound [shire] Hobbit companion
					put("smeagol", "6_45");     // Sméagol, Poor Creature: Ring-bound [gollum] companion, strength 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChasmsEdgeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Chasm's Edge
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Gollum is strength +1 for each Ring-bound companion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("edge");

		assertEquals("Chasm's Edge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GollumIsStrengthPlus1ForEachRingBoundCompanionAndNotForAnUnboundOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edge = scn.GetShadowCard("edge");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(edge);
		scn.MoveCardsToHand(ranger, aragorn);

		scn.StartGame();
		// The minions stand in play while the fellowship is made up (a shortening of the turn: Q-0228).
		scn.MoveMinionsToTable(gollum, shelob);
		scn.FreepsRefreshDecision();

		// Frodo, the Ring-bearer, is the one Ring-bound companion: 5 + 1.
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));

		// A Ring-bound Man of another culture and race: 5 + 2.
		scn.FreepsPlayCard(ranger);
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));

		// An unbound companion is not counted.
		scn.FreepsPlayCard(aragorn);
		assertEquals(7, scn.GetStrength(gollum));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AWraithGollumIsStrengthPlus1ForEachAndSmeagolIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var edge = scn.GetShadowCard("edge");
		var gollum = scn.GetShadowCard("wraithgollum");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToSupportArea(edge);
		scn.MoveCompanionsToTable(sam, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		// Frodo, Sam and Sméagol are Ring-bound: 5 + 3. Sméagol is no Gollum and keeps his own 3.
		assertEquals(8, scn.GetStrength(gollum));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
